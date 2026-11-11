package ru.mephi.finance.domain.error;

public final class AuthenticationException extends FinanceException {

    private static final long serialVersionUID = 1L;

    public AuthenticationException(String message) {
        super(message);
    }
}
