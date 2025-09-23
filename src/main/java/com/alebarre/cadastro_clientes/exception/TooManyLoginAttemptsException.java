package com.alebarre.cadastro_clientes.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TooManyLoginAttemptsException extends RuntimeException {
    private final int retryAfterSeconds;

    public TooManyLoginAttemptsException(int retryAfterSeconds) {
        super("Muitas tentativas de login. Tente novamente em " + retryAfterSeconds + " segundos.");
        this.retryAfterSeconds = retryAfterSeconds;
    }
}
