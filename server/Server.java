import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
  public static void main(String[]args){

    try {
        ServerSocket serverSocket = new ServerSocket(55000);


        System.out.println("=====================================");
        System.out.println("Server is running on port 55000");
        System.out.println("=====================================");
        System.out.println("Welcome to the server!");
        System.out.println("Waiting for clients to connect...");

        while(true){

        Socket socket = serverSocket.accept();

        System.out.println("\nClient connected: " + socket.getInetAddress().getHostAddress());

        ClientHandler handler = new ClientHandler(socket);

        Thread thread = new Thread(handler);
        thread.start();
        }
    } catch (IOException e) {
        e.printStackTrace();

    }
  
  }


}