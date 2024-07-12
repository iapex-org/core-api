package com.iapex.exceptions;

@SuppressWarnings("serial")
public class InstitutionNotFoundException extends RuntimeException {
    public InstitutionNotFoundException(String message) {
        super(message);
    }
}