package server;

import java.util.*;

public class UsersHandler {
    private HashMap<String, Identity> usersDB = new HashMap<String, Identity>();

    public UsersHandler() {
        // Constructor
    }

    public Identity getUserFromDB(String userID) {
        return usersDB.get(userID);
    }

    public Identity createNewUser(String userID, String userName, Division division) {
        // Måste hämta ut info om användare från certifikatet och skicka med i
        // konstruktor för new Identity()
        // Titta på userIDs sista numer och väljer vilken typ Identity objektet ska
        // skapas som
        int userType = -1;
        if (userID.length() < 13) {
            String[] splittedID = userID.split("-");
            switch (splittedID[1]) {
                case "00": {
                    userType = 3; // Sets new user type to governmental agency
                    break;
                }
                case "01": {
                    userType = 0; // Sets new user type to doctor
                    break;
                }
                case "02": {
                    userType = 1; // Sets new user type to nurse
                    break;
                }
                default: {
                    System.out.println("\nSomething went wrong when finding type for new user...");
                }
            }
        } else {
            userType = 2; // Sets new user type patient
        }

        Identity newUser = new Identity(userName, userType, division, userID);
        usersDB.put(userID, newUser);
        return newUser;
    }
}
