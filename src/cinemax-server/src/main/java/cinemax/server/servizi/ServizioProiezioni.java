/*
 * ============================================================================
 *  CineMax - Laboratorio Interdisciplinare B
 *  Autori:
 *    - <Renee Angelica Cabigting> - Matricola <756997> - Sede <CO>
 *  File: ServizioProiezioni.java
 * ============================================================================
 */
package cinemax.server.servizi;

import cinemax.common.dto.FilmDTO;
import cinemax.common.dto.ProiezioneDTO;
import cinemax.server.dao.FilmDAO;
import cinemax.server.dao.ProiezioneDAO;
import cinemax.server.eccezioni.EccezioneApplicativa;

import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Servizio applicativo per la consultazione e la gestione delle proiezioni.
 *
 * @author CineMax Team
 */
public class ServizioProiezioni {

    private final ProiezioneDAO proiezioneDAO;
    private final FilmDAO filmDAO;

    public ServizioProiezioni(Connection connessione) {
        this.proiezioneDAO = new ProiezioneDAO(connessione);
        this.filmDAO = new FilmDAO(connessione);
    }

    /** cercaProiezione(): ricerca combinata, tutti i criteri opzionali. */
    public List<ProiezioneDTO> cercaProiezioni(String titolo, String genere,
                                                LocalDate dataDa, LocalDate dataA,
                                                Double costoMin, Double costoMax) {
        return proiezioneDAO.cerca(titolo, genere, dataDa, dataA, costoMin, costoMax);
    }

    /** Schermata guest: proiezioni di un film nei tre mesi successivi. */
    public List<ProiezioneDTO> cercaProssimiTreMesi(String titoloParziale) {
        if (titoloParziale == null || titoloParziale.isBlank()) {
            throw new EccezioneApplicativa("Indicare almeno una parte del titolo del film");
        }
        return proiezioneDAO.cercaFilmProssimiTreMesi(titoloParziale);
    }

    /** visualizzaProiezione(): dettaglio di una proiezione. */
    public ProiezioneDTO dettaglio(int idProiezione) {
        ProiezioneDTO p = proiezioneDAO.trovaPerId(idProiezione);
        if (p == null) {
            throw new EccezioneApplicativa("Proiezione non trovata: " + idProiezione);
        }
        return p;
    }

    /** Proiezioni pianificate (proiezionista): data futura. */
    public List<ProiezioneDTO> pianificate() {
        return proiezioneDAO.pianificate();
    }

    /** Proiezioni storiche (proiezionista): data passata. */
    public List<ProiezioneDTO> storiche() {
        return proiezioneDAO.storiche();
    }

    /**
     * aggiungiProiezione(): inserisce un film (riusandolo se gia' presente
     * con lo stesso titolo/regista/anno) e la relativa proiezione.
     * <p>
     * Il controllo di sovrapposizione con altre proiezioni nella stessa sala
     * e' delegato al trigger {@code trg_verifica_sovrapposizione}: se
     * l'INSERT lo viola, arriva qui come {@link EccezioneApplicativa} (vedi
     * {@link cinemax.server.eccezioni.GestoreEccezioniSql}) e si propaga
     * cosi' com'e' al chiamante.
     */
    public ProiezioneDTO aggiungiProiezione(String titolo, String genere, String regista,
                                             int anno, int durataMinuti, int etaMinima,
                                             LocalDateTime dataOra, double costoBiglietto) {
        if (!dataOra.isAfter(LocalDateTime.now())) {
            throw new EccezioneApplicativa("La data della proiezione deve essere futura");
        }
        FilmDTO film = filmDAO.trovaPerTitoloRegistaAnno(titolo, regista, anno);
        if (film == null) {
            film = filmDAO.inserisci(titolo, genere, regista, anno, durataMinuti, etaMinima);
        }
        return proiezioneDAO.inserisci(film.getIdFilm(), dataOra, costoBiglietto);
    }

    /**
     * modificaProiezione(): il trigger {@code trg_blocca_update_proiezione}
     * impedisce la modifica se esistono gia' prenotazioni; l'eventuale
     * eccezione viene propagata cosi' com'e'.
     */
    public void modificaProiezione(int idProiezione, LocalDateTime nuovaDataOra, double nuovoCosto) {
        if (!nuovaDataOra.isAfter(LocalDateTime.now())) {
            throw new EccezioneApplicativa("La nuova data della proiezione deve essere futura");
        }
        proiezioneDAO.modifica(idProiezione, nuovaDataOra, nuovoCosto);
    }

    /**
     * eliminaProiezione(): il trigger {@code trg_blocca_delete_proiezione}
     * impedisce l'eliminazione se esistono gia' prenotazioni.
     */
    public void eliminaProiezione(int idProiezione) {
        proiezioneDAO.elimina(idProiezione);
    }
}
