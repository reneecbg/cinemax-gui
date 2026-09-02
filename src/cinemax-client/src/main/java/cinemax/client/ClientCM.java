/*
 * ============================================================================
 *  CineMax - Laboratorio Interdisciplinare B
 *  Autori:
 *    - <Renee Angelica Cabigting> - Matricola <756997> - Sede <CO>
 *  File: ClientCM.java
 * ============================================================================
 */
package cinemax.client;

import cinemax.client.gui.FinestraPrincipale;
import cinemax.client.gui.TemaCineMax;
import cinemax.client.rete.ConnessioneServer;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.io.IOException;

/**
 * Punto di ingresso del modulo clientCM.
 * <p>
 * A differenza di {@code ServerCM} (che chiede i parametri da riga di
 * comando, perché non ha interfaccia grafica), qui i parametri di
 * connessione sono chiesti con semplici finestre di dialogo Swing, dato che
 * l'intera applicazione client è pensata per essere grafica fin dal primo
 * avvio.
 *
 * @author CineMax Team
 */
public final class ClientCM {

    private ClientCM() {
        // entry point, non istanziabile
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Applica il tema visivo prima di creare qualsiasi finestra:
            // Nimbus e la palette colori devono essere impostati prima che
            // il primo componente Swing venga istanziato per avere effetto.
            TemaCineMax.applica();
            avvia();
        });
    }

    private static void avvia() {
        ConnessioneServer connessione = connettiFinoARiuscita();
        if (connessione == null) {
            return; // l'utente ha annullato la connessione
        }
        new FinestraPrincipale(connessione).setVisible(true);
    }

    /**
     * Chiede host e porta del server e tenta la connessione, riprovando
     * finché non riesce o l'utente annulla.
     *
     * @return la connessione stabilita, oppure {@code null} se annullata dall'utente
     */
    private static ConnessioneServer connettiFinoARiuscita() {
        String host = "localhost";
        String porta = "5000";
        while (true) {
            host = JOptionPane.showInputDialog(null, "Host del server CineMax:", host);
            if (host == null) {
                return null;
            }
            porta = JOptionPane.showInputDialog(null, "Porta del server CineMax:", porta);
            if (porta == null) {
                return null;
            }
            try {
                int numeroPorta = Integer.parseInt(porta.trim());
                return new ConnessioneServer(host.trim(), numeroPorta);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "La porta deve essere un numero.");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null,
                        "Impossibile connettersi a " + host + ":" + porta + " - " + e.getMessage(),
                        "Connessione fallita", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
