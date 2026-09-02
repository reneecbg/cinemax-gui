/*
 * ============================================================================
 *  CineMax - Laboratorio Interdisciplinare B
 *  Autori:
 *    - <Renee Angelica Cabigting> - Matricola <756997> - Sede <CO>
 *  File: PannelloProiezioniPianificate.java
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Schermata "proiezioni pianificate" del proiezionista (data futura):
 * permette anche di modificarle o eliminarle, se non hanno ancora
 * prenotazioni (vincolo garantito dal trigger di {@code schema.sql}, non
 * ricontrollato qui: l'eventuale rifiuto arriva come messaggio d'errore).
 *
 * @author CineMax Team
 */
public class PannelloProiezioniPianificate extends JPanel {

    private static final DateTimeFormatter FORMATO_DATA_ORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final TabellaProiezioni tabella = new TabellaProiezioni();
    private final FinestraPrincipale finestra;

    public PannelloProiezioniPianificate(FinestraPrincipale finestra) {
        this.finestra = finestra;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        add(new JLabel("Proiezioni pianificate"), BorderLayout.NORTH);
        add(new JScrollPane(tabella), BorderLayout.CENTER);

        JPanel barraInferiore = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton bottoneModifica = new JButton("Modifica data/costo");
        JButton bottoneElimina = new JButton("Elimina");
        JButton bottoneIndietro = new JButton("Indietro");
        barraInferiore.add(bottoneIndietro);
        barraInferiore.add(bottoneModifica);
        barraInferiore.add(bottoneElimina);
        add(barraInferiore, BorderLayout.SOUTH);

        bottoneIndietro.addActionListener(e -> finestra.mostraPannello(new PannelloAreaProiezionista(finestra)));

        bottoneModifica.addActionListener(e -> {
            ProiezioneDTO selezionata = tabella.getProiezioneSelezionata();
            if (selezionata == null) {
                JOptionPane.showMessageDialog(this, "Seleziona prima una proiezione dalla tabella.");
                return;
            }
            String nuovaDataTesto = JOptionPane.showInputDialog(this,
                    "Nuova data e ora (gg/mm/aaaa hh:mm):",
                    selezionata.getDataOra().format(FORMATO_DATA_ORA));
            if (nuovaDataTesto == null) {
                return; // annullato
            }
            String nuovoCostoTesto = JOptionPane.showInputDialog(this,
                    "Nuovo costo biglietto (€):",
                    String.format("%.2f", selezionata.getCostoBiglietto()));
            if (nuovoCostoTesto == null) {
                return;
            }
            LocalDateTime nuovaData;
            double nuovoCosto;
            try {
                nuovaData = LocalDateTime.parse(nuovaDataTesto.trim(), FORMATO_DATA_ORA);
                nuovoCosto = Double.parseDouble(nuovoCostoTesto.trim());
            } catch (DateTimeParseException | NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Formato data o costo non valido.");
                return;
            }
            finestra.eseguiRichiesta(
                    () -> finestra.getConnessione().invia(Comando.MODIFICA_PROIEZIONE,
                            selezionata.getIdProiezione(), nuovaData, nuovoCosto),
                    dato -> {
                        JOptionPane.showMessageDialog(this, "Proiezione modificata.");
                        ricarica();
                    });
        });

        bottoneElimina.addActionListener(e -> {
            ProiezioneDTO selezionata = tabella.getProiezioneSelezionata();
            if (selezionata == null) {
                JOptionPane.showMessageDialog(this, "Seleziona prima una proiezione dalla tabella.");
                return;
            }
            int conferma = JOptionPane.showConfirmDialog(this,
                    "Eliminare la proiezione di \"" + selezionata.getFilm().getTitolo() + "\"?",
                    "Conferma eliminazione", JOptionPane.YES_NO_OPTION);
            if (conferma != JOptionPane.YES_OPTION) {
                return;
            }
            finestra.eseguiRichiesta(
                    () -> finestra.getConnessione().invia(Comando.ELIMINA_PROIEZIONE, selezionata.getIdProiezione()),
                    dato -> {
                        JOptionPane.showMessageDialog(this, "Proiezione eliminata.");
                        ricarica();
                    });
        });

        ricarica();
    }

    private void ricarica() {
        finestra.eseguiRichiesta(
                () -> finestra.getConnessione().invia(Comando.PROIEZIONI_PIANIFICATE),
                dato -> tabella.impostaDati((List<ProiezioneDTO>) dato));
    }
}
