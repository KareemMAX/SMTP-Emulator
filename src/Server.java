import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    public static List<String> users = new ArrayList<>();
    public static List<String> passwords = new ArrayList<>();
    public static String serverName;
    public static File credentialsFile;

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        System.out.println("Please enter the server name and port number: ");
        serverName = input.nextLine();
        int port = input.nextInt();

        File serverFolder = new File(serverName);
        // Creates a folder if it doesn't exist
        serverFolder.mkdir();

        credentialsFile = new File(serverName + "/credentials.txt");



        try {
            File portFile = new File(serverName + "/port.txt");
            portFile.createNewFile();
            FileWriter myWriter = new FileWriter(portFile, false);
            myWriter.write(Integer.toString(port));
            myWriter.close();
            // Creates a file if it doesn't exist
            if(!credentialsFile.createNewFile()){
                Scanner fileScanner = new Scanner(credentialsFile);
                while (fileScanner.hasNextLine()) {
                    users.add(fileScanner.nextLine());
                    passwords.add(fileScanner.nextLine());
                }
            }
        } catch (IOException e) {
            System.out.println("Credentials file is not created");
            return;
        }

        try{
            ServerSocket server = new ServerSocket(port);
            System.out.println(serverName + " server with port number ‘" + port + "’ is booted up.");

            while(true){
                Socket client = server.accept();
                ClientHandler clientHandler = new ClientHandler(client);
                clientHandler.start();
            }
        }
        catch (IOException e){
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public static void addUser(String user, String password){
        try {
            FileWriter myWriter = new FileWriter(credentialsFile, true);
            myWriter.write(user + "\n");
            myWriter.write(password + "\n");
            myWriter.close();

            users.add(user);
            passwords.add(password);
        } catch (IOException e) {
            System.out.println("An error occurred when writing credentials.");
        }
    }
}