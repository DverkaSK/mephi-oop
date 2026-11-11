package ru.mephi.finance.domain.error;

public final class ValidationException extends FinanceException {

    private static final long serialVersionUID = 1L;

    public ValidationException(String message) {
        super(message);
    }
}
