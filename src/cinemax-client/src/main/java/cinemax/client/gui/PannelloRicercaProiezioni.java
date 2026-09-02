/*
 * ============================================================================
 *  CineMax - Laboratorio Interdisciplinare B
 *  Autori:
 *    - <Renee Angelica Cabigting> - Matricola <756997> - Sede <CO>
 *  File: PannelloRicercaProiezioni.java
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
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Schermata di ricerca proiezioni con criteri combinati (titolo parziale,
 * genere, intervallo di date, intervallo di costo), tutti opzionali —
 * schermata richiesta esplicitamente dalla traccia, accessibile senza
 * login. E' la stessa identica schermata (nessuna duplicazione) usata poi
 * anche dentro l'area cliente per scegliere la proiezione da prenotare.
 *
 * @author CineMax Team
 */
public class PannelloRicercaProiezioni extends JPanel {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public PannelloRicercaProiezioni(FinestraPrincipale finestra) {
        this(finestra, new PannelloMenuIniziale(finestra));
    }

    /**
     * @param pannelloPrecedente pannello a cui tornare con "Indietro" (permette di
     *                           riusare questa schermata sia dal menu iniziale sia
     *                           dall'area cliente durante una prenotazione)
     */
    public PannelloRicercaProiezioni(FinestraPrincipale finestra, JPanel pannelloPrecedente) {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel filtri = new JPanel(new GridLayout(2, 6, 6, 6));
        JTextField campoTitolo = new JTextField();
        JTextField campoGenere = new JTextField();
        JTextField campoDataDa = new JTextField();
        JTextField campoDataA = new JTextField();
        JTextField campoCostoMin = new JTextField();
        JTextField campoCostoMax = new JTextField();

        filtri.add(new JLabel("Titolo:"));
        filtri.add(new JLabel("Genere:"));
        filtri.add(new JLabel("Dal (gg/mm/aaaa):"));
        filtri.add(new JLabel("Al (gg/mm/aaaa):"));
        filtri.add(new JLabel("Costo min (€):"));
        filtri.add(new JLabel("Costo max (€):"));
        filtri.add(campoTitolo);
        filtri.add(campoGenere);
        filtri.add(campoDataDa);
        filtri.add(campoDataA);
        filtri.add(campoCostoMin);
        filtri.add(campoCostoMax);

        add(filtri, BorderLayout.NORTH);

        TabellaProiezioni tabella = new TabellaProiezioni();
        add(new JScrollPane(tabella), BorderLayout.CENTER);

        JPanel barraInferiore = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton bottoneCerca = new JButton("Cerca");
        JButton bottoneDettaglio = new JButton("Vedi dettaglio");
        JButton bottoneIndietro = new JButton("Indietro");
        barraInferiore.add(bottoneIndietro);
        barraInferiore.add(bottoneCerca);
        barraInferiore.add(bottoneDettaglio);
        add(barraInferiore, BorderLayout.SOUTH);

        bottoneIndietro.addActionListener(e -> finestra.mostraPannello(pannelloPrecedente));

        bottoneCerca.addActionListener(e -> {
            String titolo = testoONull(campoTitolo);
            String genere = testoONull(campoGenere);
            LocalDate dataDa, dataA;
            Double costoMin, costoMax;
            try {
                dataDa = dataONull(campoDataDa);
                dataA = dataONull(campoDataA);
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Formato data non valido: usa gg/mm/aaaa");
                return;
            }
            try {
                costoMin = numeroONull(campoCostoMin);
                costoMax = numeroONull(campoCostoMax);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Formato costo non valido: usa un numero, es. 8.50");
                return;
            }

            finestra.eseguiRichiesta(
                    () -> finestra.getConnessione().invia(Comando.CERCA_PROIEZIONI,
                            titolo, genere, dataDa, dataA, costoMin, costoMax),
                    dato -> tabella.impostaDati((List<ProiezioneDTO>) dato));
        });

        bottoneDettaglio.addActionListener(e -> {
            ProiezioneDTO selezionata = tabella.getProiezioneSelezionata();
            if (selezionata == null) {
                JOptionPane.showMessageDialog(this, "Seleziona prima una proiezione dalla tabella.");
                return;
            }
            finestra.mostraPannello(new PannelloDettaglioProiezione(finestra, selezionata.getIdProiezione(), this));
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

    private Double numeroONull(JTextField campo) {
        String testo = campo.getText().trim();
        return testo.isEmpty() ? null : Double.valueOf(testo);
    }
}
