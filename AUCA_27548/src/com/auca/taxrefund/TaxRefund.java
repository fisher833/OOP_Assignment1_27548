package com.auca.taxrefund;

import com.auca.abstracts.RegisteredTaxpayer;
import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Validatable;
import com.auca.taxassessment.TaxAssessment;
import java.math.BigDecimal;
import java.time.LocalDate;

public class TaxRefund implements Identifiable, Validatable {
    private String refundId;
    private RegisteredTaxpayer taxpayer;
    private TaxAssessment assessment;
    private BigDecimal requestedAmount;
    private LocalDate requestDate;
    private String status;

    public TaxRefund(String refundId, RegisteredTaxpayer taxpayer, TaxAssessment assessment,
                     BigDecimal requestedAmount, LocalDate requestDate, String status) {
        this.refundId = refundId;
        this.taxpayer = taxpayer;
        this.assessment = assessment;
        this.requestedAmount = requestedAmount;
        this.requestDate = requestDate;
        this.status = status;
    }
    public String getRefundId() { return refundId; }
    public RegisteredTaxpayer getTaxpayer() { return taxpayer; }
    public TaxAssessment getAssessment() { return assessment; }
    public BigDecimal getRequestedAmount() { return requestedAmount; }
    public LocalDate getRequestDate() { return requestDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    @Override public String getId() { return refundId; }
    @Override public String getDisplayName() { return "Refund " + refundId; }
    @Override public boolean isActive() { return "PENDING".equals(status); }
    @Override public void validate() throws ValidationException {
        if (refundId == null || refundId.trim().length() < 2 || refundId.trim().length() > 30
                || taxpayer == null || assessment == null || requestedAmount == null || requestDate == null)
            throw new ValidationException("All refund fields are required.");
        taxpayer.validate();
        assessment.validate();
        if (!taxpayer.isActive() || !"CONFIRMED".equals(assessment.getStatus()))
            throw new ValidationException("Refund requires an active taxpayer and a confirmed assessment.");
        if (requestedAmount.signum() <= 0 || requestedAmount.scale() > 2
                || requestedAmount.compareTo(new BigDecimal("999999999999.99")) > 0)
            throw new ValidationException("Refund request must be positive with at most 2 decimals.");
        if (requestDate.isAfter(LocalDate.now())) throw new ValidationException("Request date cannot be in the future.");
        if (!"PENDING".equals(status) && !"APPROVED".equals(status) && !"REJECTED".equals(status))
            throw new ValidationException("Refund status must be PENDING, APPROVED, or REJECTED.");
    }
    @Override public void printDetails() { System.out.println(getSummary()); }
    @Override public String getSummary() { return getDisplayName() + ", taxpayer=" + taxpayer.getDisplayName()
            + ", assessment=" + assessment.getId() + ", requested=" + requestedAmount + ", status=" + status; }
}
