package com.iapex.exceptions;

@SuppressWarnings("serial")
public class InstitutionAlreadyExistsException extends RuntimeException {
    public InstitutionAlreadyExistsException(String message) {
        super(message);
    }
}