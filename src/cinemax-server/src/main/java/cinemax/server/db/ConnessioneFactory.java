/*
 * ============================================================================
 *  CineMax - Laboratorio Interdisciplinare B
 *  Autori:
 *    - <Renee Angelica Cabigting> - Matricola <756997> - Sede <CO>
 *  File: ConnessioneFactory.java
 * ============================================================================
 */
package cinemax.server.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Crea connessioni JDBC verso il database PostgreSQL {@code dbCM}.
 * <p>
 * Il modello di concorrenza scelto (vedi Step 5 della roadmap, l'architettura
 * di rete del server) e' "una connessione JDBC per client": ogni thread che
 * gestisce un client apre, tramite questa factory, una propria
 * {@link Connection} all'avvio della sessione e la mantiene per tutta la sua
 * durata, chiudendola alla disconnessione. In questo modo il parallelismo
 * tra client e' garantito nativamente da PostgreSQL (che gestisce le proprie
 * connessioni concorrenti), mentre la correttezza sugli accessi a dati
 * condivisi (stessa proiezione prenotata da piu' client) resta comunque
 * garantita dai trigger con {@code SELECT ... FOR UPDATE} definiti in
 * {@code schema.sql}, indipendentemente da quale connessione esegue la
 * query.
 * <p>
 * Nota per un'eventuale evoluzione: con un numero elevato di client
 * simultanei, aprire una connessione fisica per ciascuno puo' diventare
 * costoso; una soluzione piu' scalabile sarebbe un connection pool (es.
 * HikariCP). Per gli scopi di questo progetto (uso didattico, numero di
 * client limitato) una connessione dedicata per client e' piu' semplice da
 * realizzare e da spiegare, ed e' comunque corretta.
 *
 * @author CineMax Team
 */
public final class ConnessioneFactory {

    private final String url;
    private final String utente;
    private final String password;

    /**
     * @param host     host del server PostgreSQL (es. "localhost")
     * @param porta    porta del server PostgreSQL (tipicamente 5432)
     * @param database nome del database (es. "dbcm")
     * @param utente   utente PostgreSQL
     * @param password password dell'utente PostgreSQL
     */
    public ConnessioneFactory(String host, int porta, String database, String utente, String password) {
        this.url = "jdbc:postgresql://" + host + ":" + porta + "/" + database;
        this.utente = utente;
        this.password = password;
    }

    /**
     * Apre una nuova connessione JDBC. Il chiamante e' responsabile di
     * chiuderla (idealmente con un blocco {@code try-with-resources} o,
     * nel caso della connessione dedicata a un client, in un blocco
     * {@code finally} alla disconnessione).
     *
     * @return una nuova connessione aperta
     * @throws SQLException se la connessione non puo' essere stabilita
     *                       (host irraggiungibile, credenziali errate, ecc.)
     */
    public Connection apriConnessione() throws SQLException {
        return DriverManager.getConnection(url, utente, password);
    }

    /**
     * Verifica che i parametri di connessione forniti al lancio del server
     * siano validi, aprendo e chiudendo immediatamente una connessione di
     * prova. Va chiamato una sola volta all'avvio di {@code ServerCM}, cosi'
     * un errore di configurazione viene segnalato subito invece che al
     * primo client connesso.
     *
     * @throws SQLException se la connessione di prova fallisce
     */
    public void verificaConnessione() throws SQLException {
        try (Connection c = apriConnessione()) {
            // connessione aperta e subito richiusa: serve solo a validare
            // host/credenziali prima di mettersi in ascolto di client.
        }
    }
}
