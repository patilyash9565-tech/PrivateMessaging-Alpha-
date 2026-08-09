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
private static int nextClientId = 1;
private static List<ClientHandler> clients = new ArrayList<>();

public ClientHandler(Socket socket) {
    this.socket = socket;
    this.clientId = nextClientId++;
    

    try {
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new PrintWriter(socket.getOutputStream(), true);
    } catch (Exception e) {
        e.printStackTrace();
    }
}

public static void broadcast(String message) {
    for (ClientHandler client : clients) {
        client.writer.println(message);
    }
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
        if (client.username.equalsIgnoreCase(username)) {
            return client;
        }
    }

    return null;
}

@Override
public void run() {
    System.out.print("Handling client: " + clientId + " : "
            + socket.getInetAddress().getHostAddress());

    writer.println("Welcome to the chat server! You are connected as: "
            + socket.getInetAddress().getHostAddress());

     

    try {
        String message;

        writer.println("Enter your username:");

       while(true){
            username = reader.readLine();   
            if (username==null || username.trim().isEmpty()){
                writer.println("User name cannot be empty. Try again: ");
                continue;
            }

            if (findClientByUsername(username)!=null){
                writer.println("User name is already taken. Try another: ");
                continue;
            }
            username = username.trim();
            break;
        }  

        clients.add(this);

        while ((message = reader.readLine()) != null) {
            System.out.println("RAW MESSAGE RECEIVED >>> " + message);

            if (message.startsWith("/msg ")) {
                String[] parts = message.split(" ", 3);

                if (parts.length < 3) {
                    writer.println("Usage: /msg <userName> <message>");
                    continue;
                }

                
                    String targetUsername = parts[1];
                    String privateMessage = parts[2];

                    if (privateMessage.trim().isEmpty()) {
                        writer.println("Private message cannot be empty.");
                        continue;
                    }

                    ClientHandler targetClient = findClientByUsername(targetUsername);

                    if (targetClient != null) {
                        targetClient.writer.println(
                                "Private message from "
                                + username + ": " + privateMessage);
                    } else {
                        writer.println("User " + targetUsername + " not found.");
                    }

                

            } else {
                broadcast(username + ": " + message);
            }
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
