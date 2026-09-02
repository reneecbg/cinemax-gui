/*
 * ============================================================================
 *  CineMax - Laboratorio Interdisciplinare B
 *  Autori:
 *    - <Renee Angelica Cabigting> - Matricola <756997> - Sede <CO>
 *  File: PannelloProiezioniStoriche.java
 * ============================================================================
 */
package cinemax.client.gui;

import cinemax.common.dto.ProiezioneDTO;
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
 * Schermata "proiezioni storiche" del proiezionista (data passata): sola
 * visualizzazione, nessuna azione di modifica prevista dalla traccia.
 *
 * @author CineMax Team
 */
public class PannelloProiezioniStoriche extends JPanel {

    public PannelloProiezioniStoriche(FinestraPrincipale finestra) {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        add(new JLabel("Proiezioni storiche"), BorderLayout.NORTH);

        TabellaProiezioni tabella = new TabellaProiezioni();
        add(new JScrollPane(tabella), BorderLayout.CENTER);

        JPanel barraInferiore = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton bottoneIndietro = new JButton("Indietro");
        barraInferiore.add(bottoneIndietro);
        add(barraInferiore, BorderLayout.SOUTH);
        bottoneIndietro.addActionListener(e -> finestra.mostraPannello(new PannelloAreaProiezionista(finestra)));

        finestra.eseguiRichiesta(
                () -> finestra.getConnessione().invia(Comando.PROIEZIONI_STORICHE),
                dato -> tabella.impostaDati((List<ProiezioneDTO>) dato));
    }
}
