package client;

import java.io.*;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.*;

public class Client extends AbstractClient {
    public Client() throws IOException {
        super();
    }

    private String host = "localhost";
    private int port = 9876;

    private void run() throws KeyStoreException, NoSuchAlgorithmException, IOException, UnrecoverableKeyException,
            KeyManagementException, CertificateException {
        while (true) {
            String id = login();
            System.out.println("\nYou are being verified - connecting to server...");
            if (!establishConnection(host, port, id)) {
                continue;
            }
            String clientInputMsg = "";
            while (true) {
                boolean endOfMessage = recieveMessage();
                if (endOfMessage) {
                    clientInputMsg = reply();
                    if (clientInputMsg.equalsIgnoreCase("quit")) {
                        send(clientInputMsg);
                        break;
                    }
                    send(clientInputMsg);
                }
            }
        }
    }

    public static void main(String[] args) throws KeyStoreException, IOException, UnrecoverableKeyException,
            KeyManagementException, CertificateException, NoSuchAlgorithmException {
        Client c = new Client();
        c.run();
    }
}
