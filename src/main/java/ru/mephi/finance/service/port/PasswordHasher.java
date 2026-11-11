package ru.mephi.finance.service.port;

public interface PasswordHasher {

    String newSalt();

    String hash(String password, String salt);
}
