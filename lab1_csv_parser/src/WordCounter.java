import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class WordCounter {
    private final Map<String, Integer> wordCounts = new HashMap<>();
    private long totalWords = 0;

    public void readFile(File inputFile) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] words = line.toLowerCase().split("[^\\p{L}'-]+");
                for (String word : words) {
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
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8)))) {

            writer.println("Слово;Кол-во;Процент");

            wordCounts.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed()
                            .thenComparing(Map.Entry.comparingByKey()))
                    .forEach(entry -> {
                        double percentage = (double) entry.getValue() / totalWords * 100;
                        writer.printf("%s;%d;%.2f%%%n", entry.getKey(), entry.getValue(), percentage);
                    });
        }
    }
}