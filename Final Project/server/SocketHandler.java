package server;

import java.io.*;
import java.net.*;
import java.security.KeyStore;
import javax.net.*;
import javax.net.ssl.*;

public class SocketHandler {
    private static final int PORT = 9876;
    private static final String TYPE = "TLS";
    private ServerSocket serverSocket = null;

    public SocketHandler() {
        // Constructor
    }

    public ServerSocket getServerSocket() throws NullPointerException, IOException {
        setupServerSocket();
        return this.serverSocket;
    }

    private void setupServerSocket() throws NullPointerException, IOException {
        ServerSocketFactory ssf = getServerSocketFactory(TYPE);
        serverSocket = ssf.createServerSocket(PORT);
        ((SSLServerSocket) serverSocket).setNeedClientAuth(true); // enables client authentication
    }

    private static ServerSocketFactory getServerSocketFactory(String type) {
        if (type.equals("TLS")) {
            SSLServerSocketFactory ssf = null;
            try { // set up key manager to perform server authentication
                SSLContext ctx = SSLContext.getInstance("TLS");
                KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
                TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
                KeyStore ks = KeyStore.getInstance("JKS");
                KeyStore ts = KeyStore.getInstance("JKS");
                char[] password = "password".toCharArray();
                ks.load(new FileInputStream("certificates/serverkeystore"), password);
                ts.load(new FileInputStream("certificates/servertruststore"), password);

                kmf.init(ks, "password".toCharArray()); // certificate password (keypass)
                tmf.init(ts); // possible to use keystore as truststore here
                ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
                ssf = ctx.getServerSocketFactory();
                return ssf;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            return ServerSocketFactory.getDefault();
        }
        return null;
    }

}
