/*
 * ============================================================================
 *  CineMax - Laboratorio Interdisciplinare B
 *  Autori:
 *    - <Renee Angelica Cabigting> - Matricola <756997> - Sede <CO>
 *  File: EccezioneApplicativa.java
 * ============================================================================
 */
package cinemax.server.eccezioni;

/**
 * Segnala la violazione di una regola di business prevista (es. "posti
 * insufficienti", "username gia' in uso", "credenziali errate"). Il
 * messaggio e' pensato per essere mostrato cosi' com'e' all'utente finale
 * nella GUI del client.
 * <p>
 * E' distinta da {@link EccezioneTecnica}: il livello di servizio (Step 4
 * della roadmap) usa questa distinzione per decidere se costruire una
 * {@code Risposta} con esito {@code ERRORE_APPLICATIVO} (messaggio mostrato
 * all'utente) oppure {@code ERRORE_TECNICO} (messaggio solo per il log).
 *
 * @author CineMax Team
 */
public class EccezioneApplicativa extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EccezioneApplicativa(String messaggio) {
        super(messaggio);
    }

    public EccezioneApplicativa(String messaggio, Throwable causa) {
        super(messaggio, causa);
    }
}
