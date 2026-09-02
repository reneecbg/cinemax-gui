/*
 * ============================================================================
 *  CineMax - Laboratorio Interdisciplinare B
 *  Autori:
 *    - <Renee Angelica Cabigting> - Matricola <756997> - Sede <CO>
 *  File: FinestraPrincipale.java
 * ============================================================================
 */
package cinemax.client.gui;

import cinemax.common.dto.UtenteDTO;
import cinemax.client.rete.ChiamataServer;
import cinemax.client.rete.ConnessioneServer;
import cinemax.common.protocol.Risposta;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import java.awt.Dimension;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * Finestra principale dell'applicazione clientCM.
 * <p>
 * Fa da "guscio" attorno a un unico {@link JPanel} alla volta (il
 * pannello corrente viene sostituito con {@link #mostraPannello(JPanel)}
 * invece di usare un {@code CardLayout} con pannelli pre-costruiti: dato
 * che molte schermate hanno bisogno di parametri per essere costruite
 * -es. il dettaglio di UNA proiezione specifica- e' piu' semplice crearle
 * al momento della navigazione che tenerle tutte pronte in memoria).
 * <p>
 * Mantiene anche lo stato di sessione condiviso da tutte le schermate:
 * la {@link ConnessioneServer} aperta e l'{@link UtenteDTO} eventualmente
 * autenticato (null se si sta navigando come guest).
 *
 * @author CineMax Team
 */
public class FinestraPrincipale extends JFrame {

    private final ConnessioneServer connessione;
    private UtenteDTO utenteCorrente;

    public FinestraPrincipale(ConnessioneServer connessione) {
        super("CineMax");
        this.connessione = connessione;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(720, 480));
        setLocationRelativeTo(null);
        mostraPannello(new PannelloMenuIniziale(this));
    }

    /**
     * Sostituisce la schermata correntemente visibile.
     *
     * @param pannello il nuovo pannello da mostrare
     */
    public void mostraPannello(JPanel pannello) {
        setContentPane(pannello);
        revalidate();
        repaint();
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * @return la connessione al server, per le schermate che devono
     *         costruire una chiamata personalizzata (uso tipico:
     *         {@code finestra.getConnessione().invia(Comando.X, ...)}
     *         dentro il primo argomento di {@link #eseguiRichiesta})
     */
    public ConnessioneServer getConnessione() {
        return connessione;
    }

    /**
     * @return l'utente autenticato, oppure {@code null} se si sta navigando come guest
     */
    public UtenteDTO getUtenteCorrente() {
        return utenteCorrente;
    }

    public void setUtenteCorrente(UtenteDTO utente) {
        this.utenteCorrente = utente;
    }

    /** Termina la sessione (logout) e torna al menu iniziale. */
    public void logout() {
        this.utenteCorrente = null;
        mostraPannello(new PannelloMenuIniziale(this));
    }

    /**
     * Esegue una chiamata al server fuori dall'Event Dispatch Thread (con
     * {@link SwingWorker}), cosi' la finestra non si blocca mentre si
     * aspetta la rete, e gestisce in modo uniforme i tre possibili esiti
     * (successo, errore applicativo, errore tecnico/di rete) mostrando un
     * messaggio comprensibile invece di uno stack trace.
     * <p>
     * Ogni schermata (Step 7) usa questo metodo invece di parlare
     * direttamente con {@link ConnessioneServer}, cosi' la gestione degli
     * errori e del threading e' scritta una volta sola.
     *
     * @param chiamata    la chiamata al server da eseguire in background
     * @param alSuccesso  cosa fare con il dato restituito, se l'esito e' OK
     *                    (eseguito di nuovo sull'Event Dispatch Thread,
     *                    quindi puo' aggiornare direttamente componenti Swing)
     */
    public void eseguiRichiesta(ChiamataServer chiamata, Consumer<Object> alSuccesso) {
        new SwingWorker<Risposta, Void>() {
            @Override
            protected Risposta doInBackground() throws IOException {
                return chiamata.chiama();
            }

            @Override
            protected void done() {
                try {
                    Risposta risposta = get();
                    if (risposta.isOk()) {
                        alSuccesso.accept(risposta.getDato());
                    } else {
                        JOptionPane.showMessageDialog(FinestraPrincipale.this,
                                risposta.getMessaggio(), "Operazione non riuscita",
                                JOptionPane.WARNING_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(FinestraPrincipale.this,
                            "Errore di comunicazione con il server: " + e.getCause(),
                            "Errore di rete", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
}
