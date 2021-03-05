package server;

import java.io.*;

public class Repl {
    private ActionHandler action;
    private PrintWriter out;
    private BufferedReader in;
    private static final String WELCOME_MESSAGE = "============== WELCOME ==============";
    private static final String NEW_ACTION_MESSAGE = "To read medical record insert:          read\n"
            + "Write to medical record insert:         write\n" + "Create a new medical record insert:     create\n"
            + "Delete medical record insert:           delete\n" + "Close the program and quit, insert:     quitEOM";

    private static final String UNAUTHERIZED = "\nUnauthorized action! \n";
    private static final String UNAUTHERIZED_SERVER_MESSAGE = "User tried to perform unautherized action";

    public Repl(ActionHandler action, BufferedReader in, PrintWriter out) {
        this.action = action;
        this.in = in;
        this.out = out;
    }

    public void start() throws IOException {
        // Print out Welcome message & manual with avaiable options for client.
        // Waits for user to type in which action to perform
        out.println(WELCOME_MESSAGE);
        out.println(NEW_ACTION_MESSAGE);
        out.flush();

        String actionOption = null;

        while ((actionOption = in.readLine()) != null) {
            switch (actionOption) {
                case "read": {
                    String patientSocialNumber = readPatientName();
                    try {
                        String medRecText = action.readRecord(patientSocialNumber);
                        out.println(medRecText);
                        out.flush();
                    } catch (UnautherizedException e) {
                        out.println(UNAUTHERIZED);
                        out.flush();
                        System.out.println(UNAUTHERIZED_SERVER_MESSAGE);
                    }
                    break;
                }
                case "write": {
                    String patientSocialNumber = readPatientName();
                    out.println("Type information to be added to medical record of patient: " + patientSocialNumber
                            + "EOM");
                    out.flush();
                    String newText = in.readLine();
                    try {
                        action.writeToRecord(patientSocialNumber, newText);
                        out.println("\nSuccessfully added new information to patient's medical record.");
                        out.flush();
                    } catch (UnautherizedException e) {
                        out.println(UNAUTHERIZED + e.getMessage());
                        out.flush();
                        System.out.println(UNAUTHERIZED_SERVER_MESSAGE);
                    }
                    break;
                }
                case "create": {
                    String patientSocialNumber = readPatientName();
                    out.println("Type id number of associated nurse:EOM");
                    out.flush();
                    String nurseOfPatient = in.readLine();
                    try {
                        action.createMedicalRecord(patientSocialNumber, nurseOfPatient);
                        out.println("\nSuccessfully created new medical record for patient.");
                        out.flush();
                    } catch (UnautherizedException e) {
                        out.println(UNAUTHERIZED + e.getMessage());
                        out.flush();
                        System.out.println(UNAUTHERIZED_SERVER_MESSAGE);
                    }
                    break;
                }
                case "delete": {
                    try {
                        String patientSocialNumber = readPatientName();
                        action.delete(patientSocialNumber);
                        out.println("\nSuccessfully deleted medical record of patient.");
                        out.flush();
                    } catch (UnautherizedException e) {
                        out.println(UNAUTHERIZED);
                        out.flush();
                        System.out.println(UNAUTHERIZED_SERVER_MESSAGE);
                    }
                    break;
                }
                case "quit": {
                    return;
                }
                default: {
                    out.println("\nNot an valid option.");
                    out.flush();
                    break;
                }
            }
            actionOption = null;
            out.println("\n=================================");
            out.println(NEW_ACTION_MESSAGE);
            out.flush();
        }
    }

    private String readPatientName() throws IOException {
        out.println("Type social security number for patient:EOM");
        out.flush();
        return in.readLine();
    }

}