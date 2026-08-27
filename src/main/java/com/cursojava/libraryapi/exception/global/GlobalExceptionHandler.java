package com.cursojava.libraryapi.exception.global;

import com.cursojava.libraryapi.dto.error.ErrorResponseDTO;
import com.cursojava.libraryapi.dto.error.FieldErrorDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        List<FieldError> fieldErrors = e.getFieldErrors();
        //mapeia os fieldErros para uma lista de DTO de erro
        List<FieldErrorDTO> errorList = fieldErrors
                .stream()
                .map(fe -> new FieldErrorDTO(fe.getField(), fe.getDefaultMessage())).toList();

        ErrorResponseDTO response =  new ErrorResponseDTO(
                HttpStatus.UNPROCESSABLE_CONTENT.value(),
                "Erro de validação",
                errorList
        );

        return ResponseEntity
                .unprocessableContent()
                .body(response);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleNotFoundException(NotFoundException e) {
        ErrorResponseDTO response = new ErrorResponseDTO(
                HttpStatus.NOT_FOUND.value(),
                e.getMessage(),
                List.of()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponseDTO> handleConflictException(ConflictException e) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorResponseDTO.conflict(e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleInternalServerError(Exception e) {
        log.error("Erro interno não tratado", e);

        ErrorResponseDTO response = new ErrorResponseDTO(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Ocorreu um erro interno. Tente novamente mais tarde.",
                List.of()
        );

        return ResponseEntity
                .internalServerError()
                .body(response);
    }
}
