package br.edu.ifpb.es.bd2.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    public record CampoInvalido(String campo, String mensagem) {}

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Object> negocio(BusinessException ex, WebRequest request) {
        return responder(ex, ex.getStatus(), ex.getMessage(), request);
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<Object> chaveDuplicada(DuplicateKeyException ex, WebRequest request) {
        return responder(ex, HttpStatus.CONFLICT, "Já existe um registro com esses dados, que devem ser únicos", request);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<Object> versaoDesatualizada(OptimisticLockingFailureException ex, WebRequest request) {
        return responder(ex, HttpStatus.CONFLICT,
                "O registro foi alterado por outra operação. Recarregue os dados e tente novamente", request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> restricaoViolada(ConstraintViolationException ex, WebRequest request) {
        List<CampoInvalido> campos = ex.getConstraintViolations().stream()
                .map(v -> new CampoInvalido(v.getPropertyPath().toString(), v.getMessage()))
                .toList();
        return responderValidacao(ex, campos, new HttpHeaders(), request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> argumentoInvalido(IllegalArgumentException ex, WebRequest request) {
        return responder(ex, HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(DataAccessResourceFailureException.class)
    public ResponseEntity<Object> bancoIndisponivel(DataAccessResourceFailureException ex, WebRequest request) {
        log.error("Falha de comunicação com o MongoDB", ex);
        return responder(ex, HttpStatus.SERVICE_UNAVAILABLE, "Falha ao comunicar com o banco de dados", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> inesperado(Exception ex, WebRequest request) {
        log.error("Erro inesperado ao processar a requisição", ex);
        return responder(ex, HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocorreu um erro inesperado. Tente novamente mais tarde.", request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatusCode status,
                                                                  WebRequest request) {
        List<CampoInvalido> campos = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> new CampoInvalido(e.getField(), e.getDefaultMessage()))
                .toList();
        return responderValidacao(ex, campos, headers, request);
    }

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(HandlerMethodValidationException ex,
                                                                            HttpHeaders headers, HttpStatusCode status,
                                                                            WebRequest request) {
        List<CampoInvalido> campos = ex.getAllValidationResults().stream()
                .flatMap(r -> r.getResolvableErrors().stream()
                        .map(e -> new CampoInvalido(r.getMethodParameter().getParameterName(), e.getDefaultMessage())))
                .toList();
        return responderValidacao(ex, campos, headers, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers, HttpStatusCode status,
                                                                  WebRequest request) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "O corpo da requisição está ausente ou mal formatado");
        return handleExceptionInternal(ex, problema, headers, HttpStatus.BAD_REQUEST, request);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex,
                                                                          HttpHeaders headers, HttpStatusCode status,
                                                                          WebRequest request) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "O parâmetro '" + ex.getParameterName() + "' é obrigatório");
        return handleExceptionInternal(ex, problema, headers, HttpStatus.BAD_REQUEST, request);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers,
                                                        HttpStatusCode status, WebRequest request) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "Valor inválido para '" + ex.getPropertyName() + "': " + ex.getValue());
        return handleExceptionInternal(ex, problema, headers, HttpStatus.BAD_REQUEST, request);
    }

    @Override
    protected ResponseEntity<Object> createResponseEntity(Object body, HttpHeaders headers,
                                                          HttpStatusCode statusCode, WebRequest request) {
        if (body instanceof ProblemDetail problema) {
            problema.setTitle(titulo(statusCode));
            if (problema.getInstance() == null && request instanceof ServletWebRequest servlet) {
                problema.setInstance(URI.create(servlet.getRequest().getRequestURI()));
            }
            problema.setProperty("timestamp", OffsetDateTime.now());
        }
        return super.createResponseEntity(body, headers, statusCode, request);
    }

    private ResponseEntity<Object> responder(Exception ex, HttpStatusCode status, String detalhe, WebRequest request) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalhe);
        return handleExceptionInternal(ex, problema, new HttpHeaders(), status, request);
    }

    private ResponseEntity<Object> responderValidacao(Exception ex, List<CampoInvalido> campos,
                                                      HttpHeaders headers, WebRequest request) {
        String detalhe = campos.isEmpty() ? "Dados inválidos" : campos.get(0).mensagem();
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detalhe);
        problema.setProperty("campos", campos);
        return handleExceptionInternal(ex, problema, headers, HttpStatus.BAD_REQUEST, request);
    }

    private String titulo(HttpStatusCode status) {
        return switch (status.value()) {
            case 400 -> "Requisição inválida";
            case 403 -> "Operação não permitida";
            case 404 -> "Recurso não encontrado";
            case 405 -> "Método não permitido";
            case 409 -> "Conflito";
            case 415 -> "Tipo de conteúdo não suportado";
            case 503 -> "Serviço indisponível";
            case 500 -> "Erro interno";
            default -> {
                HttpStatus conhecido = HttpStatus.resolve(status.value());
                yield conhecido != null ? conhecido.getReasonPhrase() : "Erro";
            }
        };
    }
}
