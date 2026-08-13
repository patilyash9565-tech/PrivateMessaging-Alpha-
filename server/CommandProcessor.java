public class CommandProcessor {

    private final ClientHandler client;
    private RoomManager roomManager;

    public CommandProcessor(ClientHandler client) {
        this.client = client;
        this.roomManager = Server.roomManager;
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

        } else if (message.equals("/rooms")){
            showRooms();
        
        }else if (message.startsWith("/create ")) {
            createRoom(message);
        
        } else if (message.startsWith("/delete ")) {
            deleteRoom(message);

        }else if (message.startsWith("/join ")) {
            joinRoom(message);

        }else if (message.startsWith("/leave ")) {
            leaveRoom(message);

        }else if (message.startsWith("/")) {
            processRoomMessage(message);

        }else {
            client.broadcastMessage(client.getUsername() + ": " + message );
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

        ClientHandler target = client.findClientByUsername(targetUsername);

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


    private void showRooms() {

        if (roomManager.getRooms().isEmpty()) {
        client.sendMessage("No rooms available.");
        return;
        }

        StringBuilder roomList = new StringBuilder("Available rooms:\n");

        for (Room room : roomManager.getRooms()) {
            roomList.append("- ").append(room.getName()).append("\n");
        }

        client.sendMessage(roomList.toString());
    }

    private void createRoom(String message) {

        String roomName = message.substring(8).trim();
        if (roomName.contains(" ")){
            client.sendMessage("Room name cannot contain spaces.");
            return;
        }


        if (roomName.isEmpty()) {
            client.sendMessage("Usage: /create <roomName>");
             return;
        }

        Room room = roomManager.createRoom(roomName, client);

        if (room == null) {
            client.sendMessage("Room already exists.");
            return;
        }

        client.sendMessage("Room '" + roomName + "' created successfully.");
    }

    private void deleteRoom(String message) {

        String roomName = message.substring(8).trim();

        if (roomName.isEmpty()) {
            client.sendMessage("Usage: /delete <roomName>");
            return;
        }

        boolean deleted = roomManager.deleteRoom(roomName, client);

        if (deleted) {
            client.sendMessage("Room '" + roomName + "' deleted successfully.");
        } else {
            client.sendMessage("Room not found or you are not the owner.");
        }
    }

    private void joinRoom(String message) {

        String roomName = message.substring(6).trim();

        if (roomName.isEmpty()) {
             client.sendMessage("Usage: /join <roomName>");
             return;
        }

        Room room = roomManager.findRoom(roomName);

        if (room == null) {
            client.sendMessage("Room '" + roomName + "' not found.");
            return;
        }

        if (room.hasMember(client)) {
            client.sendMessage("You are already a member of '" + room.getName() + "'.");
            return;
        }

        room.addMember(client);

        client.sendMessage("You joined '" + room.getName() + "'.");
    }

    private void leaveRoom(String message) {

        String roomName = message.substring(7).trim();

        if (roomName.isEmpty()) {
            client.sendMessage("Usage: /leave <roomName>");
            return;
        }

        Room room = roomManager.findRoom(roomName);

        if (room == null) {
            client.sendMessage("Room '" + roomName + "' not found.");
            return;
        }

        if (!room.hasMember(client)) {
            client.sendMessage("You are not a member of '" + room.getName() + "'.");
            return;
        }

        room.removeMember(client);
 
        client.sendMessage("You left '" + room.getName() + "'.");
    }

    private void processRoomMessage(String message) {

        String[] parts = message.split(" ", 2);

        if (parts.length < 2 || parts[1].trim().isEmpty()) {
            client.sendMessage("Usage: /<roomName> <message>");
            return;
        }

        String roomName = parts[0].substring(1);
        String roomMessage = parts[1];

        Room room = roomManager.findRoom(roomName);

        if (room == null) {
            client.sendMessage("Room '" + roomName + "' not found.");
            return;
        }

        if (!room.hasMember(client)) {
            client.sendMessage("You are not a member of '" + room.getName() + "'.");
            return;
        }

        room.broadcast("gc: " + room.getName() + ": " + client.getUsername() + ": " + roomMessage);
    }
}