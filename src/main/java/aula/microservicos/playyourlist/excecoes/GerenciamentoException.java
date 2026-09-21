package aula.microservicos.playyourlist.excecoes;

import java.util.LinkedHashMap;
import java.util.Map;
import feign.FeignException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GerenciamentoException {
    @ExceptionHandler(RecursoInexistenteException.class)
    public ResponseEntity<Map<String, String>> inexistente(RecursoInexistenteException erro) {
        return resposta(HttpStatus.NOT_FOUND, erro.getMessage());
    }

    @ExceptionHandler(ConflitoException.class)
    public ResponseEntity<Map<String, String>> conflito(ConflitoException erro) {
        return resposta(HttpStatus.CONFLICT, erro.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> integridade(DataIntegrityViolationException erro) {
        return resposta(HttpStatus.CONFLICT, "A operação conflita com os vínculos existentes");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> validar(MethodArgumentNotValidException erro) {
        Map<String, String> campos = new LinkedHashMap<String, String>();
        for (FieldError campo : erro.getBindingResult().getFieldErrors()) {
            campos.put(campo.getField(), campo.getDefaultMessage());
        }
        return new ResponseEntity<Map<String, String>>(campos, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Map<String, String>> requisicaoInvalida(Exception erro) {
        return resposta(HttpStatus.BAD_REQUEST, "JSON ou parâmetro inválido");
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<Map<String, String>> integracao(FeignException erro) {
        if (erro.status() == 404) {
            return resposta(HttpStatus.NOT_FOUND, "Música ou playlist não localizada");
        } else if (erro.status() == 409) {
            return resposta(HttpStatus.CONFLICT, "Música já associada ou conflito com os vínculos existentes");
        } else if (erro.status() == 400) {
            return resposta(HttpStatus.BAD_REQUEST, "Dados rejeitados pelo serviço");
        } else {
            return resposta(HttpStatus.BAD_GATEWAY, "Não foi possível acessar o serviço");
        }
    }

    private ResponseEntity<Map<String, String>> resposta(HttpStatus status, String mensagem) {
        Map<String, String> corpo = new LinkedHashMap<String, String>();
        corpo.put("mensagem", mensagem);
        return new ResponseEntity<Map<String, String>>(corpo, status);
    }
}
