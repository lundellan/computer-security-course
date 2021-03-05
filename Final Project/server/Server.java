package server;

import java.io.*;
import java.net.*;
import javax.net.ssl.*;

public class Server implements Runnable {
    private ServerSocket serverSocket;
    private SessionHandler sessionHandler;
    private MedicalRecordHandler medRecHandler;
    private UsersHandler usersHandler;
    private int numConnectedClients = 0;

    public Server(ServerSocket ss, UsersHandler usersHandler, MedicalRecordHandler medRecHandler) {
        serverSocket = ss;
        this.usersHandler = usersHandler;
        this.medRecHandler = medRecHandler;
        newListener();
    }

    public void run() {
        try {
            SSLSocket socket = (SSLSocket) serverSocket.accept();
            newListener();
            numConnectedClients++;
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            try {
                sessionHandler = new SessionHandler(socket, usersHandler);
            } catch (Exception e) {
                System.out.println("Something went wrong when extracing certificate identity");
                e.printStackTrace();
                return;
            }

            Identity userObject = sessionHandler.getConnectedUser();
            ActionHandler actionHandler = new ActionHandler(usersHandler, medRecHandler, userObject);
            System.out.println("User " + userObject.getName() + " logged in successfully"); // Can add new logins to the
                                                                                            // audit log
            Repl repl = new Repl(actionHandler, in, out);

            repl.start();
            System.out.println("worked here");
            in.close();
            out.close();
            socket.close();
            numConnectedClients--;
            System.out.println("Client disconnected");
            System.out.println(numConnectedClients + " concurrent connection(s)\n");
        } catch (IOException e) {
            System.out.println("Client died: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void newListener() {
        (new Thread(this)).start();
    }

    public static void main(String args[]) {
        try {
            SocketHandler socketHandler = new SocketHandler();
            ServerSocket serverSocket = socketHandler.getServerSocket();
            System.out.println("\nServer Started successfully. Waiting for connections...\n");
            UsersHandler usersHandler = new UsersHandler();
            MedicalRecordHandler medRecHandler = new MedicalRecordHandler(usersHandler);
            new Server(serverSocket, usersHandler, medRecHandler);
        } catch (IOException e) {
            System.out.println("Unable to start Server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
