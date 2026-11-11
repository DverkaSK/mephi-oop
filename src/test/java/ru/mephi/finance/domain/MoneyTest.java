package ru.mephi.finance.domain;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.mephi.finance.domain.error.ValidationException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class MoneyTest {

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "0.001", "1000000000000.01"})
    public void rejectsInvalidAmounts(String value) {
        assertThrows(ValidationException.class, () -> Money.requirePositive(new BigDecimal(value)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1", "1.5", "99.90", "1000000000000"})
    public void normalizesValidAmountsToTwoDigits(String value) {
        assertEquals(2, Money.requirePositive(new BigDecimal(value)).scale());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "-Еда", "12345678901234567890123456789012345678901"})
    public void rejectsInvalidCategoryNames(String value) {
        assertThrows(ValidationException.class, () -> CategoryName.validate(value));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Еда", "  Коммунальные   услуги  "})
    public void acceptsValidCategoryNames(String value) {
        assertEquals(value.strip().replaceAll("\\s+", " "), CategoryName.validate(value));
    }
}
