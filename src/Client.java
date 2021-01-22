import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    static public boolean isOn = true;

    public static void main(String[] args) {
        try {
            Scanner input = new Scanner(System.in);
            InetAddress ip = InetAddress.getLocalHost();
            System.out.println("Please enter server port number.");
            int port = input.nextInt();
            Socket server = new Socket(ip, port);

            DataOutputStream serverWrite = new DataOutputStream(server.getOutputStream());
            DataInputStream serverRead = new DataInputStream(server.getInputStream());

            // TODO: Fix up sending and receiving
            try {
                String message = serverRead.readUTF();
                System.out.println(message);

                while (true){
                    System.out.println("Please choose ‘REGISTER or ‘LOGIN’ or ‘QUIT’.");
                    String inputMessage = input.nextLine();
                    if (inputMessage.equalsIgnoreCase("QUIT")){
                        serverWrite.writeUTF(inputMessage);
                        message = serverRead.readUTF();
                        System.out.println(message);

                    }

                    else if (inputMessage.equalsIgnoreCase("REGISTER")){
                        System.out.println("Please enter an email and a password.");
                        serverWrite.writeUTF(inputMessage);
                        String user = input.nextLine();
                        serverWrite.writeUTF(user);
                        inputMessage = input.nextLine();
                        serverWrite.writeUTF(inputMessage);

                        message = serverRead.readUTF();
                        if (message.startsWith("250")){
                            System.out.println("HELLO "+user);
                            serverWrite.writeUTF("HELLO "+user);
                            message = serverRead.readUTF();
                        }

                        System.out.println(message);
                    }

                    else if (inputMessage.equalsIgnoreCase("LOGIN")){
                        System.out.println("Please enter an email and a password.");
                        serverWrite.writeUTF(inputMessage);
                        String user = input.nextLine();
                        serverWrite.writeUTF(user);
                        inputMessage = input.nextLine();
                        serverWrite.writeUTF(inputMessage);

                        message = serverRead.readUTF();
                        if (message.startsWith("250")){
                            System.out.println("HELLO "+user);
                            serverWrite.writeUTF("HELLO "+user);
                            message = serverRead.readUTF();
                        }

                        System.out.println(message);
                    }
                }
            } catch (IOException e) {
                System.out.println("You have been disconnected!!");
                Client.isOn = false;
            }

            while(isOn){
                String message = input.nextLine();
                serverWrite.writeUTF(message);
                if (message.equalsIgnoreCase("quit") || !isOn) {
                    serverRead.close();
                    serverWrite.close();
                    server.close();
                    break;
                }
            }

        } catch (IOException ignored) {}
    }
}