package ru.mephi.finance.domain.error;

public final class StorageException extends FinanceException {

    private static final long serialVersionUID = 1L;

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
