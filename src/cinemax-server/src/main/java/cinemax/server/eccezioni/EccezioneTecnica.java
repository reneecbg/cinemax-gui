/*
 * ============================================================================
 *  CineMax - Laboratorio Interdisciplinare B
 *  Autori:
 *    - <Renee Angelica Cabigting> - Matricola <756997> - Sede <CO>
 *  File: EccezioneTecnica.java
 * ============================================================================
 */
package cinemax.server.eccezioni;

/**
 * Segnala un problema tecnico non previsto (es. connessione al database
 * persa, errore SQL imprevisto). A differenza di {@link EccezioneApplicativa},
 * il messaggio non e' pensato per l'utente finale ma per il log del server;
 * il client ricevera' un messaggio generico.
 *
 * @author CineMax Team
 */
public class EccezioneTecnica extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EccezioneTecnica(String messaggio, Throwable causa) {
        super(messaggio, causa);
    }
}
