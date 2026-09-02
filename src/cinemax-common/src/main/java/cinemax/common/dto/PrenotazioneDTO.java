/*
 * ============================================================================
 *  CineMax - Laboratorio Interdisciplinare B
 *  Autori:
 *    - <Renee Angelica Cabigting> - Matricola <756997> - Sede <CO>
 *  File: PrenotazioneDTO.java
 * ============================================================================
 */
package cinemax.common.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Rappresentazione "sul filo" di una prenotazione, gia' arricchita con i
 * dati di proiezione/film/cliente utili alla GUI (evitando cosi' che il
 * client debba fare ulteriori richieste solo per mostrare una riga di
 * elenco o un dettaglio).
 *
 * @author CineMax Team
 */
public class PrenotazioneDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String codice;
    private final String usernameCliente;
    private final String nomeCompletoCliente;
    private final ProiezioneDTO proiezione;
    private final int numeroBiglietti;
    private final LocalDateTime dataCreazione;

    public PrenotazioneDTO(String codice, String usernameCliente, String nomeCompletoCliente,
                            ProiezioneDTO proiezione, int numeroBiglietti,
                            LocalDateTime dataCreazione) {
        this.codice = codice;
        this.usernameCliente = usernameCliente;
        this.nomeCompletoCliente = nomeCompletoCliente;
        this.proiezione = proiezione;
        this.numeroBiglietti = numeroBiglietti;
        this.dataCreazione = dataCreazione;
    }

    public String getCodice() {
        return codice;
    }

    public String getUsernameCliente() {
        return usernameCliente;
    }

    public String getNomeCompletoCliente() {
        return nomeCompletoCliente;
    }

    public ProiezioneDTO getProiezione() {
        return proiezione;
    }

    public int getNumeroBiglietti() {
        return numeroBiglietti;
    }

    public LocalDateTime getDataCreazione() {
        return dataCreazione;
    }

    /**
     * Calcola il costo totale della prenotazione a partire dal costo del
     * biglietto della proiezione associata.
     *
     * @return il costo totale in euro
     */
    public double getCostoTotale() {
        return proiezione.getCostoBiglietto() * numeroBiglietti;
    }

    @Override
    public String toString() {
        return codice + " - " + proiezione.getFilm().getTitolo() + " x" + numeroBiglietti;
    }
}
