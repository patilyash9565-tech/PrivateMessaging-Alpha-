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
private static int nextClientId = 1;
private static List<ClientHandler> clients = new ArrayList<>();

public ClientHandler(Socket socket) {
    this.socket = socket;
    this.clientId = nextClientId++;
    clients.add(this);

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

@Override
public void run() {
    System.out.print("Handling client: " + clientId + " : "
            + socket.getInetAddress().getHostAddress());

    writer.println("Welcome to the chat server! You are connected as: "
            + socket.getInetAddress().getHostAddress());

    try {
        String message;

        while ((message = reader.readLine()) != null) {
            System.out.println("RAW MESSAGE RECEIVED >>> " + message);

            if (message.startsWith("/msg ")) {
                String[] parts = message.split(" ", 3);

                if (parts.length < 3) {
                    writer.println("Usage: /msg <clientId> <message>");
                    continue;
                }

                try {
                    int targetId = Integer.parseInt(parts[1]);
                    String privateMessage = parts[2];

                    if (privateMessage.trim().isEmpty()) {
                        writer.println("Private message cannot be empty.");
                        continue;
                    }

                    ClientHandler targetClient = findClient(targetId);

                    if (targetClient != null) {
                        targetClient.writer.println(
                                "Private message from Client "
                                + clientId + ": " + privateMessage);
                    } else {
                        writer.println("Client " + targetId + " not found.");
                    }

                } catch (NumberFormatException e) {
                    writer.println("Invalid client Id. Please use a number.");
                }

            } else {
                broadcast("Client " + clientId + ": " + message);
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
