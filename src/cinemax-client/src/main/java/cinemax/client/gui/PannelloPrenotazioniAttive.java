/*
 * ============================================================================
 *  CineMax - Laboratorio Interdisciplinare B
 *  Autori:
 *    - <Renee Angelica Cabigting> - Matricola <756997> - Sede <CO>
 *  File: PannelloPrenotazioniAttive.java
 * ============================================================================
 */
package cinemax.client.gui;

import cinemax.common.dto.PrenotazioneDTO;
import cinemax.common.dto.ProiezioneDTO;
import cinemax.common.protocol.Comando;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

/**
 * Schermata "le mie prenotazioni attive" del cliente — richiesta
 * esplicitamente dalla traccia come relativa a proiezioni successive alla
 * data odierna (il filtro è già applicato dal server, vedi
 * {@code ServizioPrenotazioni#prenotazioniAttive}). Permette anche di
 * modificarle (cambio proiezione) o cancellarle.
 *
 * @author CineMax Team
 */
public class PannelloPrenotazioniAttive extends JPanel {

    private final TabellaPrenotazioni tabella = new TabellaPrenotazioni();
    private final FinestraPrincipale finestra;

    public PannelloPrenotazioniAttive(FinestraPrincipale finestra) {
        this.finestra = finestra;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        add(new JLabel("Le mie prenotazioni attive"), BorderLayout.NORTH);
        add(new JScrollPane(tabella), BorderLayout.CENTER);

        JPanel barraInferiore = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton bottoneModifica = new JButton("Modifica (cambia proiezione)");
        JButton bottoneElimina = new JButton("Cancella prenotazione");
        JButton bottoneIndietro = new JButton("Indietro");
        barraInferiore.add(bottoneIndietro);
        barraInferiore.add(bottoneModifica);
        barraInferiore.add(bottoneElimina);
        add(barraInferiore, BorderLayout.SOUTH);

        bottoneIndietro.addActionListener(e -> finestra.mostraPannello(new PannelloAreaCliente(finestra)));

        bottoneModifica.addActionListener(e -> {
            PrenotazioneDTO selezionata = tabella.getPrenotazioneSelezionata();
            if (selezionata == null) {
                JOptionPane.showMessageDialog(this, "Seleziona prima una prenotazione dalla tabella.");
                return;
            }
            DialogoSelezionaProiezione dialogo = new DialogoSelezionaProiezione(finestra);
            dialogo.setVisible(true); // modale: si blocca finche' non viene chiuso
            ProiezioneDTO nuovaProiezione = dialogo.getProiezioneSelezionata();
            if (nuovaProiezione == null) {
                return; // annullato dall'utente
            }
            finestra.eseguiRichiesta(
                    () -> finestra.getConnessione().invia(Comando.MODIFICA_PRENOTAZIONE,
                            selezionata.getCodice(), nuovaProiezione.getIdProiezione()),
                    dato -> {
                        JOptionPane.showMessageDialog(this, "Prenotazione modificata.");
                        ricarica();
                    });
        });

        bottoneElimina.addActionListener(e -> {
            PrenotazioneDTO selezionata = tabella.getPrenotazioneSelezionata();
            if (selezionata == null) {
                JOptionPane.showMessageDialog(this, "Seleziona prima una prenotazione dalla tabella.");
                return;
            }
            int conferma = JOptionPane.showConfirmDialog(this,
                    "Cancellare la prenotazione " + selezionata.getCodice() + "?",
                    "Conferma cancellazione", JOptionPane.YES_NO_OPTION);
            if (conferma != JOptionPane.YES_OPTION) {
                return;
            }
            finestra.eseguiRichiesta(
                    () -> finestra.getConnessione().invia(Comando.ELIMINA_PRENOTAZIONE, selezionata.getCodice()),
                    dato -> {
                        JOptionPane.showMessageDialog(this, "Prenotazione cancellata.");
                        ricarica();
                    });
        });

        ricarica();
    }

    private void ricarica() {
        finestra.eseguiRichiesta(
                () -> finestra.getConnessione().invia(Comando.PRENOTAZIONI_ATTIVE_CLIENTE,
                        finestra.getUtenteCorrente().getUsername()),
                dato -> tabella.impostaDati((List<PrenotazioneDTO>) dato));
    }
}
