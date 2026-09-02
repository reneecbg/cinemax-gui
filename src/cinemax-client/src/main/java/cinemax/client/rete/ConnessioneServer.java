/*
 * ============================================================================
 *  CineMax - Laboratorio Interdisciplinare B
 *  Autori:
 *    - <Renee Angelica Cabigting> - Matricola <756997> - Sede <CO>
 *  File: ConnessioneServer.java
 * ============================================================================
 */
package cinemax.client.rete;

import cinemax.common.protocol.Comando;
import cinemax.common.protocol.Richiesta;
import cinemax.common.protocol.Risposta;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Unico punto del client che apre un socket e scambia oggetti con serverCM.
 * <p>
 * Tutte le schermate Swing (Step 7) chiamano esclusivamente
 * {@link #invia(Comando, Object...)}: nessun {@code JPanel} conosce
 * l'esistenza di un socket, di uno stream o del protocollo a basso livello.
 * Questo separa nettamente "presentazione" (le schermate) da "comunicazione"
 * (questa classe), cosi' un domani si potesse sostituire socket+
 * serializzazione con un altro meccanismo di trasporto, le schermate non
 * cambierebbero di una riga.
 * <p>
 * La connessione, una volta aperta, resta attiva per tutta la sessione
 * dell'applicazione (lo stesso identico modello usato lato server in
 * {@link cinemax.server.rete.GestoreClient}, anche se qui non e' visibile
 * per la separazione tra moduli): non si apre un socket per ogni richiesta,
 * altrimenti si perderebbe la possibilita' di mantenere una sessione
 * autenticata sul server.
 *
 * @author CineMax Team
 */
public class ConnessioneServer implements AutoCloseable {

    private final Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;

    /**
     * Apre la connessione verso serverCM.
     *
     * @param host host del server (es. "localhost")
     * @param porta porta su cui serverCM e' in ascolto
     * @throws IOException se la connessione non puo' essere stabilita
     */
    public ConnessioneServer(String host, int porta) throws IOException {
        this.socket = new Socket(host, porta);
        // L'ordine di creazione degli stream e' importante con
        // ObjectOutputStream/ObjectInputStream: l'output stream va creato e
        // "flushato" (l'header dello stream) prima di creare l'input
        // stream sull'altro lato, altrimenti il primo readObject() del
        // lato che legge per primo si blocca in attesa dell'header. Qui
        // creiamo prima out, lo flush lo fa implicitamente il costruttore
        // di ObjectOutputStream.
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());
    }

    /**
     * Invia un comando al server e attende la risposta. Chiamata bloccante:
     * le schermate Swing che la usano devono farlo fuori dall'Event
     * Dispatch Thread (tipicamente con {@link javax.swing.SwingWorker}) per
     * non congelare l'interfaccia mentre si attende la rete.
     *
     * @param comando   il comando da eseguire
     * @param parametri i parametri posizionali richiesti dal comando (vedi
     *                  il Javadoc di {@code cinemax.server.rete.Dispatcher}
     *                  per il contratto esatto di ciascun comando)
     * @return la risposta del server
     * @throws IOException se la comunicazione con il server fallisce
     */
    public synchronized Risposta invia(Comando comando, Object... parametri) throws IOException {
        out.writeObject(new Richiesta(comando, parametri));
        out.flush();
        // reset() evita che ObjectOutputStream mantenga in cache i vecchi
        // oggetti gia' spediti: senza, un secondo invio con un DTO "simile"
        // ma di valore diverso rischierebbe di essere serializzato in modo
        // errato (fenomeno noto degli stream di oggetti Java riutilizzati a
        // lungo).
        out.reset();
        try {
            return (Risposta) in.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException("Risposta del server non riconosciuta", e);
        }
    }

    /**
     * Invia il comando di logout e chiude la connessione. Va chiamato alla
     * chiusura dell'applicazione o al logout esplicito dell'utente.
     */
    @Override
    public void close() {
        try {
            invia(Comando.LOGOUT);
        } catch (IOException e) {
            // Il server potrebbe essere gia' irraggiungibile: non e' un
            // problema bloccante in fase di chiusura del client.
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {
                // niente da fare se anche la chiusura del socket fallisce
            }
        }
    }
}
