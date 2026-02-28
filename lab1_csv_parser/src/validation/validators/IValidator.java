package validation.validators;

import validation.exceptions.ValidationException;

public interface IValidator<T> {
    void validate(T target) throws ValidationException;
}