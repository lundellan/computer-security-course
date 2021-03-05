package server;

import java.io.*;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

public class ActionHandler {
    private UsersHandler usersHandler;
    private MedicalRecordHandler medRecHandler;
    private Identity user;

    public ActionHandler(UsersHandler usersHandler, MedicalRecordHandler medRecHandler, Identity user) {
        this.usersHandler = usersHandler;
        this.medRecHandler = medRecHandler;
        this.user = user;
    }

    public String readRecord(String patientSocialNumber) throws UnautherizedException, IOException {
        MedicalRecord medicalRec = medRecHandler.getMedicalRecord(patientSocialNumber);
        String medicalRecordText = medicalRec.readRecord(user);
        if (medicalRecordText == null) {
            writeToAuditLog("unsuccessfully read medical record of patient " + patientSocialNumber);
            throw new UnautherizedException();
        }
        writeToAuditLog("successfully read medical record of patient " + patientSocialNumber);
        return medicalRecordText;
    }

    public void writeToRecord(String patientSocialNumber, String newText) throws UnautherizedException, IOException {
        MedicalRecord medicalRec = medRecHandler.getMedicalRecord(patientSocialNumber);
        if (!medicalRec.writeToRecord(user, newText)) {
            writeToAuditLog("unsuccessfully wrote to medical record of patient " + patientSocialNumber);
            throw new UnautherizedException();
        }
        writeToAuditLog("successfully wrote to medical record of patient " + patientSocialNumber);
    }

    public void createMedicalRecord(String patientSocialNumber, String nurseOfPatient) throws UnautherizedException, IOException {
        if (user.getTitleEnum().equals(Title.DOCTOR)) {
            Identity nurse = usersHandler.getUserFromDB(nurseOfPatient);
            Division division = user.getDivision();
            if (!nurse.getDivision().equals(division)) {
                throw new UnautherizedException("Nurse and Doctor must belong to same division!");
            }
            Identity patient = usersHandler.getUserFromDB(patientSocialNumber);
            String patientCurrentDoc = patient.getDoctor().getUserID();
            if (!patientCurrentDoc.equals(user.getUserID())){
                throw new UnautherizedException("Only doctor current treating patient can create new medical record");
            }
            medRecHandler.createMedicalRecord(user, nurse, division, patientSocialNumber);
            writeToAuditLog("successfully created new medical record for patient " + patientSocialNumber);
        } else {
            writeToAuditLog("unsuccessfully created new medical record for patient " + patientSocialNumber);
            throw new UnautherizedException();
        }
    }

    public void delete(String patientSocialNumber) throws UnautherizedException, IOException {
        if (user.getTitleEnum().equals(Title.GOVERNMENT_AGENCY)) {
            medRecHandler.deleteMedicalRecord(patientSocialNumber);
            writeToAuditLog("successfully deleted medical record for patient " + patientSocialNumber);
        } else {
            writeToAuditLog("unsuccessfully deleted medical record for patient " + patientSocialNumber);
            throw new UnautherizedException();
        }
    }

    private void writeToAuditLog(String auditMessage) throws IOException {
        try {
            FileWriter auditLogWriter = new FileWriter("server/auditlog.txt", true);
            String audit = "\n" + getDateTime() + " " + user.getName() + " " + auditMessage;
            auditLogWriter.append(audit);
            auditLogWriter.close();
        } catch (IOException e) {
            System.out.println("An error occurred when writing to auditLog.");
            e.printStackTrace();
        }
    }

    private String getDateTime() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }

}
