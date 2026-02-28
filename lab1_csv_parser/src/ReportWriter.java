import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;

public class ReportWriter {
    public void writeCsv(File file, Map<String, Integer> counts, long totalWords) throws IOException {
        try (BufferedWriter bw = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8);
             PrintWriter writer = new PrintWriter(bw)) {

            writer.println("Слово;Кол-во;Процент");

            counts.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed()
                            .thenComparing(Map.Entry.comparingByKey()))
                    .forEach(entry -> {
                        double percentage = (double) entry.getValue() / totalWords * 100;
                        writer.printf("%s;%d;%.2f%%%n", entry.getKey(), entry.getValue(), percentage);
                    });
        }
    }
}