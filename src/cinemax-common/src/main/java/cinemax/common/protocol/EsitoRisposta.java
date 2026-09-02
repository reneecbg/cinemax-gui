/*
 * ============================================================================
 *  CineMax - Laboratorio Interdisciplinare B
 *  Autori:
 *    - <Renee Angelica Cabigting> - Matricola <756997> - Sede <CO>
 *  File: EsitoRisposta.java
 * ============================================================================
 */
package cinemax.common.protocol;

/**
 * Esito di una {@link Risposta} inviata dal server al client.
 *
 * @author CineMax Team
 */
public enum EsitoRisposta {

    /** L'operazione richiesta e' andata a buon fine. */
    OK,

    /**
     * L'operazione e' fallita per una violazione di regola applicativa
     * prevedibile (es. posti insufficienti, credenziali errate, username
     * gia' in uso): il messaggio della risposta e' pensato per essere
     * mostrato direttamente all'utente nella GUI.
     */
    ERRORE_APPLICATIVO,

    /**
     * L'operazione e' fallita per un problema tecnico non previsto (es.
     * eccezione SQL imprevista, connessione al DB persa): il messaggio e'
     * pensato per il log, non necessariamente per l'utente finale.
     */
    ERRORE_TECNICO
}
