package persistencia;

// Tentativa de misturar famílias (ex: repositório MySQL com unidade de trabalho em memória)
public class FamiliaIncompativelException extends IllegalArgumentException {
    public FamiliaIncompativelException(String esperada, UnidadeDeTrabalho recebida) {
        super("A fabrica da familia '" + esperada + "' nao aceita unidade de trabalho da familia '"
                + recebida.familia() + "'");
    }
}
