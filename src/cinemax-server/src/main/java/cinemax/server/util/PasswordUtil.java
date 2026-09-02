/*
 * ============================================================================
 *  CineMax - Laboratorio Interdisciplinare B
 *  Autori:
 *    - <Renee Angelica Cabigting> - Matricola <756997> - Sede <CO>
 *  File: PasswordUtil.java
 * ============================================================================
 */
package cinemax.server.util;

import cinemax.server.eccezioni.EccezioneTecnica;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Calcola l'hash SHA-256 di una password, nello stesso formato (64
 * caratteri esadecimali minuscoli) gia' usato nella versione TUI del
 * Lab A: questo mantiene compatibili gli account di test gia' esistenti
 * in {@code seed.sql}.
 * <p>
 * Questa e' l'unica classe del server che conosce l'algoritmo di hashing:
 * ne' i DAO ne' il protocollo di rete lo conoscono, cosi' un'eventuale
 * evoluzione dell'algoritmo (es. passaggio a bcrypt) tocca un solo punto
 * del codice.
 *
 * @author CineMax Team
 */
public final class PasswordUtil {

    private PasswordUtil() {
        // utility class
    }

    /**
     * Calcola l'hash SHA-256 della password in chiaro.
     *
     * @param passwordInChiaro la password cosi' come digitata dall'utente
     * @return l'hash in esadecimale minuscolo, 64 caratteri
     */
    public static String hash(String passwordInChiaro) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(passwordInChiaro.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 e' sempre disponibile in una JVM standard: se manca,
            // e' un problema di ambiente, non un caso applicativo previsto.
            throw new EccezioneTecnica("Algoritmo di hashing non disponibile", e);
        }
    }

    /**
     * Verifica che una password in chiaro corrisponda a un hash memorizzato.
     *
     * @param passwordInChiaro password digitata dall'utente
     * @param hashMemorizzato  hash gia' presente nel database
     * @return {@code true} se corrispondono
     */
    public static boolean verifica(String passwordInChiaro, String hashMemorizzato) {
        return hash(passwordInChiaro).equals(hashMemorizzato);
    }
}
