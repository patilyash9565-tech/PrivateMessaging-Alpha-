import java.util.ArrayList;
import java.util.List;

public class Room{
    private String name;
    private ClientHandler owner;
    private List<ClientHandler>members;
    

    public Room(String name, ClientHandler owner){
        this.name = name ;
        this.owner = owner;
        this.members = new ArrayList<>();
        this.members.add(owner);
    }

    public void addMember(ClientHandler client){
        if(!members.contains(client)){
        members.add(client);
        }
    }

    public void removeMember(ClientHandler client) {
        members.remove(client);
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