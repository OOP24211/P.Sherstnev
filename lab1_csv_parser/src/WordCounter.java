import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public class WordCounter {
    private final Map<String, Integer> wordCounts = new HashMap<>();
    private long totalWords = 0;

    public void readFile(File inputFile) throws IOException {
        try (BufferedReader br = Files.newBufferedReader(inputFile.toPath(), StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                // Разбиваем строку на слова, учитывая буквы, дефисы и апострофы
                String[] words = line.toLowerCase().split("[^\\p{L}'-]+");
                for (String word : words) {
                    // Убираем дефисы по краям слова
                    word = word.replaceAll("^-+|-+$", "");
                    if (!word.isEmpty()) {
                        wordCounts.put(word, wordCounts.getOrDefault(word, 0) + 1);
                        totalWords++;
                    }
                }
            }
        }
    }

    public void saveReport(File outputFile) throws IOException {
        try (BufferedWriter bw = Files.newBufferedWriter(outputFile.toPath(), StandardCharsets.UTF_8);
             PrintWriter writer = new PrintWriter(bw)) {

            writer.println("Слово;Кол-во;Процент");

            wordCounts.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed()
                            .thenComparing(Map.Entry.comparingByKey()))
                    .forEach(entry -> {
                        double percentage = (double) entry.getValue() / totalWords * 100;
                        // Используем printf для удобного форматирования чисел
                        writer.printf("%s;%d;%.2f%%%n", entry.getKey(), entry.getValue(), percentage);
                    });
        }
    }
}