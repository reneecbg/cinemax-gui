/*
 * ============================================================================
 *  CineMax - Laboratorio Interdisciplinare B
 *  Autori:
 *    - <Renee Angelica Cabigting> - Matricola <756997> - Sede <CO>
 *  File: PannelloGuestRisultati.java
 * ============================================================================
 */
package cinemax.client.gui;

import cinemax.common.dto.ProiezioneDTO;
import cinemax.common.protocol.Comando;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

/**
 * Schermata guest richiesta dal Lab B: proiezioni nei tre mesi successivi
 * alla data odierna per il film indicato nel menu iniziale. Non richiede
 * login (comando {@link Comando#CERCA_PROIEZIONI_FILM_GUEST}).
 *
 * @author CineMax Team
 */
public class PannelloGuestRisultati extends JPanel {

    public PannelloGuestRisultati(FinestraPrincipale finestra, String titoloParziale) {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel titolo = new JLabel(
                "Proiezioni nei prossimi 3 mesi per: \"" + titoloParziale + "\"", SwingConstants.LEFT);
        add(titolo, BorderLayout.NORTH);

        TabellaProiezioni tabella = new TabellaProiezioni();
        add(new JScrollPane(tabella), BorderLayout.CENTER);

        JPanel barraInferiore = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton bottoneDettaglio = new JButton("Vedi dettaglio");
        JButton bottoneIndietro = new JButton("Torna al menu");
        barraInferiore.add(bottoneIndietro);
        barraInferiore.add(bottoneDettaglio);
        add(barraInferiore, BorderLayout.SOUTH);

        bottoneIndietro.addActionListener(e -> finestra.mostraPannello(new PannelloMenuIniziale(finestra)));

        bottoneDettaglio.addActionListener(e -> {
            ProiezioneDTO selezionata = tabella.getProiezioneSelezionata();
            if (selezionata == null) {
                JOptionPane.showMessageDialog(this, "Seleziona prima una proiezione dalla tabella.");
                return;
            }
            finestra.mostraPannello(new PannelloDettaglioProiezione(finestra, selezionata.getIdProiezione(), this));
        });

        // Esegue subito la ricerca all'apertura della schermata.
        finestra.eseguiRichiesta(
                () -> finestra.getConnessione().invia(Comando.CERCA_PROIEZIONI_FILM_GUEST, titoloParziale),
                dato -> tabella.impostaDati((List<ProiezioneDTO>) dato));
    }
}
