import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Represents a regular gym member with specific plan options.
 * Extends the GymMember abstract class to inherit common member attributes and methods.
 *
 * @version 1.5
 */
public class RegularMember extends GymMember implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private static final double BASIC_PRICE = 6500.0;
    private static final double STANDARD_PRICE = 12500.0;
    private static final double DELUXE_PRICE = 18500.0;
    private String plan;
    private double paidAmount;
    private String removalReason;

    /**
     * Constructs a new RegularMember with the specified attributes.
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
     * @param plan The membership plan (Basic, Standard, Deluxe)
     * @param paidAmount The amount paid by the member
     */
    public RegularMember(String id, String name, String address, String phone, String email, String gender,
                         String dob, String membershipStart, String referral, String trainer,
                         String plan, double paidAmount) {
        super(id, name, address, phone, email, gender, dob, membershipStart, referral, trainer);
        this.plan = validatePlan(plan);
        this.paidAmount = paidAmount >= 0 ? paidAmount : 0;
        this.removalReason = "";
    }

    /**
     * Validates and normalizes the membership plan.
     * @param plan The plan to validate
     * @return The validated and normalized plan name
     * @throws IllegalArgumentException if the plan is invalid
     */
    private String validatePlan(String plan) {
        if (plan == null || plan.trim().isEmpty()) {
            return "Basic";
        }
        String trimmedPlan = plan.trim();
        if (trimmedPlan.equalsIgnoreCase("Basic") ||
            trimmedPlan.equalsIgnoreCase("Standard") ||
            trimmedPlan.equalsIgnoreCase("Deluxe")) {
            return trimmedPlan.substring(0, 1).toUpperCase() + trimmedPlan.substring(1).toLowerCase();
        }
        throw new IllegalArgumentException("Invalid plan type: " + plan);
    }

    /**
     * Gets the price for the specified plan.
     * @param plan The membership plan
     * @return The price of the plan
     * @throws IllegalArgumentException if the plan is invalid
     */
    public double getPlanPrice(String plan) {
        return switch (validatePlan(plan)) {
            case "Basic" -> BASIC_PRICE;
            case "Standard" -> STANDARD_PRICE;
            case "Deluxe" -> DELUXE_PRICE;
            default -> throw new IllegalArgumentException("Invalid plan type: " + plan);
        };
    }

    /**
     * Gets the current membership plan.
     * @return The current plan
     */
    public String getPlan() {
        return plan;
    }

    /**
     * Upgrades the member's plan based on attendance requirements.
     * @param newPlan The new plan to upgrade to
     * @throws IllegalArgumentException if the plan is invalid or attendance requirements are not met
     */
    public void upgradePlan(String newPlan) {
        String validatedPlan = validatePlan(newPlan);
        int attendance = getAttendanceCount();
        System.out.println("Checking upgrade for ID " + getId() + ": Plan = " + newPlan + ", Attendance = " + attendance);
        if (validatedPlan.equals("Standard") && attendance < 10) {
            throw new IllegalArgumentException("Need at least 10 attendance records to upgrade to Standard plan");
        } else if (validatedPlan.equals("Deluxe") && attendance < 15) {
            throw new IllegalArgumentException("Need at least 15 attendance records to upgrade to Deluxe plan");
        }
        this.plan = validatedPlan;
        System.out.println("Upgraded ID " + getId() + " to plan: " + validatedPlan);
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
     * Logs the member's current attendance count to the console.
     */
    public void logAttendance() {
        System.out.println("Regular Member ID " + getId() + ": Current attendance count = " + getAttendanceCount());
    }

    /**
     * Displays the regular member's details, including plan and payment information.
     * @return A formatted string containing the member's details
     */
    @Override
    public String display() {
        logAttendance();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate dobDate = LocalDate.parse(getDob(), formatter);
        LocalDate msDate = LocalDate.parse(getMembershipStart(), formatter);
        double planPrice = getPlanPrice(plan);
        return String.format("Regular Member Details:%n" +
                        "ID: %s%nName: %s%nAddress: %s%nPhone: %s%nEmail: %s%n" +
                        "Gender: %s%nDOB: %s%nMembership Start: %s%nReferral: %s%n" +
                        "Trainer: %s%nPlan: %s%nPrice: %.2f%nPaid Amount: %.2f%n" +
                        "Due Amount: %.2f%nExtra Paid: %.2f%nRemoval Reason: %s%n" +
                        "Active: %s%nAttendance: %d",
                getId(), getName(), getAddress(), getPhone(), getEmail(),
                getGender(), dobDate.format(formatter), msDate.format(formatter),
                getReferral(), getTrainer(), plan, planPrice, paidAmount,
                Math.max(0.0, planPrice - paidAmount),
                Math.max(0.0, paidAmount - planPrice),
                removalReason, isActive() ? "Yes" : "No", getAttendanceCount());
    }
}