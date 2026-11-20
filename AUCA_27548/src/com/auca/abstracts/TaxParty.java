package com.auca.abstracts;

import java.time.LocalDate;
import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Validatable;


public abstract class TaxParty implements Identifiable, Validatable {
    
    private String partyId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private LocalDate dateOfBirth;

    protected TaxParty(String partyId, String firstName, String lastName,
                       String email, String phoneNumber, LocalDate dateOfBirth) {
        this.partyId = partyId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.dateOfBirth = dateOfBirth;
    }

    public String getPartyId() { return partyId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }

    @Override public String getId() { return partyId; }
    @Override public String getDisplayName() { return firstName + " " + lastName; }
    @Override public boolean isActive() { return true; }
    @Override public void validate() throws ValidationException {
        if (partyId == null || partyId.trim().length() < 4 || partyId.trim().length() > 30)
            throw new ValidationException("Party ID must contain 4-30 characters.");
        if (firstName == null || firstName.trim().length() < 2 || firstName.trim().length() > 60)
            throw new ValidationException("First name must contain 2-60 characters.");
        if (lastName == null || lastName.trim().length() < 2 || lastName.trim().length() > 60)
            throw new ValidationException("Last name must contain 2-60 characters.");
        if (email == null || email.length() > 120 || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))
            throw new ValidationException("Enter a valid email address (maximum 120 characters).");
        if (phoneNumber == null || !phoneNumber.matches("\\+?\\d{10,15}"))
            throw new ValidationException("Phone must contain 10-15 digits, optionally prefixed by +.");
        if (dateOfBirth == null || !dateOfBirth.isBefore(LocalDate.now()))
            throw new ValidationException("Date of birth must be a past date.");
    }
    @Override public void printDetails() { System.out.println(getSummary()); }
    @Override public String getSummary() {
        return getDisplayName() + " [ID=" + partyId + ", email=" + email + ", phone=" + phoneNumber + "]";
    }
}
