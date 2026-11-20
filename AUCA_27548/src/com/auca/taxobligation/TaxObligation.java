package com.auca.taxobligation;

import com.auca.abstracts.RegisteredTaxpayer;
import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Validatable;
import com.auca.taxtype.TaxType;
import java.time.LocalDate;

public class TaxObligation implements Identifiable, Validatable {
    private String obligationId;
    
    private RegisteredTaxpayer taxpayer;
    private TaxType taxType;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private LocalDate dueDate;

    public TaxObligation(String obligationId, RegisteredTaxpayer taxpayer, TaxType taxType,
                         LocalDate periodStart, LocalDate periodEnd, LocalDate dueDate) {
        this.obligationId = obligationId;
        this.taxpayer = taxpayer;
        this.taxType = taxType;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.dueDate = dueDate;
    }
    public String getObligationId() { return obligationId; }
    public RegisteredTaxpayer getTaxpayer() { return taxpayer; }
    public TaxType getTaxType() { return taxType; }
    public LocalDate getPeriodStart() { return periodStart; }
    public LocalDate getPeriodEnd() { return periodEnd; }
    public LocalDate getDueDate() { return dueDate; }
    @Override public String getId() { return obligationId; }
    @Override public String getDisplayName() { return "Obligation " + obligationId + " - " + taxType.getTaxName(); }
    @Override public boolean isActive() { return taxpayer.isActive() && taxType.isActive(); }
    @Override public void validate() throws ValidationException {
        if (obligationId == null || obligationId.trim().length() < 2 || obligationId.trim().length() > 30
                || taxpayer == null || taxType == null)
            throw new ValidationException("Obligation ID must contain 2-30 characters; taxpayer and tax type are required.");
        taxpayer.validate();
        taxType.validate();
        if (!taxpayer.isActive() || !taxType.isActive()) throw new ValidationException("Taxpayer and tax type must be active.");
        if (periodStart == null || periodEnd == null || dueDate == null || periodStart.isAfter(periodEnd))
            throw new ValidationException("Invalid obligation period: start must be on or before end.");
        if (dueDate.isBefore(periodEnd)) throw new ValidationException("Due date cannot precede period end.");
    }
    @Override public void printDetails() { System.out.println(getSummary()); }
    @Override public String getSummary() { return getDisplayName() + ", taxpayer=" + taxpayer.getDisplayName()
            + ", " + periodStart + " to " + periodEnd + ", due=" + dueDate; }
}
