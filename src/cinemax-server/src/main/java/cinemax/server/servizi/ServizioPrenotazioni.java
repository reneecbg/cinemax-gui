/*
 * ============================================================================
 *  CineMax - Laboratorio Interdisciplinare B
 *  Autori:
 *    - <Renee Angelica Cabigting> - Matricola <756997> - Sede <CO>
 *  File: ServizioPrenotazioni.java
 * ============================================================================
 */
package cinemax.server.servizi;

import cinemax.common.dto.PrenotazioneDTO;
import cinemax.common.dto.ProiezioneDTO;
import cinemax.server.dao.PrenotazioneDAO;
import cinemax.server.dao.ProiezioneDAO;
import cinemax.server.eccezioni.EccezioneApplicativa;

import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Servizio applicativo per la gestione delle prenotazioni.
 * <p>
 * <b>Nota sulla regola di cancellazione/modifica.</b> Il documento del Lab A
 * richiedeva testualmente di poter eliminare una prenotazione solo se la
 * proiezione fosse gia' passata; il documento del Lab B descrive invece la
 * schermata cliente come gestione delle "prenotazioni attive (relative ad
 * una proiezione successiva alla data odierna)". Qui si e' seguita la
 * versione Lab B (piu' recente e coerente con l'uso comune: si annulla una
 * prenotazione futura, non una passata): sia {@link #modificaPrenotazione}
 * che {@link #eliminaPrenotazione} operano solo su prenotazioni la cui
 * proiezione e' futura. Va confermato con il docente quale delle due
 * versioni e' quella attesa in sede di valutazione.
 *
 * @author CineMax Team
 */
public class ServizioPrenotazioni {

    private final PrenotazioneDAO prenotazioneDAO;
    private final ProiezioneDAO proiezioneDAO;

    public ServizioPrenotazioni(Connection connessione) {
        this.prenotazioneDAO = new PrenotazioneDAO(connessione);
        this.proiezioneDAO = new ProiezioneDAO(connessione);
    }

    /**
     * creaPrenotazione(): il controllo sui posti disponibili e' delegato al
     * trigger {@code trg_verifica_capienza} (vedi Step 1 e Step 3): questo
     * metodo si limita a verificare che la proiezione esista e non sia gia'
     * passata, poi inoltra la richiesta al DAO.
     */
    public PrenotazioneDTO creaPrenotazione(String usernameCliente, int idProiezione, int numeroBiglietti) {
        if (numeroBiglietti <= 0) {
            throw new EccezioneApplicativa("Il numero di biglietti deve essere maggiore di zero");
        }
        ProiezioneDTO proiezione = proiezioneDAO.trovaPerId(idProiezione);
        if (proiezione == null) {
            throw new EccezioneApplicativa("Proiezione non trovata: " + idProiezione);
        }
        if (!proiezione.getDataOra().isAfter(LocalDateTime.now())) {
            throw new EccezioneApplicativa("Non e' possibile prenotare una proiezione gia' passata");
        }
        return prenotazioneDAO.crea(usernameCliente, idProiezione, numeroBiglietti);
    }

    /** Prenotazioni attive (future) del cliente autenticato. */
    public List<PrenotazioneDTO> prenotazioniAttive(String usernameCliente) {
        return prenotazioneDAO.attivePerCliente(usernameCliente);
    }

    /**
     * modificaPrenotazione() (cambio proiezione): sia la vecchia che la
     * nuova proiezione devono essere future, secondo entrambi i documenti
     * dei requisiti (qui non c'e' discrepanza tra Lab A e Lab B).
     */
    public void modificaPrenotazione(String codice, int nuovoIdProiezione) {
        PrenotazioneDTO esistente = prenotazioneDAO.trovaPerCodice(codice);
        if (esistente == null) {
            throw new EccezioneApplicativa("Prenotazione non trovata: " + codice);
        }
        if (!esistente.getProiezione().getDataOra().isAfter(LocalDateTime.now())) {
            throw new EccezioneApplicativa("La prenotazione originale non e' piu' modificabile: proiezione gia' passata");
        }
        ProiezioneDTO nuovaProiezione = proiezioneDAO.trovaPerId(nuovoIdProiezione);
        if (nuovaProiezione == null) {
            throw new EccezioneApplicativa("Proiezione non trovata: " + nuovoIdProiezione);
        }
        if (!nuovaProiezione.getDataOra().isAfter(LocalDateTime.now())) {
            throw new EccezioneApplicativa("La nuova proiezione scelta deve essere futura");
        }
        prenotazioneDAO.modifica(codice, nuovoIdProiezione);
    }

    /**
     * eliminaPrenotazione(): consentita solo se la proiezione associata e'
     * futura (vedi nota della classe sulla discrepanza Lab A / Lab B).
     */
    public void eliminaPrenotazione(String codice) {
        PrenotazioneDTO esistente = prenotazioneDAO.trovaPerCodice(codice);
        if (esistente == null) {
            throw new EccezioneApplicativa("Prenotazione non trovata: " + codice);
        }
        if (!esistente.getProiezione().getDataOra().isAfter(LocalDateTime.now())) {
            throw new EccezioneApplicativa("Non e' possibile cancellare una prenotazione di una proiezione gia' passata");
        }
        prenotazioneDAO.elimina(codice);
    }

    /** cercaPrenotazione() (bigliettaio): ricerca combinata. */
    public List<PrenotazioneDTO> cercaPrenotazioni(String codice, String nome, String cognome,
                                                    String titoloFilm, LocalDate dataDa, LocalDate dataA) {
        return prenotazioneDAO.cerca(codice, nome, cognome, titoloFilm, dataDa, dataA);
    }

    /** Prenotazioni relative a proiezioni della data odierna (bigliettaio). */
    public List<PrenotazioneDTO> prenotazioniDiOggi() {
        return prenotazioneDAO.diOggi();
    }
}
