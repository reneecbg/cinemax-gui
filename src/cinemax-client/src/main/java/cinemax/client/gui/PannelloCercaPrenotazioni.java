/*
 * ============================================================================
 *  CineMax - Laboratorio Interdisciplinare B
 *  Autori:
 *    - <Renee Angelica Cabigting> - Matricola <756997> - Sede <CO>
 *  File: PannelloCercaPrenotazioni.java
 * ============================================================================
 */
package cinemax.client.gui;

import cinemax.common.dto.PrenotazioneDTO;
import cinemax.common.protocol.Comando;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Schermata "cerca prenotazioni" del bigliettaio: ricerca combinata per
 * codice, nome/cognome cliente, titolo film (parziale) e intervallo date.
 *
 * @author CineMax Team
 */
public class PannelloCercaPrenotazioni extends JPanel {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public PannelloCercaPrenotazioni(FinestraPrincipale finestra) {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel filtri = new JPanel(new GridLayout(2, 6, 6, 6));
        JTextField campoCodice = new JTextField();
        JTextField campoNome = new JTextField();
        JTextField campoCognome = new JTextField();
        JTextField campoTitolo = new JTextField();
        JTextField campoDataDa = new JTextField();
        JTextField campoDataA = new JTextField();

        filtri.add(new JLabel("Codice:"));
        filtri.add(new JLabel("Nome cliente:"));
        filtri.add(new JLabel("Cognome cliente:"));
        filtri.add(new JLabel("Titolo film:"));
        filtri.add(new JLabel("Dal (gg/mm/aaaa):"));
        filtri.add(new JLabel("Al (gg/mm/aaaa):"));
        filtri.add(campoCodice);
        filtri.add(campoNome);
        filtri.add(campoCognome);
        filtri.add(campoTitolo);
        filtri.add(campoDataDa);
        filtri.add(campoDataA);
        add(filtri, BorderLayout.NORTH);

        TabellaPrenotazioni tabella = new TabellaPrenotazioni();
        add(new JScrollPane(tabella), BorderLayout.CENTER);

        JPanel barraInferiore = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton bottoneCerca = new JButton("Cerca");
        JButton bottoneIndietro = new JButton("Indietro");
        barraInferiore.add(bottoneIndietro);
        barraInferiore.add(bottoneCerca);
        add(barraInferiore, BorderLayout.SOUTH);

        bottoneIndietro.addActionListener(e -> finestra.mostraPannello(new PannelloAreaBigliettaio(finestra)));

        bottoneCerca.addActionListener(e -> {
            String codice = testoONull(campoCodice);
            String nome = testoONull(campoNome);
            String cognome = testoONull(campoCognome);
            String titolo = testoONull(campoTitolo);
            LocalDate dataDa, dataA;
            try {
                dataDa = dataONull(campoDataDa);
                dataA = dataONull(campoDataA);
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Formato data non valido: usa gg/mm/aaaa");
                return;
            }
            finestra.eseguiRichiesta(
                    () -> finestra.getConnessione().invia(Comando.CERCA_PRENOTAZIONI,
                            codice, nome, cognome, titolo, dataDa, dataA),
                    dato -> tabella.impostaDati((List<PrenotazioneDTO>) dato));
        });
    }

    private String testoONull(JTextField campo) {
        String testo = campo.getText().trim();
        return testo.isEmpty() ? null : testo;
    }

    private LocalDate dataONull(JTextField campo) {
        String testo = campo.getText().trim();
        return testo.isEmpty() ? null : LocalDate.parse(testo, FORMATO_DATA);
    }
}
