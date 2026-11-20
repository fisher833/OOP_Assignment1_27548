package com.auca.revenueofficer;

import java.math.BigDecimal;
import java.time.LocalDate;
import com.auca.abstracts.TaxParty;
import com.auca.exceptions.ValidationException;


public class RevenueOfficer extends TaxParty {
    private String staffNumber;
    private String department;
    private String role;
    private LocalDate hireDate;
    private BigDecimal approvalLimit;
    private boolean active;

    public RevenueOfficer(String partyId, String firstName, String lastName, String email,
            String phoneNumber, LocalDate dateOfBirth, String staffNumber, String department,
            String role, LocalDate hireDate, BigDecimal approvalLimit, boolean active) {
        // super(...) reuses shared party identity and contact initialization for the officer branch.
        super(partyId, firstName, lastName, email, phoneNumber, dateOfBirth);
        this.staffNumber = staffNumber;
        this.department = department;
        this.role = role;
        this.hireDate = hireDate;
        this.approvalLimit = approvalLimit;
        this.active = active;
    }
    public String getStaffNumber() { return staffNumber; }
    public String getDepartment() { return department; }
    public String getRole() { return role; }
    public LocalDate getHireDate() { return hireDate; }
    public BigDecimal getApprovalLimit() { return approvalLimit; }
    @Override public boolean isActive() { return active; }
    @Override public String getDisplayName() { return getFirstName() + " " + getLastName() + " (Officer: " + role + ")"; }
    @Override public String getSummary() { return super.getSummary() + ", staff=" + staffNumber + ", active=" + active; }
    public void setActive(boolean active) { this.active = active; }
    @Override public void validate() throws ValidationException {
        super.validate();
        if (staffNumber == null || staffNumber.trim().length() < 2 || staffNumber.trim().length() > 30)
            throw new ValidationException("Staff number must contain 2-30 characters.");
        if (!validStaffText(department) || !validStaffText(role))
            throw new ValidationException("Department and role must each contain 2-60 characters.");
        if (hireDate == null || hireDate.isAfter(LocalDate.now()) || hireDate.isBefore(getDateOfBirth()))
            throw new ValidationException("Hire date cannot be future-dated or precede the officer's birth date.");
        if (approvalLimit == null || approvalLimit.signum() < 0 || approvalLimit.scale() > 2
                || approvalLimit.compareTo(new BigDecimal("999999999999.99")) > 0)
            throw new ValidationException("Approval limit must be 0-999999999999.99 with at most 2 decimals.");
    }
    private boolean validStaffText(String value) {
        return value != null && value.trim().length() >= 2 && value.trim().length() <= 60;
    }
}
