/*
 * ============================================================================
 *  CineMax - Laboratorio Interdisciplinare B
 *  Autori:
 *    - <Renee Angelica Cabigting> - Matricola <756997> - Sede <CO>
 *  File: UtenteDTO.java
 * ============================================================================
 */
package cinemax.common.dto;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Rappresentazione "sul filo" di un utente.
 * <p>
 * <b>Non contiene la password ne' il suo hash.</b> Il client invia la
 * password in chiaro solo al momento del login/registrazione, all'interno
 * dei parametri della {@link cinemax.common.protocol.Richiesta}; il server
 * la trasforma subito in hash e non la restituisce mai in nessuna risposta,
 * nemmeno cifrata. Questo evita che l'hash (che di per se' e' comunque un
 * segreto da proteggere) viaggi piu' volte del necessario sulla rete.
 *
 * @author CineMax Team
 */
public class UtenteDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String username;
    private final String nome;
    private final String cognome;
    private final LocalDate dataNascita;
    private final String domicilio;
    private final RuoloDTO ruolo;

    public UtenteDTO(String username, String nome, String cognome,
                      LocalDate dataNascita, String domicilio, RuoloDTO ruolo) {
        this.username = username;
        this.nome = nome;
        this.cognome = cognome;
        this.dataNascita = dataNascita;
        this.domicilio = domicilio;
        this.ruolo = ruolo;
    }

    public String getUsername() {
        return username;
    }

    public String getNome() {
        return nome;
    }

    public String getCognome() {
        return cognome;
    }

    public String getNomeCompleto() {
        return nome + " " + cognome;
    }

    public LocalDate getDataNascita() {
        return dataNascita;
    }

    public String getDomicilio() {
        return domicilio;
    }

    public RuoloDTO getRuolo() {
        return ruolo;
    }

    @Override
    public String toString() {
        return getNomeCompleto() + " (" + username + ") - " + ruolo;
    }
}
