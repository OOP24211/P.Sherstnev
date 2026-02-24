package validation.validators;

import validation.exceptions.ValidationException;
import java.io.File;

public class FileValidator implements IValidator<File> {
    @Override
    public void validate(File file) throws ValidationException {
        if (file == null || !file.exists()) {
            throw new ValidationException("Файл не найден: " + (file != null ? file.getPath() : "null"));
        }
        if (!file.isFile()) {
            throw new ValidationException("Путь указывает не на файл: " + file.getPath());
        }
        if (!file.canRead()) {
            throw new ValidationException("Нет прав на чтение файла: " + file.getPath());
        }
        if (file.length() == 0) {
            throw new ValidationException("Файл пуст: " + file.getPath());
        }
    }
}