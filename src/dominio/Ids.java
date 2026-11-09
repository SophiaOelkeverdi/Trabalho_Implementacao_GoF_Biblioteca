package dominio;

import java.util.UUID;

// Gera ids das transações, ex: RES-1a2b3c4d
public final class Ids {
    private Ids() {
    }

    public static String novo(String prefixo) {
        return prefixo + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
