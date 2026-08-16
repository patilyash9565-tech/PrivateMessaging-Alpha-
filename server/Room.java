import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Room{
    private String name;
    private ClientHandler owner;
    private List<ClientHandler>members;
    

    public Room(String name, ClientHandler owner){
        this.name = name ;
        this.owner = owner;
        this.members = new CopyOnWriteArrayList<>();
        this.members.add(owner);
    }

    public synchronized void addMember(ClientHandler client){
        if(!members.contains(client)){
        members.add(client);
        }
    }

    public synchronized void removeMember(ClientHandler client) {

        boolean wasOwner = (owner == client);

        members.remove(client);

        if (wasOwner && !members.isEmpty()) {
            owner = members.get(0);
        }
    }
    
    public boolean isEmpty(){
        return members.isEmpty();
    }

    public boolean hasMember(ClientHandler client){
        return members.contains(client);
    }

    public String getName(){
        return name;
    }

     public ClientHandler getOwner(){
          return owner;
    }

    public void broadcast(String message){
        for (ClientHandler client : members){
            client.sendMessage(message);
        }
    }

}