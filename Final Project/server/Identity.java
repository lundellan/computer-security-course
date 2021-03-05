package server;

public class Identity {
	private String name;
	private Title title;
	private Division division;
	private String userID;
	private Identity currentDoctor;

	public Identity(String name, int title, Division division, String userID) {
		this.name = name;
		this.division = division;
		this.userID = userID;
		this.title = setTitle(title);
	}

	private Title setTitle(int title) {

		switch (title) {
			case 0: {
				return Title.DOCTOR;
			}
			case 1: {
				return Title.NURSE;
			}
			case 2: {
				return Title.PATIENT;
			}
			case 3: {
				return Title.GOVERNMENT_AGENCY;
			}
			default:
				return Title.UNKNOWN;
		}
	}

	public String getName() {
		return name;
	}

	public void setDoctor(Identity doctor) {
		if (title == Title.PATIENT) {
			currentDoctor = doctor;
		} else {
			System.out.println("Cannot assign a doctor to a nonpatient");
		}
	}

	public Identity getDoctor() {
		if (currentDoctor != null) {
			return currentDoctor;
		}
		System.out.println("This person has no assigned doctor");
		return currentDoctor;
	}

	public Division getDivision() {
		return division;
	}

	public String getUserID() {
		return userID;
	}

	public Title getTitleEnum() {
		return title;
	}

}
