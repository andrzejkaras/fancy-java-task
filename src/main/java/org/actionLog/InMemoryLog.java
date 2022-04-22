package org.actionLog;

import java.util.*;

final class InMemoryLog implements ApplicationLog {
    private final List<LogEntry> actionLog;
    private final Map<String, Integer> errorOperationStats;
    private final Map<String, Integer> addOperationStats;
    private final Map<String, Integer> subOperationStats;

    InMemoryLog() {
        this.actionLog = new ArrayList<>();
        this.errorOperationStats = new HashMap<>();
        this.addOperationStats = new HashMap<>();
        this.subOperationStats = new HashMap<>();
    }

    @Override
    public void append(LogEntry entry) {
        actionLog.add(entry);
        error(entry);

        addOp(entry);
        subOp(entry);
    }
    @Override
    public Set<String> getContainerNameWithTheMostErrors() {
        return findMaxFrom(errorOperationStats);
    }

    @Override
    public Set<String> getContainerWithMaxOpType(String operationType) {
        if ("ADD".equals(operationType)) {
            return findMaxFrom(addOperationStats);
        } else if ("SUB".equals(operationType)) {
            return findMaxFrom(subOperationStats);
        }

        throw new IllegalArgumentException("Invalid operation type!");
    }

    @Override
    public double findActualCapacity(String name) {
        var temp = actionLog.stream()
                .filter(entry -> entry.getContainerName().equals(name))
                .sorted(Comparator.comparing(LogEntry::getCreated))
                .toList();

        double capacity = 0;
        for (var entry: temp) {
            if (entry.isSuccess() && entry.getOperationName().equals("ADD")) {
                capacity += entry.getDelta();
            } else if (entry.isSuccess() && entry.getOperationName().equals("SUB")) {
                capacity -= entry.getDelta();
            }
        }

        return capacity;
    }

    private Set<String> findMaxFrom(Map<String, Integer> map) {
        int max = -1;
        Set<String> names = new HashSet<>();

        for (var temp : map.entrySet()) {
            if (temp.getValue() > max) {
                max = temp.getValue();
            }
        }

        for (var temp : map.entrySet()) {
            if (temp.getValue() == max) {
                names.add(temp.getKey());
            }
        }

        return names;
    }

    private void error(LogEntry entry) {
        if (entry.isSuccess()) {
            return;
        }

        calculate(errorOperationStats, entry.getContainerName());
    }

    private void addOp(LogEntry entry) {
        if (!entry.getOperationName().equals("ADD")) {
            return;
        }

        calculate(addOperationStats, entry.getContainerName());
    }

    private void subOp(LogEntry entry) {
        if (!entry.getOperationName().equals("SUB")) {
            return;
        }

        calculate(subOperationStats, entry.getContainerName());
    }

    private void calculate(Map<String, Integer> map, String container) {
        if (!map.containsKey(container)) {
            map.put(container, 1);
        } else {
            map.merge(container, 1, Integer::sum);
        }
    }
}
