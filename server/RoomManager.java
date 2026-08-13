import java.util.ArrayList;
import java.util.List;

public class RoomManager {

    private List<Room>rooms;

    public RoomManager() {
    this.rooms = new ArrayList<>();
    }

    public void addRoom(Room room) {
        if (findRoom(room.getName()) == null){
        rooms.add(room);
        }
    }

    public Room createRoom(String roomName, ClientHandler owner) {
        if (findRoom(roomName)!=null){
            return null;
        }
        Room room = new Room(roomName, owner);
        rooms.add(room);

        return room;
    }

    public boolean deleteRoom(String roomName, ClientHandler requester) {
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

    public Room findRoom(String roomName) {
      for (Room room : rooms) {
          if (room.getName().equalsIgnoreCase(roomName)) {
              return room;
            }
         
        }   return null;
        
    }

    public List<Room>getRooms(){
        return rooms; 
    }

    
}