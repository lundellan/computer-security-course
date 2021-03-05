package server;

import java.util.ArrayList;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

public class MedicalRecord {
	private ArrayList<Identity> doctors;
	private ArrayList<Identity> nurses;
	private String patientSocialNumber;
	private Division division;
	private String medicalData;

	public MedicalRecord(Identity doctor, Identity nurse, Division division, String patientSecurityNumber) {
		this.doctors = new ArrayList<Identity>();
		this.nurses = new ArrayList<Identity>();
		this.doctors.add(doctor);
		this.nurses.add(nurse);
		this.patientSocialNumber = patientSecurityNumber;
		this.division = division;
		this.medicalData = "\n---------------------------------------------\nMedical Journal for patient: "
				+ patientSecurityNumber + "\n---------------------------------------------";
	}

	public String readRecord(Identity userObject) {
		Title title = userObject.getTitleEnum();
		switch (title) {
			case DOCTOR: {
				if (doctors.contains(userObject) || userObject.getDivision().equals(division)) {
					return medicalData;
				}
				return null;
			}
			case NURSE: {
				if (nurses.contains(userObject)) {
					return medicalData;
				}
				return null;
			}
			case PATIENT: {
				if (userObject.getUserID().equals(patientSocialNumber)) {
					return medicalData;
				}
				return null;
			}
			case GOVERNMENT_AGENCY: {
				return medicalData;
			}
			default: {
				return null;
			}
		}
	}

	public boolean writeToRecord(Identity userObject, String textToAdd) {
		Title title = userObject.getTitleEnum();
		switch (title) {
			case DOCTOR: {
				if (doctors.contains(userObject) || userObject.getDivision().equals(division)) {
					medicalData = medicalData.concat("\n" + getDateTime() + ":  " + textToAdd); // Writes to medical
																								// data
					return true;
				}
				return false;
			}
			case NURSE: {
				if (nurses.contains(userObject)) {
					medicalData = medicalData.concat("\n" + getDateTime() + ":  " + textToAdd); // Writes to medical
																								// data
					return true;
				}
				return false;
			}
			default: {
				return false;
			}
		}
	}

	public void deleteMedicalData() {
		medicalData = "\n---------------------------------------------\nMedical Journal for patient: "
				+ patientSocialNumber + "\n---------------------------------------------";
	}

	private String getDateTime() {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		LocalDateTime now = LocalDateTime.now();
		return dtf.format(now);
	}
}
