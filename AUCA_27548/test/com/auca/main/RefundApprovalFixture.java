package com.auca.main;

import com.auca.abstracts.RegisteredTaxpayer;
import com.auca.exceptions.UnauthorizedApprovalException;
import com.auca.individualtaxpayer.IndividualTaxpayer;
import com.auca.revenueofficer.RevenueOfficer;
import com.auca.taxassessment.TaxAssessment;
import com.auca.taxdeclaration.TaxDeclaration;
import com.auca.taxobligation.TaxObligation;
import com.auca.taxpayment.TaxPayment;
import com.auca.taxrefund.TaxRefund;
import com.auca.taxtype.TaxType;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

public final class RefundApprovalFixture {
    private RefundApprovalFixture() { }

    public static void main(String[] args) throws Exception {
        System.setIn(new ByteArrayInputStream(("REFUND1\nOFF-INACTIVE\n"
                + "REFUND1\nOFF-LOW\nREFUND1\nOFF-GOOD\n").getBytes(StandardCharsets.UTF_8)));
        Class<?> main = Class.forName("com.auca.main.Main");

        RegisteredTaxpayer taxpayer = new IndividualTaxpayer("PARTY1", "Amina", "Demo", "demo@example.com",
                "07881234567", LocalDate.of(1980, 1, 1), "123456789", LocalDate.of(2020, 1, 1),
                "ACTIVE", "Kigali", "Services", "Office", "1234567890123456", "Teacher",
                "Demo Business", LocalDate.of(2010, 1, 1), new BigDecimal("1000.00"), true);
        TaxType type = new TaxType("TYPE1", "TST", "Fixture Tax", new BigDecimal("10.00"),
                "ANNUAL", true);
        TaxObligation obligation = new TaxObligation("OB1", taxpayer, type, LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 12, 31), LocalDate.of(2026, 1, 31));
        TaxDeclaration declaration = new TaxDeclaration("DEC1", obligation, LocalDate.of(2026, 1, 15),
                new BigDecimal("100.00"), new BigDecimal("10.00"), "FILED");
        TaxAssessment assessment = new TaxAssessment("ASM1", declaration, new BigDecimal("10.00"),
                new BigDecimal("0.00"), LocalDate.now(), "CONFIRMED");

        RevenueOfficer inactive = officer("OFF-INACTIVE", "STAFF-I", false, "100.00");
        RevenueOfficer lowLimit = officer("OFF-LOW", "STAFF-L", true, "4.00");
        RevenueOfficer approver = officer("OFF-GOOD", "STAFF-G", true, "10.00");
        // This fixture injects a verified legacy overpayment; production payments still obey the balance cap.
        TaxPayment fixtureOverpayment = new TaxPayment("PAY-FIXTURE", assessment, "REF-FIXTURE",
                new BigDecimal("15.00"), LocalDate.now(), "TEST FIXTURE");
        TaxRefund refund = new TaxRefund("REFUND1", taxpayer, assessment, new BigDecimal("5.00"),
                LocalDate.now(), "PENDING");

        put(main, "officers", new Object[]{inactive, lowLimit, approver});
        put(main, "officerCount", 3);
        put(main, "assessments", new Object[]{assessment});
        put(main, "assessmentCount", 1);
        put(main, "payments", new Object[]{fixtureOverpayment});
        put(main, "paymentCount", 1);
        put(main, "refunds", new Object[]{refund});
        put(main, "refundCount", 1);

        Method approve = main.getDeclaredMethod("approveRefund");
        approve.setAccessible(true);
        expectUnauthorized(approve, "inactive officer rejected");
        expectUnauthorized(approve, "below-limit officer rejected");
        approve.invoke(null);
        if (!"APPROVED".equals(refund.getStatus()))
            throw new AssertionError("Adequate active officer did not approve the verified refund.");
        System.out.println("PASS authorized officer approved fixture overpayment refund");
    }

    private static RevenueOfficer officer(String id, String staff, boolean active, String limit) {
        return new RevenueOfficer(id, "Revenue", "Officer", id.toLowerCase() + "@example.com",
                "07881234568", LocalDate.of(1980, 1, 1), staff, "Revenue", "Approver",
                LocalDate.of(2020, 1, 1), new BigDecimal(limit), active);
    }

    private static void expectUnauthorized(Method approve, String message) throws Exception {
        try {
            approve.invoke(null);
        } catch (InvocationTargetException ex) {
            if (ex.getCause() instanceof UnauthorizedApprovalException) {
                System.out.println("PASS " + message);
                return;
            }
            throw ex;
        }
        throw new AssertionError(message + ": operation unexpectedly succeeded.");
    }

    private static void put(Class<?> main, String name, Object[] objects) throws ReflectiveOperationException {
        Field field = main.getDeclaredField(name);
        field.setAccessible(true);
        Object array = field.get(null);
        for (int i = 0; i < objects.length; i++) Array.set(array, i, objects[i]);
    }

    private static void put(Class<?> main, String name, int value) throws ReflectiveOperationException {
        Field field = main.getDeclaredField(name);
        field.setAccessible(true);
        field.setInt(null, value);
    }
}
