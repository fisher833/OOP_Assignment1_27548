package com.auca.individualtaxpayer;

import java.math.BigDecimal;
import java.time.LocalDate;
import com.auca.abstracts.RegisteredTaxpayer;
import com.auca.exceptions.ValidationException;


public class IndividualTaxpayer extends RegisteredTaxpayer {
    private String nationalId;
    private String occupation;
    private String businessName;
    private LocalDate businessStartDate;
    private BigDecimal annualTurnover;
    private boolean resident;

    public IndividualTaxpayer(String partyId, String firstName, String lastName, String email,
            String phoneNumber, LocalDate dateOfBirth, String tin, LocalDate registrationDate,
            String taxpayerStatus, String district, String sector, String taxOffice,
            String nationalId, String occupation, String businessName, LocalDate businessStartDate,
            BigDecimal annualTurnover, boolean resident) {
        
        super(partyId, firstName, lastName, email, phoneNumber, dateOfBirth, tin, registrationDate,
                taxpayerStatus, district, sector, taxOffice);
        this.nationalId = nationalId;
        this.occupation = occupation;
        this.businessName = businessName;
        this.businessStartDate = businessStartDate;
        this.annualTurnover = annualTurnover;
        this.resident = resident;
    }

    
    public IndividualTaxpayer(String partyId, String firstName, String lastName, String email,
            String phoneNumber, LocalDate dateOfBirth, String tin, LocalDate registrationDate,
            String taxpayerStatus, String district, String sector, String taxOffice,
            String nationalId, String occupation, String businessName, LocalDate businessStartDate,
            BigDecimal annualTurnover) {
        this(partyId, firstName, lastName, email, phoneNumber, dateOfBirth, tin, registrationDate,
                taxpayerStatus, district, sector, taxOffice, nationalId, occupation, businessName,
                businessStartDate, annualTurnover, true);
    }

    public String getNationalId() { return nationalId; }
    public String getOccupation() { return occupation; }
    public String getBusinessName() { return businessName; }
    public LocalDate getBusinessStartDate() { return businessStartDate; }
    public BigDecimal getAnnualTurnover() { return annualTurnover; }
    public boolean isResident() { return resident; }
    @Override public String getDisplayName() { return getFirstName() + " " + getLastName() + " (Individual)"; }
    @Override public String getSummary() { return super.getSummary() + ", TIN=" + getTin() + ", national ID=" + nationalId; }
    @Override public void validate() throws ValidationException {
        super.validate();
        if (nationalId == null || !nationalId.matches("\\d{16}"))
            throw new ValidationException("National ID must be exactly 16 digits.");
        if (occupation == null || occupation.trim().length() < 2 || occupation.trim().length() > 60)
            throw new ValidationException("Occupation must contain 2-60 characters.");
        if (businessName == null || businessName.trim().length() < 2 || businessName.trim().length() > 120)
            throw new ValidationException("Business name must contain 2-120 characters.");
        if (businessStartDate == null || businessStartDate.isAfter(LocalDate.now())
                || !businessStartDate.isBefore(getRegistrationDate()))
            throw new ValidationException("Business start date must precede registration and cannot be future.");
        if (annualTurnover == null || annualTurnover.signum() < 0 || annualTurnover.scale() > 2
                || annualTurnover.compareTo(new BigDecimal("999999999999.99")) > 0)
            throw new ValidationException("Annual turnover must be 0-999999999999.99 with at most 2 decimals.");
    }
}
