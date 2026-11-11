package ru.mephi.finance.infra.codec;

import ru.mephi.finance.domain.Operation;
import ru.mephi.finance.domain.OperationType;
import ru.mephi.finance.domain.WalletSnapshot;
import ru.mephi.finance.domain.error.StorageException;
import ru.mephi.finance.infra.FileSupport;
import ru.mephi.finance.service.port.WalletCodec;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class CsvWalletCodec implements WalletCodec {

    private static final List<String> HEADER = List.of("kind", "id", "type", "category", "amount", "date", "note");
    private static final String CATEGORY = "category";
    private static final String BUDGET = "budget";
    private static final String OPERATION = "operation";
    private static final String OWNER = "owner";

    @Override
    public String format() {
        return "csv";
    }

    @Override
    public void write(WalletSnapshot snapshot, Path file) {
        Stream<List<String>> owner = Stream.of(List.of(OWNER, "", "", snapshot.owner(), "", "", ""));
        Stream<List<String>> categories = snapshot.categories().stream()
                .map(category -> List.of(CATEGORY, "", "", category, "", "", ""));
        Stream<List<String>> budgets = snapshot.budgets().entrySet().stream()
                .map(entry -> List.of(BUDGET, "", "", entry.getKey(), entry.getValue().toPlainString(), "", ""));
        Stream<List<String>> operations = snapshot.operations().stream()
                .map(operation -> List.of(
                        OPERATION,
                        operation.id().toString(),
                        operation.type().name(),
                        operation.category(),
                        operation.amount().toPlainString(),
                        operation.date().toString(),
                        operation.note()));
        List<String> lines = Stream.of(Stream.of(HEADER), owner, categories, budgets, operations)
                .flatMap(Function.identity())
                .map(this::toLine)
                .toList();
        try {
            FileSupport.createParent(file);
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new StorageException("Не удалось записать файл " + file.toAbsolutePath(), e);
        }
    }

    @Override
    public WalletSnapshot read(Path file) {
        List<String> lines;
        try {
            lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new StorageException("Не удалось прочитать файл " + file.toAbsolutePath(), e);
        }
        try {
            List<List<String>> rows = lines.stream()
                    .skip(1)
                    .filter(line -> !line.isBlank())
                    .map(this::parseLine)
                    .toList();
            String owner = rows.stream()
                    .filter(row -> row.getFirst().equals(OWNER))
                    .map(row -> row.get(3))
                    .findFirst()
                    .orElse("imported");
            List<String> categories = rows.stream()
                    .filter(row -> row.getFirst().equals(CATEGORY))
                    .map(row -> row.get(3))
                    .toList();
            Map<String, BigDecimal> budgets = rows.stream()
                    .filter(row -> row.getFirst().equals(BUDGET))
                    .collect(Collectors.toMap(
                            row -> row.get(3), row -> new BigDecimal(row.get(4)), (a, b) -> b, LinkedHashMap::new));
            List<Operation> operations = rows.stream()
                    .filter(row -> row.getFirst().equals(OPERATION))
                    .map(row -> new Operation(
                            UUID.fromString(row.get(1)),
                            OperationType.valueOf(row.get(2)),
                            row.get(3),
                            new BigDecimal(row.get(4)),
                            LocalDate.parse(row.get(5)),
                            row.get(6)))
                    .toList();
            return new WalletSnapshot(owner, categories, budgets, operations);
        } catch (RuntimeException e) {
            throw new StorageException(
                    "Файл %s повреждён или имеет неверный формат".formatted(file.toAbsolutePath()), e);
        }
    }

    private String toLine(List<String> cells) {
        return cells.stream().map(this::escape).collect(Collectors.joining(","));
    }

    private String escape(String value) {
        boolean quote = value.chars().anyMatch(ch -> ch == ',' || ch == '"' || ch == '\n' || ch == '\r');
        return quote ? "\"" + value.replace("\"", "\"\"") + "\"" : value;
    }

    private List<String> parseLine(String line) {
        List<String> cells = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (quoted && ch == '"' && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                current.append('"');
                i++;
            } else if (ch == '"') {
                quoted = !quoted;
            } else if (ch == ',' && !quoted) {
                cells.add(current.toString());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }
        cells.add(current.toString());
        if (cells.size() != HEADER.size()) {
            throw new IllegalArgumentException("Ожидалось %d колонок, найдено %d: %s"
                    .formatted(HEADER.size(), cells.size(), line));
        }
        return cells;
    }
}
