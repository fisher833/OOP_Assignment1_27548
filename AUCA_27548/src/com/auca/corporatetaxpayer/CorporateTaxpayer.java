package com.auca.corporatetaxpayer;

import java.math.BigDecimal;
import java.time.LocalDate;
import com.auca.abstracts.RegisteredTaxpayer;
import com.auca.exceptions.ValidationException;


public class CorporateTaxpayer extends RegisteredTaxpayer {
    private String companyRegistrationNo;
    private String legalName;
    private LocalDate incorporationDate;
    private String businessSector;
    private String authorizedRepresentative;
    private BigDecimal annualTurnover;

    public CorporateTaxpayer(String partyId, String firstName, String lastName, String email,
            String phoneNumber, LocalDate dateOfBirth, String tin, LocalDate registrationDate,
            String taxpayerStatus, String district, String sector, String taxOffice,
            String companyRegistrationNo, String legalName, LocalDate incorporationDate,
            String businessSector, String authorizedRepresentative, BigDecimal annualTurnover) {
        super(partyId, firstName, lastName, email, phoneNumber, dateOfBirth, tin, registrationDate,
                taxpayerStatus, district, sector, taxOffice);
        this.companyRegistrationNo = companyRegistrationNo;
        this.legalName = legalName;
        this.incorporationDate = incorporationDate;
        this.businessSector = businessSector;
        this.authorizedRepresentative = authorizedRepresentative;
        this.annualTurnover = annualTurnover;
    }

    public String getCompanyRegistrationNo() { return companyRegistrationNo; }
    public String getLegalName() { return legalName; }
    public LocalDate getIncorporationDate() { return incorporationDate; }
    public String getBusinessSector() { return businessSector; }
    public String getAuthorizedRepresentative() { return authorizedRepresentative; }
    public BigDecimal getAnnualTurnover() { return annualTurnover; }
    @Override public String getDisplayName() { return legalName + " (Corporate)"; }
    @Override public String getSummary() { return super.getSummary() + ", registration=" + companyRegistrationNo; }
    @Override public void validate() throws ValidationException {
        super.validate();
        if (companyRegistrationNo == null || !companyRegistrationNo.matches("[A-Z0-9-]{5,30}"))
            throw new ValidationException("Company registration number must be 5-30 uppercase letters, digits, or hyphens.");
        if (legalName == null || legalName.trim().length() < 3 || legalName.trim().length() > 120)
            throw new ValidationException("Legal name must contain 3-120 characters.");
        if (incorporationDate == null || incorporationDate.isAfter(LocalDate.now())
                || !incorporationDate.isBefore(getRegistrationDate()))
            throw new ValidationException("Incorporation date must precede registration and cannot be future.");
        if (businessSector == null || businessSector.trim().length() < 2 || businessSector.trim().length() > 60
                || authorizedRepresentative == null || authorizedRepresentative.trim().length() < 2
                || authorizedRepresentative.trim().length() > 60)
            throw new ValidationException("Business sector and authorized representative must contain 2-60 characters.");
        if (annualTurnover == null || annualTurnover.signum() < 0 || annualTurnover.scale() > 2
                || annualTurnover.compareTo(new BigDecimal("999999999999.99")) > 0)
            throw new ValidationException("Annual turnover must be 0-999999999999.99 with at most 2 decimals.");
    }
}
