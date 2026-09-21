package aula.microservicos.playyourlist.excecoes;

public class RecursoInexistenteException extends RuntimeException {
    public RecursoInexistenteException(String mensagem) {
        super(mensagem);
    }
}
