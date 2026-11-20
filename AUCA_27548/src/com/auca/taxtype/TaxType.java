package com.auca.taxtype;

import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Validatable;
import java.math.BigDecimal;

public class TaxType implements Identifiable, Validatable {
    private String taxTypeId;
    private String taxCode;
    private String taxName;
    private BigDecimal rate;
    private String filingFrequency;
    private boolean active;

    public TaxType(String taxTypeId, String taxCode, String taxName, BigDecimal rate,
                   String filingFrequency, boolean active) {
        this.taxTypeId = taxTypeId;
        this.taxCode = taxCode;
        this.taxName = taxName;
        this.rate = rate;
        this.filingFrequency = filingFrequency;
        this.active = active;
    }
    public String getTaxTypeId() { return taxTypeId; }
    public String getTaxCode() { return taxCode; }
    public String getTaxName() { return taxName; }
    public BigDecimal getRate() { return rate; }
    public String getFilingFrequency() { return filingFrequency; }
    public void setRate(BigDecimal rate) { this.rate = rate; }
    public void setActive(boolean active) { this.active = active; }
    @Override public String getId() { return taxTypeId; }
    @Override public String getDisplayName() { return taxName + " (" + taxCode + ")"; }
    @Override public boolean isActive() { return active; }
    @Override public void validate() throws ValidationException {
        if (!requiredText(taxTypeId, 2, 30) || !requiredText(taxCode, 2, 30))
            throw new ValidationException("Tax type ID and code must contain 2-30 characters.");
        if (!requiredText(taxName, 2, 60)) throw new ValidationException("Tax name must contain 2-60 characters.");
        if (rate == null || rate.signum() < 0 || rate.compareTo(new BigDecimal("100")) > 0 || rate.scale() > 2)
            throw new ValidationException("Fictional rate must be 0-100 with at most 2 decimal places.");
        if (!"MONTHLY".equals(filingFrequency) && !"QUARTERLY".equals(filingFrequency)
                && !"ANNUAL".equals(filingFrequency))
            throw new ValidationException("Filing frequency must be MONTHLY, QUARTERLY, or ANNUAL.");
    }
    private boolean requiredText(String value, int minimum, int maximum) {
        return value != null && value.trim().length() >= minimum && value.trim().length() <= maximum;
    }
    @Override public void printDetails() { System.out.println(getSummary()); }
    @Override public String getSummary() { return getDisplayName() + ", rate=" + rate + "%, " + filingFrequency + ", active=" + active; }
}
