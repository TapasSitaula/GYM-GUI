import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

/**
 * A Java Swing-based GUI for managing gym members, supporting addition, display, activation/deactivation,
 * attendance tracking, plan upgrades, payments, and data persistence in text and binary formats.
 *
 * @version 1.13
 * @author [Tapas Situala]
 */
public class GymGUI extends JFrame implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private static final double REGULAR_BASE_PRICE = 6500.0;
    private static final double PREMIUM_CHARGE = 50000.0;

    private JTextField idField, nameField, locationField, phoneField, emailField, referralField,
            paidAmountField, removalReasonField, trainerField, regularPriceField, premiumChargeField, discountField;
    private JRadioButton maleRadio, femaleRadio;
    private JList<String> planList;
    private JComboBox<Integer> dobDayComboBox, dobMonthComboBox, dobYearComboBox;
    private JComboBox<Integer> msDayComboBox, msMonthComboBox, msYearComboBox;
    private Map<String, GymMember> members;
    private JToggleButton memberTypeToggle;
    private JTextArea messageArea;

    private static final Color PRIMARY_COLOR = new Color(0, 123, 255);
    private static final Color SUCCESS_COLOR = new Color(40, 167, 69);
    private static final Color WARNING_COLOR = new Color(255, 193, 7);
    private static final Color DANGER_COLOR = new Color(220, 53, 69);
    private static final Color BACKGROUND_COLOR = new Color(245, 247, 250);
    private static final Color PANEL_COLOR = Color.WHITE;

    /**
     * Constructs a new GymGUI instance, initializing the member map and setting up the UI.
     */
    public GymGUI() {
        members = new HashMap<>();
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.put("OptionPane.messageFont", new Font("Arial", Font.PLAIN, 12));
            UIManager.put("OptionPane.buttonFont", new Font("Arial", Font.PLAIN, 12));
        } catch (Exception e) {
            appendToMessageArea("Error setting look and feel: " + e.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
        initializeComponents();
    }

    /**
     * Initializes all UI components and layouts for the gym management system.
     */
    private void initializeComponents() {
        setTitle("Gym Management System");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(BACKGROUND_COLOR);

        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel contentPanel = createContentPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        JPanel messagePanel = createMessagePanel();
        mainPanel.add(messagePanel, BorderLayout.SOUTH);

        add(mainPanel);
        setVisible(true);
    }

    /**
     * Creates the header panel containing the title and control buttons.
     *
     * @return JPanel containing the header components
     */
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PANEL_COLOR);
        headerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("Gym Management System");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(PRIMARY_COLOR);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(PANEL_COLOR);

        memberTypeToggle = new JToggleButton("Premium Member");
        memberTypeToggle.setFont(new Font("Arial", Font.BOLD, 14));
        memberTypeToggle.setBackground(WARNING_COLOR);
        memberTypeToggle.setForeground(Color.BLACK);
        memberTypeToggle.setBorder(new LineBorder(Color.BLACK, 1));
        memberTypeToggle.setPreferredSize(new Dimension(150, 35));
        memberTypeToggle.addActionListener(e -> {
            boolean isPremium = memberTypeToggle.isSelected();
            memberTypeToggle.setText(isPremium ? "Regular Member" : "Premium Member");
            planList.setEnabled(isPremium);
            trainerField.setEnabled(!isPremium);
            trainerField.setText("");
            appendToMessageArea(isPremium ? "Switched to Regular mode" : "Switched to Premium mode", JOptionPane.INFORMATION_MESSAGE);
        });
        buttonPanel.add(memberTypeToggle);

        JButton clearButton = createButton("Clear", DANGER_COLOR);
        clearButton.setPreferredSize(new Dimension(150, 35));
        clearButton.addActionListener(e -> clearFields());
        buttonPanel.add(clearButton);

        headerPanel.add(buttonPanel, BorderLayout.EAST);

        return headerPanel;
    }

    /**
     * Creates the main content panel containing input and action panels.
     *
     * @return JPanel containing the content components
     */
    private JPanel createContentPanel() {
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBackground(PANEL_COLOR);
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel inputPanel = createInputPanel();
        JPanel actionPanel = createActionPanel();

        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.X_AXIS));
        mainContent.setBackground(PANEL_COLOR);
        mainContent.add(inputPanel);
        mainContent.add(Box.createHorizontalStrut(20));
        mainContent.add(actionPanel);

        contentPanel.add(new JScrollPane(mainContent), BorderLayout.CENTER);
        return contentPanel;
    }

    /**
     * Creates the input panel for entering member details.
     *
     * @return JPanel containing input fields and selectors
     */
    private JPanel createInputPanel() {
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.X_AXIS));
        inputPanel.setBackground(PANEL_COLOR);
        inputPanel.setBorder(new TitledBorder(new LineBorder(Color.GRAY), "Member Details", TitledBorder.CENTER, TitledBorder.TOP, new Font("Arial", Font.BOLD, 14)));

        JPanel personalPanel = new JPanel();
        personalPanel.setLayout(new BoxLayout(personalPanel, BoxLayout.Y_AXIS));
        personalPanel.setBackground(PANEL_COLOR);
        personalPanel.setBorder(new TitledBorder(new LineBorder(Color.GRAY), "Personal Information", TitledBorder.LEFT, TitledBorder.TOP, new Font("Arial", Font.BOLD, 14)));

        idField = createTextField("Member ID:", "Enter ID (e.g., M123)", 15);
        nameField = createTextField("Name:", "Enter full name", 15);
        locationField = createTextField("Address:", "Enter address", 15);
        phoneField = createTextField("Phone:", "Enter 10-digit phone", 15);
        emailField = createTextField("Email:", "Enter email", 15);
        referralField = createTextField("Referral:", "Enter referral (optional)", 15);

        personalPanel.add(createLabeledComponent("Member ID:", idField));
        personalPanel.add(Box.createVerticalStrut(5));
        personalPanel.add(createLabeledComponent("Name:", nameField));
        personalPanel.add(Box.createVerticalStrut(5));
        personalPanel.add(createLabeledComponent("Address:", locationField));
        personalPanel.add(Box.createVerticalStrut(5));
        personalPanel.add(createLabeledComponent("Phone:", phoneField));
        personalPanel.add(Box.createVerticalStrut(5));
        personalPanel.add(createLabeledComponent("Email:", emailField));
        personalPanel.add(Box.createVerticalStrut(5));
        personalPanel.add(createLabeledComponent("Referral:", referralField));
        personalPanel.add(Box.createVerticalStrut(5));

        JPanel genderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        genderPanel.setBackground(PANEL_COLOR);
        JLabel genderLabel = new JLabel("Gender:");
        genderLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        maleRadio = new JRadioButton("Male");
        femaleRadio = new JRadioButton("Female");
        maleRadio.setFont(new Font("Arial", Font.PLAIN, 14));
        femaleRadio.setFont(new Font("Arial", Font.PLAIN, 14));
        maleRadio.setBackground(PANEL_COLOR);
        femaleRadio.setBackground(PANEL_COLOR);
        ButtonGroup genderGroup = new ButtonGroup();
        genderGroup.add(maleRadio);
        genderGroup.add(femaleRadio);
        genderPanel.add(genderLabel);
        genderPanel.add(maleRadio);
        genderPanel.add(femaleRadio);
        personalPanel.add(genderPanel);

        Integer[] days = new Integer[31];
        Integer[] months = new Integer[12];
        Integer[] years = new Integer[100];
        for (int i = 0; i < 31; i++) days[i] = i + 1;
        for (int i = 0; i < 12; i++) months[i] = i + 1;
        for (int i = 0; i < 100; i++) years[i] = 2025 - i;

        JPanel dobPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        dobPanel.setBackground(PANEL_COLOR);
        JLabel dobLabel = new JLabel("Date of Birth:");
        dobLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        dobYearComboBox = new JComboBox<>(years);
        dobMonthComboBox = new JComboBox<>(months);
        dobDayComboBox = new JComboBox<>(days);
        dobYearComboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        dobMonthComboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        dobDayComboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        dobPanel.add(dobLabel);
        dobPanel.add(dobYearComboBox);
        dobPanel.add(dobMonthComboBox);
        dobPanel.add(dobDayComboBox);
        personalPanel.add(dobPanel);

        JPanel membershipPanel = new JPanel();
        membershipPanel.setLayout(new BoxLayout(membershipPanel, BoxLayout.Y_AXIS));
        membershipPanel.setBackground(PANEL_COLOR);
        membershipPanel.setBorder(new TitledBorder(new LineBorder(Color.GRAY), "Membership Information", TitledBorder.LEFT, TitledBorder.TOP, new Font("Arial", Font.BOLD, 14)));

        JPanel msPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        msPanel.setBackground(PANEL_COLOR);
        JLabel msLabel = new JLabel("Membership Start:");
        msLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        msYearComboBox = new JComboBox<>(years);
        msMonthComboBox = new JComboBox<>(months);
        msDayComboBox = new JComboBox<>(days);
        msYearComboBox.setSelectedIndex(0);
        msYearComboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        msMonthComboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        msDayComboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        msPanel.add(msLabel);
        msPanel.add(msYearComboBox);
        msPanel.add(msMonthComboBox);
        msPanel.add(msDayComboBox);
        membershipPanel.add(msPanel);

        JPanel planPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        planPanel.setBackground(PANEL_COLOR);
        JLabel planLabel = new JLabel("Plan:");
        planLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        String[] plans = {"Basic", "Standard", "Deluxe"};
        planList = new JList<>(plans);
        planList.setFont(new Font("Arial", Font.PLAIN, 14));
        planList.setSelectedIndex(0);
        planList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane planScrollPane = new JScrollPane(planList);
        planScrollPane.setPreferredSize(new Dimension(120, 60));
        planPanel.add(planLabel);
        planPanel.add(planScrollPane);
        membershipPanel.add(planPanel);

        trainerField = createTextField("Trainer:", "Enter trainer name (required for premium)", 15);
        paidAmountField = createTextField("Paid Amount:", "Enter amount paid", 15);
        removalReasonField = createTextField("Removal Reason:", "Enter removal reason (optional)", 15);
        regularPriceField = createNonEditableTextField("Regular Price:",  String.format("%.2f", REGULAR_BASE_PRICE), 15);
        premiumChargeField = createNonEditableTextField("Premium Charge:",  String.format("%.2f", PREMIUM_CHARGE), 15);
        discountField = createNonEditableTextField("Discount:", "0.00", 15);

        membershipPanel.add(createLabeledComponent("Trainer:", trainerField));
        membershipPanel.add(Box.createVerticalStrut(5));
        membershipPanel.add(createLabeledComponent("Paid Amount:", paidAmountField));
        membershipPanel.add(Box.createVerticalStrut(5));
        membershipPanel.add(createLabeledComponent("Removal Reason:", removalReasonField));
        membershipPanel.add(Box.createVerticalStrut(5));
        membershipPanel.add(createLabeledComponent("Regular Price:", regularPriceField));
        membershipPanel.add(Box.createVerticalStrut(5));
        membershipPanel.add(createLabeledComponent("Premium Charge:", premiumChargeField));
        membershipPanel.add(Box.createVerticalStrut(5));
        membershipPanel.add(createLabeledComponent("Discount:", discountField));

        planList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedPlan = planList.getSelectedValue();
                if (selectedPlan != null) {
                    double price = new RegularMember("", "", "", "", "", "", "", "", "", "", selectedPlan, 0.0).getPlanPrice(selectedPlan);
                    regularPriceField.setText( String.format("%.2f", price));
                }
            }
        });

        inputPanel.add(personalPanel);
        inputPanel.add(Box.createHorizontalStrut(20));
        inputPanel.add(membershipPanel);
        return inputPanel;
    }

    /**
     * Creates the action panel containing buttons for various operations.
     *
     * @return JPanel containing action buttons
     */
    private JPanel createActionPanel() {
        JPanel actionPanel = new JPanel(new GridLayout(4, 3, 10, 10));
        actionPanel.setBackground(PANEL_COLOR);
        actionPanel.setBorder(new TitledBorder(new LineBorder(Color.GRAY), "Actions", TitledBorder.LEFT, TitledBorder.TOP, new Font("Arial", Font.BOLD, 14)));
        actionPanel.setPreferredSize(new Dimension(320, 180));

        JButton addBtn = createButton("Add Member", PRIMARY_COLOR);
        JButton displayBtn = createButton("Display", PRIMARY_COLOR);
        JButton activateBtn = createButton("Activate", SUCCESS_COLOR);
        JButton deactivateBtn = createButton("Deactivate", SUCCESS_COLOR);
        JButton markAttendanceBtn = createButton("Mark Attendance", SUCCESS_COLOR);
        JButton revertBtn = createButton("Revert Member", DANGER_COLOR);
        JButton upgradeBtn = createButton("Upgrade Plan", WARNING_COLOR);
        JButton saveTextBtn = createButton("Save Text", PRIMARY_COLOR);
        JButton saveFileBtn = createButton("Save File", PRIMARY_COLOR);
        JButton loadTextBtn = createButton("Load Text", PRIMARY_COLOR);
        JButton remainingDueBtn = createButton("Remaining Due", PRIMARY_COLOR);
        JButton payDueBtn = createButton("Pay Due", PRIMARY_COLOR);

        actionPanel.add(addBtn);
        actionPanel.add(displayBtn);
        actionPanel.add(activateBtn);
        actionPanel.add(deactivateBtn);
        actionPanel.add(markAttendanceBtn);
        actionPanel.add(revertBtn);
        actionPanel.add(upgradeBtn);
        actionPanel.add(saveTextBtn);
        actionPanel.add(saveFileBtn);
        actionPanel.add(loadTextBtn);
        actionPanel.add(remainingDueBtn);
        actionPanel.add(payDueBtn);

        addBtn.addActionListener(e -> addMember(memberTypeToggle.isSelected()));
        displayBtn.addActionListener(e -> displayMembers());
        activateBtn.addActionListener(e -> openActivatePopup());
        deactivateBtn.addActionListener(e -> openDeactivatePopup());
        markAttendanceBtn.addActionListener(e -> openMarkAttendancePopup());
        revertBtn.addActionListener(e -> openRevertPopup(memberTypeToggle.isSelected()));
        upgradeBtn.addActionListener(e -> openUpgradePlanPopup());
        saveTextBtn.addActionListener(e -> saveToTextFile());
        saveFileBtn.addActionListener(e -> saveToFile());
        loadTextBtn.addActionListener(e -> loadFromTextFile());
        remainingDueBtn.addActionListener(e -> openRemainingDuePopup());
        payDueBtn.addActionListener(e -> openPayDuePopup());
        return actionPanel;
    }

    /**
     * Creates the message panel for displaying system messages.
     *
     * @return JPanel containing the message area
     */
    private JPanel createMessagePanel() {
        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.setBackground(PANEL_COLOR);
        messagePanel.setBorder(new TitledBorder(new LineBorder(Color.GRAY), "Messages", TitledBorder.LEFT, TitledBorder.TOP, new Font("Arial", Font.BOLD, 14)));
        messagePanel.setPreferredSize(new Dimension(0, 150));

        messageArea = new JTextArea();
        messageArea.setFont(new Font("Arial", Font.PLAIN, 12));
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setText("Ready\n");
        JScrollPane scrollPane = new JScrollPane(messageArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        messagePanel.add(scrollPane, BorderLayout.CENTER);

        return messagePanel;
    }

    /**
     * Appends a message to the message area with a timestamp and displays it in a dialog.
     *
     * @param message The message to display
     * @param messageType The type of message (e.g., JOptionPane.INFORMATION_MESSAGE)
     */
    private void appendToMessageArea(String message, int messageType) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedMessage = String.format("Time: %s, Message: %s%n", now.format(formatter), message);
        messageArea.append(formattedMessage);
        messageArea.setCaretPosition(messageArea.getDocument().getLength());
        String title = switch (messageType) {
            case JOptionPane.ERROR_MESSAGE -> "Error";
            case JOptionPane.WARNING_MESSAGE -> "Warning";
            case JOptionPane.INFORMATION_MESSAGE -> "Success";
            default -> "System Message";
        };
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }

    /**
     * Creates a styled button with hover effects.
     *
     * @param text The button text
     * @param background The background color
     * @return JButton with specified properties
     */
    private JButton createButton(String text, Color background) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 12));
        button.setBackground(background);
        button.setForeground(Color.BLACK);
        button.setBorder(new LineBorder(Color.GRAY, 1));
        button.setPreferredSize(new Dimension(90, 25));
        button.setFocusPainted(false);
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(background.brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(background);
            }
        });
        return button;
    }

    /**
     * Creates a labeled component with a text field.
     *
     * @param label The label text
     * @param field The associated text field
     * @return JPanel containing the labeled component
     */
    private JPanel createLabeledComponent(String label, JTextField field) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(PANEL_COLOR);
        JLabel jLabel = new JLabel(label);
        jLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(jLabel);
        panel.add(field);
        return panel;
    }

    /**
     * Creates a text field with validation and tooltip.
     *
     * @param label The label for the field
     * @param tooltip The tooltip text
     * @param columns The number of columns
     * @return JTextField with specified properties
     */
    private JTextField createTextField(String label, String tooltip, int columns) {
        JTextField textField = new JTextField(columns);
        textField.setFont(new Font("Arial", Font.PLAIN, 14));
        textField.setToolTipText(tooltip);
        textField.setBorder(new LineBorder(Color.GRAY, 1));
        textField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { validateField(textField); }
            public void removeUpdate(DocumentEvent e) { validateField(textField); }
            public void changedUpdate(DocumentEvent e) { validateField(textField); }
        });
        return textField;
    }

    /**
     * Creates a non-editable text field.
     *
     * @param label The label for the field
     * @param value The initial value
     * @param columns The number of columns
     * @return JTextField with specified properties
     */
    private JTextField createNonEditableTextField(String label, String value, int columns) {
        JTextField textField = new JTextField(value, columns);
        textField.setFont(new Font("Arial", Font.PLAIN, 14));
        textField.setEditable(false);
        textField.setBackground(new Color(230, 230, 230));
        textField.setBorder(new LineBorder(Color.GRAY, 1));
        return textField;
    }

    /**
     * Validates the input in a text field, updating its border based on validity.
     *
     * @param field The text field to validate
     */
    private void validateField(JTextField field) {
        String text = field.getText().trim();
        if (field == emailField && !text.isEmpty() && !isValidEmail(text)) {
            field.setBorder(new LineBorder(DANGER_COLOR, 2));
        } else if (field == phoneField && !text.isEmpty() && !isValidPhone(text)) {
            field.setBorder(new LineBorder(DANGER_COLOR, 2));
        } else if (field == nameField && !text.isEmpty() && !isValidName(text)) {
            field.setBorder(new LineBorder(DANGER_COLOR, 2));
        } else if (!text.isEmpty()) {
            field.setBorder(new LineBorder(SUCCESS_COLOR, 2));
        } else {
            field.setBorder(new LineBorder(Color.GRAY, 1));
        }
    }

    /**
     * Validates an email address format.
     *
     * @param email The email to validate
     * @return true if the email is valid, false otherwise
     */
    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    /**
     * Validates a phone number format (10 digits).
     *
     * @param phone The phone number to validate
     * @return true if the phone number is valid, false otherwise
     */
    private boolean isValidPhone(String phone) {
        return phone.matches("\\d{10}");
    }

    /**
     * Validates a name format (letters and spaces only).
     *
     * @param name The name to validate
     * @return true if the name is valid, false otherwise
     */
    private boolean isValidName(String name) {
        return name.matches("[A-Za-z\\s]+");
    }

    /**
     * Formats a date from combo box selections.
     *
     * @param yearComboBox The year selection combo box
     * @param monthComboBox The month selection combo box
     * @param dayComboBox The day selection combo box
     * @return Formatted date string (YYYY-MM-DD)
     * @throws IllegalArgumentException if the date is invalid
     */
    private String formatDate(JComboBox<Integer> yearComboBox, JComboBox<Integer> monthComboBox, JComboBox<Integer> dayComboBox) {
        Integer year = (Integer) yearComboBox.getSelectedItem();
        Integer month = (Integer) monthComboBox.getSelectedItem();
        Integer day = (Integer) dayComboBox.getSelectedItem();
        if (year == null || month == null || day == null) {
            throw new IllegalArgumentException("Date selections cannot be empty");
        }
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }
        if (day < 1 || day > 31) {
            throw new IllegalArgumentException("Day must be between 1 and 31");
        }
        if (year < 1900 || year > 2025) {
            throw new IllegalArgumentException("Year must be between 1900 and 2025");
        }
        try {
            LocalDate date = LocalDate.of(year, month, day);
            return String.format("%04d-%02d-%02d", year, month, day);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid date: " + year + "-" + month + "-" + day);
        }
    }

    /**
     * Finds a member by their ID.
     *
     * @param id The member ID
     * @return The GymMember object or null if not found
     */
    private GymMember findMember(String id) {
        return members.get(id.trim());
    }

    /**
     * Adds a new member to the system.
     *
     * @param isRegular True for regular member, false for premium member
     */
    private void addMember(boolean isRegular) {
        try {
            validateInputs(isRegular);
            String id = idField.getText().trim();
            if (findMember(id) != null) {
                throw new IllegalArgumentException("ID already exists");
            }
            String plan = isRegular ? planList.getSelectedValue() : null;
            if (isRegular && plan == null) {
                throw new IllegalArgumentException("A plan must be selected for regular members");
            }
            double paidAmount = Double.parseDouble(paidAmountField.getText().trim());
            if (paidAmount < 0) {
                throw new IllegalArgumentException("Paid amount cannot be negative");
            }
            String dob = formatDate(dobYearComboBox, dobMonthComboBox, dobDayComboBox);
            String membershipStart = formatDate(msYearComboBox, msMonthComboBox, msDayComboBox);
            appendToMessageArea("Attempting to add member with DOB: " + dob + ", Membership Start: " + membershipStart, JOptionPane.PLAIN_MESSAGE);
            GymMember member = isRegular ?
                    new RegularMember(id, nameField.getText().trim(), locationField.getText().trim(),
                            phoneField.getText().trim(), emailField.getText().trim(),
                            maleRadio.isSelected() ? "Male" : "Female", dob, membershipStart,
                            referralField.getText().trim(), trainerField.getText().trim(), plan, paidAmount) :
                    new PremiumMember(id, nameField.getText().trim(), locationField.getText().trim(),
                            phoneField.getText().trim(), emailField.getText().trim(),
                            maleRadio.isSelected() ? "Male" : "Female", dob, membershipStart,
                            referralField.getText().trim(), trainerField.getText().trim(), paidAmount,
                            removalReasonField.getText().trim());
            if (!isRegular) {
                ((PremiumMember) member).calculateDiscount();
                discountField.setText(String.format("%.2f", ((PremiumMember) member).getDiscountAmount()));
            }
            members.put(id, member);
            String successMsg = String.format("Added %s member: %s\n\n%s", isRegular ? "regular" : "premium", member.getName(), member.display());
            appendToMessageArea(successMsg, JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            highlightInvalidFields();
            String errorMsg = "Error adding " + (isRegular ? "regular" : "premium") + " member: " + ex.getMessage();
            appendToMessageArea(errorMsg, JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Highlights invalid input fields with an error border.
     */
    private void highlightInvalidFields() {
        Color errorColor = DANGER_COLOR;
        if (idField.getText().trim().isEmpty()) idField.setBorder(new LineBorder(errorColor, 2));
        if (nameField.getText().trim().isEmpty()) nameField.setBorder(new LineBorder(errorColor, 2));
        if (locationField.getText().trim().isEmpty()) locationField.setBorder(new LineBorder(errorColor, 2));
        if (phoneField.getText().trim().isEmpty()) phoneField.setBorder(new LineBorder(errorColor, 2));
        if (emailField.getText().trim().isEmpty()) emailField.setBorder(new LineBorder(errorColor, 2));
        if (!maleRadio.isSelected() && !femaleRadio.isSelected()) {
            maleRadio.setForeground(errorColor);
            femaleRadio.setForeground(errorColor);
        }
    }

    /**
     * Validates all input fields before adding a member.
     *
     * @param isRegular True for regular member, false for premium member
     * @throws IllegalArgumentException if validation fails
     */
    private void validateInputs(boolean isRegular) {
        if (idField.getText().trim().isEmpty() || nameField.getText().trim().isEmpty() ||
                locationField.getText().trim().isEmpty() || phoneField.getText().trim().isEmpty() ||
                emailField.getText().trim().isEmpty() || (!maleRadio.isSelected() && !femaleRadio.isSelected())) {
            throw new IllegalArgumentException("All required fields must be filled");
        }
        if (!isRegular && trainerField.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("Trainer name is required for premium members");
        }
        if (!isValidEmail(emailField.getText().trim())) {
            throw new IllegalArgumentException("Invalid email format");
        }
        if (!isValidPhone(phoneField.getText().trim())) {
            throw new IllegalArgumentException("Phone number must be 10 digits");
        }
        if (!isValidName(nameField.getText().trim())) {
            throw new IllegalArgumentException("Name must contain only letters and spaces");
        }
        try {
            Double.parseDouble(paidAmountField.getText().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid paid amount format");
        }
    }

    /**
     * Opens a popup to activate a member's membership.
     */
    private void openActivatePopup() {
        String id = JOptionPane.showInputDialog(this, "Enter Member ID to activate:", "Activate Member", JOptionPane.PLAIN_MESSAGE);
        if (id == null || id.trim().isEmpty()) {
            appendToMessageArea("Activation cancelled: No ID provided", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        GymMember member = findMember(id);
        if (member != null) {
            if (!member.isActive()) {
                member.activateMembership();
                appendToMessageArea("Membership activated for: " + member.getName(), JOptionPane.INFORMATION_MESSAGE);
            } else {
                appendToMessageArea("Member is already active: " + member.getName(), JOptionPane.WARNING_MESSAGE);
            }
        } else {
            appendToMessageArea("Activation failed: Member ID " + id + " not found", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Opens a popup to deactivate a member's membership.
     */
    private void openDeactivatePopup() {
        String id = JOptionPane.showInputDialog(this, "Enter Member ID to deactivate:", "Deactivate Membership", JOptionPane.PLAIN_MESSAGE);
        if (id == null || id.trim().isEmpty()) {
            appendToMessageArea("Deactivation cancelled: No ID provided", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        GymMember member = findMember(id);
        if (member != null) {
            if (member.isActive()) {
                member.deactivateMembership();
                appendToMessageArea("Membership deactivated for: " + member.getName(), JOptionPane.INFORMATION_MESSAGE);
            } else {
                appendToMessageArea("Member is already inactive: " + member.getName(), JOptionPane.WARNING_MESSAGE);
            }
        } else {
            appendToMessageArea("Deactivation failed: Member ID " + id + " not found", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Opens a popup to mark attendance for a member.
     */
    private void openMarkAttendancePopup() {
        String id = JOptionPane.showInputDialog(this, "Enter Member ID to mark attendance:", "Mark Attendance", JOptionPane.PLAIN_MESSAGE);
        if (id == null || id.trim().isEmpty()) {
            appendToMessageArea("Attendance marking cancelled: No ID provided", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        GymMember member = findMember(id);
        if (member != null && member.isActive()) {
            int previousCount = member.getAttendanceCount();
            member.markAttendance();
            int newCount = member.getAttendanceCount();
            appendToMessageArea(String.format("Attendance marked for: %s (Previous: %d, New: %d)", 
                    member.getName(), previousCount, newCount), JOptionPane.INFORMATION_MESSAGE);
            if (member instanceof RegularMember) {
                ((RegularMember) member).logAttendance();
            } else {
                ((PremiumMember) member).logAttendance();
            }
        } else {
            appendToMessageArea("Attendance marking failed: Invalid or inactive member ID " + id, JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Opens a popup to remove a member.
     *
     * @param isRegular True for regular member, false for premium member
     */
    private void openRevertPopup(boolean isRegular) {
        JTextField idField = new JTextField(15);
        JTextField reasonField = new JTextField(15);
        Object[] fields = {
                "Member ID:", idField,
                "Removal Reason:", reasonField
        };
        int option = JOptionPane.showConfirmDialog(this, fields, "Remove " + (isRegular ? "Regular" : "Premium") + " Member", JOptionPane.OK_CANCEL_OPTION);
        if (option != JOptionPane.OK_OPTION) {
            appendToMessageArea("Removal cancelled: No action taken", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String id = idField.getText().trim();
        if (id.isEmpty()) {
            appendToMessageArea("Remove failed: No ID provided", JOptionPane.ERROR_MESSAGE);
            return;
        }
        GymMember member = findMember(id);
        if (member == null) {
            appendToMessageArea(String.format("Remove failed: Member ID %s not found", id), JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (isRegular && !(member instanceof RegularMember) || !isRegular && !(member instanceof PremiumMember)) {
            appendToMessageArea(String.format("Error: Member ID %s is not a %s member", id, isRegular ? "Regular" : "Premium"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        member.revertMember();
        String reason = reasonField.getText().trim();
        if (member instanceof RegularMember) {
            ((RegularMember) member).setRemovalReason(reason);
        } else {
            ((PremiumMember) member).setRemovalReason(reason);
        }
        members.remove(id);
        appendToMessageArea(String.format("Removed %s member: %s", isRegular ? "Regular" : "Premium", id), JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Opens a popup to upgrade a regular member's plan.
     */
    private void openUpgradePlanPopup() {
        JTextField idField = new JTextField(15);
        JComboBox<String> planComboBox = new JComboBox<>(new String[]{"Basic", "Standard", "Deluxe"});
        planComboBox.setFont(new Font("Arial", Font.PLAIN, 12));
        Object[] fields = {
                "Member ID:", idField,
                "New Plan:", planComboBox
        };
        int option = JOptionPane.showConfirmDialog(this, fields, "Upgrade Plan", JOptionPane.OK_CANCEL_OPTION);
        if (option != JOptionPane.OK_OPTION) {
            appendToMessageArea("Plan upgrade cancelled: No action taken", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String id = idField.getText().trim();
        String newPlan = (String) planComboBox.getSelectedItem();
        if (id.isEmpty()) {
            appendToMessageArea("Plan upgrade failed: No ID provided", JOptionPane.ERROR_MESSAGE);
            return;
        }
        GymMember member = findMember(id);
        if (member != null) {
            if (member instanceof RegularMember) {
                int attendanceCount = member.getAttendanceCount();
                appendToMessageArea(String.format("Checking upgrade plan for ID %s: Attendance = %d", id, attendanceCount), JOptionPane.INFORMATION_MESSAGE);
                try {
                    ((RegularMember) member).upgradePlan(newPlan);
                    double newPrice = ((RegularMember) member).getPlanPrice(newPlan);
                    regularPriceField.setText(String.format("%.2f", newPrice));
                    appendToMessageArea(String.format("Plan upgraded to %s for: %s", newPlan, member.getName()), JOptionPane.INFORMATION_MESSAGE);
                } catch (IllegalArgumentException ex) {
                    appendToMessageArea(String.format("Plan upgrade failed: %s", ex.getMessage()), JOptionPane.ERROR_MESSAGE);
                }
            } else {
                appendToMessageArea(String.format("Plan upgrade failed: Member ID %s is not a Regular Member", id), JOptionPane.ERROR_MESSAGE);
            }
        } else {
            appendToMessageArea(String.format("Plan upgrade failed: Member ID %s not found", id), JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Opens a popup to record a payment for a member.
     */
    private void openPayDuePopup() {
        JTextField idField = new JTextField(15);
        JTextField paymentField = new JTextField(10);
        Object[] fields = {
                "Member ID:", idField,
                "Payment Amount:", paymentField
        };
        int option = JOptionPane.showConfirmDialog(this, fields, "Pay Due Amount", JOptionPane.OK_CANCEL_OPTION);
        if (option != JOptionPane.OK_OPTION) {
            appendToMessageArea("Payment cancelled: No action taken", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String id = idField.getText().trim();
        String paymentText = paymentField.getText().trim();
        if (id.isEmpty()) {
            appendToMessageArea("Payment failed: No ID provided", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (paymentText.isEmpty()) {
            appendToMessageArea("Payment failed: No payment amount provided", JOptionPane.ERROR_MESSAGE);
            return;
        }
        double paymentAmount;
        try {
            paymentAmount = Double.parseDouble(paymentText);
            if (paymentAmount < 0) {
                throw new IllegalArgumentException("Payment amount cannot be negative");
            }
        } catch (NumberFormatException ex) {
            appendToMessageArea("Payment failed: Invalid payment amount format", JOptionPane.ERROR_MESSAGE);
            return;
        }
        GymMember member = findMember(id);
        if (member != null) {
            try {
                double previousPaid = member instanceof RegularMember ?
                        ((RegularMember) member).getPaidAmount() :
                        ((PremiumMember) member).getPaidAmount();
                if (member instanceof RegularMember) {
                    ((RegularMember) member).addPayment(paymentAmount);
                } else {
                    ((PremiumMember) member).addPayment(paymentAmount);
                }
                appendToMessageArea(String.format("Payment of %.2f recorded for: %s (Total Paid: %.2f)", paymentAmount, member.getName(), previousPaid + paymentAmount), JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                appendToMessageArea(String.format("Error processing payment for ID %s: %s", id, ex.getMessage()), JOptionPane.ERROR_MESSAGE);
            }
        } else {
            appendToMessageArea(String.format("Payment failed: Member ID %s not found", id), JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Opens a popup to display a member's payment details.
     */
    private void openRemainingDuePopup() {
        String id = JOptionPane.showInputDialog(this, "Enter Member ID to view payment details:", "Payment Details", JOptionPane.PLAIN_MESSAGE);
        if (id == null || id.trim().isEmpty()) {
            appendToMessageArea("Payment details cancelled: No ID provided", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        GymMember member = findMember(id);
        if (member != null) {
            double paidAmount = member instanceof RegularMember ?
                    ((RegularMember) member).getPaidAmount() :
                    ((PremiumMember) member).getPaidAmount();
            double planPrice = member instanceof RegularMember ?
                    ((RegularMember) member).getPlanPrice(((RegularMember) member).getPlan()) :
                    PREMIUM_CHARGE;
            double dueAmount = Math.max(0.0, planPrice - paidAmount);
            double extraAmount = Math.max(0.0, paidAmount - planPrice);
            String paymentDetails = String.format("Payment Details for %s:%nPaid: %.2f%nDue: %.2f%s",
                    member.getName(), paidAmount, dueAmount,
                    extraAmount > 0 ? String.format("%nExtra Paid: %.2f", extraAmount) : "");
            appendToMessageArea(paymentDetails, JOptionPane.INFORMATION_MESSAGE);
        } else {
            appendToMessageArea(String.format("Payment details check failed: Member ID %s not found", id), JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Saves member data to a text file in CSV format.
     */
    private void saveToTextFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Save Location");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().endsWith(".txt");
            }
            public String getDescription() {
                return "Text Files (*.txt)";
            }
        });
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().endsWith(".txt")) {
                file = new File(file.getAbsolutePath() + ".txt");
            }
            if (file.exists()) {
                int result = JOptionPane.showConfirmDialog(this, "File exists. Overwrite?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (result != JOptionPane.YES_OPTION) {
                    appendToMessageArea("Text save cancelled: File not overwritten", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
            }
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write("ID|Name|Email|Phone|Gender|Address|DOB|Membership Start|Referral|Trainer|Type|Plan|Paid Amount|Due Amount|Extra Amount|Removal Reason|Discount Amount|Active|Attendance");
                writer.newLine();
                for (GymMember member : members.values()) {
                    String type = member instanceof RegularMember ? "Regular" : "Premium";
                    String plan = member instanceof RegularMember ? ((RegularMember) member).getPlan() : "";
                    double paidAmount = member instanceof RegularMember ?
                            ((RegularMember) member).getPaidAmount() :
                            ((PremiumMember) member).getPaidAmount();
                    double planPrice = member instanceof RegularMember ?
                            ((RegularMember) member).getPlanPrice(((RegularMember) member).getPlan()) :
                            PREMIUM_CHARGE;
                    double dueAmount = Math.max(0, planPrice - paidAmount);
                    double extraAmount = Math.max(0, paidAmount - planPrice);
                    String reason = member instanceof RegularMember ?
                            ((RegularMember) member).getRemovalReason() :
                            ((PremiumMember) member).getRemovalReason();
                    double discountAmount = member instanceof PremiumMember ?
                            ((PremiumMember) member).getDiscountAmount() : 0.0;
                    String line = String.format("%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%.2f|%.2f|%.2f|%s|%.2f|%s|%d",
                            escapeCsv(member.getId()),
                            escapeCsv(member.getName()),
                            escapeCsv(member.getEmail()),
                            escapeCsv(member.getPhone()),
                            escapeCsv(member.getGender()),
                            escapeCsv(member.getAddress()),
                            escapeCsv(member.getDob()),
                            escapeCsv(member.getMembershipStart()),
                            escapeCsv(member.getReferral()),
                            escapeCsv(member.getTrainer()),
                            type,
                            escapeCsv(plan),
                            paidAmount,
                            dueAmount,
                            extraAmount,
                            escapeCsv(reason),
                            discountAmount,
                            member.isActive() ? "Yes" : "No",
                            member.getAttendanceCount());
                    writer.write(line);
                    writer.newLine();
                }
                appendToMessageArea(String.format("Successfully saved to %s", file.getName()), JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                appendToMessageArea(String.format("Error saving text file: %s", ex.getMessage()), JOptionPane.ERROR_MESSAGE);
            }
        } else {
            appendToMessageArea("Operation cancelled: No file selected", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Loads member data from a text file in CSV format.
     */
    private void loadFromTextFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Load File");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().endsWith(".txt");
            }
            public String getDescription() {
                return "Text Files (*.txt)";
            }
        });
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (file.length() == 0) {
                appendToMessageArea("Error loading text file: File is empty", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String header = reader.readLine();
                if (header == null || header.trim().isEmpty()) {
                    throw new IOException("Invalid file format: Missing or empty header");
                }
                String[] headerFields = parseCsvLine(header);
                if (headerFields.length < 18) {
                    appendToMessageArea(String.format("Invalid file format: Header has %d fields, expected at least 18: %s", headerFields.length, header), JOptionPane.ERROR_MESSAGE);
                    throw new IOException("Invalid header format: Insufficient fields");
                }
                members.clear();
                String line;
                int lineNumber = 2;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) {
                        continue;
                    }
                    try {
                        String[] fields = parseCsvLine(line);
                        if (fields.length < 18) {
                            appendToMessageArea(String.format("Skipping line %d: Insufficient fields (%d found, 18 expected)", lineNumber, fields.length), JOptionPane.WARNING_MESSAGE);
                            continue;
                        }
                        String id = unescapeCsv(fields[0]);
                        String name = unescapeCsv(fields[1]);
                        String email = unescapeCsv(fields[2]);
                        String phone = unescapeCsv(fields[3]);
                        String gender = unescapeCsv(fields[4]);
                        String address = unescapeCsv(fields[5]);
                        String dob = unescapeCsv(fields[6]);
                        String membershipStart = unescapeCsv(fields[7]);
                        String referral = unescapeCsv(fields[8]);
                        String trainer = unescapeCsv(fields[9]);
                        String type = fields[10];
                        String plan = unescapeCsv(fields[11]);
                        double paidAmount;
                        double discount;
                        boolean active;
                        int attendance;
                        try {
                            paidAmount = Double.parseDouble(fields[12]);
                            discount = Double.parseDouble(fields[16]);
                            active = fields[17].equalsIgnoreCase("Yes");
                            attendance = Integer.parseInt(fields[18]);
                        } catch (NumberFormatException e) {
                            appendToMessageArea(String.format("Skipping line %d: Invalid numeric format in fields", lineNumber), JOptionPane.WARNING_MESSAGE);
                            continue;
                        }
                        String removalReason = unescapeCsv(fields[15]);

                        if (!isValidEmail(email)) {
                            appendToMessageArea(String.format("Skipping line %d: Invalid email format for ID %s", lineNumber, id), JOptionPane.WARNING_MESSAGE);
                            continue;
                        }
                        if (!isValidPhone(phone)) {
                            appendToMessageArea(String.format("Skipping line %d: Invalid phone number for ID %s", lineNumber, id), JOptionPane.WARNING_MESSAGE);
                            continue;
                        }
                        if (!isValidName(name)) {
                            appendToMessageArea(String.format("Skipping line %d: Invalid name format for ID %s", lineNumber, id), JOptionPane.WARNING_MESSAGE);
                            continue;
                        }
                        if (!gender.equalsIgnoreCase("Male") && !gender.equalsIgnoreCase("Female")) {
                            appendToMessageArea(String.format("Skipping line %d: Invalid gender for ID %s", lineNumber, id), JOptionPane.WARNING_MESSAGE);
                            continue;
                        }
                        try {
                            String[] dobParts = dob.split("-");
                            String[] msParts = membershipStart.split("-");
                            if (dobParts.length != 3 || msParts.length != 3) {
                                throw new IllegalArgumentException("Invalid date format");
                            }
                            LocalDate.of(Integer.parseInt(dobParts[0]), Integer.parseInt(dobParts[1]), Integer.parseInt(dobParts[2]));
                            LocalDate.of(Integer.parseInt(msParts[0]), Integer.parseInt(msParts[1]), Integer.parseInt(msParts[2]));
                        } catch (Exception e) {
                            appendToMessageArea(String.format("Skipping line %d: Invalid date format for ID %s: %s", lineNumber, id, e.getMessage()), JOptionPane.WARNING_MESSAGE);
                            continue;
                        }
                        if (type.equalsIgnoreCase("Regular") && !plan.equalsIgnoreCase("Basic") && !plan.equalsIgnoreCase("Standard") && !plan.equalsIgnoreCase("Deluxe")) {
                            appendToMessageArea(String.format("Skipping line %d: Invalid plan for Regular member ID %s", lineNumber, id), JOptionPane.WARNING_MESSAGE);
                            continue;
                        }
                        if (paidAmount < 0 || discount < 0 || attendance < 0) {
                            appendToMessageArea(String.format("Skipping line %d: Negative values not allowed for ID %s", lineNumber, id), JOptionPane.WARNING_MESSAGE);
                            continue;
                        }

                        GymMember member;
                        if (type.equalsIgnoreCase("Regular")) {
                            member = new RegularMember(id, name, address, phone, email, gender, dob, membershipStart, referral, trainer, plan, paidAmount);
                        } else if (type.equalsIgnoreCase("Premium")) {
                            member = new PremiumMember(id, name, address, phone, email, gender, dob, membershipStart, referral, trainer, paidAmount, removalReason);
                            ((PremiumMember) member).calculateDiscount();
                        } else {
                            appendToMessageArea(String.format("Skipping line %d: Invalid member type for ID %s", lineNumber, id), JOptionPane.WARNING_MESSAGE);
                            continue;
                        }
                        member.deactivateMembership();
                        if (active) {
                            member.activateMembership();
                        }
                        for (int i = 0; i < attendance; i++) {
                            member.markAttendance();
                        }
                        if (members.containsKey(id)) {
                            appendToMessageArea(String.format("Skipping line %d: Duplicate member ID %s", lineNumber, id), JOptionPane.WARNING_MESSAGE);
                            continue;
                        }
                        System.out.println("Loaded member ID " + id + " with attendance count: " + attendance);
                        members.put(id, member);
                    } catch (Exception e) {
                        appendToMessageArea(String.format("Skipping line %d: Error processing member: %s", lineNumber, e.getMessage()), JOptionPane.WARNING_MESSAGE);
                    }
                    lineNumber++;
                }
                String message = members.isEmpty() ?
                        String.format("No valid members loaded from %s", file.getName()) :
                        String.format("Successfully loaded %d members from %s", members.size(), file.getName());
                appendToMessageArea(message, JOptionPane.INFORMATION_MESSAGE);
                if (!members.isEmpty()) {
                    displayMembers();
                }
            } catch (IOException ex) {
                appendToMessageArea(String.format("Error loading text file: %s", ex.getMessage()), JOptionPane.ERROR_MESSAGE);
            }
        } else {
            appendToMessageArea("Operation cancelled: No file selected", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Parses a CSV line into fields, handling quoted values.
     *
     * @param line The CSV line to parse
     * @return Array of parsed fields
     */
    private String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == '|' && !inQuotes) {
                fields.add(field.toString());
                field = new StringBuilder();
            } else {
                field.append(c);
            }
        }
        fields.add(field.toString());
        return fields.toArray(new String[0]);
    }

    /**
     * Saves member data to a binary file.
     */
    private void saveToFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Save Location");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().endsWith(".dat");
            }
            public String getDescription() {
                return "Data Files (*.dat)";
            }
        });
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().endsWith(".dat")) {
                file = new File(file.getAbsolutePath() + ".dat");
            }
            if (file.exists()) {
                int result = JOptionPane.showConfirmDialog(this, "File exists. Overwrite?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (result != JOptionPane.YES_OPTION) {
                    appendToMessageArea("Operation cancelled: File not overwritten", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
            }
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                oos.writeObject(new HashMap<>(members));
                appendToMessageArea(String.format("Successfully saved to %s", file.getName()), JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                appendToMessageArea(String.format("Error saving file: %s", ex.getMessage()), JOptionPane.ERROR_MESSAGE);
            }
        } else {
            appendToMessageArea("Operation cancelled: No file selected", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Escapes a string for CSV output.
     *
     * @param value The string to escape
     * @return The escaped string
     */
    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.startsWith("=") || value.startsWith("+") || value.startsWith("-") || value.startsWith("@")) {
            value = "'" + value;
        }
        if (value.contains("|") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /**
     * Unescapes a CSV string.
     *
     * @param value The string to unescape
     * @return The unescaped string
     */
    private String unescapeCsv(String value) {
        if (value == null) return "";
        if (value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1).replace("\"\"", "\"");
        }
        return value;
    }

    /**
     * Displays a table of all members.
     */
    private void displayMembers() {
        if (members.isEmpty()) {
            appendToMessageArea("No members registered", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String[] columns = {"ID", "Name", "Email", "Phone", "Plan/Type", "Active", "Attendance"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        for (GymMember member : members.values()) {
            String planOrType = member instanceof RegularMember ? ((RegularMember) member).getPlan() : "Premium";
            model.addRow(new Object[]{
                    member.getId(),
                    member.getName(),
                    member.getEmail(),
                    member.getPhone(),
                    planOrType,
                    member.isActive() ? "Yes" : "No",
                    member.getAttendanceCount()
            });
        }
        JTable memberTable = new JTable(model);
        memberTable.setFont(new Font("Arial", Font.PLAIN, 12));
        memberTable.setRowHeight(25);
        memberTable.setAutoCreateRowSorter(true);
        memberTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = memberTable.getSelectedRow();
                if (selectedRow >= 0) {
                    String id = (String) memberTable.getValueAt(selectedRow, 0);
                    GymMember member = findMember(id);
                    if (member != null) {
                        idField.setText(member.getId());
                        nameField.setText(member.getName());
                        locationField.setText(member.getAddress());
                        phoneField.setText(member.getPhone());
                        emailField.setText(member.getEmail());
                        referralField.setText(member.getReferral());
                        trainerField.setText(member.getTrainer());
                        maleRadio.setSelected(member.getGender().equals("Male"));
                        femaleRadio.setSelected(member.getGender().equals("Female"));
                        if (member instanceof RegularMember) {
                            planList.setSelectedValue(((RegularMember) member).getPlan(), true);
                            paidAmountField.setText(String.format("%.2f", ((RegularMember) member).getPaidAmount()));
                            discountField.setText("0.00");
                            removalReasonField.setText(((RegularMember) member).getRemovalReason());
                            memberTypeToggle.setSelected(true);
                            memberTypeToggle.setText("Regular Member");
                            planList.setEnabled(true);
                            trainerField.setEnabled(false);
                        } else {
                            paidAmountField.setText(String.format("%.2f", ((PremiumMember) member).getPaidAmount()));
                            discountField.setText(String.format("%.2f", ((PremiumMember) member).getDiscountAmount()));
                            removalReasonField.setText(((PremiumMember) member).getRemovalReason());
                            memberTypeToggle.setSelected(false);
                            memberTypeToggle.setText("Premium Member");
                            planList.setEnabled(false);
                            trainerField.setEnabled(true);
                        }
                    }
                }
            }
        });
        JScrollPane scrollPane = new JScrollPane(memberTable);
        scrollPane.setPreferredSize(new Dimension(600, 300));
        JOptionPane.showMessageDialog(this, scrollPane, "Member List", JOptionPane.INFORMATION_MESSAGE);
        appendToMessageArea("Displayed member list", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Clears all input fields and resets their borders.
     */
    private void clearFields() {
        idField.setText("");
        nameField.setText("");
        locationField.setText("");
        phoneField.setText("");
        emailField.setText("");
        referralField.setText("");
        paidAmountField.setText("");
        removalReasonField.setText("");
        trainerField.setText("");
        maleRadio.setSelected(false);
        femaleRadio.setSelected(false);
        dobDayComboBox.setSelectedIndex(-1);
        dobMonthComboBox.setSelectedIndex(-1);
        dobYearComboBox.setSelectedIndex(-1);
        msDayComboBox.setSelectedIndex(-1);
        msMonthComboBox.setSelectedIndex(-1);
        msYearComboBox.setSelectedIndex(-1);
        planList.setSelectedIndex(0);
        regularPriceField.setText( String.format("%.2f", REGULAR_BASE_PRICE));
        discountField.setText("0.00");

        idField.setBorder(new LineBorder(Color.GRAY, 1));
        nameField.setBorder(new LineBorder(Color.GRAY, 1));
        locationField.setBorder(new LineBorder(Color.GRAY, 1));
        phoneField.setBorder(new LineBorder(Color.GRAY, 1));
        emailField.setBorder(new LineBorder(Color.GRAY, 1));
        maleRadio.setForeground(Color.BLACK);
        femaleRadio.setForeground(Color.BLACK);

        appendToMessageArea("Successfully cleared all fields", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * The main entry point for the application.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GymGUI());
    }
}
