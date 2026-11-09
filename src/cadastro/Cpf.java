package cadastro;

// Deixa o CPF só com números. Não confere dígito verificador.
final class Cpf {
    private Cpf() {
    }

    static String normalizar(String cpf) {
        return cpf == null ? null : cpf.replaceAll("\\D", "");
    }

    static boolean valido(String cpf) {
        String d = normalizar(cpf);
        return d != null && d.length() == 11;
    }
}
