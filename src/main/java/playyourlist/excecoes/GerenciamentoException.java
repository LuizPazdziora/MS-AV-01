package playyourlist.excecoes;

import java.util.HashMap;
import java.util.Map;
import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GerenciamentoException {
    // O Feign mantém o 404 quando a API consulta um recurso inexistente.
    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Recurso não localizado")
    @ExceptionHandler({RecursoInexistenteException.class, FeignException.NotFound.class})
    public void inexistente() {
    }

    @ResponseStatus(value = HttpStatus.CONFLICT, reason = "Música já associada à playlist")
    @ExceptionHandler({ConflitoException.class, FeignException.Conflict.class})
    public void conflito() {
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> validar(MethodArgumentNotValidException erro) {
        Map<String, String> campos = new HashMap<String, String>();
        for (FieldError campo : erro.getBindingResult().getFieldErrors()) {
            campos.put(campo.getField(), campo.getDefaultMessage());
        }
        return campos;
    }
}
