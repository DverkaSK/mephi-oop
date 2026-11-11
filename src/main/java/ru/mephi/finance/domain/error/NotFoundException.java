package ru.mephi.finance.domain.error;

public final class NotFoundException extends FinanceException {

    private static final long serialVersionUID = 1L;

    public NotFoundException(String message) {
        super(message);
    }
}
