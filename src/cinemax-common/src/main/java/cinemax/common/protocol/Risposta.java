/*
 * ============================================================================
 *  CineMax - Laboratorio Interdisciplinare B
 *  Autori:
 *    - <Renee Angelica Cabigting> - Matricola <756997> - Sede <CO>
 *  File: Risposta.java
 * ============================================================================
 */
package cinemax.common.protocol;

import java.io.Serializable;

/**
 * Messaggio di risposta inviato dal serverCM al clientCM in seguito a una
 * {@link Richiesta}.
 * <p>
 * Il campo {@code dato} e' volutamente di tipo {@link Object}: a seconda del
 * comando puo' contenere un singolo DTO (es. l'esito di un login), una
 * {@link java.util.List} di DTO (es. risultati di una ricerca) oppure essere
 * {@code null} (es. una eliminazione andata a buon fine). Il client conosce,
 * comando per comando, quale cast applicare: questa convenzione e'
 * documentata nel Javadoc del rispettivo servizio server.
 *
 * @author CineMax Team
 */
public class Risposta implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Esito dell'operazione. */
    private final EsitoRisposta esito;

    /** Messaggio descrittivo (sempre presente, anche in caso di successo). */
    private final String messaggio;

    /** Dato restituito dall'operazione, oppure {@code null}. */
    private final Object dato;

    private Risposta(EsitoRisposta esito, String messaggio, Object dato) {
        this.esito = esito;
        this.messaggio = messaggio;
        this.dato = dato;
    }

    /**
     * Costruisce una risposta di successo.
     *
     * @param messaggio messaggio descrittivo da mostrare all'utente
     * @param dato      dato restituito (puo' essere {@code null})
     * @return la risposta di successo
     */
    public static Risposta ok(String messaggio, Object dato) {
        return new Risposta(EsitoRisposta.OK, messaggio, dato);
    }

    /**
     * Costruisce una risposta di errore applicativo (regola di business
     * violata, prevista e da mostrare all'utente cosi' com'e').
     *
     * @param messaggio messaggio da mostrare all'utente
     * @return la risposta di errore applicativo
     */
    public static Risposta erroreApplicativo(String messaggio) {
        return new Risposta(EsitoRisposta.ERRORE_APPLICATIVO, messaggio, null);
    }

    /**
     * Costruisce una risposta di errore tecnico (eccezione non prevista).
     *
     * @param messaggio messaggio tecnico, tipicamente per il log
     * @return la risposta di errore tecnico
     */
    public static Risposta erroreTecnico(String messaggio) {
        return new Risposta(EsitoRisposta.ERRORE_TECNICO, messaggio, null);
    }

    /**
     * @return l'esito dell'operazione
     */
    public EsitoRisposta getEsito() {
        return esito;
    }

    /**
     * @return {@code true} se l'esito e' {@link EsitoRisposta#OK}
     */
    public boolean isOk() {
        return esito == EsitoRisposta.OK;
    }

    /**
     * @return il messaggio descrittivo della risposta
     */
    public String getMessaggio() {
        return messaggio;
    }

    /**
     * @return il dato restituito dall'operazione, oppure {@code null}
     */
    public Object getDato() {
        return dato;
    }

    /**
     * Estrae, con cast, il dato restituito.
     *
     * @param tipo classe attesa del dato
     * @param <T>  tipo atteso del dato
     * @return il dato convertito al tipo richiesto, oppure {@code null}
     */
    public <T> T getDato(Class<T> tipo) {
        return dato == null ? null : tipo.cast(dato);
    }

    @Override
    public String toString() {
        return "Risposta{" + esito + ", messaggio='" + messaggio + "', dato=" + dato + '}';
    }
}
