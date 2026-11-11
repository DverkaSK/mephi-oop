package ru.mephi.finance.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class CliParsingTest {

    @Test
    public void tokenizerKeepsQuotedTextTogether() {
        assertEquals(List.of("expense", "Коммунальные услуги", "3000", "--note", ""),
                Tokenizer.tokenize("  expense \"Коммунальные услуги\"   3000 --note \"\""));
    }

    @Test
    public void tokenizerRejectsUnclosedQuote() {
        assertThrows(CommandException.class, () -> Tokenizer.tokenize("expense \"Еда 300"));
    }

    @Test
    public void argumentsSeparateOptionsFromPositional() {
        Arguments args = Arguments.parse(List.of("Еда", "--from", "2026-11-01", "Такси"), Set.of("from", "to"));

        assertEquals(List.of("Еда", "Такси"), args.positional());
        assertEquals(Optional.of("2026-11-01"), args.option("from"));
        assertEquals(Optional.empty(), args.option("to"));
    }

    @Test
    public void argumentsRejectUnknownDuplicateAndEmptyOptions() {
        assertThrows(CommandException.class, () -> Arguments.parse(List.of("--file", "x"), Set.of("from")));
        assertThrows(CommandException.class, () -> Arguments.parse(List.of("--from"), Set.of("from")));
        assertThrows(CommandException.class,
                () -> Arguments.parse(List.of("--from", "a", "--from", "b"), Set.of("from")));
    }

    @Test
    public void amountAcceptsCommaAsDecimalSeparator() {
        assertEquals(new BigDecimal("99.9"), InputParser.amount("99,9"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "-5", "10.555", "1e5", ""})
    public void invalidAmountsAreRejected(String value) {
        assertThrows(CommandException.class, () -> InputParser.amount(value));
    }

    @Test
    public void invalidDateIsRejected() {
        assertThrows(CommandException.class, () -> InputParser.date("01.11.2026"));
    }
}
