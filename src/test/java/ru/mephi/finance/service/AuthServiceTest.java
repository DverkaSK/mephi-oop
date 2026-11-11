package ru.mephi.finance.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.mephi.finance.domain.User;
import ru.mephi.finance.domain.error.AuthenticationException;
import ru.mephi.finance.domain.error.ValidationException;
import ru.mephi.finance.infra.security.Pbkdf2PasswordHasher;
import ru.mephi.finance.support.InMemoryUserRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class AuthServiceTest {

    private final AuthService auth = new AuthService(new InMemoryUserRepository(), new Pbkdf2PasswordHasher(1_000));

    @Test
    public void registeredUserCanAuthenticate() {
        User registered = auth.register("alice", "secret123");

        assertNotEquals("secret123", registered.passwordHash());
        assertEquals("alice", auth.authenticate("alice", "secret123").login());
    }

    @Test
    public void supportsSeveralUsers() {
        auth.register("alice", "secret123");
        auth.register("bob", "qwerty");

        assertEquals("bob", auth.authenticate("bob", "qwerty").login());
        assertThrows(AuthenticationException.class, () -> auth.authenticate("bob", "secret123"));
    }

    @Test
    public void wrongPasswordOrUnknownUserIsRejected() {
        auth.register("alice", "secret123");

        assertThrows(AuthenticationException.class, () -> auth.authenticate("alice", "wrong"));
        assertThrows(AuthenticationException.class, () -> auth.authenticate("nobody", "secret123"));
    }

    @Test
    public void duplicateLoginIsRejected() {
        auth.register("alice", "secret123");

        assertThrows(ValidationException.class, () -> auth.register("alice", "another"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ab", "имя", "with space", "a/b"})
    public void invalidLoginIsRejected(String login) {
        assertThrows(ValidationException.class, () -> auth.register(login, "secret123"));
    }

    @Test
    public void shortPasswordIsRejected() {
        assertThrows(ValidationException.class, () -> auth.register("alice", "123"));
    }
}
