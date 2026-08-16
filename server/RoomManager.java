import java.util.ArrayList;
import java.util.List;

public class RoomManager {

    private List<Room>rooms;

    public RoomManager() {
    this.rooms = new ArrayList<>();
    }

    public synchronized void addRoom(Room room) {
        if (findRoom(room.getName()) == null){
        rooms.add(room);
        }
    }

    public synchronized Room createRoom(String roomName, ClientHandler owner) {
        if (findRoom(roomName)!=null){
            return null;
        }
        Room room = new Room(roomName, owner);
        rooms.add(room);

        return room;
    }

    public synchronized boolean deleteRoom(String roomName, ClientHandler requester) {
        Room room = findRoom(roomName);

        if (room == null){
            return false;
        }

        if (room.getOwner()!= requester){
            return false;
        }

        rooms.remove(room);
        return true;

    }

    public synchronized Room findRoom(String roomName) {
      for (Room room : rooms) {
          if (room.getName().equalsIgnoreCase(roomName)) {
              return room;
            }
         
        }   return null;
        
    }

    public synchronized void removeClientFromAllRooms(ClientHandler client) {
        rooms.removeIf(room -> {
        room.removeMember(client);
        return room.isEmpty();
        });
    }

    public synchronized List<Room> getRooms() {
    return new ArrayList<>(rooms);
}
    
}