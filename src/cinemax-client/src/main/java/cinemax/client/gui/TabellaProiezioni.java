/*
 * ============================================================================
 *  CineMax - Laboratorio Interdisciplinare B
 *  Autori:
 *    - <Renee Angelica Cabigting> - Matricola <756997> - Sede <CO>
 *  File: TabellaProiezioni.java
 * ============================================================================
 */
package cinemax.client.gui;

import cinemax.common.dto.ProiezioneDTO;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Componente riutilizzabile: una {@link JTable} che mostra un elenco di
 * {@link ProiezioneDTO}, usata da tutte le schermate di ricerca/elenco
 * proiezioni (guest, ricerca generica, pianificate, storiche) per non
 * duplicare la stessa logica di formattazione in quattro punti diversi.
 *
 * @author CineMax Team
 */
public class TabellaProiezioni extends JTable {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String[] COLONNE = {"Id", "Titolo", "Genere", "Data e ora", "Costo (€)", "Posti liberi"};

    private List<ProiezioneDTO> proiezioni = List.of();

    public TabellaProiezioni() {
        super(new DefaultTableModel(COLONNE, 0) {
            @Override
            public boolean isCellEditable(int riga, int colonna) {
                return false;
            }
        });
        setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    }

    /** Sostituisce il contenuto della tabella con il nuovo elenco di proiezioni. */
    public void impostaDati(List<ProiezioneDTO> nuoveProiezioni) {
        this.proiezioni = nuoveProiezioni;
        DefaultTableModel modello = (DefaultTableModel) getModel();
        modello.setRowCount(0);
        for (ProiezioneDTO p : nuoveProiezioni) {
            modello.addRow(new Object[]{
                    p.getIdProiezione(),
                    p.getFilm().getTitolo(),
                    p.getFilm().getGenere(),
                    p.getDataOra().format(FORMATO),
                    String.format("%.2f", p.getCostoBiglietto()),
                    p.getPostiLiberi()
            });
        }
    }

    /** @return la proiezione corrispondente alla riga selezionata, oppure {@code null} */
    public ProiezioneDTO getProiezioneSelezionata() {
        int riga = getSelectedRow();
        return riga >= 0 && riga < proiezioni.size() ? proiezioni.get(riga) : null;
    }
}
