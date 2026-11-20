package com.auca.taxdeclaration;

import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Validatable;
import com.auca.taxobligation.TaxObligation;
import java.math.BigDecimal;
import java.time.LocalDate;

public class TaxDeclaration implements Identifiable, Validatable {
    private String declarationId;
    private TaxObligation obligation;
    private LocalDate filingDate;
    private BigDecimal taxableAmount;
    private BigDecimal declaredTax;
    private String status;

    public TaxDeclaration(String declarationId, TaxObligation obligation, LocalDate filingDate,
                          BigDecimal taxableAmount, BigDecimal declaredTax, String status) {
        this.declarationId = declarationId;
        this.obligation = obligation;
        this.filingDate = filingDate;
        this.taxableAmount = taxableAmount;
        this.declaredTax = declaredTax;
        this.status = status;
    }
    public String getDeclarationId() { return declarationId; }
    public TaxObligation getObligation() { return obligation; }
    public LocalDate getFilingDate() { return filingDate; }
    public BigDecimal getTaxableAmount() { return taxableAmount; }
    public BigDecimal getDeclaredTax() { return declaredTax; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    @Override public String getId() { return declarationId; }
    @Override public String getDisplayName() { return "Declaration " + declarationId; }
    @Override public boolean isActive() { return !"REJECTED".equals(status); }
    @Override public void validate() throws ValidationException {
        if (declarationId == null || declarationId.trim().length() < 2 || declarationId.trim().length() > 30
                || obligation == null || filingDate == null)
            throw new ValidationException("Declaration ID, obligation, and filing date are required.");
        obligation.validate();
        if (filingDate.isAfter(LocalDate.now())) throw new ValidationException("Filing date cannot be in the future.");
        if (taxableAmount == null || declaredTax == null || taxableAmount.signum() < 0 || declaredTax.signum() < 0
                || taxableAmount.scale() > 2 || declaredTax.scale() > 2
                || taxableAmount.compareTo(new BigDecimal("999999999999.99")) > 0
                || declaredTax.compareTo(new BigDecimal("999999999999.99")) > 0)
            throw new ValidationException("Taxable amount and declared tax must be non-negative with at most 2 decimals.");
        if (!"FILED".equals(status) && !"LATE".equals(status) && !"REJECTED".equals(status))
            throw new ValidationException("Declaration status must be FILED, LATE, or REJECTED.");
    }
    @Override public void printDetails() { System.out.println(getSummary()); }
    @Override public String getSummary() { return getDisplayName() + ", obligation=" + obligation.getId()
            + ", taxable=" + taxableAmount + ", tax=" + declaredTax + ", status=" + status; }
}
