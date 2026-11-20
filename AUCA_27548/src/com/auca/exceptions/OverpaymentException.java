package com.auca.exceptions;

public class OverpaymentException extends Exception {
    private static final long serialVersionUID = 1L;
    public OverpaymentException(String message) { super(message); }
}
