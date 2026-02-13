import java.io.File;
import java.io.IOException;

public class main  {
    public static void main(String[] args) {
        WordCounter counter = new WordCounter();

        try {
            counter.readFile(new File(Config.INPUT_FILE));      // Читаем
            counter.saveReport(new File(Config.OUTPUT_FILE));   // Пишем результат
        }
        catch (IOException e) {
            System.err.println("Произошла ошибка: " + e.getMessage());
        }
    }
}