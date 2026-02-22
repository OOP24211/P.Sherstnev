import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        WordCounter counter = new WordCounter();

        try {
            counter.readFile(new File(Config.INPUT_FILE));      // Читаем
            counter.saveReport(new File(Config.OUTPUT_FILE));   // Пишем результат
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Произошла ошибка при работе с файлами", e);
        }
    }
}