package ru.mephi.finance.service.format;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public final class MoneyFormat {

    private static final ThreadLocal<DecimalFormat> FORMAT = ThreadLocal.withInitial(
            () -> new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US)));

    private MoneyFormat() {
    }

    public static String format(BigDecimal value) {
        return FORMAT.get().format(value);
    }
}
