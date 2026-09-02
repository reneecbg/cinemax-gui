/*
 * ============================================================================
 *  CineMax - Laboratorio Interdisciplinare B
 *  Autori:
 *    - <Renee Angelica Cabigting> - Matricola <756997> - Sede <CO>
 *  File: DialogoSelezionaProiezione.java
 * ============================================================================
 */
package cinemax.client.gui;

import cinemax.common.dto.ProiezioneDTO;
import cinemax.common.protocol.Comando;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;

/**
 * Finestra di dialogo modale per cercare (per titolo) e selezionare una
 * proiezione. Usata dal cliente per scegliere la nuova proiezione quando
 * modifica una prenotazione esistente ("cambio data"): evita di duplicare
 * l'intera schermata di ricerca solo per questo caso d'uso.
 *
 * @author CineMax Team
 */
public class DialogoSelezionaProiezione extends JDialog {

    private ProiezioneDTO proiezioneSelezionata;

    public DialogoSelezionaProiezione(FinestraPrincipale finestra) {
        super(finestra, "Seleziona una nuova proiezione", true);
        setPreferredSize(new Dimension(600, 400));

        setLayout(new BorderLayout(8, 8));

        JPanel barraRicerca = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField campoTitolo = new JTextField(20);
        JButton bottoneCerca = new JButton("Cerca");
        barraRicerca.add(new JLabel("Titolo:"));
        barraRicerca.add(campoTitolo);
        barraRicerca.add(bottoneCerca);
        add(barraRicerca, BorderLayout.NORTH);

        TabellaProiezioni tabella = new TabellaProiezioni();
        add(new JScrollPane(tabella), BorderLayout.CENTER);

        JPanel barraInferiore = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton bottoneSeleziona = new JButton("Seleziona");
        JButton bottoneAnnulla = new JButton("Annulla");
        barraInferiore.add(bottoneAnnulla);
        barraInferiore.add(bottoneSeleziona);
        add(barraInferiore, BorderLayout.SOUTH);

        bottoneCerca.addActionListener(e -> {
            String titolo = campoTitolo.getText().trim();
            finestra.eseguiRichiesta(
                    () -> finestra.getConnessione().invia(Comando.CERCA_PROIEZIONI,
                            titolo.isEmpty() ? null : titolo, null, null, null, null, null),
                    dato -> tabella.impostaDati((List<ProiezioneDTO>) dato));
        });

        bottoneSeleziona.addActionListener(e -> {
            ProiezioneDTO scelta = tabella.getProiezioneSelezionata();
            if (scelta == null) {
                JOptionPane.showMessageDialog(this, "Seleziona prima una proiezione dalla tabella.");
                return;
            }
            this.proiezioneSelezionata = scelta;
            dispose();
        });

        bottoneAnnulla.addActionListener(e -> {
            this.proiezioneSelezionata = null;
            dispose();
        });

        pack();
        setLocationRelativeTo(finestra);
    }

    /** @return la proiezione scelta, oppure {@code null} se il dialogo è stato annullato */
    public ProiezioneDTO getProiezioneSelezionata() {
        return proiezioneSelezionata;
    }
}
