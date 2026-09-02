/*
 * ============================================================================
 *  CineMax - Laboratorio Interdisciplinare B
 *  Autori:
 *    - <Renee Angelica Cabigting> - Matricola <756997> - Sede <CO>
 *  File: PannelloPrenotazioniOggi.java
 * ============================================================================
 */
package cinemax.client.gui;

import cinemax.common.dto.PrenotazioneDTO;
import cinemax.common.protocol.Comando;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

/**
 * Schermata "prenotazioni di oggi" del bigliettaio.
 *
 * @author CineMax Team
 */
public class PannelloPrenotazioniOggi extends JPanel {

    public PannelloPrenotazioniOggi(FinestraPrincipale finestra) {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        add(new JLabel("Prenotazioni di oggi"), BorderLayout.NORTH);

        TabellaPrenotazioni tabella = new TabellaPrenotazioni();
        add(new JScrollPane(tabella), BorderLayout.CENTER);

        JPanel barraInferiore = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton bottoneIndietro = new JButton("Indietro");
        barraInferiore.add(bottoneIndietro);
        add(barraInferiore, BorderLayout.SOUTH);
        bottoneIndietro.addActionListener(e -> finestra.mostraPannello(new PannelloAreaBigliettaio(finestra)));

        finestra.eseguiRichiesta(
                () -> finestra.getConnessione().invia(Comando.PRENOTAZIONI_DI_OGGI),
                dato -> tabella.impostaDati((List<PrenotazioneDTO>) dato));
    }
}
