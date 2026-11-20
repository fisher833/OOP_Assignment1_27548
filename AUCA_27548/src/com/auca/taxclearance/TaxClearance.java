package com.auca.taxclearance;

import com.auca.abstracts.RegisteredTaxpayer;
import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Validatable;
import com.auca.revenueofficer.RevenueOfficer;
import java.time.LocalDate;

public class TaxClearance implements Identifiable, Validatable {
    private String clearanceId;
    private RegisteredTaxpayer taxpayer;
    private LocalDate requestDate;
    private LocalDate expiryDate;
    private String status;
    private RevenueOfficer approvedBy;

    public TaxClearance(String clearanceId, RegisteredTaxpayer taxpayer, LocalDate requestDate,
                        LocalDate expiryDate, String status, RevenueOfficer approvedBy) {
        this.clearanceId = clearanceId;
        this.taxpayer = taxpayer;
        this.requestDate = requestDate;
        this.expiryDate = expiryDate;
        this.status = status;
        this.approvedBy = approvedBy;
    }
    public String getClearanceId() { return clearanceId; }
    public RegisteredTaxpayer getTaxpayer() { return taxpayer; }
    public LocalDate getRequestDate() { return requestDate; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public String getStatus() { return status; }
    public RevenueOfficer getApprovedBy() { return approvedBy; }
    public void setStatus(String status) { this.status = status; }
    public void setApprovedBy(RevenueOfficer approvedBy) { this.approvedBy = approvedBy; }
    @Override public String getId() { return clearanceId; }
    @Override public String getDisplayName() { return "Tax clearance " + clearanceId; }
    @Override public boolean isActive() { return "APPROVED".equals(status); }
    @Override public void validate() throws ValidationException {
        if (clearanceId == null || clearanceId.trim().length() < 2 || clearanceId.trim().length() > 30
                || taxpayer == null || requestDate == null || expiryDate == null)
            throw new ValidationException("Clearance fields are required.");
        taxpayer.validate();
        if (requestDate.isAfter(LocalDate.now()) || !expiryDate.isAfter(requestDate))
            throw new ValidationException("Request cannot be future-dated; expiry must follow request date.");
        if (!"PENDING".equals(status) && !"APPROVED".equals(status) && !"REJECTED".equals(status))
            throw new ValidationException("Clearance status must be PENDING, APPROVED, or REJECTED.");
        if ("APPROVED".equals(status) && (approvedBy == null || !approvedBy.isActive()))
            throw new ValidationException("Approved clearance must reference an active officer.");
        if (approvedBy != null) approvedBy.validate();
    }
    @Override public void printDetails() { System.out.println(getSummary()); }
    @Override public String getSummary() { return getDisplayName() + ", taxpayer=" + taxpayer.getDisplayName()
            + ", status=" + status + ", expiry=" + expiryDate
            + (approvedBy == null ? "" : ", approved by=" + approvedBy.getDisplayName()); }
}
