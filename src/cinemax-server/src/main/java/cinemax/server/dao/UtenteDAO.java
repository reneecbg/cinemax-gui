/*
 * ============================================================================
 *  CineMax - Laboratorio Interdisciplinare B
 *  Autori:
 *    - <Renee Angelica Cabigting> - Matricola <756997> - Sede <CO>
 *  File: UtenteDAO.java
 * ============================================================================
 */
package cinemax.server.dao;

import cinemax.common.dto.RuoloDTO;
import cinemax.common.dto.UtenteDTO;
import cinemax.server.eccezioni.GestoreEccezioniSql;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Data Access Object per la tabella {@code utente}.
 * <p>
 * Nota sulla password: questo DAO lavora sempre e solo con l'hash SHA-256
 * gia' calcolato dal chiamante (livello di servizio, Step 4). Il DAO non
 * conosce l'algoritmo di hashing, ne' riceve mai la password in chiaro: la
 * responsabilita' di cifrarla e' del livello superiore, per tenere la
 * conoscenza dell'algoritmo crittografico in un unico punto del codice.
 *
 * @author CineMax Team
 */
public class UtenteDAO {

    private final Connection connessione;

    public UtenteDAO(Connection connessione) {
        this.connessione = connessione;
    }

    /**
     * Cerca un utente per username.
     *
     * @return l'utente trovato, oppure {@code null} se non esiste
     */
    public UtenteDTO trovaPerUsername(String username) {
        String sql = "SELECT username, nome, cognome, data_nascita, domicilio, ruolo "
                + "FROM utente WHERE username = ?";
        try (PreparedStatement ps = connessione.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mappa(rs) : null;
            }
        } catch (SQLException e) {
            throw GestoreEccezioniSql.classifica(e, "ricerca utente per username");
        }
    }

    /**
     * Restituisce l'hash della password memorizzato per l'utente indicato,
     * usato dal livello di servizio per verificare le credenziali al login.
     *
     * @return l'hash SHA-256 (64 caratteri esadecimali), oppure {@code null}
     *         se l'utente non esiste
     */
    public String trovaHashPassword(String username) {
        String sql = "SELECT password_hash FROM utente WHERE username = ?";
        try (PreparedStatement ps = connessione.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("password_hash") : null;
            }
        } catch (SQLException e) {
            throw GestoreEccezioniSql.classifica(e, "lettura hash password");
        }
    }

    /**
     * Registra un nuovo cliente. Solleva un'eccezione applicativa (tramite
     * {@link GestoreEccezioniSql}) se lo username e' gia' in uso, grazie al
     * vincolo PRIMARY KEY sulla colonna {@code username}.
     */
    public UtenteDTO registraCliente(String username, String nome, String cognome,
                                      String passwordHash, LocalDate dataNascita, String domicilio) {
        String sql = "INSERT INTO utente (username, nome, cognome, password_hash, data_nascita, domicilio, ruolo) "
                + "VALUES (?, ?, ?, ?, ?, ?, 'CLIENTE')";
        try (PreparedStatement ps = connessione.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, nome);
            ps.setString(3, cognome);
            ps.setString(4, passwordHash);
            if (dataNascita != null) {
                ps.setDate(5, Date.valueOf(dataNascita));
            } else {
                ps.setNull(5, java.sql.Types.DATE);
            }
            ps.setString(6, domicilio);
            ps.executeUpdate();
            return new UtenteDTO(username, nome, cognome, dataNascita, domicilio, RuoloDTO.CLIENTE);
        } catch (SQLException e) {
            throw GestoreEccezioniSql.classifica(e, "registrazione cliente");
        }
    }

    private UtenteDTO mappa(ResultSet rs) throws SQLException {
        Date dataNascitaSql = rs.getDate("data_nascita");
        LocalDate dataNascita = dataNascitaSql != null ? dataNascitaSql.toLocalDate() : null;
        return new UtenteDTO(
                rs.getString("username"),
                rs.getString("nome"),
                rs.getString("cognome"),
                dataNascita,
                rs.getString("domicilio"),
                RuoloDTO.valueOf(rs.getString("ruolo")));
    }
}
