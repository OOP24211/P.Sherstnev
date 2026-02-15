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
                // Регулярка теперь разрешает буквы, дефисы и апострофы внутри слов
                // [^\\p{L}'-]+ означает: "разбивай по всему, что НЕ буква, НЕ дефис и НЕ апостроф"
                String[] words = line.toLowerCase().split("[^\\p{L}'-]+");
                for (String word : words) {
                    // Убираем дефисы в начале или конце (если это тире в тексте)
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
            // В CSV заголовки разделяются просто запятой без пробелов для выравнивания
            writer.println("Слово;Кол-во;Процент");

            wordCounts.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed()
                            // Вторая сортировка: если числа равны, сортируем по ключу (алфавиту)
                            .thenComparing(Map.Entry.comparingByKey()))
                    .forEach(entry -> {
                        double percentage = (double) entry.getValue() / totalWords * 100;
                        // Чистый CSV формат: данные,запятая,данные
                        writer.printf("%s;%d;%.2f%%%n", entry.getKey(), entry.getValue(), percentage);
                    });
        }
    }
}