package com.auca.penalty;

import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Validatable;
import com.auca.taxobligation.TaxObligation;
import java.math.BigDecimal;

public class Penalty implements Identifiable, Validatable {
    private String penaltyId;
    private TaxObligation obligation;
    private String penaltyType;
    private BigDecimal baseAmount;
    private BigDecimal penaltyRate;
    private BigDecimal penaltyAmount;

    public Penalty(String penaltyId, TaxObligation obligation, String penaltyType,
                   BigDecimal baseAmount, BigDecimal penaltyRate, BigDecimal penaltyAmount) {
        this.penaltyId = penaltyId;
        this.obligation = obligation;
        this.penaltyType = penaltyType;
        this.baseAmount = baseAmount;
        this.penaltyRate = penaltyRate;
        this.penaltyAmount = penaltyAmount;
    }
    public String getPenaltyId() { return penaltyId; }
    public TaxObligation getObligation() { return obligation; }
    public String getPenaltyType() { return penaltyType; }
    public BigDecimal getBaseAmount() { return baseAmount; }
    public BigDecimal getPenaltyRate() { return penaltyRate; }
    public BigDecimal getPenaltyAmount() { return penaltyAmount; }
    @Override public String getId() { return penaltyId; }
    @Override public String getDisplayName() { return penaltyType + " penalty " + penaltyId; }
    @Override public boolean isActive() { return true; }
    @Override public void validate() throws ValidationException {
        if (penaltyId == null || penaltyId.trim().length() < 2 || penaltyId.trim().length() > 30
                || obligation == null || penaltyType == null || penaltyType.trim().isEmpty())
            throw new ValidationException("Penalty ID, obligation, and type are required.");
        obligation.validate();
        if (baseAmount == null || penaltyRate == null || penaltyAmount == null || baseAmount.signum() < 0
                || penaltyRate.signum() < 0 || penaltyAmount.signum() < 0 || baseAmount.scale() > 2
                || penaltyRate.scale() > 2 || penaltyAmount.scale() > 2
                || penaltyRate.compareTo(new BigDecimal("100")) > 0
                || baseAmount.compareTo(new BigDecimal("999999999999.99")) > 0
                || penaltyAmount.compareTo(new BigDecimal("999999999999.99")) > 0)
            throw new ValidationException("Penalty amounts and rate must be non-negative with at most 2 decimals.");
    }
    @Override public void printDetails() { System.out.println(getSummary()); }
    @Override public String getSummary() { return getDisplayName() + ", obligation=" + obligation.getId()
            + ", base=" + baseAmount + ", rate=" + penaltyRate + "%, amount=" + penaltyAmount; }
}
