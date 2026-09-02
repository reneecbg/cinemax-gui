/*
 * ============================================================================
 *  CineMax - Laboratorio Interdisciplinare B
 *  Autori:
 *    - <Renee Angelica Cabigting> - Matricola <756997> - Sede <CO>
 *  File: TabellaPrenotazioni.java
 * ============================================================================
 */
package cinemax.client.gui;

import cinemax.common.dto.PrenotazioneDTO;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Componente riutilizzabile: elenco di {@link PrenotazioneDTO}, usato sia
 * dalla schermata "le mie prenotazioni" del cliente sia dalle due schermate
 * del bigliettaio (prenotazioni di oggi, ricerca prenotazioni).
 *
 * @author CineMax Team
 */
public class TabellaPrenotazioni extends JTable {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String[] COLONNE =
            {"Codice", "Cliente", "Film", "Data e ora proiezione", "Biglietti", "Totale (€)"};

    private List<PrenotazioneDTO> prenotazioni = List.of();

    public TabellaPrenotazioni() {
        super(new DefaultTableModel(COLONNE, 0) {
            @Override
            public boolean isCellEditable(int riga, int colonna) {
                return false;
            }
        });
        setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    }

    public void impostaDati(List<PrenotazioneDTO> nuovePrenotazioni) {
        this.prenotazioni = nuovePrenotazioni;
        DefaultTableModel modello = (DefaultTableModel) getModel();
        modello.setRowCount(0);
        for (PrenotazioneDTO p : nuovePrenotazioni) {
            modello.addRow(new Object[]{
                    p.getCodice(),
                    p.getNomeCompletoCliente(),
                    p.getProiezione().getFilm().getTitolo(),
                    p.getProiezione().getDataOra().format(FORMATO),
                    p.getNumeroBiglietti(),
                    String.format("%.2f", p.getCostoTotale())
            });
        }
    }

    /** @return la prenotazione corrispondente alla riga selezionata, oppure {@code null} */
    public PrenotazioneDTO getPrenotazioneSelezionata() {
        int riga = getSelectedRow();
        return riga >= 0 && riga < prenotazioni.size() ? prenotazioni.get(riga) : null;
    }
}
