package com.auca.main;

import com.auca.abstracts.RegisteredTaxpayer;
import com.auca.abstracts.TaxParty;
import com.auca.corporatetaxpayer.CorporateTaxpayer;
import com.auca.exceptions.*;
import com.auca.individualtaxpayer.IndividualTaxpayer;
import com.auca.interfaces.Identifiable;
import com.auca.penalty.Penalty;
import com.auca.revenueofficer.RevenueOfficer;
import com.auca.taxassessment.TaxAssessment;
import com.auca.taxclearance.TaxClearance;
import com.auca.taxdeclaration.TaxDeclaration;
import com.auca.taxobligation.TaxObligation;
import com.auca.taxpayment.TaxPayment;
import com.auca.taxrefund.TaxRefund;
import com.auca.taxtype.TaxType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class Main {
    private static final Scanner SCANNER = new Scanner(System.in); 
    private static final BigDecimal MAX_MONEY = new BigDecimal("999999999999.99");
    private static final BigDecimal ZERO = new BigDecimal("0.00");
    private static final BigDecimal LATE_RATE = new BigDecimal("5.00"); 

    private static final IndividualTaxpayer[] individuals = new IndividualTaxpayer[100];
    private static final CorporateTaxpayer[] corporates = new CorporateTaxpayer[100];
    private static final RevenueOfficer[] officers = new RevenueOfficer[100];
    private static final TaxType[] taxTypes = new TaxType[100];
    private static final TaxObligation[] obligations = new TaxObligation[300];
    private static final TaxDeclaration[] declarations = new TaxDeclaration[300];
    private static final TaxAssessment[] assessments = new TaxAssessment[300];
    private static final TaxPayment[] payments = new TaxPayment[300];
    private static final Penalty[] penalties = new Penalty[300];
    private static final TaxRefund[] refunds = new TaxRefund[100];
    private static final TaxClearance[] clearances = new TaxClearance[100];
    private static int individualCount, corporateCount, officerCount, taxTypeCount;
    private static int obligationCount, declarationCount, assessmentCount, paymentCount;
    private static int penaltyCount, refundCount, clearanceCount;

    private Main() { }

    @FunctionalInterface
    private interface SaveAction {
        void save(String[] values) throws ValidationException, DuplicateRecordException,
                RecordNotFoundException, PrerequisiteMissingException, InvalidTaxPeriodException,
                InvalidTaxCalculationException, OverpaymentException, UnauthorizedApprovalException,
                InvalidStatusTransitionException;
    }

    private static final class Field {
        private final String label;
        private final String typeAndFormat;
        private Field(String label, String typeAndFormat) {
            this.label = label;
            this.typeAndFormat = typeAndFormat;
        }
    }

    public static void main(String[] args) {
        System.out.println("TAX ADMINISTRATION MANAGEMENT SYSTEM (TAMS)");
        System.out.println("Fictional educational rates only; never use for actual tax liabilities.");
        boolean running = true;
        while (running) {
            printMenu();
            String choice = SCANNER.nextLine().trim();
            try {
                int option = Integer.parseInt(choice);
                if (option == 0) {
                    running = false;
                } else if (option < 1 || option > 21) {
                    System.out.println("Choose a menu option from 0 to 21.");
                } else {
                    runOption(option);
                }
            } catch (NumberFormatException ex) {
                System.out.println("Menu option must be a whole number from 0 to 21.");
            } catch (ValidationException | DuplicateRecordException | RecordNotFoundException
                     | PrerequisiteMissingException | InvalidTaxPeriodException
                     | InvalidTaxCalculationException | OverpaymentException
                     | UnauthorizedApprovalException | InvalidStatusTransitionException ex) {
                System.out.println("Operation not completed: " + ex.getMessage());
            }
        }
        System.out.println("TAMS closed.");
    }

    private static void printMenu() {
        System.out.println("\n================ TAX ADMINISTRATION MANAGEMENT ================");
        System.out.println(" 1 Register Individual Taxpayer       12 Record Tax Payment");
        System.out.println(" 2 Register Corporate Taxpayer        13 Calculate / Record Penalty");
        System.out.println(" 3 Register Revenue Officer           14 Request / Approve Refund");
        System.out.println(" 4 Register Tax Type                  15 Request / Issue Tax Clearance");
        System.out.println(" 5 Create Tax Obligation              16 Search Record by ID / TIN");
        System.out.println(" 6 File Tax Declaration               17 Update Eligible Record");
        System.out.println(" 7 Assess Declaration                 18 View Taxpayer Statement");
        System.out.println(" 8 Confirm Assessment                 19 View Outstanding Balances");
        System.out.println(" 9 View All Records                   20 View Tax Reports");
        System.out.println("10 View Declaration History           21 Demonstrate Polymorphism");
        System.out.println("11 View Assessment History             0 Exit");
        System.out.print("Select option: ");
    }

    private static void runOption(int option) throws ValidationException, DuplicateRecordException,
            RecordNotFoundException, PrerequisiteMissingException, InvalidTaxPeriodException,
            InvalidTaxCalculationException, OverpaymentException, UnauthorizedApprovalException,
            InvalidStatusTransitionException {
        switch (option) {
            case 1 -> registerIndividual();
            case 2 -> registerCorporate();
            case 3 -> registerOfficer();
            case 4 -> registerTaxType();
            case 5 -> createObligation();
            case 6 -> fileDeclaration();
            case 7 -> assessDeclaration();
            case 8 -> confirmAssessment();
            case 9 -> viewAllRecords();
            case 10 -> viewDeclarationHistory();
            case 11 -> viewAssessmentHistory();
            case 12 -> recordPayment();
            case 13 -> recordPenalty();
            case 14 -> manageRefund();
            case 15 -> issueClearance();
            case 16 -> searchRecord();
            case 17 -> updateRecord();
            case 18 -> viewStatement();
            case 19 -> viewOutstandingBalances();
            case 20 -> viewTaxReports();
            case 21 -> demonstratePolymorphism();
            default -> System.out.println("Unknown option.");
        }
    }

    private static void recordForm(String title, Field[] fields, int[] stepEnds, SaveAction save) {
        String[] values = new String[fields.length];
        int step = 0;
        while (true) {
            int first = step == 0 ? 0 : stepEnds[step - 1];
            int last = stepEnds[step];
            boolean backRequested = false;
            System.out.println("\n+---------------- " + title + " ----------------+");
            System.out.println("STEP " + (step + 1) + " OF 4");
            for (int i = first; i < last; i++) {
                System.out.printf("%02d %-24s [%s]%n", i + 1, fields[i].label, fields[i].typeAndFormat);
                values[i] = promptField(fields[i], values[i]);
                if ("\u0000CANCEL".equals(values[i])) {
                    System.out.println("Form cancelled; no record was saved.");
                    return;
                }
                if ("\u0000BACK".equals(values[i])) {
                    values[i] = null;
                    if (step > 0) step--;
                    else System.out.println("Already at the first form step.");
                    backRequested = true;
                    break;
                }
            }
            if (backRequested) continue;
            if (step < 2) {
                System.out.print("NEXT / BACK / CANCEL: ");
                String nav = SCANNER.nextLine().trim();
                if (nav.equalsIgnoreCase("CANCEL")) {
                    System.out.println("Form cancelled; no record was saved.");
                    return;
                } else if (nav.equalsIgnoreCase("BACK")) {
                    if (step > 0) step--;
                    else System.out.println("Already at the first form step.");
                } else if (nav.isEmpty() || nav.equalsIgnoreCase("NEXT")) {
                    step++;
                } else {
                    System.out.println("Enter NEXT, BACK, or CANCEL.");
                }
                continue;
            }

            while (true) {
                refreshComputedFields(title, fields, values);
                printReview(title, fields, values);
                System.out.print("[1] SAVE  [2] EDIT FIELD  [0] CANCEL: ");
                String action = SCANNER.nextLine().trim();
                if (action.equals("0") || action.equalsIgnoreCase("CANCEL")) {
                    System.out.println("Form cancelled; no record was saved.");
                    return;
                }
                if (action.equals("2")) {
                    int field = readInteger("Field number to edit (1-" + fields.length + "): ", 1, fields.length);
                    String revised = promptField(fields[field - 1], values[field - 1]);
                    if (revised == null || revised.equals("\u0000CANCEL")) {
                        System.out.println("Edit cancelled; saved form values are unchanged.");
                    } else if (!revised.equals("\u0000BACK")) {
                        values[field - 1] = revised;
                    }
                    continue;
                }
                if (!action.equals("1")) {
                    System.out.println("Choose 1, 2, or 0.");
                    continue;
                }
                try {
                    save.save(values);
                    return;
                } catch (ValidationException | DuplicateRecordException | RecordNotFoundException
                         | PrerequisiteMissingException | InvalidTaxPeriodException
                         | InvalidTaxCalculationException | OverpaymentException
                         | UnauthorizedApprovalException | InvalidStatusTransitionException ex) {
                    
                    System.out.println("SAVE rejected: " + ex.getMessage());
                    System.out.println("Your entered values are retained; edit a field or retry SAVE.");
                }
            }
        }
    }

    private static String promptField(Field field, String prior) {
        while (true) {
            System.out.print(field.label + (prior == null ? "" : " [" + prior + "]") + ": ");
            String value = SCANNER.nextLine().trim();
            if (value.equalsIgnoreCase("CANCEL")) return "\u0000CANCEL";
            if (value.equalsIgnoreCase("BACK")) return "\u0000BACK";
            if (value.isEmpty() && prior != null) value = prior;
            String error = validateInput(field, value);
            if (error == null) return value;
            System.out.println(error);
        }
    }

    private static String validateInput(Field field, String value) {
        String name = field.label.toLowerCase();
        if (value == null || value.trim().isEmpty()) return field.label + " is required.";
        if (field.typeAndFormat.contains("2-60 chars") && (value.length() < 2 || value.length() > 60))
            return field.label + " must contain 2-60 characters.";
        if (field.typeAndFormat.contains("2-120 chars") && (value.length() < 2 || value.length() > 120))
            return field.label + " must contain 2-120 characters.";
        if (field.typeAndFormat.contains("3-120 chars") && (value.length() < 3 || value.length() > 120))
            return field.label + " must contain 3-120 characters.";
        if (field.typeAndFormat.contains("2-30 chars") && (value.length() < 2 || value.length() > 30))
            return field.label + " must contain 2-30 characters.";
        if (name.contains("email") && (!value.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$") || value.length() > 120))
            return "Enter a syntactically valid email (maximum 120 characters).";
        if (name.contains("phone") && !value.matches("\\+?\\d{10,15}"))
            return "Phone must contain 10-15 digits, optionally prefixed by +.";
        if (name.equals("tin") && !value.matches("\\d{9}")) return "TIN must be exactly 9 digits.";
        if (name.contains("national id") && !value.matches("\\d{16}")) return "National ID must be exactly 16 digits.";
        if (name.contains("registration no") && !value.matches("[A-Z0-9-]{5,30}"))
            return "Use 5-30 uppercase letters, digits, or hyphens.";
        if (name.contains("first name") || name.contains("last name")) {
            if (value.length() < 2 || value.length() > 60) return "Name must contain 2-60 characters.";
        }
        if (name.equals("party id") && (value.length() < 4 || value.length() > 30))
            return "Party ID must contain 4-30 characters.";
        if (name.equals("taxpayer status") && !value.equalsIgnoreCase("ACTIVE") && !value.equalsIgnoreCase("INACTIVE"))
            return "Status must be ACTIVE or INACTIVE.";
        if (name.equals("status") && !value.equalsIgnoreCase("AUTO") && !value.equalsIgnoreCase("DRAFT")
                && !value.equalsIgnoreCase("PENDING"))
            return "Status is system-controlled; enter AUTO, DRAFT, or PENDING as shown.";
        if (name.contains("date") || name.contains("period") || name.contains("start") || name.contains("due")
                || name.contains("expiry") || name.contains("hire") || name.contains("incorporation")) {
            try {
                LocalDate date = LocalDate.parse(value);
                if (!date.toString().equals(value)) return "Date must use strict YYYY-MM-DD format.";
            } catch (DateTimeParseException ex) {
                return "Invalid date; use YYYY-MM-DD.";
            }
        }
        if (name.contains("amount") || name.contains("rate") || name.contains("turnover")
                || name.equals("declared tax") || name.equals("assessed tax")
                || name.equals("approval limit") || name.equals("adjustment")) {
            try {
                BigDecimal amount = new BigDecimal(value);
                if (amount.scale() > 2 || amount.abs().compareTo(MAX_MONEY) > 0)
                    return "Enter an amount with at most 2 decimal places and absolute value <= 999999999999.99.";
                if (!name.contains("adjustment") && amount.signum() < 0) return "Amount must be non-negative.";
                if (name.equals("rate") && amount.compareTo(new BigDecimal("100")) > 0) return "Rate must be 0-100.";
            } catch (NumberFormatException ex) {
                return "Enter a valid decimal number.";
            }
        }
        if (name.contains("resident") || name.equals("active")) {
            if (!value.equalsIgnoreCase("YES") && !value.equalsIgnoreCase("NO")
                    && !value.equalsIgnoreCase("TRUE") && !value.equalsIgnoreCase("FALSE"))
                return "Enter YES or NO.";
        }
        if (name.contains("frequency") && !value.matches("MONTHLY|QUARTERLY|ANNUAL"))
            return "Frequency must be MONTHLY, QUARTERLY, or ANNUAL.";
        return null;
    }

    private static void printReview(String title, Field[] fields, String[] values) {
        System.out.println("\nSTEP 4 OF 4 - REVIEW " + title);
        for (int i = 0; i < fields.length; i++) {
            System.out.printf("%02d %-25s = %s%n", i + 1, fields[i].label, values[i]);
        }
    }

    private static void refreshComputedFields(String title, Field[] fields, String[] values) {
        if (!"FILE TAX DECLARATION".equals(title) || fields.length < 6 || values[1] == null || values[2] == null)
            return;
        TaxObligation obligation = findObligation(values[1]);
        if (obligation == null) return;
        try {
            values[5] = LocalDate.parse(values[2]).isAfter(obligation.getDueDate()) ? "LATE" : "FILED";
        } catch (DateTimeParseException ex) {
            System.out.println("Cannot preview declaration status until the filing date is corrected.");
        }
    }

    private static Field f(String label, String typeAndFormat) { return new Field(label, typeAndFormat); }
    private static String v(String[] values, int index) { return values[index].trim(); }
    private static LocalDate date(String[] values, int index) throws ValidationException {
        try { return LocalDate.parse(v(values, index)); }
        catch (DateTimeParseException ex) { throw new ValidationException("Invalid date for " + (index + 1) + "."); }
    }
    private static BigDecimal money(String[] values, int index) throws ValidationException {
        try { return new BigDecimal(v(values, index)).setScale(2, RoundingMode.UNNECESSARY); }
        catch (NumberFormatException | ArithmeticException ex) {
            throw new ValidationException("Invalid money amount for " + (index + 1) + "; use at most two decimals.");
        }
    }
    private static boolean yes(String value) { return value.equalsIgnoreCase("YES") || value.equalsIgnoreCase("TRUE"); }
    private static int readInteger(String prompt, int minimum, int maximum) {
        while (true) {
            System.out.print(prompt);
            try {
                int value = Integer.parseInt(SCANNER.nextLine().trim());
                if (value >= minimum && value <= maximum) return value;
                System.out.println("Enter a number from " + minimum + " to " + maximum + ".");
            } catch (NumberFormatException ex) {
                System.out.println("Enter a whole number.");
            }
        }
    }

    private static boolean capacity(int count, Object[] array, String kind) {
        if (count < array.length) return true;
        System.out.println("Cannot save: " + kind + " capacity (" + array.length + ") is full.");
        return false;
    }

    private static void registerIndividual() {
        Field[] fields = {
            f("Party ID", "String, unique, 4-30 chars"), f("First name", "String, 2-60 chars"),
            f("Last name", "String, 2-60 chars"), f("Email", "String, valid email <=120"),
            f("Phone", "String, 10-15 digits, optional +"), f("Date of birth", "LocalDate, YYYY-MM-DD, past"),
            f("TIN", "String, unique, exactly 9 digits"), f("Registration date", "LocalDate, YYYY-MM-DD"),
            f("Taxpayer status", "String, ACTIVE or INACTIVE"), f("District", "String, required, 2-60 chars"),
            f("Sector", "String, required, 2-60 chars"), f("Tax office", "String, required, 2-60 chars"),
            f("National ID", "String, unique, exactly 16 digits"), f("Occupation", "String, required, 2-60 chars"),
            f("Business name", "String, required, 2-120 chars"), f("Business start date", "LocalDate, YYYY-MM-DD"),
            f("Annual turnover", "BigDecimal, 0-999999999999.99"), f("Resident", "boolean, YES or NO")
        };
        recordForm("REGISTER INDIVIDUAL TAXPAYER", fields, new int[]{6, 12, 18}, values -> {
            if (!capacity(individualCount, individuals, "Individual taxpayer")) return;
            IndividualTaxpayer taxpayer = new IndividualTaxpayer(v(values,0), v(values,1), v(values,2),
                    v(values,3), v(values,4), date(values,5), v(values,6), date(values,7), v(values,8).toUpperCase(),
                    v(values,9), v(values,10), v(values,11), v(values,12), v(values,13), v(values,14),
                    date(values,15), money(values,16), yes(v(values,17)));
            taxpayer.validate();
            checkTaxpayerUniqueness(taxpayer.getTin(), taxpayer.getNationalId(), null);
            checkPartyId(taxpayer.getPartyId());
            individuals[individualCount++] = taxpayer;
            System.out.println("Saved: " + taxpayer.getSummary());
        });
    }

    private static void registerCorporate() {
        // The inherited name and birth-date fields identify the corporate taxpayer's authorized contact.
        Field[] fields = {
            f("Party ID", "String, unique, 4-30 chars"), f("First name", "String, 2-60 chars"),
            f("Last name", "String, 2-60 chars"), f("Email", "String, valid email <=120"),
            f("Phone", "String, 10-15 digits, optional +"), f("Date of birth", "LocalDate, YYYY-MM-DD, past"),
            f("TIN", "String, unique, exactly 9 digits"), f("Registration date", "LocalDate, YYYY-MM-DD"),
            f("Taxpayer status", "String, ACTIVE or INACTIVE"), f("District", "String, required, 2-60 chars"),
            f("Sector", "String, required, 2-60 chars"), f("Tax office", "String, required, 2-60 chars"),
            f("Company registration no", "String, unique, 5-30 uppercase letters/digits/hyphens"),
            f("Legal name", "String, 3-120 chars"), f("Incorporation date", "LocalDate, YYYY-MM-DD"),
            f("Business sector", "String, required, 2-60 chars"), f("Authorized representative", "String, required, 2-60 chars"),
            f("Annual turnover", "BigDecimal, 0-999999999999.99")
        };
        recordForm("REGISTER CORPORATE TAXPAYER", fields, new int[]{6, 12, 18}, values -> {
            if (!capacity(corporateCount, corporates, "Corporate taxpayer")) return;
            CorporateTaxpayer taxpayer = new CorporateTaxpayer(v(values,0), v(values,1), v(values,2),
                    v(values,3), v(values,4), date(values,5), v(values,6), date(values,7), v(values,8).toUpperCase(),
                    v(values,9), v(values,10), v(values,11), v(values,12), v(values,13), date(values,14),
                    v(values,15), v(values,16), money(values,17));
            taxpayer.validate();
            checkTaxpayerUniqueness(taxpayer.getTin(), null, taxpayer.getCompanyRegistrationNo());
            checkPartyId(taxpayer.getPartyId());
            corporates[corporateCount++] = taxpayer;
            System.out.println("Saved: " + taxpayer.getSummary());
        });
    }

    private static void registerOfficer() {
        Field[] fields = {
            f("Party ID", "String, unique, 4-30 chars"), f("First name", "String, 2-60 chars"),
            f("Last name", "String, 2-60 chars"), f("Email", "String, valid email <=120"),
            f("Phone", "String, 10-15 digits, optional +"), f("Date of birth", "LocalDate, YYYY-MM-DD, past"),
            f("Staff number", "String, unique, 2-30 chars"), f("Department", "String, required, 2-60 chars"),
            f("Role", "String, required, 2-60 chars"), f("Hire date", "LocalDate, YYYY-MM-DD"),
            f("Approval limit", "BigDecimal, 0-999999999999.99"), f("Active", "boolean, YES or NO")
        };
        recordForm("REGISTER REVENUE OFFICER", fields, new int[]{6, 9, 12}, values -> {
            if (!capacity(officerCount, officers, "Revenue officer")) return;
            RevenueOfficer officer = new RevenueOfficer(v(values,0),v(values,1),v(values,2),v(values,3),v(values,4),
                    date(values,5),v(values,6),v(values,7),v(values,8),date(values,9),money(values,10),yes(v(values,11)));
            officer.validate();
            checkPartyId(officer.getPartyId());
            for (int i=0;i<officerCount;i++)
                if (officers[i].getStaffNumber().equalsIgnoreCase(officer.getStaffNumber()))
                    throw new DuplicateRecordException("Staff number already exists.");
            officers[officerCount++] = officer;
            System.out.println("Saved: " + officer.getSummary());
        });
    }

    private static void registerTaxType() {
        Field[] fields = {
            f("Tax type ID", "String, unique, 2-30 chars"), f("Tax code", "String, unique, 2-30 chars"),
            f("Tax name", "String, required, 2-60 chars"), f("Rate", "BigDecimal, fictional 0-100%, 2 decimals max"),
            f("Filing frequency", "String, MONTHLY/QUARTERLY/ANNUAL"), f("Active", "boolean, YES or NO")
        };
        recordForm("REGISTER TAX TYPE", fields, new int[]{2, 4, 6}, values -> {
            if (!capacity(taxTypeCount, taxTypes, "Tax type")) return;
            TaxType taxType = new TaxType(v(values,0),v(values,1),v(values,2),money(values,3),
                    v(values,4).toUpperCase(),yes(v(values,5)));
            taxType.validate();
            for (int i=0;i<taxTypeCount;i++) {
                if (taxTypes[i].getId().equalsIgnoreCase(taxType.getId())
                        || taxTypes[i].getTaxCode().equalsIgnoreCase(taxType.getTaxCode()))
                    throw new DuplicateRecordException("Tax type ID or code already exists.");
            }
            taxTypes[taxTypeCount++] = taxType;
            System.out.println("Saved: " + taxType.getSummary());
        });
    }

    private static void createObligation() {
        Field[] fields = {
            f("Obligation ID", "String, unique, 2-30 chars"), f("Taxpayer ID or TIN", "String, active taxpayer"),
            f("Tax type ID or code", "String, active tax type"), f("Period start", "LocalDate, YYYY-MM-DD"),
            f("Period end", "LocalDate, YYYY-MM-DD, >= start"), f("Due date", "LocalDate, YYYY-MM-DD, >= period end")
        };
        recordForm("CREATE TAX OBLIGATION", fields, new int[]{3, 5, 6}, values -> {
            if (!capacity(obligationCount, obligations, "Obligation")) return;
            RegisteredTaxpayer taxpayer = findTaxpayer(v(values,1));
            if (taxpayer == null) {
                registerMissingPrerequisite("taxpayer");
                throw new PrerequisiteMissingException("Taxpayer is not registered; values retained for retry.");
            }
            TaxType taxType = findTaxType(v(values,2));
            if (taxType == null) {
                registerMissingPrerequisite("tax type");
                throw new PrerequisiteMissingException("Tax type is not registered; values retained for retry.");
            }
            if (!taxpayer.isActive()) throw new ValidationException("Obligation requires an active taxpayer.");
            if (!taxType.isActive()) throw new ValidationException("Obligation requires an active tax type.");
            LocalDate start = date(values,3);
            LocalDate end = date(values,4);
            LocalDate due = date(values,5);
            if (start.isAfter(end) || due.isBefore(end))
                throw new InvalidTaxPeriodException("Period start must not follow end, and due date must not precede period end.");
            for (int i=0;i<obligationCount;i++) {
                TaxObligation existing = obligations[i];
                if (existing.getId().equalsIgnoreCase(v(values,0)))
                    throw new DuplicateRecordException("Obligation ID already exists.");
                if (existing.getTaxpayer().getTin().equals(taxpayer.getTin())
                        && existing.getTaxType().getId().equals(taxType.getId())
                        && !start.isAfter(existing.getPeriodEnd()) && !end.isBefore(existing.getPeriodStart()))
                    throw new InvalidTaxPeriodException("Obligation overlaps an existing period for this taxpayer and tax type.");
            }
            TaxObligation obligation = new TaxObligation(v(values,0), taxpayer, taxType, start, end, due);
            obligation.validate();
            obligations[obligationCount++] = obligation;
            System.out.println("Saved: " + obligation.getSummary());
        });
    }

    private static void fileDeclaration() {
        Field[] fields = {
            f("Declaration ID", "String, unique, 2-30 chars"), f("Obligation ID", "String, existing obligation"),
            f("Filing date", "LocalDate, YYYY-MM-DD, not future"), f("Taxable amount", "BigDecimal, 0-999999999999.99"),
            f("Declared tax", "BigDecimal, expected rate calculation +/- 0.01"),
            f("Status", "String, enter AUTO; system calculates FILED or LATE")
        };
        recordForm("FILE TAX DECLARATION", fields, new int[]{2, 4, 6}, values -> {
            if (!capacity(declarationCount, declarations, "Declaration")) return;
            TaxObligation obligation = findObligation(v(values,1));
            if (obligation == null) {
                registerMissingPrerequisite("obligation");
                throw new PrerequisiteMissingException("Obligation is not registered; values retained for retry.");
            }
            if (!obligation.isActive()) throw new ValidationException("Obligation taxpayer and tax type must be active.");
            LocalDate filing = date(values,2);
            if (filing.isAfter(LocalDate.now())) throw new ValidationException("Filing date cannot be in the future.");
            BigDecimal taxable = money(values,3);
            BigDecimal declared = money(values,4);
            BigDecimal expected = taxable.multiply(obligation.getTaxType().getRate())
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            if (declared.subtract(expected).abs().compareTo(new BigDecimal("0.01")) > 0)
                throw new InvalidTaxCalculationException("Declared tax " + declared + " does not match expected "
                        + expected + " (taxable amount x fictional rate, rounded HALF_UP).");
            for (int i=0;i<declarationCount;i++) {
                if (declarations[i].getId().equalsIgnoreCase(v(values,0)))
                    throw new DuplicateRecordException("Declaration ID already exists.");
                if (declarations[i].getObligation().getId().equals(obligation.getId())
                        && declarations[i].isActive())
                    throw new InvalidStatusTransitionException("Only one active declaration is allowed per obligation.");
            }
            String status = filing.isAfter(obligation.getDueDate()) ? "LATE" : "FILED";
            TaxDeclaration declaration = new TaxDeclaration(v(values,0), obligation, filing, taxable, declared, status);
            declaration.validate();
            declarations[declarationCount++] = declaration;
            System.out.println("Saved: " + declaration.getSummary());
            if ("LATE".equals(status)) System.out.println("Late filing recorded using the entered filing date.");
        });
    }

    private static void assessDeclaration() {
        Field[] fields = {
            f("Assessment ID", "String, unique, 2-30 chars"), f("Declaration ID", "String, filed and not rejected"),
            f("Assessed tax", "BigDecimal, matches filed declared tax"),
            f("Adjustment amount", "BigDecimal, +/-; total must remain non-negative"),
            f("Adjustment approver", "RevenueOfficer reference; NONE for zero adjustment"),
            f("Assessment date", "LocalDate, YYYY-MM-DD, not future"), f("Status", "String, initially DRAFT")
        };
        recordForm("ASSESS DECLARATION", fields, new int[]{2, 5, 7}, values -> {
            if (!capacity(assessmentCount, assessments, "Assessment")) return;
            TaxDeclaration declaration = findDeclaration(v(values,1));
            if (declaration == null) {
                registerMissingPrerequisite("declaration");
                throw new PrerequisiteMissingException("Declaration is not registered; values retained for retry.");
            }
            if ("REJECTED".equals(declaration.getStatus()))
                throw new InvalidStatusTransitionException("A rejected declaration cannot be assessed.");
            for (int i=0;i<assessmentCount;i++) {
                if (assessments[i].getId().equalsIgnoreCase(v(values,0)))
                    throw new DuplicateRecordException("Assessment ID already exists.");
                if (assessments[i].getDeclaration() == declaration
                        && !"CANCELLED".equals(assessments[i].getStatus()))
                    throw new InvalidStatusTransitionException("This declaration already has an active assessment.");
            }
            BigDecimal assessed = money(values,2);
            if (assessed.compareTo(declaration.getDeclaredTax()) != 0)
                throw new InvalidTaxCalculationException("Assessed tax must equal the verified declared tax.");
            BigDecimal adjustment = money(values,3);
            if (assessed.add(adjustment).signum() < 0)
                throw new ValidationException("Assessment total cannot be negative.");
            RevenueOfficer authorization = null;
            if (adjustment.signum() != 0) {
                authorization = findOfficer(v(values,4));
                if (authorization == null) {
                    registerMissingPrerequisite("revenue officer");
                    throw new PrerequisiteMissingException("Adjustment approver is not registered; values retained for retry.");
                }
                if (!authorization.isActive())
                    throw new UnauthorizedApprovalException("A non-zero adjustment requires an active officer authorization.");
            } else if (!v(values,4).equalsIgnoreCase("NONE")) {
                authorization = findOfficer(v(values,4));
                if (authorization == null) throw new RecordNotFoundException("Adjustment approver not found.");
            }
            TaxAssessment assessment = new TaxAssessment(v(values,0), declaration, assessed, adjustment,
                    date(values,5), "DRAFT");
            assessment.setAdjustmentApprovedBy(authorization);
            assessment.validate();
            assessments[assessmentCount++] = assessment;
            System.out.println("Saved draft assessment: " + assessment.getSummary());
        });
    }

    private static void confirmAssessment() throws RecordNotFoundException, InvalidStatusTransitionException,
            ValidationException {
        if (assessmentCount == 0) throw new RecordNotFoundException("No assessments exist.");
        String id = readRequired("Assessment ID to confirm: ");
        TaxAssessment assessment = findAssessment(id);
        if (assessment == null) throw new RecordNotFoundException("Assessment not found: " + id);
        if (!"DRAFT".equals(assessment.getStatus()))
            throw new InvalidStatusTransitionException("Only DRAFT assessments can be confirmed.");
        assessment.validate();
        assessment.setStatus("CONFIRMED");
        System.out.println("Assessment confirmed: " + assessment.getId() + ", total " + assessment.getTotal());
    }

    private static void recordPayment() {
        Field[] fields = {
            f("Payment ID", "String, unique, 2-30 chars"), f("Assessment ID", "String, CONFIRMED assessment"),
            f("Payment reference", "String, globally unique"), f("Amount", "BigDecimal, >0, <= outstanding balance"),
            f("Payment date", "LocalDate, YYYY-MM-DD, not future"),
            f("Payment method", "String, required, non-blank (spec has no fixed method list)")
        };
        recordForm("RECORD TAX PAYMENT", fields, new int[]{2, 4, 6}, values -> {
            if (!capacity(paymentCount, payments, "Payment")) return;
            TaxAssessment assessment = findAssessment(v(values,1));
            if (assessment == null) {
                registerMissingPrerequisite("assessment");
                throw new PrerequisiteMissingException("Assessment is not registered; values retained for retry.");
            }
            if (!"CONFIRMED".equals(assessment.getStatus()))
                throw new InvalidStatusTransitionException("Payments require a CONFIRMED assessment.");
            for (int i=0;i<paymentCount;i++) {
                if (payments[i].getId().equalsIgnoreCase(v(values,0)))
                    throw new DuplicateRecordException("Payment ID already exists.");
                if (payments[i].getPaymentReference().equalsIgnoreCase(v(values,2)))
                    throw new DuplicateRecordException("Payment reference already exists globally.");
            }
            BigDecimal amount = money(values,3);
            BigDecimal balance = outstanding(assessment);
            if (amount.signum() <= 0) throw new ValidationException("Payment amount must be greater than zero.");
            if (amount.compareTo(balance) > 0)
                throw new OverpaymentException("Payment exceeds outstanding balance " + balance + "; no record was saved.");
            TaxPayment payment = new TaxPayment(v(values,0), assessment, v(values,2), amount, date(values,4), v(values,5));
            payment.validate();
            if (!payment.getPaymentDate().isAfter(LocalDate.now()))
                payments[paymentCount++] = payment;
            else throw new ValidationException("Payment date cannot be in the future.");
            System.out.println("Saved: " + payment.getSummary() + ", remaining balance=" + outstanding(assessment));
        });
    }

    private static void recordPenalty() {
        Field[] fields = {
            f("Penalty ID", "String, unique, 2-30 chars"), f("Obligation ID", "String, late filing obligation"),
            f("Penalty type", "String, unique per obligation; LATE_FILING only"),
            f("Base amount", "BigDecimal, verified declaration tax"),
            f("Penalty rate", "BigDecimal, fixed illustrative 5.00%"),
            f("Penalty amount", "BigDecimal, base x rate / 100, HALF_UP")
        };
        recordForm("CALCULATE / RECORD PENALTY", fields, new int[]{2, 4, 6}, values -> {
            if (!capacity(penaltyCount, penalties, "Penalty")) return;
            TaxObligation obligation = findObligation(v(values,1));
            if (obligation == null) {
                registerMissingPrerequisite("obligation");
                throw new PrerequisiteMissingException("Obligation is not registered; values retained for retry.");
            }
            TaxDeclaration lateDeclaration = null;
            for (int i=0;i<declarationCount;i++) {
                if (declarations[i].getObligation().getId().equals(obligation.getId())
                        && "LATE".equals(declarations[i].getStatus())) lateDeclaration = declarations[i];
            }
            if (lateDeclaration == null) throw new ValidationException("No late declaration exists for this obligation.");
            if (!"LATE_FILING".equalsIgnoreCase(v(values,2)))
                throw new ValidationException("Penalty type must be LATE_FILING.");
            for (int i=0;i<penaltyCount;i++) {
                if (penalties[i].getId().equalsIgnoreCase(v(values,0)))
                    throw new DuplicateRecordException("Penalty ID already exists.");
                if (penalties[i].getObligation().getId().equals(obligation.getId())
                        && penalties[i].getPenaltyType().equalsIgnoreCase(v(values,2)))
                    throw new DuplicateRecordException("Penalty type already exists for this obligation.");
            }
            BigDecimal base = lateDeclaration.getDeclaredTax(); // Assumption: late-filing base is the verified tax due.
            BigDecimal rate = LATE_RATE;
            BigDecimal expected = base.multiply(rate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            if (money(values,3).compareTo(base) != 0 || money(values,4).compareTo(rate) != 0
                    || money(values,5).compareTo(expected) != 0)
                throw new ValidationException("Penalty values must equal base " + base + ", rate 5.00%, and calculated amount " + expected + ".");
            Penalty penalty = new Penalty(v(values,0), obligation, "LATE_FILING", base, rate, expected);
            penalty.validate();
            penalties[penaltyCount++] = penalty;
            System.out.println("Saved: " + penalty.getSummary());
        });
    }

    private static void manageRefund() {
        System.out.println("[1] Request refund  [2] Approve pending refund  [0] Cancel");
        String choice = SCANNER.nextLine().trim();
        if (choice.equals("0")) return;
        if (choice.equals("2")) {
            try {
                approveRefund();
            } catch (RecordNotFoundException | ValidationException | UnauthorizedApprovalException | OverpaymentException
                     | InvalidStatusTransitionException ex) {
                System.out.println("Refund not approved: " + ex.getMessage());
            }
            return;
        }
        if (!choice.equals("1")) {
            System.out.println("Choose 1, 2, or 0.");
            return;
        }
        Field[] fields = {
            f("Refund ID", "String, unique, 2-30 chars"), f("Taxpayer ID or TIN", "String, registered taxpayer"),
            f("Assessment ID", "String, verified overpayment assessment"),
            f("Requested amount", "BigDecimal, positive and <= verified overpayment"),
            f("Request date", "LocalDate, YYYY-MM-DD, not future"), f("Status", "String, PENDING")
        };
        recordForm("REQUEST TAX REFUND", fields, new int[]{2, 4, 6}, values -> {
            if (!capacity(refundCount, refunds, "Refund")) return;
            RegisteredTaxpayer taxpayer = findTaxpayer(v(values,1));
            if (taxpayer == null) {
                registerMissingPrerequisite("taxpayer");
                throw new PrerequisiteMissingException("Taxpayer is not registered; values retained for retry.");
            }
            TaxAssessment assessment = findAssessment(v(values,2));
            if (assessment == null) {
                registerMissingPrerequisite("assessment");
                throw new PrerequisiteMissingException("Assessment is not registered; values retained for retry.");
            }
            if (!assessment.getDeclaration().getObligation().getTaxpayer().getTin().equals(taxpayer.getTin()))
                throw new ValidationException("Refund taxpayer must own the assessment.");
            for (int i=0;i<refundCount;i++) {
                if (refunds[i].getId().equalsIgnoreCase(v(values,0)))
                    throw new DuplicateRecordException("Refund ID already exists.");
                if (refunds[i].getAssessment() == assessment && refunds[i].isActive())
                    throw new DuplicateRecordException("A pending refund already exists for this assessment.");
            }
            BigDecimal overpayment = outstanding(assessment).negate().max(ZERO);
            if (overpayment.signum() <= 0)
                throw new OverpaymentException("No verified overpayment exists for assessment " + assessment.getId() + ".");
            BigDecimal amount = money(values,3);
            if (amount.compareTo(overpayment) > 0)
                throw new OverpaymentException("Request exceeds verified overpayment of " + overpayment + ".");
            TaxRefund refund = new TaxRefund(v(values,0), taxpayer, assessment, amount, date(values,4), "PENDING");
            refund.validate();
            refunds[refundCount++] = refund;
            System.out.println("Saved pending request: " + refund.getSummary());
        });
    }

    private static void approveRefund() throws RecordNotFoundException, ValidationException,
            UnauthorizedApprovalException, OverpaymentException, InvalidStatusTransitionException {
        String id = readRequired("Pending refund ID: ");
        TaxRefund refund = findRefund(id);
        if (refund == null) throw new RecordNotFoundException("Refund not found: " + id);
        if (!"PENDING".equals(refund.getStatus()))
            throw new InvalidStatusTransitionException("Only PENDING refunds can be approved.");
        refund.validate();
        RevenueOfficer officer = promptForOfficer();
        if (officer == null || !officer.isActive())
            throw new UnauthorizedApprovalException("An active revenue officer is required.");
        if (refund.getRequestedAmount().compareTo(officer.getApprovalLimit()) > 0)
            throw new UnauthorizedApprovalException("Refund amount exceeds this officer's approval limit.");
        BigDecimal overpayment = outstanding(refund.getAssessment()).negate().max(ZERO);
        if (refund.getRequestedAmount().compareTo(overpayment) > 0)
            throw new OverpaymentException("The supporting verified overpayment no longer covers this refund.");
        refund.setStatus("APPROVED");
        refund.validate();
        System.out.println("Refund approved by " + officer.getDisplayName() + ".");
    }

    private static void issueClearance() {
        Field[] fields = {
            f("Clearance ID", "String, unique, 2-30 chars"), f("Taxpayer ID or TIN", "String, active taxpayer"),
            f("Request date", "LocalDate, YYYY-MM-DD, not future"), f("Expiry date", "LocalDate, after request date"),
            f("Status", "String, PENDING at request"), f("Approving officer ID", "String, active officer ID/staff no")
        };
        recordForm("REQUEST / ISSUE TAX CLEARANCE", fields, new int[]{2, 4, 6}, values -> {
            if (!capacity(clearanceCount, clearances, "Tax clearance")) return;
            RegisteredTaxpayer taxpayer = findTaxpayer(v(values,1));
            if (taxpayer == null) {
                registerMissingPrerequisite("taxpayer");
                throw new PrerequisiteMissingException("Taxpayer is not registered; values retained for retry.");
            }
            if (!taxpayer.isActive()) throw new ValidationException("Only an active taxpayer may receive clearance.");
            LocalDate requestDate = date(values,2);
            LocalDate expiryDate = date(values,3);
            if (!expiryDate.isAfter(requestDate)) throw new ValidationException("Expiry date must be after request date.");
            for (int i=0;i<clearanceCount;i++)
                if (clearances[i].getId().equalsIgnoreCase(v(values,0)))
                    throw new DuplicateRecordException("Clearance ID already exists.");
            RevenueOfficer officer = findOfficer(v(values,5));
            if (officer == null) {
                registerMissingPrerequisite("revenue officer");
                throw new PrerequisiteMissingException("Approving officer is not registered; values retained for retry.");
            }
            if (!officer.isActive()) throw new UnauthorizedApprovalException("Approving officer must be active.");
            TaxClearance clearance = new TaxClearance(v(values,0), taxpayer, requestDate, expiryDate, "PENDING", null);
            clearance.validate();
            String refusal = clearanceEligibility(taxpayer);
            if (refusal != null) {
                clearance.setStatus("REJECTED");
                System.out.println("Clearance denied: " + refusal);
            } else {
                clearance.setStatus("APPROVED");
                clearance.setApprovedBy(officer);
                System.out.println("Clearance approved by " + officer.getDisplayName() + ".");
            }
            clearance.validate();
            clearances[clearanceCount++] = clearance;
            System.out.println("Saved: " + clearance.getSummary());
        });
    }

    private static void searchRecord() {
        String query = readRequired("Enter record ID, TIN, or tax code: ");
        boolean found = false;
        for (int i=0;i<individualCount;i++) if (matches(individuals[i], query)) { individuals[i].printDetails(); found=true; }
        for (int i=0;i<corporateCount;i++) if (matches(corporates[i], query)) { corporates[i].printDetails(); found=true; }
        for (int i=0;i<officerCount;i++) if (matches(officers[i], query)) { officers[i].printDetails(); found=true; }
        for (int i=0;i<taxTypeCount;i++) if (matches(taxTypes[i], query) || taxTypes[i].getTaxCode().equalsIgnoreCase(query)) { taxTypes[i].printDetails(); found=true; }
        for (int i=0;i<obligationCount;i++) if (matches(obligations[i], query)) { obligations[i].printDetails(); found=true; }
        for (int i=0;i<declarationCount;i++) if (matches(declarations[i], query)) { declarations[i].printDetails(); found=true; }
        for (int i=0;i<assessmentCount;i++) if (matches(assessments[i], query)) { assessments[i].printDetails(); found=true; }
        for (int i=0;i<paymentCount;i++) if (matches(payments[i], query) || payments[i].getPaymentReference().equalsIgnoreCase(query)) { payments[i].printDetails(); found=true; }
        for (int i=0;i<penaltyCount;i++) if (matches(penalties[i], query)) { penalties[i].printDetails(); found=true; }
        for (int i=0;i<refundCount;i++) if (matches(refunds[i], query)) { refunds[i].printDetails(); found=true; }
        for (int i=0;i<clearanceCount;i++) if (matches(clearances[i], query)) { clearances[i].printDetails(); found=true; }
        if (!found) System.out.println("No record matched \"" + query + "\".");
    }

    private static boolean matches(Identifiable record, String query) {
        return record.getId().equalsIgnoreCase(query);
    }

    private static void updateRecord() throws RecordNotFoundException, ValidationException,
            InvalidStatusTransitionException {
        System.out.println("Eligible updates: taxpayer/officer/tax-type status, declaration rejection, draft-assessment cancellation.");
        String id = readRequired("Record ID / TIN / tax code: ");
        TaxDeclaration declaration = findDeclaration(id);
        if (declaration != null) {
            if (!"FILED".equals(declaration.getStatus()) && !"LATE".equals(declaration.getStatus()))
                throw new InvalidStatusTransitionException("Only a filed or late declaration may be rejected.");
            for (int i=0;i<assessmentCount;i++)
                if (assessments[i].getDeclaration() == declaration)
                    throw new InvalidStatusTransitionException("A declaration with an assessment cannot be rejected.");
            declaration.setStatus("REJECTED");
            declaration.validate();
            System.out.println("Declaration " + id + " rejected; it can no longer be assessed.");
            return;
        }
        TaxAssessment assessment = findAssessment(id);
        if (assessment != null) {
            if (!"DRAFT".equals(assessment.getStatus()))
                throw new InvalidStatusTransitionException("Only a DRAFT assessment may be cancelled.");
            assessment.setStatus("CANCELLED");
            assessment.validate();
            System.out.println("Draft assessment " + id + " cancelled.");
            return;
        }
        RegisteredTaxpayer taxpayer = findTaxpayer(id);
        if (taxpayer != null) {
            String status = readChoice("New taxpayer status (ACTIVE/INACTIVE): ", "ACTIVE", "INACTIVE");
            taxpayer.setTaxpayerStatus(status);
            taxpayer.validate();
            System.out.println("Updated " + taxpayer.getDisplayName() + " status to " + status + ".");
            return;
        }
        RevenueOfficer officer = findOfficer(id);
        if (officer != null) {
            officer.setActive(readChoice("Set officer active? (YES/NO): ", "YES", "NO").equals("YES"));
            officer.validate();
            System.out.println("Officer status updated.");
            return;
        }
        TaxType type = findTaxType(id);
        if (type != null) {
            System.out.println("[1] Update rate [2] Toggle active");
            String choice = SCANNER.nextLine().trim();
            if (choice.equals("1")) {
                BigDecimal rate = readDecimal("New fictional rate (0-100): ", ZERO, new BigDecimal("100"));
                type.setRate(rate);
            } else if (choice.equals("2")) {
                type.setActive(!type.isActive());
            } else {
                System.out.println("No changes made.");
                return;
            }
            type.validate();
            System.out.println("Updated: " + type.getSummary());
            return;
        }
        throw new RecordNotFoundException("No eligible record found for " + id + ".");
    }

    private static void viewStatement() throws RecordNotFoundException {
        RegisteredTaxpayer taxpayer = findTaxpayer(readRequired("Taxpayer ID or TIN: "));
        if (taxpayer == null) throw new RecordNotFoundException("Taxpayer not found.");
        System.out.println("\nSTATEMENT: " + taxpayer.getDisplayName() + " / TIN " + taxpayer.getTin());
        for (int i=0;i<obligationCount;i++) if (sameTaxpayer(obligations[i].getTaxpayer(), taxpayer)) {
            System.out.println("  " + obligations[i].getSummary());
            for (int d=0;d<declarationCount;d++) if (declarations[d].getObligation()==obligations[i])
                System.out.println("    " + declarations[d].getSummary());
            for (int a=0;a<assessmentCount;a++) if (assessments[a].getDeclaration().getObligation()==obligations[i]) {
                System.out.println("    " + assessments[a].getSummary() + ", balance=" + outstanding(assessments[a]));
                for (int p=0;p<paymentCount;p++) if (payments[p].getAssessment()==assessments[a])
                    System.out.println("      " + payments[p].getSummary());
            }
            for (int p=0;p<penaltyCount;p++) if (penalties[p].getObligation()==obligations[i])
                System.out.println("    " + penalties[p].getSummary());
        }
        for (int r=0;r<refundCount;r++) if (sameTaxpayer(refunds[r].getTaxpayer(), taxpayer))
            System.out.println("  " + refunds[r].getSummary());
        for (int c=0;c<clearanceCount;c++) if (sameTaxpayer(clearances[c].getTaxpayer(), taxpayer))
            System.out.println("  " + clearances[c].getSummary());
    }

    private static void viewOutstandingBalances() {
        if (assessmentCount == 0) {
            System.out.println("No assessments exist.");
            return;
        }
        for (int i=0;i<assessmentCount;i++)
            System.out.println(assessments[i].getId() + " / "
                    + assessments[i].getDeclaration().getObligation().getTaxpayer().getDisplayName()
                    + " : " + outstanding(assessments[i]));
    }

    private static void viewTaxReports() {
        BigDecimal assessedTotal = ZERO;
        BigDecimal paidTotal = ZERO;
        BigDecimal penaltyTotal = ZERO;
        BigDecimal outstandingTotal = ZERO;
        for (int i=0;i<assessmentCount;i++) {
            assessedTotal = assessedTotal.add(assessments[i].getTotal());
            outstandingTotal = outstandingTotal.add(outstanding(assessments[i]));
        }
        for (int i=0;i<paymentCount;i++) paidTotal = paidTotal.add(payments[i].getAmount());
        for (int i=0;i<penaltyCount;i++) penaltyTotal = penaltyTotal.add(penalties[i].getPenaltyAmount());
        System.out.println("\nTAX REPORT (fictional amounts)");
        System.out.println("Taxpayers: " + (individualCount + corporateCount) + " (individual "
                + individualCount + ", corporate " + corporateCount + ")");
        System.out.println("Tax types / obligations / declarations: " + taxTypeCount + " / " + obligationCount + " / " + declarationCount);
        System.out.println("Assessments: " + assessmentCount + ", gross assessed=" + assessedTotal.setScale(2, RoundingMode.HALF_UP));
        System.out.println("Payments: " + paymentCount + ", total received=" + paidTotal.setScale(2, RoundingMode.HALF_UP));
        System.out.println("Penalties: " + penaltyCount + ", total=" + penaltyTotal.setScale(2, RoundingMode.HALF_UP));
        System.out.println("Outstanding balance: " + outstandingTotal.setScale(2, RoundingMode.HALF_UP));
        System.out.println("Refund requests: " + refundCount + ", clearances: " + clearanceCount);
    }

    private static void viewAllRecords() {
        printArray(individuals, individualCount);
        printArray(corporates, corporateCount);
        printArray(officers, officerCount);
        printArray(taxTypes, taxTypeCount);
        printArray(obligations, obligationCount);
        printArray(declarations, declarationCount);
        printArray(assessments, assessmentCount);
        printArray(payments, paymentCount);
        printArray(penalties, penaltyCount);
        printArray(refunds, refundCount);
        printArray(clearances, clearanceCount);
    }

    private static void printArray(Identifiable[] records, int count) {
        for (int i=0;i<count;i++) ((com.auca.interfaces.Validatable) records[i]).printDetails();
    }

    private static void viewDeclarationHistory() {
        if (declarationCount == 0) System.out.println("No declarations exist.");
        for (int i=0;i<declarationCount;i++) declarations[i].printDetails();
    }

    private static void viewAssessmentHistory() {
        if (assessmentCount == 0) System.out.println("No assessments exist.");
        for (int i=0;i<assessmentCount;i++) assessments[i].printDetails();
    }

    private static void demonstratePolymorphism() {
        // A common abstract type dispatches to taxpayer- and officer-specific overrides at runtime.
        TaxParty[] parties = new TaxParty[individualCount + corporateCount + officerCount];
        int next = 0;
        for (int i=0;i<individualCount;i++) parties[next++] = individuals[i];
        for (int i=0;i<corporateCount;i++) parties[next++] = corporates[i];
        for (int i=0;i<officerCount;i++) parties[next++] = officers[i];
        System.out.println("TaxParty[] runtime dispatch:");
        for (int i=0;i<next;i++) System.out.println(parties[i].getDisplayName() + " | " + parties[i].getSummary());

        // Identifiable[] holds unrelated concrete entities and dispatches interface methods dynamically.
        Identifiable[] records = new Identifiable[individualCount + corporateCount + officerCount + taxTypeCount
                + obligationCount + declarationCount + assessmentCount + paymentCount + penaltyCount + refundCount + clearanceCount];
        next = 0;
        for (int i=0;i<individualCount;i++) records[next++] = individuals[i];
        for (int i=0;i<corporateCount;i++) records[next++] = corporates[i];
        for (int i=0;i<officerCount;i++) records[next++] = officers[i];
        for (int i=0;i<taxTypeCount;i++) records[next++] = taxTypes[i];
        for (int i=0;i<obligationCount;i++) records[next++] = obligations[i];
        for (int i=0;i<declarationCount;i++) records[next++] = declarations[i];
        for (int i=0;i<assessmentCount;i++) records[next++] = assessments[i];
        for (int i=0;i<paymentCount;i++) records[next++] = payments[i];
        for (int i=0;i<penaltyCount;i++) records[next++] = penalties[i];
        for (int i=0;i<refundCount;i++) records[next++] = refunds[i];
        for (int i=0;i<clearanceCount;i++) records[next++] = clearances[i];
        System.out.println("Identifiable[] runtime dispatch:");
        for (int i=0;i<next;i++)
            System.out.println(records[i].getId() + " | " + records[i].getDisplayName() + " | active=" + records[i].isActive());
    }

    private static BigDecimal outstanding(TaxAssessment assessment) {
        if (!"CONFIRMED".equals(assessment.getStatus())) return ZERO;
        BigDecimal balance = assessment.getTotal();
        TaxObligation obligation = assessment.getDeclaration().getObligation();
        for (int i=0;i<penaltyCount;i++)
            if (penalties[i].getObligation() == obligation) balance = balance.add(penalties[i].getPenaltyAmount());
        for (int i=0;i<paymentCount;i++)
            if (payments[i].getAssessment() == assessment) balance = balance.subtract(payments[i].getAmount());
        return balance.setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal obligationBalance(TaxObligation obligation) {
        BigDecimal total = ZERO;
        for (int i=0;i<assessmentCount;i++)
            if (assessments[i].getDeclaration().getObligation() == obligation
                    && "CONFIRMED".equals(assessments[i].getStatus()))
                total = total.add(assessments[i].getTotal());
        for (int i=0;i<penaltyCount;i++)
            if (penalties[i].getObligation() == obligation) total = total.add(penalties[i].getPenaltyAmount());
        for (int i=0;i<paymentCount;i++)
            if (payments[i].getAssessment().getDeclaration().getObligation() == obligation)
                total = total.subtract(payments[i].getAmount());
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    private static String clearanceEligibility(RegisteredTaxpayer taxpayer) {
        if (!taxpayer.isActive()) return "taxpayer is inactive.";
        for (int i=0;i<obligationCount;i++) {
            TaxObligation obligation = obligations[i];
            if (sameTaxpayer(obligation.getTaxpayer(), taxpayer)
                    && obligation.getDueDate().isBefore(LocalDate.now())) {
                boolean hasDeclaration = false;
                for (int d=0;d<declarationCount;d++)
                    if (declarations[d].getObligation() == obligation) hasDeclaration = true;
                if (!hasDeclaration || obligationBalance(obligation).signum() > 0)
                    return "overdue unpaid obligation " + obligation.getId() + " has balance "
                            + obligationBalance(obligation) + " or no filed declaration.";
            }
        }
        for (int i=0;i<declarationCount;i++) {
            TaxDeclaration declaration = declarations[i];
            if (!sameTaxpayer(declaration.getObligation().getTaxpayer(), taxpayer)) continue;
            boolean confirmed = false;
            for (int a=0;a<assessmentCount;a++)
                if (assessments[a].getDeclaration() == declaration && "CONFIRMED".equals(assessments[a].getStatus()))
                    confirmed = true;
            
            if (!confirmed) return "declaration " + declaration.getId() + " has no confirmed assessment.";
        }
        return null;
    }

    private static boolean sameTaxpayer(RegisteredTaxpayer first, RegisteredTaxpayer second) {
        return first != null && second != null && first.getTin().equals(second.getTin());
    }

    private static RegisteredTaxpayer findTaxpayer(String key) {
        for (int i=0;i<individualCount;i++)
            if (individuals[i].getTin().equalsIgnoreCase(key) || individuals[i].getPartyId().equalsIgnoreCase(key))
                return individuals[i];
        for (int i=0;i<corporateCount;i++)
            if (corporates[i].getTin().equalsIgnoreCase(key) || corporates[i].getPartyId().equalsIgnoreCase(key))
                return corporates[i];
        return null;
    }

    private static RevenueOfficer findOfficer(String key) {
        for (int i=0;i<officerCount;i++)
            if (officers[i].getPartyId().equalsIgnoreCase(key) || officers[i].getStaffNumber().equalsIgnoreCase(key))
                return officers[i];
        return null;
    }

    private static TaxType findTaxType(String key) {
        for (int i=0;i<taxTypeCount;i++)
            if (taxTypes[i].getId().equalsIgnoreCase(key) || taxTypes[i].getTaxCode().equalsIgnoreCase(key))
                return taxTypes[i];
        return null;
    }

    private static TaxObligation findObligation(String key) {
        for (int i=0;i<obligationCount;i++) if (obligations[i].getId().equalsIgnoreCase(key)) return obligations[i];
        return null;
    }

    private static TaxDeclaration findDeclaration(String key) {
        for (int i=0;i<declarationCount;i++) if (declarations[i].getId().equalsIgnoreCase(key)) return declarations[i];
        return null;
    }

    private static TaxAssessment findAssessment(String key) {
        for (int i=0;i<assessmentCount;i++) if (assessments[i].getId().equalsIgnoreCase(key)) return assessments[i];
        return null;
    }

    private static TaxRefund findRefund(String key) {
        for (int i=0;i<refundCount;i++) if (refunds[i].getId().equalsIgnoreCase(key)) return refunds[i];
        return null;
    }

    private static void checkPartyId(String partyId) throws DuplicateRecordException {
        for (int i=0;i<individualCount;i++)
            if (individuals[i].getPartyId().equalsIgnoreCase(partyId)) throw new DuplicateRecordException("Party ID already exists.");
        for (int i=0;i<corporateCount;i++)
            if (corporates[i].getPartyId().equalsIgnoreCase(partyId)) throw new DuplicateRecordException("Party ID already exists.");
        for (int i=0;i<officerCount;i++)
            if (officers[i].getPartyId().equalsIgnoreCase(partyId)) throw new DuplicateRecordException("Party ID already exists.");
    }

    private static void checkTaxpayerUniqueness(String tin, String nationalId, String companyNumber)
            throws DuplicateRecordException {
        for (int i=0;i<individualCount;i++) {
            if (individuals[i].getTin().equals(tin)) throw new DuplicateRecordException("TIN already exists across taxpayer types.");
            if (nationalId != null && individuals[i].getNationalId().equals(nationalId))
                throw new DuplicateRecordException("National ID already exists.");
        }
        for (int i=0;i<corporateCount;i++) {
            if (corporates[i].getTin().equals(tin)) throw new DuplicateRecordException("TIN already exists across taxpayer types.");
            if (companyNumber != null && corporates[i].getCompanyRegistrationNo().equals(companyNumber))
                throw new DuplicateRecordException("Company registration number already exists.");
        }
    }

    private static void registerMissingPrerequisite(String prerequisite) throws PrerequisiteMissingException {
        System.out.println("Missing prerequisite: " + prerequisite + ".");
        System.out.print("[1] Register prerequisite now  [0] Cancel: ");
        String answer = SCANNER.nextLine().trim();
        if (answer.equals("1")) {
            if (prerequisite.equals("taxpayer")) {
                System.out.print("[1] Individual taxpayer  [2] Corporate taxpayer  [0] Cancel: ");
                String kind = SCANNER.nextLine().trim();
                if (kind.equals("1")) registerIndividual();
                else if (kind.equals("2")) registerCorporate();
                else throw new PrerequisiteMissingException("Prerequisite registration cancelled.");
            } else if (prerequisite.equals("tax type")) {
                registerTaxType();
            } else if (prerequisite.equals("revenue officer")) {
                registerOfficer();
            } else if (prerequisite.equals("obligation")) {
                createObligation();
            } else if (prerequisite.equals("declaration")) {
                fileDeclaration();
            } else if (prerequisite.equals("assessment")) {
                assessDeclaration();
            } else {
                throw new PrerequisiteMissingException("No registration workflow for " + prerequisite + ".");
            }
            return;
        }
        throw new PrerequisiteMissingException("Prerequisite registration was cancelled.");
    }

    private static RevenueOfficer promptForOfficer() {
        if (officerCount == 0) {
            System.out.println("No revenue officers are registered.");
            return null;
        }
        String key = readRequired("Officer party ID or staff number: ");
        RevenueOfficer officer = findOfficer(key);
        if (officer == null) System.out.println("Officer not found.");
        return officer;
    }

    private static String readRequired(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = SCANNER.nextLine().trim();
            if (!value.isEmpty()) return value;
            System.out.println("This value is required.");
        }
    }

    private static String readChoice(String prompt, String first, String second) {
        while (true) {
            String value = readRequired(prompt).toUpperCase();
            if (value.equals(first) || value.equals(second)) return value;
            System.out.println("Choose " + first + " or " + second + ".");
        }
    }

    private static BigDecimal readDecimal(String prompt, BigDecimal minimum, BigDecimal maximum) {
        while (true) {
            try {
                BigDecimal value = new BigDecimal(readRequired(prompt));
                if (value.scale() <= 2 && value.compareTo(minimum) >= 0 && value.compareTo(maximum) <= 0)
                    return value.setScale(2, RoundingMode.UNNECESSARY);
                System.out.println("Enter a value from " + minimum + " to " + maximum + " with at most 2 decimals.");
            } catch (NumberFormatException | ArithmeticException ex) {
                System.out.println("Enter a valid decimal number with at most 2 decimals.");
            }
        }
    }
}
