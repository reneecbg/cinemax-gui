/*
 * ============================================================================
 *  CineMax - Laboratorio Interdisciplinare B
 *  Autori:
 *    - <Renee Angelica Cabigting> - Matricola <756997> - Sede <CO>
 *  File: Comando.java
 * ============================================================================
 */
package cinemax.common.protocol;

/**
 * Elenca tutte le operazioni che il clientCM puo' richiedere al serverCM.
 * <p>
 * Ogni valore corrisponde a un servizio applicativo lato server (vedi il
 * dispatcher del server, che smista una {@link Richiesta} in base al suo
 * comando verso il servizio giusto). L'elenco riflette 1:1 le funzionalita'
 * richieste dalla traccia per le tre categorie di utenti e per l'accesso
 * pubblico/guest.
 *
 * @author CineMax Team
 */
public enum Comando {

    // ---- Funzionalita' pubbliche (nessun login necessario) ----

    /** Ricerca combinata di proiezioni (titolo, genere, date, costo). */
    CERCA_PROIEZIONI,

    /** Proiezioni di un film nei tre mesi successivi (schermata guest). */
    CERCA_PROIEZIONI_FILM_GUEST,

    /** Dettaglio di una singola proiezione (dati film + posti liberi). */
    DETTAGLIO_PROIEZIONE,

    /** Registrazione di un nuovo cliente. */
    REGISTRA_CLIENTE,

    /** Autenticazione di un utente gia' registrato. */
    LOGIN,

    // ---- Funzionalita' cliente (login necessario) ----

    /** Creazione di una nuova prenotazione. */
    CREA_PRENOTAZIONE,

    /** Prenotazioni attive (future) del cliente autenticato. */
    PRENOTAZIONI_ATTIVE_CLIENTE,

    /** Cambio della proiezione associata a una prenotazione esistente. */
    MODIFICA_PRENOTAZIONE,

    /** Cancellazione di una prenotazione esistente. */
    ELIMINA_PRENOTAZIONE,

    // ---- Funzionalita' proiezionista (login necessario) ----

    /** Inserimento di un nuovo film e della relativa proiezione. */
    AGGIUNGI_PROIEZIONE,

    /** Proiezioni pianificate (data futura). */
    PROIEZIONI_PIANIFICATE,

    /** Proiezioni storiche (data passata). */
    PROIEZIONI_STORICHE,

    /** Modifica di data/ora e costo di una proiezione esistente. */
    MODIFICA_PROIEZIONE,

    /** Eliminazione di una proiezione esistente. */
    ELIMINA_PROIEZIONE,

    // ---- Funzionalita' bigliettaio (login necessario) ----

    /** Prenotazioni relative a proiezioni della data odierna. */
    PRENOTAZIONI_DI_OGGI,

    /** Ricerca di prenotazioni per codice/cliente/film/intervallo date. */
    CERCA_PRENOTAZIONI,

    // ---- Comune a tutti gli utenti autenticati ----

    /** Chiusura della sessione lato server (facoltativo: si puo' anche solo chiudere il socket). */
    LOGOUT
}
