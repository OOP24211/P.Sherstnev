import java.util.Map;
import java.util.TreeMap;

public class WordProcessor {
    public void processLine(String line, Map<String, Integer> counts, long[] total) {
        String[] words = line.toLowerCase().split("[^\\p{L}'-]+");
        for (String word : words) {
            word = word.replaceAll("^-+|-+$", "");
            if (!word.isEmpty()) {
                counts.put(word, counts.getOrDefault(word, 0) + 1);
                total[0]++;
            }
        }
    }
}