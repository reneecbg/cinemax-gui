/*
 * ============================================================================
 *  CineMax - Laboratorio Interdisciplinare B
 *  Autori:
 *    - <Renee Angelica Cabigting> - Matricola <756997> - Sede <CO>
 *  File: FilmDTO.java
 * ============================================================================
 */
package cinemax.common.dto;

import java.io.Serializable;

/**
 * Rappresentazione "sul filo" di un film, corrispondente alla tabella
 * {@code film} del database. E' un semplice contenitore di dati (nessuna
 * logica): la logica di dominio (es. calcolo della sovrapposizione tra
 * proiezioni) resta lato server, dove ha accesso diretto al database.
 *
 * @author CineMax Team
 */
public class FilmDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int idFilm;
    private final String titolo;
    private final String genere;
    private final String regista;
    private final int anno;
    private final int durataMinuti;
    private final int etaMinima;

    public FilmDTO(int idFilm, String titolo, String genere, String regista,
                    int anno, int durataMinuti, int etaMinima) {
        this.idFilm = idFilm;
        this.titolo = titolo;
        this.genere = genere;
        this.regista = regista;
        this.anno = anno;
        this.durataMinuti = durataMinuti;
        this.etaMinima = etaMinima;
    }

    public int getIdFilm() {
        return idFilm;
    }

    public String getTitolo() {
        return titolo;
    }

    public String getGenere() {
        return genere;
    }

    public String getRegista() {
        return regista;
    }

    public int getAnno() {
        return anno;
    }

    public int getDurataMinuti() {
        return durataMinuti;
    }

    public int getEtaMinima() {
        return etaMinima;
    }

    @Override
    public String toString() {
        return titolo + " (" + anno + ") - " + genere;
    }
}
