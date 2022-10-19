package ru.practicum.shareit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.exception.ElementNotFoundException;
import ru.practicum.shareit.exception.EmailAlreadyExistsException;
import ru.practicum.shareit.exception.ValidationException;

import java.util.Map;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleBadRequestException(final ValidationException e) {
        log.error("Неверный параметр, {}", e.getMessage());
        return Map.of("error", String.format("Неверный параметр %s", e.getMessage()));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationException(final MethodArgumentNotValidException e) {
        log.error("Неверный параметр, {}", e.getMessage());
        return Map.of("error", String.format("Неверный параметр %s", e.getMessage()));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleElementNotFoundException(final ElementNotFoundException e) {
        log.error("Не найден {}", e.getMessage());
        return Map.of("error", String.format("Не найден %s", e.getMessage()));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleEmailAlreadyExistsException(final EmailAlreadyExistsException e) {
        log.error("Ошибка, {}", e.getMessage());
        return Map.of("error", String.format("ошибка - %s", e.getMessage()));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleThrowableException(final Throwable e) {
        log.error("Возникла непредвиденная ошибка {}.", e.getMessage());
        return Map.of("error", String.format("%s", e.getMessage()));
    }
}
