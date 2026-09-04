package co.edu.javeriana.bmpn.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import co.edu.javeriana.bmpn.exception.AccesoDenegadoException;
import co.edu.javeriana.bmpn.exception.AutenticacionRequeridaException;
import co.edu.javeriana.bmpn.exception.RecursoDuplicadoException;
import co.edu.javeriana.bmpn.exception.RecursoNoEncontradoException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail manejarValidacion(MethodArgumentNotValidException exception) {
        ProblemDetail problema = crearProblema(
                HttpStatus.BAD_REQUEST, "Solicitud invalida",
                "Uno o mas campos no cumplen las reglas de validacion");

        Map<String, List<String>> errores = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                errores.computeIfAbsent(error.getField(), campo -> new java.util.ArrayList<>())
                        .add(error.getDefaultMessage()));
        problema.setProperty("errores", errores);
        return problema;
    }

    @ExceptionHandler(AutenticacionRequeridaException.class)
    public ProblemDetail manejarAutenticacion(AutenticacionRequeridaException exception) {
        return crearProblema(HttpStatus.UNAUTHORIZED, "Autenticacion requerida",
                exception.getMessage());
    }

    @ExceptionHandler(AccesoDenegadoException.class)
    public ProblemDetail manejarAccesoDenegado(AccesoDenegadoException exception) {
        return crearProblema(HttpStatus.FORBIDDEN, "Acceso denegado", exception.getMessage());
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ProblemDetail manejarNoEncontrado(RecursoNoEncontradoException exception) {
        return crearProblema(HttpStatus.NOT_FOUND, "Recurso no encontrado",
                exception.getMessage());
    }

    @ExceptionHandler(RecursoDuplicadoException.class)
    public ProblemDetail manejarDuplicado(RecursoDuplicadoException exception) {
        return crearProblema(HttpStatus.CONFLICT, "Recurso duplicado", exception.getMessage());
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public ProblemDetail manejarOperacionPendiente(UnsupportedOperationException exception) {
        return crearProblema(HttpStatus.NOT_IMPLEMENTED, "Operacion pendiente",
                exception.getMessage());
    }

    private ProblemDetail crearProblema(HttpStatus estado, String titulo, String detalle) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(estado, detalle);
        problema.setTitle(titulo);
        return problema;
    }
}
