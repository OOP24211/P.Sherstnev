import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class WordCounter {
    private final Map<String, Integer> wordCounts = new HashMap<>();
    private final long[] totalWords = {0}; // Используем массив, чтобы передавать по ссылке

    public void execute(File input, File output) throws IOException {
        WordProcessor processor = new WordProcessor();
        try (BufferedReader br = Files.newBufferedReader(input.toPath(), StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                processor.processLine(line, wordCounts, totalWords);
            }
        }

        ReportWriter writer = new ReportWriter();
        writer.writeCsv(output, wordCounts, totalWords[0]);
    }
}