package com.iapex.exceptions;

public class InstitutionAlreadyExistsException extends RuntimeException {
    public InstitutionAlreadyExistsException(String message) {
        super(message);
    }
}