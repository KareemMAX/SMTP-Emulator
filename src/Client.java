import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    static public boolean isOn = true;
    static Scanner input = new Scanner(System.in);
    static Socket server;
    static DataOutputStream serverWrite;
    static DataInputStream serverRead;
    static String user;

    public static void main(String[] args) {
        try {

            InetAddress ip = InetAddress.getLocalHost();
            System.out.println("Please enter server port number.");
            int port = input.nextInt();
            // To clear remaining string in the line
            input.nextLine();
            server = new Socket(ip, port);

            serverWrite = new DataOutputStream(server.getOutputStream());
            serverRead = new DataInputStream(server.getInputStream());
            String message = serverRead.readUTF();
            System.out.println(message);

            while (true) {
                System.out.println("Please choose ‘REGISTER or ‘LOGIN’ or ‘QUIT’.");
                String inputMessage = input.nextLine();
                if (inputMessage.equalsIgnoreCase("QUIT")) {
                    serverWrite.writeUTF(inputMessage);
                    message = serverRead.readUTF();
                    System.out.println(message);
                    serverWrite.close();
                    serverRead.close();
                    server.close();
                    break;
                } else if (inputMessage.equalsIgnoreCase("REGISTER")) {
                    System.out.println("Please enter an email and a password.");
                    serverWrite.writeUTF(inputMessage);
                    user = input.nextLine();
                    serverWrite.writeUTF(user);
                    inputMessage = input.nextLine();
                    serverWrite.writeUTF(inputMessage);

                    message = serverRead.readUTF();
                    if (message.startsWith("250")) {
                        System.out.println("HELLO " + user);
                        serverWrite.writeUTF("HELLO " + user);
                        message = serverRead.readUTF();
                        System.out.println(message);
                        Send();
                        break;
                    }

                    System.out.println(message);
                } else if (inputMessage.equalsIgnoreCase("LOGIN")) {
                    System.out.println("Please enter an email and a password.");
                    serverWrite.writeUTF(inputMessage);
                    user = input.nextLine();
                    serverWrite.writeUTF(user);
                    inputMessage = input.nextLine();
                    serverWrite.writeUTF(inputMessage);

                    message = serverRead.readUTF();
                    if (message.startsWith("250")) {
                        System.out.println("HELLO " + user);
                        serverWrite.writeUTF("HELLO " + user);
                        message = serverRead.readUTF();
                        System.out.println(message);
                        Send();
                        break;
                    }

                    System.out.println(message);
                }
            }
        } catch (IOException e) {
            System.out.println("You have been disconnected!!");
            Client.isOn = false;
        }


    }

    public static void Send() throws IOException {

        while (true) {
            String Mess;
            System.out.println("Please choose ‘SEND’ or ‘QUIT’.");
            Mess = input.nextLine();
            if (Mess.equalsIgnoreCase("SEND")) {
                serverWrite.writeUTF("MAIL FROM " + user);
                System.out.println("MAIL FROM " + user);
                Mess = serverRead.readUTF();
                System.out.println("Server: " + Mess);
                serverWrite.writeUTF("RCPT TO");
                System.out.println("RCPT TO");
                Mess = input.nextLine();
                serverWrite.writeUTF(Mess);
                Mess = serverRead.readUTF();
                System.out.println("Server: " + Mess);
                serverWrite.writeUTF("DATA");
                System.out.println("DATA");
                Mess = serverRead.readUTF();
                System.out.println("Server: " + Mess);
                while (true) {
                    Mess = input.nextLine();
                    serverWrite.writeUTF(Mess);
                    if (Mess.equalsIgnoreCase("&&&"))
                        break;
                }
                Mess = serverRead.readUTF();
                System.out.println("Server: " + Mess);
            } else if (Mess.equalsIgnoreCase("QUIT")) {
                serverWrite.writeUTF(Mess);
                Mess = serverRead.readUTF();
                System.out.println("Server: " + Mess);
                serverWrite.close();
                serverRead.close();
                server.close();
                break;
            }

        }

    }
}