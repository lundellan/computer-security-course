package server;

import java.util.*;

public class MedicalRecordHandler {
    private UsersHandler usersHandler;
    private HashMap<String, MedicalRecord> medicalRecordDB = new HashMap<String, MedicalRecord>();

    public MedicalRecordHandler(UsersHandler usersHandler) {
        this.usersHandler = usersHandler;
        addSimulatedUsers();
    }

    public MedicalRecord getMedicalRecord(String patientSocialNumber) {
        return medicalRecordDB.get(patientSocialNumber);
    }

    public MedicalRecord createMedicalRecord(Identity user, Identity nurse, Division division, String patientSocialNumber) {
        MedicalRecord newRecord = new MedicalRecord(user, nurse, division, patientSocialNumber);
        medicalRecordDB.put(patientSocialNumber, newRecord);
        return newRecord;
    }

    public void deleteMedicalRecord(String patientSocialNumber) {
        MedicalRecord patientRecord = medicalRecordDB.get(patientSocialNumber);
        patientRecord.deleteMedicalData();
    }

    private void addSimulatedUsers() {
        Identity doctor1 = usersHandler.createNewUser("10734-01", "doktor1", Division.ORTHROPEDICS); // Creates doctor1
        Identity doctor2 = usersHandler.createNewUser("43616-01", "doktor2", Division.PEDIATRICS); // Creates doctor2
        Identity nurse1 = usersHandler.createNewUser("56468-02", "nurse1", Division.ORTHROPEDICS); // Creates nurse1
        Identity nurse2 = usersHandler.createNewUser("34582-02", "nurse2", Division.PEDIATRICS); // Creates nurse2
        Identity kalle = usersHandler.createNewUser("19850115-5679", "Kalle", Division.ORTHROPEDICS);
        Identity ella = usersHandler.createNewUser("20181105-8654", "Ella", Division.PEDIATRICS); 
        Identity kim = usersHandler.createNewUser("19950624-5386", "Kim", Division.ORTHROPEDICS);
        kalle.setDoctor(doctor1);
        ella.setDoctor(doctor2);
        kim.setDoctor(doctor1);
        usersHandler.createNewUser("66546-00", "GOVERNMENTAGENCY", Division.NONE); // Creates govermental agency
        
        // Creates medical record for Kalle
        MedicalRecord newMedicalRecord = createMedicalRecord(doctor1, nurse1, Division.ORTHROPEDICS, "19850115-5679");
        newMedicalRecord.writeToRecord(doctor1,
                "Patient explaining about pain in joint. Looks like a nasty tendon injury..");
        newMedicalRecord.writeToRecord(doctor1, "Follow up visit on nasty tendon injury. Still looks nasty.");
        // Creates medical record for Ella
        createMedicalRecord(doctor2, nurse2, Division.PEDIATRICS, "20181105-8654");
    }
}
