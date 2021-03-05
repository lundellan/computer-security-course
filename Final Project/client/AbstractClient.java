package client;

import java.io.*;
import javax.net.ssl.*;
import javax.security.cert.X509Certificate;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.*;

public abstract class AbstractClient {
    private BufferedReader read;
    private PrintWriter out;
    private BufferedReader in;
    private char[] password;

    protected AbstractClient() {
        read = new BufferedReader(new InputStreamReader(System.in));
    }

    public String reply() throws IOException {
        System.out.print(">> ");
        return read.readLine();
    }

    public boolean recieveMessage() throws IOException {
        boolean eom = false;
        String message = in.readLine();
        if (message.length() > 3) {
            String end = message.substring(message.length() - 3);
            if (end.equals("EOM")) {
                eom = true;
                message = message.substring(0, message.length() - 3);
            }
        }
        System.out.println(message);
        return eom;
    }

    public void send(String message) {
        out.println(message);
        out.flush();
    }

    public String login() {
        System.out.print("============= LOGIN =============\n");
        System.out.print("Id: ");
        String id = System.console().readLine();
        System.out.print("Password: ");
        password = System.console().readPassword();
        return id;
    }

    public boolean establishConnection(String host, int port, String id)
            throws KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException,
            IOException, UnrecoverableKeyException, KeyManagementException {
        KeyStore ks = KeyStore.getInstance("JKS");
        KeyStore ts = KeyStore.getInstance("JKS");
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        SSLContext ctx = SSLContext.getInstance("TLS");
        try {
            ks.load(new FileInputStream("certificates/" + id + "keystore"), password);
            ts.load(new FileInputStream("certificates/" + id + "truststore"), password);
        } catch (Exception e) {
            System.out.println("Incorrect Id or password");
            return false;
        }

        kmf.init(ks, password); // user password (keypass)
        tmf.init(ts); // keystore can be used as truststore here
        ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        SSLSocketFactory factory = ctx.getSocketFactory();
        SSLSocket socket = (SSLSocket) factory.createSocket(host, port);
        socket.startHandshake();
        SSLSession session = socket.getSession();
        X509Certificate cert = (X509Certificate) session.getPeerCertificateChain()[0];

        System.out.println("Secure connection established to peer host: " + session.getPeerHost() + " with Protocol: "
                + session.getProtocol() + "\n");
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        return true;
    }

}
