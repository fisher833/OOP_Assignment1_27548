package com.auca.taxpayment;

import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Validatable;
import com.auca.taxassessment.TaxAssessment;
import java.math.BigDecimal;
import java.time.LocalDate;

public class TaxPayment implements Identifiable, Validatable {
    private String paymentId;
    
    private TaxAssessment assessment;
    private String paymentReference;
    private BigDecimal amount;
    private LocalDate paymentDate;
    private String paymentMethod;

    public TaxPayment(String paymentId, TaxAssessment assessment, String paymentReference,
                      BigDecimal amount, LocalDate paymentDate, String paymentMethod) {
        this.paymentId = paymentId;
        this.assessment = assessment;
        this.paymentReference = paymentReference;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.paymentMethod = paymentMethod;
    }
    public String getPaymentId() { return paymentId; }
    public TaxAssessment getAssessment() { return assessment; }
    public String getPaymentReference() { return paymentReference; }
    public BigDecimal getAmount() { return amount; }
    public LocalDate getPaymentDate() { return paymentDate; }
    public String getPaymentMethod() { return paymentMethod; }
    @Override public String getId() { return paymentId; }
    @Override public String getDisplayName() { return "Payment " + paymentId; }
    @Override public boolean isActive() { return true; }
    @Override public void validate() throws ValidationException {
        if (paymentId == null || paymentId.trim().length() < 2 || paymentId.trim().length() > 30
                || assessment == null || paymentReference == null || paymentReference.trim().isEmpty()
                || paymentDate == null || paymentMethod == null || paymentMethod.trim().isEmpty())
            throw new ValidationException("All payment fields are required.");
        assessment.validate();
        if (!"CONFIRMED".equals(assessment.getStatus()))
            throw new ValidationException("Payments require a CONFIRMED assessment.");
        if (amount == null || amount.signum() <= 0 || amount.scale() > 2
                || amount.compareTo(new BigDecimal("999999999999.99")) > 0)
            throw new ValidationException("Payment must be positive with at most 2 decimal places.");
        if (paymentDate.isAfter(LocalDate.now())) throw new ValidationException("Payment date cannot be in the future.");
    }
    @Override public void printDetails() { System.out.println(getSummary()); }
    @Override public String getSummary() { return getDisplayName() + ", assessment=" + assessment.getId()
            + ", reference=" + paymentReference + ", amount=" + amount + ", date=" + paymentDate; }
}
