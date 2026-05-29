import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Represents a premium gym member with additional benefits such as a fixed 10% discount.
 * Extends the GymMember abstract class to inherit common member attributes and methods.
 *
 * @version 1.7
 */
public class PremiumMember extends GymMember implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private static final double PREMIUM_CHARGE = 50000.0;
    private double paidAmount;
    private String removalReason;
    private double discountAmount;

    /**
     * Constructs a new PremiumMember with the specified attributes.
     * @param id The unique identifier for the member
     * @param name The member's full name
     * @param address The member's address
     * @param phone The member's phone number
     * @param email The member's email address
     * @param gender The member's gender
     * @param dob The member's date of birth
     * @param membershipStart The start date of the membership
     * @param referral The referral source (optional)
     * @param trainer The assigned trainer
     * @param paidAmount The amount paid by the member
     * @param removalReason The reason for member removal (optional)
     */
    public PremiumMember(String id, String name, String address, String phone, String email, String gender,
                         String dob, String membershipStart, String referral, String trainer,
                         double paidAmount, String removalReason) {
        super(id, name, address, phone, email, gender, dob, membershipStart, referral, trainer);
        this.paidAmount = paidAmount >= 0 ? paidAmount : 0;
        this.removalReason = removalReason != null ? removalReason : "";
        calculateDiscount();
    }

    /**
     * Calculates the fixed 10% discount on the premium charge.
     */
    public void calculateDiscount() {
        // Apply a fixed 10% discount on the premium charge
        this.discountAmount = 0.10 * PREMIUM_CHARGE;
    }

    /**
     * Gets the discount amount applied to the premium membership.
     * @return The discount amount
     */
    public double getDiscountAmount() {
        return discountAmount;
    }

    /**
     * Adds a payment to the member's account.
     * @param amount The payment amount
     * @throws IllegalArgumentException if the payment amount is negative
     */
    public void addPayment(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Payment amount cannot be negative");
        }
        this.paidAmount += amount;
    }

    /**
     * Gets the total amount paid by the member.
     * @return The paid amount
     */
    public double getPaidAmount() {
        return paidAmount;
    }

    /**
     * Gets the reason for member removal.
     * @return The removal reason
     */
    public String getRemovalReason() {
        return removalReason;
    }

    /**
     * Sets the reason for member removal.
     * @param reason The removal reason
     */
    public void setRemovalReason(String reason) {
        this.removalReason = reason != null ? reason : "";
    }

    /**
     * Gets the premium membership charge.
     * @return The premium charge
     */
    public double getPremiumCharge() {
        return PREMIUM_CHARGE;
    }

    /**
     * Logs the member's current attendance count to the console.
     */
    public void logAttendance() {
        System.out.println("Premium Member ID " + getId() + ": Current attendance count = " + getAttendanceCount());
    }

    /**
     * Displays the premium member's details, including payment and attendance information.
     * @return A formatted string containing the member's details
     */
    @Override
    public String display() {
        logAttendance();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate dobDate = LocalDate.parse(getDob(), formatter);
        LocalDate msDate = LocalDate.parse(getMembershipStart(), formatter);
        return String.format("Premium Member Details:%n" +
                        "ID: %s%nName: %s%nAddress: %s%nPhone: %s%nEmail: %s%n" +
                        "Gender: %s%nDOB: %s%nMembership Start: %s%nReferral: %s%n" +
                        "Trainer: %s%nPaid Amount: %.2f%nDiscount: %.2f%n" +
                        "Due Amount: %.2f%nExtra Paid: %.2f%nRemoval Reason: %s%n" +
                        "Active: %s%nAttendance: %d",
                getId(), getName(), getAddress(), getPhone(), getEmail(),
                getGender(), dobDate.format(formatter), msDate.format(formatter),
                getReferral(), getTrainer(), paidAmount, discountAmount,
                Math.max(0.0, (PREMIUM_CHARGE - discountAmount) - paidAmount),
                Math.max(0.0, paidAmount - (PREMIUM_CHARGE - discountAmount)),
                removalReason, isActive() ? "Yes" : "No", getAttendanceCount());
    }
}
