/*
 * ============================================================================
 *  CineMax - Laboratorio Interdisciplinare B
 *  Autori:
 *    - <Renee Angelica Cabigting> - Matricola <756997> - Sede <CO>
 *  File: GestoreClient.java
 * ============================================================================
 */
package cinemax.server.rete;

import cinemax.common.protocol.Comando;
import cinemax.common.protocol.Richiesta;
import cinemax.common.protocol.Risposta;
import cinemax.server.db.ConnessioneFactory;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Gestisce l'intera sessione di un singolo client connesso, in un thread
 * dedicato (modello "thread-per-connessione", vedi Step 5 della roadmap).
 * <p>
 * Apre una {@link Connection} JDBC dedicata a questo client (vedi
 * {@link ConnessioneFactory}) e la mantiene per tutta la durata della
 * sessione, condivisa da tutti i comandi che il client invia. Il
 * parallelismo tra client diversi e' quindi naturale: ogni thread ha la
 * propria connessione al DB e il proprio {@link Dispatcher}; l'unica
 * concorrenza da gestire esplicitamente e' quella sulle righe condivise del
 * database, di cui si occupano i trigger di {@code schema.sql} (Step 1).
 *
 * @author CineMax Team
 */
public class GestoreClient implements Runnable {

    private final Socket socket;
    private final ConnessioneFactory connessioneFactory;

    public GestoreClient(Socket socket, ConnessioneFactory connessioneFactory) {
        this.socket = socket;
        this.connessioneFactory = connessioneFactory;
    }

    @Override
    public void run() {
        String indirizzoClient = socket.getRemoteSocketAddress().toString();
        System.out.println("[serverCM] Client connesso: " + indirizzoClient);

        try (Socket s = socket;
             Connection connessioneDb = connessioneFactory.apriConnessione();
             ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(s.getInputStream())) {

            Dispatcher dispatcher = new Dispatcher(connessioneDb);
            boolean sessioneAttiva = true;

            while (sessioneAttiva) {
                Richiesta richiesta = (Richiesta) in.readObject();
                System.out.println("[serverCM] " + indirizzoClient + " -> " + richiesta);

                Risposta risposta = dispatcher.gestisci(richiesta);
                out.writeObject(risposta);
                out.flush();

                if (richiesta.getComando() == Comando.LOGOUT) {
                    sessioneAttiva = false;
                }
            }

        } catch (EOFException e) {
            // Il client ha chiuso la connessione senza inviare LOGOUT
            // (es. chiusura improvvisa della GUI): non e' un errore da
            // segnalare, e' la normale fine di una sessione interrotta.
            System.out.println("[serverCM] Client disconnesso: " + indirizzoClient);
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("[serverCM] Errore di comunicazione con " + indirizzoClient + ": " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("[serverCM] Impossibile aprire la connessione al DB per "
                    + indirizzoClient + ": " + e.getMessage());
        } finally {
            System.out.println("[serverCM] Sessione terminata: " + indirizzoClient);
        }
    }
}
