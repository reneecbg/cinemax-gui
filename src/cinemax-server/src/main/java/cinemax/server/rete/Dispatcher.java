/*
 * ============================================================================
 *  CineMax - Laboratorio Interdisciplinare B
 *  Autori:
 *    - <Renee Angelica Cabigting> - Matricola <756997> - Sede <CO>
 *  File: Dispatcher.java
 * ============================================================================
 */
package cinemax.server.rete;

import cinemax.common.protocol.Comando;
import cinemax.common.protocol.Richiesta;
import cinemax.common.protocol.Risposta;
import cinemax.server.eccezioni.EccezioneApplicativa;
import cinemax.server.eccezioni.EccezioneTecnica;
import cinemax.server.servizi.ServizioAutenticazione;
import cinemax.server.servizi.ServizioPrenotazioni;
import cinemax.server.servizi.ServizioProiezioni;

import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Smista ogni {@link Richiesta} ricevuta da un client verso il servizio
 * giusto, in base al suo {@link Comando}, e ne cattura le eccezioni
 * traducendole in una {@link Risposta}.
 * <p>
 * Questa classe e' anche il punto in cui e' documentato, comando per
 * comando, l'esatto significato posizionale dei parametri di
 * {@code Richiesta} e del dato restituito in {@code Risposta}: e' il
 * "contratto" del protocollo applicativo (vedi Step 2 della roadmap), che
 * sia {@code cinemax-server} che {@code cinemax-client} devono rispettare.
 * <p>
 * Una nuova istanza viene creata per ogni client connesso (vedi
 * {@link GestoreClient}), condividendo con esso la stessa {@link Connection}
 * JDBC per l'intera sessione.
 *
 * @author CineMax Team
 */
public class Dispatcher {

    private final ServizioAutenticazione servizioAutenticazione;
    private final ServizioProiezioni servizioProiezioni;
    private final ServizioPrenotazioni servizioPrenotazioni;

    public Dispatcher(Connection connessione) {
        this.servizioAutenticazione = new ServizioAutenticazione(connessione);
        this.servizioProiezioni = new ServizioProiezioni(connessione);
        this.servizioPrenotazioni = new ServizioPrenotazioni(connessione);
    }

    /**
     * Esegue la richiesta e restituisce la risposta da inviare al client.
     * Non lancia mai eccezioni: qualunque problema (applicativo o tecnico)
     * viene convertito in una {@code Risposta} di errore, perche' il
     * protocollo di rete (Step 2) non prevede la propagazione di eccezioni
     * Java sul socket.
     */
    public Risposta gestisci(Richiesta richiesta) {
        try {
            return eseguiComando(richiesta);
        } catch (EccezioneApplicativa e) {
            return Risposta.erroreApplicativo(e.getMessage());
        } catch (EccezioneTecnica e) {
            return Risposta.erroreTecnico(e.getMessage());
        } catch (RuntimeException e) {
            // Rete di sicurezza: qualunque altra eccezione non prevista non
            // deve mai interrompere la sessione del client, ne' arrivargli
            // come stack trace.
            return Risposta.erroreTecnico("Errore interno del server: " + e);
        }
    }

