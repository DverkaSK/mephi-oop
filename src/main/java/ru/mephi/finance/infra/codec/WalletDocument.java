package ru.mephi.finance.infra.codec;

import ru.mephi.finance.domain.Operation;
import ru.mephi.finance.domain.OperationType;
import ru.mephi.finance.domain.WalletSnapshot;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public record WalletDocument(
        String owner,
        List<String> categories,
        Map<String, String> budgets,
        List<OperationDocument> operations) {

    public static WalletDocument from(WalletSnapshot snapshot) {
        return new WalletDocument(
                snapshot.owner(),
                snapshot.categories(),
                snapshot.budgets().entrySet().stream().collect(Collectors.toMap(
                        Map.Entry::getKey, entry -> entry.getValue().toPlainString(), (a, b) -> b,
                        LinkedHashMap::new)),
                snapshot.operations().stream().map(OperationDocument::from).toList());
    }

    public WalletSnapshot toSnapshot() {
        return new WalletSnapshot(
                owner,
                categories == null ? List.of() : categories,
                budgets == null ? Map.of() : budgets.entrySet().stream().collect(Collectors.toMap(
                        Map.Entry::getKey, entry -> new BigDecimal(entry.getValue()), (a, b) -> b,
                        LinkedHashMap::new)),
                operations == null ? List.of() : operations.stream().map(OperationDocument::toDomain).toList());
    }

    public record OperationDocument(String id, String type, String category, String amount, String date, String note) {

        public static OperationDocument from(Operation operation) {
            return new OperationDocument(
                    operation.id().toString(),
                    operation.type().name(),
                    operation.category(),
                    operation.amount().toPlainString(),
                    operation.date().toString(),
                    operation.note());
        }

        public Operation toDomain() {
            return new Operation(
                    UUID.fromString(id),
                    OperationType.valueOf(type),
                    category,
                    new BigDecimal(amount),
                    LocalDate.parse(date),
                    note);
        }
    }
}
