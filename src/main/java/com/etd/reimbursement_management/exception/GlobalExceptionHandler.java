package com.etd.reimbursement_management.exception;

import com.etd.reimbursement_management.dto.ErrorDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorDTO> toHandleNotFoundException(NotFoundException ex)
    {
        ErrorDTO errorDTO = ErrorDTO
                .builder()
                .message(ex.getMessage())
                .fieldName(ex.getFieldName())
                .status(HttpStatus.NOT_FOUND)
                .build();
        return new ResponseEntity<>(errorDTO, errorDTO.getStatus());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorDTO> toHandleIllegalArgumentException(IllegalArgumentException ex)
    {
        ErrorDTO errorDTO = ErrorDTO
                .builder()
                .message(ex.getMessage())
                .fieldName(ex.getFieldName())
                .status(HttpStatus.BAD_REQUEST)
                .build();
        return new ResponseEntity<>(errorDTO, errorDTO.getStatus());
    }

    @ExceptionHandler(DocumentSizeLimitExceededException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public ResponseEntity<ErrorDTO> toHandleDocumentSizeLimitExceededException(DocumentSizeLimitExceededException ex)
    {
        ErrorDTO errorDTO = ErrorDTO
                .builder()
                .message(ex.getMessage())
                .status(HttpStatus.PAYLOAD_TOO_LARGE)
                .build();
        return new ResponseEntity<>(errorDTO, errorDTO.getStatus());
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorDTO> toHandleBadRequestException(BadRequestException ex)
    {
        ErrorDTO errorDTO = ErrorDTO
                .builder()
                .message(ex.getMessage())
                .fieldName(ex.getFieldName())
                .status(HttpStatus.BAD_REQUEST)
                .build();
        return new ResponseEntity<>(errorDTO, errorDTO.getStatus());
    }

}
