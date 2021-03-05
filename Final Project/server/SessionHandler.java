package server;

import javax.net.ssl.*;
import javax.security.cert.X509Certificate;

public class SessionHandler {
    private SSLSocket socket;
    private UsersHandler usersHandler;
    private String nameOfUser;
    private String userIDFromCert;
    private Division division;

    public SessionHandler(SSLSocket socket, UsersHandler usersHandler) throws SSLPeerUnverifiedException {
        this.socket = socket;
        this.usersHandler = usersHandler;
        extractCertInformation();
    }

    public Identity getConnectedUser() {
        Identity user = usersHandler.getUserFromDB(userIDFromCert);
        if (user == null) {
            System.out.println("Could not find user with given ID. Creating a new user.");
            user = usersHandler.createNewUser(userIDFromCert, nameOfUser, division);
        }
        return user;
    }

    private void extractCertInformation() throws SSLPeerUnverifiedException {
        SSLSession session = socket.getSession();
        X509Certificate cert = (X509Certificate) session.getPeerCertificateChain()[0];
        String subject = cert.getSubjectDN().getName();
        int endCN = subject.indexOf(',');
        String commonNameField = subject.substring(3, endCN);
        int inxDivStart = subject.indexOf('=', endCN);
        int inxDivEnd = subject.indexOf(",", endCN + 1);
        String[] CnParts = commonNameField.split(":");
        nameOfUser = CnParts[0];
        userIDFromCert = CnParts[1];
        String div = subject.substring(inxDivStart + 1, inxDivEnd).toUpperCase();
        if (div.equals("UNKNOWN")) {
            division = Division.NONE;
        } else {
            division = Division.valueOf(div);
        }
        System.out.println(
                "New peer host connected from " + session.getPeerHost() + " with Protocol: " + session.getProtocol());
    }
}