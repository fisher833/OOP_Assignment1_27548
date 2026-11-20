package com.auca.exceptions;

public class InvalidTaxCalculationException extends Exception {
    private static final long serialVersionUID = 1L;
    public InvalidTaxCalculationException(String message) { super(message); }
}