    private Risposta eseguiComando(Richiesta r) {
        switch (r.getComando()) {

            // Parametri: [String titolo?, String genere?, LocalDate dataDa?,
            //             LocalDate dataA?, Double costoMin?, Double costoMax?]
            // Dato risposta: List<ProiezioneDTO>
            case CERCA_PROIEZIONI:
                return Risposta.ok("Ricerca completata", servizioProiezioni.cercaProiezioni(
                        r.getParametro(0, String.class),
                        r.getParametro(1, String.class),
                        r.getParametro(2, LocalDate.class),
                        r.getParametro(3, LocalDate.class),
                        r.getParametro(4, Double.class),
                        r.getParametro(5, Double.class)));

            // Parametri: [String titoloParziale]
            // Dato risposta: List<ProiezioneDTO>
            case CERCA_PROIEZIONI_FILM_GUEST:
                return Risposta.ok("Ricerca completata", servizioProiezioni.cercaProssimiTreMesi(
                        r.getParametro(0, String.class)));

            // Parametri: [Integer idProiezione]
            // Dato risposta: ProiezioneDTO
            case DETTAGLIO_PROIEZIONE:
                return Risposta.ok("Dettaglio proiezione", servizioProiezioni.dettaglio(
                        r.getParametro(0, Integer.class)));

            // Parametri: [String username, String nome, String cognome,
            //             String password, LocalDate dataNascita?, String domicilio]
            // Dato risposta: UtenteDTO
            case REGISTRA_CLIENTE:
                return Risposta.ok("Registrazione completata", servizioAutenticazione.registraCliente(
                        r.getParametro(0, String.class),
                        r.getParametro(1, String.class),
                        r.getParametro(2, String.class),
                        r.getParametro(3, String.class),
                        r.getParametro(4, LocalDate.class),
                        r.getParametro(5, String.class)));

            // Parametri: [String username, String password]
            // Dato risposta: UtenteDTO
            case LOGIN:
                return Risposta.ok("Login effettuato", servizioAutenticazione.login(
                        r.getParametro(0, String.class),
                        r.getParametro(1, String.class)));

            // Parametri: [String usernameCliente, Integer idProiezione, Integer numeroBiglietti]
            // Dato risposta: PrenotazioneDTO
            case CREA_PRENOTAZIONE:
                return Risposta.ok("Prenotazione creata", servizioPrenotazioni.creaPrenotazione(
                        r.getParametro(0, String.class),
                        r.getParametro(1, Integer.class),
                        r.getParametro(2, Integer.class)));

            // Parametri: [String usernameCliente]
            // Dato risposta: List<PrenotazioneDTO>
            case PRENOTAZIONI_ATTIVE_CLIENTE:
                return Risposta.ok("Prenotazioni attive", servizioPrenotazioni.prenotazioniAttive(
                        r.getParametro(0, String.class)));

            // Parametri: [String codice, Integer nuovoIdProiezione]
            // Dato risposta: nessuno (null)
            case MODIFICA_PRENOTAZIONE:
                servizioPrenotazioni.modificaPrenotazione(
                        r.getParametro(0, String.class),
                        r.getParametro(1, Integer.class));
                return Risposta.ok("Prenotazione modificata", null);

            // Parametri: [String codice]
            // Dato risposta: nessuno (null)
            case ELIMINA_PRENOTAZIONE:
                servizioPrenotazioni.eliminaPrenotazione(r.getParametro(0, String.class));
                return Risposta.ok("Prenotazione eliminata", null);

            // Parametri: [String titolo, String genere, String regista, Integer anno,
            //             Integer durataMinuti, Integer etaMinima, LocalDateTime dataOra,
            //             Double costoBiglietto]
            // Dato risposta: ProiezioneDTO
            case AGGIUNGI_PROIEZIONE:
                return Risposta.ok("Proiezione aggiunta", servizioProiezioni.aggiungiProiezione(
                        r.getParametro(0, String.class),
                        r.getParametro(1, String.class),
                        r.getParametro(2, String.class),
                        r.getParametro(3, Integer.class),
                        r.getParametro(4, Integer.class),
                        r.getParametro(5, Integer.class),
                        r.getParametro(6, LocalDateTime.class),
                        r.getParametro(7, Double.class)));

            // Nessun parametro. Dato risposta: List<ProiezioneDTO>
            case PROIEZIONI_PIANIFICATE:
                return Risposta.ok("Proiezioni pianificate", servizioProiezioni.pianificate());

            // Nessun parametro. Dato risposta: List<ProiezioneDTO>
            case PROIEZIONI_STORICHE:
                return Risposta.ok("Proiezioni storiche", servizioProiezioni.storiche());

            // Parametri: [Integer idProiezione, LocalDateTime nuovaDataOra, Double nuovoCosto]
            // Dato risposta: nessuno (null)
            case MODIFICA_PROIEZIONE:
                servizioProiezioni.modificaProiezione(
                        r.getParametro(0, Integer.class),
                        r.getParametro(1, LocalDateTime.class),
                        r.getParametro(2, Double.class));
                return Risposta.ok("Proiezione modificata", null);

            // Parametri: [Integer idProiezione]
            // Dato risposta: nessuno (null)
            case ELIMINA_PROIEZIONE:
                servizioProiezioni.eliminaProiezione(r.getParametro(0, Integer.class));
                return Risposta.ok("Proiezione eliminata", null);

            // Nessun parametro. Dato risposta: List<PrenotazioneDTO>
            case PRENOTAZIONI_DI_OGGI:
                return Risposta.ok("Prenotazioni di oggi", servizioPrenotazioni.prenotazioniDiOggi());

            // Parametri: [String codice?, String nome?, String cognome?,
            //             String titoloFilm?, LocalDate dataDa?, LocalDate dataA?]
            // Dato risposta: List<PrenotazioneDTO>
            case CERCA_PRENOTAZIONI:
                return Risposta.ok("Ricerca completata", servizioPrenotazioni.cercaPrenotazioni(
                        r.getParametro(0, String.class),
                        r.getParametro(1, String.class),
                        r.getParametro(2, String.class),
                        r.getParametro(3, String.class),
                        r.getParametro(4, LocalDate.class),
                        r.getParametro(5, LocalDate.class)));

            // Nessun parametro. Gestito qui solo per completezza: la vera
            // chiusura della sessione avviene in GestoreClient, che dopo
            // aver inviato questa risposta esce dal ciclo e chiude il socket.
            case LOGOUT:
                return Risposta.ok("Logout effettuato", null);

            default:
                return Risposta.erroreTecnico("Comando non gestito: " + r.getComando());
        }
    }
}
