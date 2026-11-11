package ru.mephi.finance.cli;

import java.util.ArrayList;
import java.util.List;

public final class Tokenizer {

    private Tokenizer() {
    }

    public static List<String> tokenize(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        boolean started = false;
        for (char ch : line.toCharArray()) {
            if (ch == '"') {
                quoted = !quoted;
                started = true;
            } else if (Character.isWhitespace(ch) && !quoted) {
                if (started) {
                    tokens.add(current.toString());
                    current.setLength(0);
                    started = false;
                }
            } else {
                current.append(ch);
                started = true;
            }
        }
        if (quoted) {
            throw new CommandException("Не закрыта кавычка в команде");
        }
        if (started) {
            tokens.add(current.toString());
        }
        return List.copyOf(tokens);
    }
}
