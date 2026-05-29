/**
 * Abstract class representing a gym member with basic attributes and behaviors.
 *
 * @version 1.4
 */
public abstract class GymMember implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String name;
    private String address;
    private String phone;
    private String email;
    private String gender;
    private String dob;
    private String membershipStart;
    private String referral;
    private String trainer;
    private boolean active;
    private int attendanceCount;

    /**
     * Constructs a new GymMember with the specified attributes.
     * @param id The unique identifier for the member
     * @param name The member's full name
     * @param address The member's address
     * @param phone The member's phone number
     * @param email The member's email address
     * @param gender The member's gender
     * @param dob The member's date of birth
     * @param membershipStart The start date of the membership
     * @param referral The referral source (optional)
     * @param trainer The assigned trainer (optional)
     */
    public GymMember(String id, String name, String address, String phone, String email, String gender,
                     String dob, String membershipStart, String referral, String trainer) {
        this.id = id != null ? id.trim() : "";
        this.name = name != null ? name.trim() : "";
        this.address = address != null ? address.trim() : "";
        this.phone = phone != null ? phone.trim() : "";
        this.email = email != null ? email.trim() : "";
        this.gender = gender != null ? gender.trim() : "";
        this.dob = dob != null ? dob.trim() : "";
        this.membershipStart = membershipStart != null ? membershipStart.trim() : "";
        this.referral = referral != null ? referral.trim() : "";
        this.trainer = trainer != null ? trainer.trim() : "";
        this.active = true;
        this.attendanceCount = 0;
    }

    /**
     * Activates the member's membership.
     */
    public void activateMembership() {
        active = true;
    }

    /**
     * Deactivates the member's membership.
     */
    public void deactivateMembership() {
        active = false;
    }

    /**
     * Marks attendance for the member if active.
     * @throws IllegalStateException if the member is inactive
     */
    public void markAttendance() {
        if (active) {
            attendanceCount++;
            System.out.println("Attendance marked for ID " + id + ": New count = " + attendanceCount);
        } else {
            throw new IllegalStateException("Cannot mark attendance for inactive member");
        }
    }

    /**
     * Reverts the member's status to inactive.
     */
    public void revertMember() {
        active = false;
    }

    /**
     * Gets the member's ID.
     * @return The member's ID
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the member's name.
     * @return The member's name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the member's address.
     * @return The member's address
     */
    public String getAddress() {
        return address;
    }

    /**
     * Gets the member's phone number.
     * @return The member's phone number
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Gets the member's email address.
     * @return The member's email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Gets the member's gender.
     * @return The member's gender
     */
    public String getGender() {
        return gender;
    }

    /**
     * Gets the member's date of birth.
     * @return The member's date of birth
     */
    public String getDob() {
        return dob;
    }

    /**
     * Gets the membership start date.
     * @return The membership start date
     */
    public String getMembershipStart() {
        return membershipStart;
    }

    /**
     * Gets the member's referral source.
     * @return The referral source
     */
    public String getReferral() {
        return referral;
    }

    /**
     * Gets the member's assigned trainer.
     * @return The trainer's name
     */
    public String getTrainer() {
        return trainer;
    }

    /**
     * Checks if the member's membership is active.
     * @return true if the membership is active, false otherwise
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Gets the member's attendance count.
     * @return The number of attendances
     */
    public int getAttendanceCount() {
        return attendanceCount;
    }

    /**
     * Displays the member's details.
     * @return A string representation of the member's details
     */
    public abstract String display();
}
