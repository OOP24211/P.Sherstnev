import validation.exceptions.ValidationException;
import validation.validators.FileValidator;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        try {
            File inputFile = new File(Config.INPUT_FILE);
            File outputFile = new File(Config.OUTPUT_FILE);

            FileValidator fileValidator = new FileValidator();
            fileValidator.validate(inputFile);

            WordCounter counter = new WordCounter();
            counter.execute(inputFile, outputFile);

            logger.info("Программа успешно завершила работу.");

        } catch (ValidationException e) {
            logger.log(Level.WARNING, "Ошибка валидации данных: " + e.getMessage());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Критическая ошибка выполнения", e);
        }
    }
}