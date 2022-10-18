package br.com.aab.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

public class LogTransactionDuration {
    final String transactionBeginsPath = "src/main/resources/transaction-begins.log";
    final String transactionEndsPath = "src/main/resources/transaction-ends.log";

    public List<Duration> duration() throws IOException {
        Map<String, LocalDateTime> trxLogBegin = readFile(transactionBeginsPath);
        Map<String, LocalDateTime> trxLogEnd = readFile(transactionEndsPath);
        return trxLogBegin
                .entrySet().stream().map(log -> {
                    boolean foundTrx = trxLogEnd.containsKey(log.getKey());
                    return foundTrx
                            ? Duration.between(log.getValue(), trxLogEnd.get(log.getKey()))
                            : Duration.ZERO;
                }).collect(Collectors.toList());
    }

    private Map<String, LocalDateTime> readFile(String transactionLogPath) throws IOException {
        Map<String, LocalDateTime> result = new LinkedHashMap<>();
        try(BufferedReader bufferedReader = new BufferedReader(new FileReader(transactionLogPath))) {
            String line;
            while(Objects.nonNull((line = bufferedReader.readLine()))) {
                String[][] parsedLog = buildEntrySet(line);
                LocalDateTime dtTrx = LocalDateTime.parse(parsedLog[1][0], DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss").withLocale(Locale.ENGLISH));
                result.put(parsedLog[0][0], dtTrx);
            }
        }
        return result;
    }

    private String[][] buildEntrySet(String log) {
        StringTokenizer logTokenized = new StringTokenizer(log, ",");
        if (logTokenized.hasMoreElements()) {
            String key = logTokenized.nextToken();
            String timeStamp = logTokenized.nextToken();
            key += ":::" + logTokenized.nextToken();
            return new String[][] {{key}, {timeStamp}};
        }
        return new String[][] {};
    }

    public static void main(String[] args) throws IOException {
        LogTransactionDuration logTransactionDuration = new LogTransactionDuration();
        logTransactionDuration.duration().forEach(System.out::println);
    }
}
