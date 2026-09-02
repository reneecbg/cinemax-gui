/*
 * ============================================================================
 *  CineMax - Laboratorio Interdisciplinare B
 *  Autori:
 *    - <Renee Angelica Cabigting> - Matricola <756997> - Sede <CO>
 *  File: ServizioAutenticazione.java
 * ============================================================================
 */
package cinemax.server.servizi;

import cinemax.common.dto.UtenteDTO;
import cinemax.server.dao.UtenteDAO;
import cinemax.server.eccezioni.EccezioneApplicativa;
import cinemax.server.util.PasswordUtil;

import java.sql.Connection;
import java.time.LocalDate;

/**
 * Servizio applicativo per login e registrazione.
 * <p>
 * I servizi sono il livello che il dispatcher del server (Step 5) invoca in
 * base al {@link cinemax.common.protocol.Comando} ricevuto: non conoscono
 * ne' il socket ne' il formato di {@code Richiesta}/{@code Risposta} (quella
 * traduzione e' compito del dispatcher), e non contengono SQL diretto (quello
 * e' compito dei DAO). Un servizio orchestra uno o piu' DAO applicando le
 * regole di business che non sono (o non devono essere) gia' garantite dal
 * database.
 *
 * @author CineMax Team
 */
public class ServizioAutenticazione {

    private final UtenteDAO utenteDAO;

    public ServizioAutenticazione(Connection connessione) {
        this.utenteDAO = new UtenteDAO(connessione);
    }

    /**
     * Autentica un utente.
     *
     * @return i dati dell'utente autenticato
     * @throws EccezioneApplicativa se username inesistente o password errata
     *         (messaggio volutamente generico in entrambi i casi, per non
     *         rivelare a un tentativo di accesso se lo username esiste o
     *         meno)
     */
    public UtenteDTO login(String username, String passwordInChiaro) {
        String hashMemorizzato = utenteDAO.trovaHashPassword(username);
        if (hashMemorizzato == null || !PasswordUtil.verifica(passwordInChiaro, hashMemorizzato)) {
            throw new EccezioneApplicativa("Username o password non validi");
        }
        return utenteDAO.trovaPerUsername(username);
    }

    /**
     * Registra un nuovo cliente. Lo username duplicato viene intercettato
     * dal vincolo PRIMARY KEY della tabella {@code utente} e arriva qui gia'
     * come {@link EccezioneApplicativa} (vedi
     * {@link cinemax.server.eccezioni.GestoreEccezioniSql}).
     */
    public UtenteDTO registraCliente(String username, String nome, String cognome,
                                      String passwordInChiaro, LocalDate dataNascita, String domicilio) {
        if (username == null || username.isBlank()) {
            throw new EccezioneApplicativa("Lo username non puo' essere vuoto");
        }
        if (passwordInChiaro == null || passwordInChiaro.isBlank()) {
            throw new EccezioneApplicativa("La password non puo' essere vuota");
        }
        String hash = PasswordUtil.hash(passwordInChiaro);
        return utenteDAO.registraCliente(username, nome, cognome, hash, dataNascita, domicilio);
    }
}
