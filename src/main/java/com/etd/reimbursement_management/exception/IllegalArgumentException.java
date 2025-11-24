package com.etd.reimbursement_management.exception;

import lombok.Getter;

@Getter
public class IllegalArgumentException extends RuntimeException {

    private final String fieldName;

    public IllegalArgumentException(String message, String fieldName) {
        super(message);
        this.fieldName = fieldName;
    }

}
