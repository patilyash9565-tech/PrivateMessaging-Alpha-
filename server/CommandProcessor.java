public class CommandProcessor {

    private final ClientHandler client;

    public CommandProcessor(ClientHandler client) {
        this.client = client;
    }

    public void process(String message) {

        if (message.startsWith("/msg ")) {
            processPrivateMessage(message);

        } else if (message.equals("/users")) {
            showUsers();

        } else if (message.equals("/help")) {
            showHelp();

        } else if (message.equals("/quit")) {
            client.disconnect();

        } else {
            client.broadcastMessage(
                client.getUsername() + ": " + message );
        }
    }

    private void processPrivateMessage(String message) {

        String[] parts = message.split(" ", 3);

        if (parts.length < 3) {
            client.sendMessage("Usage: /msg <username> <message>");
            return;
        }

        String targetUsername = parts[1];
        String privateMessage = parts[2];

        if (privateMessage.trim().isEmpty()) {
            client.sendMessage( "Private message cannot be empty." );
            return;
        }

        ClientHandler target =
            client.findClientByUsername(targetUsername);

        if (target != null) {

            target.sendMessage("Private message from "+ client.getUsername()+ ": " + privateMessage);

        } else {

            client.sendMessage("User " + targetUsername + " not found.");
        }
    }

    private void showUsers() {
        client.sendOnlineUsers();
    }

    private void showHelp() {

        client.sendMessage("Available commands:\n"
            + "/msg <username> <message> - Send a private message\n"
            + "/users - Show online users\n"
            + "/help - Show available commands\n"
            + "/quit - Disconnect");
    }
}