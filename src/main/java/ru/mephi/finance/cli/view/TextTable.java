package ru.mephi.finance.cli.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class TextTable {

    private static final String GAP = "  ";

    private final List<String> headers;
    private final Set<Integer> rightAligned = new HashSet<>();
    private final List<List<String>> rows = new ArrayList<>();

    private TextTable(List<String> headers) {
        this.headers = List.copyOf(headers);
    }

    public static TextTable of(String... headers) {
        return new TextTable(Arrays.asList(headers));
    }

    public TextTable alignRight(int... columns) {
        Arrays.stream(columns).forEach(rightAligned::add);
        return this;
    }

    public TextTable row(Object... cells) {
        if (cells.length != headers.size()) {
            throw new IllegalArgumentException(
                    "Ожидалось %d ячеек, получено %d".formatted(headers.size(), cells.length));
        }
        rows.add(Arrays.stream(cells).map(String::valueOf).toList());
        return this;
    }

    public boolean isEmpty() {
        return rows.isEmpty();
    }

    public List<String> render() {
        List<Integer> widths = IntStream.range(0, headers.size())
                .mapToObj(column -> Stream.concat(Stream.of(headers), rows.stream())
                        .mapToInt(row -> row.get(column).length())
                        .max()
                        .orElse(0))
                .toList();
        List<String> separator = widths.stream().map("-"::repeat).toList();
        return Stream.of(Stream.of(headers, separator), rows.stream())
                .flatMap(stream -> stream)
                .map(row -> line(row, widths))
                .toList();
    }

    private String line(List<String> cells, List<Integer> widths) {
        return IntStream.range(0, cells.size())
                .mapToObj(column -> pad(cells.get(column), widths.get(column), rightAligned.contains(column)))
                .collect(Collectors.joining(GAP))
                .stripTrailing();
    }

    private String pad(String value, int width, boolean right) {
        String padding = " ".repeat(width - value.length());
        return right ? padding + value : value + padding;
    }
}
