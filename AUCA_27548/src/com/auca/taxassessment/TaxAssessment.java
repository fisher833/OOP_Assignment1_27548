package com.auca.taxassessment;

import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Validatable;
import com.auca.revenueofficer.RevenueOfficer;
import com.auca.taxdeclaration.TaxDeclaration;
import java.math.BigDecimal;
import java.time.LocalDate;

public class TaxAssessment implements Identifiable, Validatable {
    private String assessmentId;
    
    private TaxDeclaration declaration;
    private BigDecimal assessedTax;
    private BigDecimal adjustmentAmount;
    private LocalDate assessmentDate;
    private String status;
    
    private RevenueOfficer adjustmentApprovedBy;

    public TaxAssessment(String assessmentId, TaxDeclaration declaration, BigDecimal assessedTax,
                         BigDecimal adjustmentAmount, LocalDate assessmentDate, String status) {
        this.assessmentId = assessmentId;
        this.declaration = declaration;
        this.assessedTax = assessedTax;
        this.adjustmentAmount = adjustmentAmount;
        this.assessmentDate = assessmentDate;
        this.status = status;
    }
    public String getAssessmentId() { return assessmentId; }
    public TaxDeclaration getDeclaration() { return declaration; }
    public BigDecimal getAssessedTax() { return assessedTax; }
    public BigDecimal getAdjustmentAmount() { return adjustmentAmount; }
    public BigDecimal getTotal() { return assessedTax.add(adjustmentAmount); }
    public LocalDate getAssessmentDate() { return assessmentDate; }
    public String getStatus() { return status; }
    public RevenueOfficer getAdjustmentApprovedBy() { return adjustmentApprovedBy; }
    public void setStatus(String status) { this.status = status; }
    public void setAdjustmentApprovedBy(RevenueOfficer adjustmentApprovedBy) {
        this.adjustmentApprovedBy = adjustmentApprovedBy;
    }
    @Override public String getId() { return assessmentId; }
    @Override public String getDisplayName() { return "Assessment " + assessmentId; }
    @Override public boolean isActive() { return "CONFIRMED".equals(status); }
    @Override public void validate() throws ValidationException {
        if (assessmentId == null || assessmentId.trim().length() < 2 || assessmentId.trim().length() > 30
                || declaration == null || assessedTax == null || adjustmentAmount == null
                || assessmentDate == null) throw new ValidationException("All assessment fields are required.");
        declaration.validate();
        if (!"FILED".equals(declaration.getStatus()) && !"LATE".equals(declaration.getStatus()))
            throw new ValidationException("A rejected declaration cannot be assessed.");
        if (assessmentDate.isAfter(LocalDate.now())) throw new ValidationException("Assessment date cannot be in the future.");
        BigDecimal maxMoney = new BigDecimal("999999999999.99");
        if (assessedTax.signum() < 0 || assessedTax.scale() > 2 || adjustmentAmount.scale() > 2
                || assessedTax.compareTo(maxMoney) > 0 || adjustmentAmount.abs().compareTo(maxMoney) > 0
                || getTotal().signum() < 0 || getTotal().compareTo(maxMoney) > 0)
            throw new ValidationException("Assessment amounts must be valid and total cannot be negative.");
        if (adjustmentAmount.signum() != 0 && (adjustmentApprovedBy == null || !adjustmentApprovedBy.isActive()))
            throw new ValidationException("A non-zero adjustment requires an active approving officer.");
        if (adjustmentApprovedBy != null) adjustmentApprovedBy.validate();
        if (!"DRAFT".equals(status) && !"CONFIRMED".equals(status) && !"CANCELLED".equals(status))
            throw new ValidationException("Assessment status must be DRAFT, CONFIRMED, or CANCELLED.");
    }
    @Override public void printDetails() { System.out.println(getSummary()); }
    @Override public String getSummary() { return getDisplayName() + ", declaration=" + declaration.getId()
            + ", assessed=" + assessedTax + ", adjustment=" + adjustmentAmount + ", total=" + getTotal()
            + ", status=" + status + (adjustmentApprovedBy == null ? "" : ", adjustment approved by=" + adjustmentApprovedBy.getDisplayName()); }
}
