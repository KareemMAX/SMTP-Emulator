import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientHandler extends Thread {
    Socket client;
    DataInputStream clientRead;
    DataOutputStream clientWrite;
    String name = null;
    boolean isUser = false;

    ClientHandler(Socket client) {
        try {
            this.client = client;
            clientRead = new DataInputStream(client.getInputStream());
            clientWrite = new DataOutputStream(client.getOutputStream());
            clientWrite.writeUTF("220 " + Server.serverName);
        } catch (IOException e) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                String message = clientRead.readUTF();

                if (message.equalsIgnoreCase("REGISTER") ||
                        message.equalsIgnoreCase("LOGIN") ||
                        message.startsWith("HELLO")) {
                    if (message.equalsIgnoreCase("REGISTER")) {
                        String user = clientRead.readUTF();
                        String password = clientRead.readUTF();

                        if (Server.users.contains(user)) {
                            clientWrite.writeUTF("535 Authentication credentials invalid - Email Exists");
                        } else if (!user.endsWith("@" + Server.serverName)) {
                            clientWrite.writeUTF("535 Authentication credentials invalid - Incorrect email");
                        } else {
                            clientWrite.writeUTF("250 REGISTERED");
                            Server.addUser(user, password);
                            name = user;
                            File userFolder = new File(Server.serverName + "/" + name);
                            userFolder.mkdir();
                            File userFile = new File(Server.serverName + "/" + name + "/inbox.txt");
                            userFile.createNewFile();
                        }
                    } else if (message.equalsIgnoreCase("LOGIN")) {
                        String user = clientRead.readUTF();
                        String password = clientRead.readUTF();

                        int userId = Server.users.indexOf(user);
                        if (userId == -1) {
                            clientWrite.writeUTF("535 Authentication credentials invalid - Email Doesn't Exists");
                            continue;
                        } else {
                            if (!password.equals(Server.passwords.get(userId))) {
                                clientWrite.writeUTF("535 Authentication credentials invalid - Incorrect password");
                                continue;
                            }
                        }
                        clientWrite.writeUTF("250 LOGIN");
                        name = user;

                    } else if (message.startsWith("HELLO")) {
                        if (name != null) {
                            // User has joined
                            isUser = true;
                        }
                        // else: Other server has joined

                        name = message.replaceFirst("HELLO ", "");
                        clientWrite.writeUTF("250 Hello " + name + ", pleased to meet you");
                        handleUser();
                        break;
                    }
                } else if (message.equalsIgnoreCase("QUIT")) {
                    clientWrite.writeUTF("221 " + Server.serverName + " closing connection");
                    clientWrite.close();
                    clientRead.close();
                    client.close();
                    break;
                }
            }
        } catch (IOException e) {
            try {
                clientWrite.close();
            } catch (IOException ignored) {
            }
            try {
                clientRead.close();
            } catch (IOException ignored) {
            }
            try {
                client.close();
            } catch (IOException ignored) {
            }
        }
    }

    private void handleUser() throws IOException {
        String from = "";
        String to = "";
        StringBuilder data = new StringBuilder();

        while (true) {
            String message = clientRead.readUTF();
            if (message.startsWith("MAIL FROM")) {
                // Replace first occurrence of "MAIL FROM" with empty string
                from = message.replaceFirst("MAIL FROM ", "");
                clientWrite.writeUTF("250 " + from + "...Sender ok");
            } else if (message.equalsIgnoreCase("RCPT TO")) {
                to = clientRead.readUTF();
                clientWrite.writeUTF("250 " + to + "...Recipient ok");
            } else if (message.equalsIgnoreCase("DATA")) {
                clientWrite.writeUTF("Please enter the body of your email ended by ‘&&&‘");
                while (true) {
                    message = clientRead.readUTF();
                    if (!message.equals("&&&"))
                        data.append(message).append("\n");
                    else {
                        clientWrite.writeUTF("250 Message accepted for delivery");
                        break;
                    }
                }
                String messageData = data.toString();

                if (!isUser) //Server
                {
                    File userFile = new File(Server.serverName + "/" + to + "/inbox.txt");
                    FileWriter writer = new FileWriter(userFile, true);

                    writer.write("FROM: " + from + '\n');
                    writer.write("TO: " + to + '\n');
                    writer.write('\n' + messageData);
                    writer.write("========================" + '\n');
                    writer.close();
                } else {
                    if (to.endsWith("@" + Server.serverName)) {
                        File userFile = new File(Server.serverName + "/" + to + "/inbox.txt");
                        FileWriter writer = new FileWriter(userFile, true);

                        writer.write("FROM: " + from + '\n');
                        writer.write("TO: " + to + '\n');
                        writer.write('\n' + messageData + '\n');
                        writer.write("========================" + '\n');
                        writer.close();
                    } else {
                        String otherServer = to.substring(to.indexOf("@") + 1);
                        File portFile = new File(otherServer + "/port.txt");
                        Scanner fileScanner = new Scanner(portFile);
                        int port = fileScanner.nextInt();

                        InetAddress ip = InetAddress.getLocalHost();
                        Socket server = new Socket(ip, port);

                        DataOutputStream serverWrite = new DataOutputStream(server.getOutputStream());
                        DataInputStream serverRead = new DataInputStream(server.getInputStream());
                        serverRead.readUTF();
                        serverWrite.writeUTF("HELLO " + Server.serverName);
                        serverRead.readUTF();
                        serverWrite.writeUTF("MAIL FROM " + from);
                        serverRead.readUTF();
                        serverWrite.writeUTF("RCPT TO");
                        serverWrite.writeUTF(to);
                        serverRead.readUTF();
                        serverWrite.writeUTF("DATA");
                        serverWrite.writeUTF(messageData);
                        serverWrite.writeUTF("&&&");
                        serverRead.readUTF();
                        serverWrite.writeUTF("QUIT");
                        serverRead.readUTF();
                        serverRead.close();
                        serverWrite.close();
                        server.close();
                    }
                }
            } else if (message.equalsIgnoreCase("QUIT")) {
                clientWrite.writeUTF("221 " + Server.serverName + " closing connection");
                clientWrite.close();
                clientRead.close();
                client.close();
                break;
            }
        }
    }

    public Socket getClient() {
        return client;
    }

    public DataInputStream getClientRead() {
        return clientRead;
    }

    public DataOutputStream getClientWrite() {
        return clientWrite;
    }

    public String getClientName() {
        return name;
    }

    public void setClientName(String name) {
        this.name = name;
    }
}