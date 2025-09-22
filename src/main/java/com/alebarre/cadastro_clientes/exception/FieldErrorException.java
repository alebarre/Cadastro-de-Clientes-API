package com.alebarre.cadastro_clientes.exception;

import java.util.Map;

public class FieldErrorException extends RuntimeException {
    private final Map<String,String> fieldErrors;

    public FieldErrorException(String message, Map<String,String> fieldErrors) {
        super(message);
        this.fieldErrors = fieldErrors;
    }
    public Map<String,String> getFieldErrors() { return fieldErrors; }

    public static FieldErrorException of(String message, String field, String error) {
        return new FieldErrorException(message, Map.of(field, error));
    }
}

