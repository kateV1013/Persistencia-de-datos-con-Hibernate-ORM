package com.quilate.api;

import com.quilate.exception.EntidadConDependenciasException;
import com.quilate.exception.ReglaNegocioException;
import com.quilate.exception.ServicioExternoException;
import com.quilate.exception.ValidacionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class ManejoErrores {

    @ExceptionHandler({ValidacionException.class, ReglaNegocioException.class})
    public ResponseEntity<Map<String, String>> datosInvalidos(RuntimeException e) {
        return error(HttpStatus.BAD_REQUEST, e);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> noEncontrado(RuntimeException e) {
        return error(HttpStatus.NOT_FOUND, e);
    }

    @ExceptionHandler(EntidadConDependenciasException.class)
    public ResponseEntity<Map<String, String>> conflicto(RuntimeException e) {
        return error(HttpStatus.CONFLICT, e);
    }

    @ExceptionHandler(ServicioExternoException.class)
    public ResponseEntity<Map<String, String>> servicioExterno(RuntimeException e) {
        return error(HttpStatus.BAD_GATEWAY, e);
    }

    private static ResponseEntity<Map<String, String>> error(HttpStatus estado, RuntimeException e) {
        return ResponseEntity.status(estado).contentType(MediaType.APPLICATION_JSON).body(Map.of("error", e.getMessage()));
    }
}
