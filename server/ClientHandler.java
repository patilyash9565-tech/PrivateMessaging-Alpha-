import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientHandler implements Runnable {

private Socket socket;
private BufferedReader reader;
private PrintWriter writer;

private int clientId;
private String username;

private CommandProcessor commandProcessor;

private boolean running = true;

private static int nextClientId = 1;
private static List<ClientHandler> clients = new ArrayList<>();

private RoomManager roomManager;


public ClientHandler(Socket socket) {
    this.socket = socket;
    this.clientId = nextClientId++;
    this.roomManager = Server.roomManager;
    this.commandProcessor = new CommandProcessor(this);
    
    try {
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new PrintWriter(socket.getOutputStream(), true);
    } catch (Exception e) {
        e.printStackTrace();
    }
}

public void sendMessage(String message) {
    writer.println(message);
}


public String getUsername() {
    return username;
}

public void disconnect(){
    running = false;
}
public static void broadcast(String message) {
    for (ClientHandler client : clients) {
        client.writer.println(message);
    }
}
public void broadcastMessage(String message){
    broadcast(message);
}

public void sendOnlineUsers(){
    StringBuilder users = new StringBuilder("Online user(s): ");

    for (ClientHandler client :clients){
       users.append(client.username).append(" ");
    }
    sendMessage(users.toString().trim());
}

public static ClientHandler findClient(int clientId) {
    for (ClientHandler client : clients) {
        if (client.clientId == clientId) {
            return client;
        }
    }
    return null;
}

public static ClientHandler findClientByUsername(String username) {

    for (ClientHandler client : clients) {
        if (client.username != null && client.username.equalsIgnoreCase(username)) {
            return client;
        }
    }
    
    return null;
}

@Override
public void run() {
    System.out.print("Handling client: " + clientId + " : " + socket.getInetAddress().getHostAddress());

    writer.println("Welcome to the chat server! You are connected as: "+ socket.getInetAddress().getHostAddress());

     

    try {
        String message;

        writer.println("Enter your username:");

       while (true) {
            username = reader.readLine();

            if (username == null) {
               return;
            }

            username = username.trim();

            if (username.isEmpty()) {
               writer.println("User name cannot be empty. Try again: ");
               continue;
            }

            if (findClientByUsername(username) != null) {
               writer.println("User name is already taken. Try another: ");
               continue;
            }

            break;
        }

        clients.add(this);

        while (running && (message = reader.readLine()) != null) {
            System.out.println("RAW MESSAGE RECEIVED >>> " + message);

           commandProcessor.process(message);
        }

    } catch (Exception e) {
        e.printStackTrace();

    } finally {
        clients.remove(this);

        try {
            reader.close();
            writer.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


}
