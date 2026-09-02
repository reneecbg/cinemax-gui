/*
 * ============================================================================
 *  CineMax - Laboratorio Interdisciplinare B
 *  Autori:
 *    - <Renee Angelica Cabigting> - Matricola <756997> - Sede <CO>
 *  File: ProiezioneDTO.java
 * ============================================================================
 */
package cinemax.common.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Rappresentazione "sul filo" di una proiezione, corrispondente alla join
 * tra {@code proiezione} e {@code film} nel database.
 * <p>
 * Include il campo {@code postiLiberi}: pur essendo un dato derivato non
 * memorizzato nel database (vedi {@code progettazione-DB.md}, sezione 3),
 * viene calcolato dal server al momento della query e incluso qui perche'
 * il client non ha (e non deve avere) accesso diretto al database per
 * calcolarlo da solo.
 *
 * @author CineMax Team
 */
public class ProiezioneDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int idProiezione;
    private final FilmDTO film;
    private final LocalDateTime dataOra;
    private final double costoBiglietto;
    private final int postiLiberi;

    public ProiezioneDTO(int idProiezione, FilmDTO film, LocalDateTime dataOra,
                          double costoBiglietto, int postiLiberi) {
        this.idProiezione = idProiezione;
        this.film = film;
        this.dataOra = dataOra;
        this.costoBiglietto = costoBiglietto;
        this.postiLiberi = postiLiberi;
    }

    public int getIdProiezione() {
        return idProiezione;
    }

    public FilmDTO getFilm() {
        return film;
    }

    public LocalDateTime getDataOra() {
        return dataOra;
    }

    public double getCostoBiglietto() {
        return costoBiglietto;
    }

    public int getPostiLiberi() {
        return postiLiberi;
    }

    @Override
    public String toString() {
        return "#" + idProiezione + " " + film.getTitolo() + " - " + dataOra;
    }
}
