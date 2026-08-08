import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {

    public static void main(String[] args) {

        try {

            Socket socket = new Socket("localhost", 55000);

            System.out.println("Connected to chat server!");
            
            BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
           
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            Thread receiverThread = new Thread(() -> {
                try{
                String serverMessage;

                while ((serverMessage = reader.readLine()) != null) {
                    System.out.println("Server: " + serverMessage);
                }
            } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            receiverThread.start();

            PrintWriter writer = new PrintWriter (socket.getOutputStream(), true);

            String message;
            while ((message = keyboard.readLine())!= null){
                System.out.println("Client sending: " + message);
                writer.println(message);
            }

        } catch (IOException e) {

            e.printStackTrace();

        }
    }
}