import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class WordCounter {
    private final Map<String, Integer> wordCounts = new HashMap<>();
    private long totalWords = 0;

    // Чтение и  подсчет
    public void readFile(File inputFile) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] words = line.toLowerCase().split("\\P{L}+");
                for (String word : words) {
                    if (!word.isEmpty()) {
                        wordCounts.put(word, wordCounts.getOrDefault(word, 0) + 1);
                        totalWords++;
                    }
                }
            }
        }
    }

    // Запись результата
    public void saveReport(File outputFile) throws IOException {
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8)))) {
            writer.printf("%-20s | %-10s | %-10s%n", "Слово", "Кол-во", "Процент");
            writer.println("--------------------------------------------------");

            wordCounts.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .forEach(entry -> {
                        double percentage = (double) entry.getValue() / totalWords * 100;
                        writer.printf("%-20s | %-10d | %.2f%%%n", entry.getKey(), entry.getValue(), percentage);
                    });
        }
    }
}