package com.auca.abstracts;

import java.time.LocalDate;
import com.auca.exceptions.ValidationException;


public abstract class RegisteredTaxpayer extends TaxParty {
    private String tin;
    private LocalDate registrationDate;
    private String taxpayerStatus;
    private String district;
    private String sector;
    private String taxOffice;

    protected RegisteredTaxpayer(String partyId, String firstName, String lastName,
            String email, String phoneNumber, LocalDate dateOfBirth, String tin,
            LocalDate registrationDate, String taxpayerStatus, String district,
            String sector, String taxOffice) {
        super(partyId, firstName, lastName, email, phoneNumber, dateOfBirth);
        this.tin = tin;
        this.registrationDate = registrationDate;
        this.taxpayerStatus = taxpayerStatus;
        this.district = district;
        this.sector = sector;
        this.taxOffice = taxOffice;
    }

    public String getTin() { return tin; }
    public LocalDate getRegistrationDate() { return registrationDate; }
    public String getTaxpayerStatus() { return taxpayerStatus; }
    public String getDistrict() { return district; }
    public String getSector() { return sector; }
    public String getTaxOffice() { return taxOffice; }
    public void setTaxpayerStatus(String taxpayerStatus) { this.taxpayerStatus = taxpayerStatus; }
    @Override public boolean isActive() { return "ACTIVE".equals(taxpayerStatus); }

    @Override public void validate() throws ValidationException {
        super.validate();
        if (tin == null || !tin.matches("\\d{9}")) throw new ValidationException("TIN must be exactly 9 digits.");
        if (registrationDate == null || registrationDate.isAfter(LocalDate.now()))
            throw new ValidationException("Registration date cannot be in the future.");
        if (taxpayerStatus == null || !(taxpayerStatus.equals("ACTIVE") || taxpayerStatus.equals("INACTIVE")))
            throw new ValidationException("Status must be ACTIVE or INACTIVE.");
        if (!validLocationText(district) || !validLocationText(sector) || !validLocationText(taxOffice))
            throw new ValidationException("District, sector, and tax office must each contain 2-60 characters.");
        if (getDateOfBirth() == null || !getDateOfBirth().isBefore(registrationDate))
            throw new ValidationException("Birth date must precede registration date.");
    }

    private boolean validLocationText(String value) {
        return value != null && value.trim().length() >= 2 && value.trim().length() <= 60;
    }
}
