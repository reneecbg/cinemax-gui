/*
 * ============================================================================
 *  CineMax - Laboratorio Interdisciplinare B
 *  Autori:
 *    - <Renee Angelica Cabigting> - Matricola <756997> - Sede <CO>
 *  File: GestoreEccezioniSql.java
 * ============================================================================
 */
package cinemax.server.eccezioni;

import java.sql.SQLException;

/**
 * Traduce una {@link SQLException} sollevata da PostgreSQL in una delle due
 * eccezioni del server ({@link EccezioneApplicativa} o
 * {@link EccezioneTecnica}), in base al suo SQLState.
 * <p>
 * Questa classe e' il punto in cui si "raccoglie" il lavoro fatto nello
 * schema del database (Step 1 della roadmap): i trigger di
 * {@code schema.sql} segnalano le violazioni di regola di business con
 * {@code RAISE EXCEPTION}, che JDBC riceve come SQLException con SQLState
 * {@code P0001} ("raise_exception", la categoria generica usata da PL/pgSQL
 * per le RAISE EXCEPTION senza un codice piu' specifico). Una violazione di
 * vincolo UNIQUE (es. username duplicato) arriva invece con SQLState
 * {@code 23505}. In entrambi i casi il messaggio che PostgreSQL restituisce
 * e' gia' scritto per essere comprensibile, quindi lo si propaga cosi'
 * com'e' in una {@link EccezioneApplicativa}. Qualunque altro SQLState
 * (connessione persa, sintassi, ecc.) e' un problema tecnico imprevisto e
 * diventa una {@link EccezioneTecnica}.
 *
 * @author CineMax Team
 */
public final class GestoreEccezioniSql {

    private static final String RAISE_EXCEPTION = "P0001";
    private static final String UNIQUE_VIOLATION = "23505";
    private static final String FOREIGN_KEY_VIOLATION = "23503";

    private GestoreEccezioniSql() {
        // utility class
    }

    /**
     * Classifica una {@link SQLException} e restituisce l'eccezione del
     * server corrispondente, pronta per essere lanciata dal chiamante.
     *
     * @param e           l'eccezione SQL originale
     * @param descrizione breve descrizione dell'operazione in corso, usata
     *                    solo per il messaggio dell'eccezione tecnica (log)
     * @return l'eccezione applicativa o tecnica corrispondente
     */
    public static RuntimeException classifica(SQLException e, String descrizione) {
        String stato = e.getSQLState();

        if (RAISE_EXCEPTION.equals(stato)) {
            // Messaggio generato da un RAISE EXCEPTION nei trigger di schema.sql:
            // e' gia' pensato per l'utente finale (es. "Posti insufficienti...").
            return new EccezioneApplicativa(e.getMessage());
        }
        if (UNIQUE_VIOLATION.equals(stato)) {
            return new EccezioneApplicativa("Valore gia' esistente: " + dettaglioBreve(e));
        }
        if (FOREIGN_KEY_VIOLATION.equals(stato)) {
            return new EccezioneApplicativa("Riferimento non valido: " + dettaglioBreve(e));
        }
        return new EccezioneTecnica("Errore tecnico durante: " + descrizione, e);
    }

    private static String dettaglioBreve(SQLException e) {
        // Il driver PostgreSQL include nel messaggio dettagli come
        // "Detail: Key (username)=(mrossi) already exists.": si estrae solo
        // la prima riga per non esporre dettagli interni superflui.
        String messaggio = e.getMessage();
        int fineRiga = messaggio.indexOf('\n');
        return fineRiga > 0 ? messaggio.substring(0, fineRiga) : messaggio;
    }
}
