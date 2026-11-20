package com.auca.exceptions;

public class UnauthorizedApprovalException extends Exception {
    private static final long serialVersionUID = 1L;
    public UnauthorizedApprovalException(String message) { super(message); }
}
