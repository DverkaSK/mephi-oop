package ru.mephi.finance.domain;

import ru.mephi.finance.domain.error.ValidationException;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Money {

    public static final int SCALE = 2;
    public static final BigDecimal MAX_AMOUNT = new BigDecimal("1000000000000");

    private Money() {
    }

    public static BigDecimal zero() {
        return BigDecimal.ZERO.setScale(SCALE, RoundingMode.UNNECESSARY);
    }

    public static BigDecimal normalize(BigDecimal value) {
        if (value == null) {
            throw new ValidationException("Сумма не указана");
        }
        if (value.stripTrailingZeros().scale() > SCALE) {
            throw new ValidationException("Сумма может содержать не больше двух знаков после запятой");
        }
        if (value.abs().compareTo(MAX_AMOUNT) > 0) {
            throw new ValidationException("Сумма слишком большая, максимум " + MAX_AMOUNT.toPlainString());
        }
        return value.setScale(SCALE, RoundingMode.UNNECESSARY);
    }

    public static BigDecimal requirePositive(BigDecimal value) {
        BigDecimal normalized = normalize(value);
        if (normalized.signum() <= 0) {
            throw new ValidationException("Сумма должна быть больше нуля");
        }
        return normalized;
    }

    public static int percent(BigDecimal part, BigDecimal whole) {
        if (whole.signum() == 0) {
            return part.signum() > 0 ? Integer.MAX_VALUE : 0;
        }
        return part.multiply(BigDecimal.valueOf(100)).divide(whole, 0, RoundingMode.HALF_UP).intValueExact();
    }
}
