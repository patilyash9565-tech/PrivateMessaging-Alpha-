import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientHandler implements Runnable {

private Socket socket;
private BufferedReader reader;
private PrintWriter writer;

private int clientId;
private String username;
private long userId;

private CommandProcessor commandProcessor;

private boolean running = true;

private static final AtomicInteger nextClientId = new AtomicInteger(1);
private static final List<ClientHandler> clients = new CopyOnWriteArrayList<>();

private RoomManager roomManager;


public ClientHandler(Socket socket) {
    this.socket = socket;
    this.clientId = nextClientId.getAndIncrement();
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

private static synchronized boolean registerClient(ClientHandler client, String username) {

    if (findClientByUsername(username) != null) {
        return false;
    }

    client.username = username;
    clients.add(client);

    return true;
}

@Override
public void run() {
    System.out.print("Handling client: " + clientId + " : " + socket.getInetAddress().getHostAddress());

    writer.println("Welcome to the chat server! You are connected as: "+ socket.getInetAddress().getHostAddress());

     

    try {
        String message;

        writer.println("Enter your username:");

       while (true) {
            String requestedUsername = reader.readLine();

            if (requestedUsername == null) {
                return;
            }

            requestedUsername = requestedUsername.trim();

            if (requestedUsername.isEmpty()) {
               writer.println("User name cannot be empty. Try again: ");
               continue;
            }

            if (!registerClient(this, requestedUsername)) {
               writer.println("User name is already taken. Try another: ");
               continue;
            }

            Long existingUserId = DatabaseManager.findUserIdByUsername(requestedUsername);

            if (existingUserId != null) {
                userId = existingUserId;
            } else {
                Long newUserId = DatabaseManager.createUser(requestedUsername);

                if (newUserId == null) {
                    writer.println("Could not create persistent user.");
                    clients.remove(this);
                    return;
                }

                userId = newUserId;
            }

            System.out.println("Persistent user: " + requestedUsername + " -> userId " + userId);

            break;
        }

        

        while (running && (message = reader.readLine()) != null) {
            System.out.println("RAW MESSAGE RECEIVED >>> " + message);

           commandProcessor.process(message);
        }

    } catch (Exception e) {
        e.printStackTrace();

    } finally {
        clients.remove(this);
        roomManager.removeClientFromAllRooms(this);

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
