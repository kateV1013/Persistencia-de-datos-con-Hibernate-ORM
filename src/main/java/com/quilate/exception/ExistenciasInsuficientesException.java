package com.quilate.exception;

public class ExistenciasInsuficientesException extends RuntimeException {
    public ExistenciasInsuficientesException(String mensaje) {
        super(mensaje);
    }
}