/*
 * ============================================================================
 *  CineMax - Laboratorio Interdisciplinare B
 *  Autori:
 *    - <Renee Angelica Cabigting> - Matricola <756997> - Sede <CO>
 *  File: PrenotazioneDAO.java
 * ============================================================================
 */
package cinemax.server.dao;

import cinemax.common.dto.PrenotazioneDTO;
import cinemax.common.dto.ProiezioneDTO;
import cinemax.server.eccezioni.EccezioneApplicativa;
import cinemax.server.eccezioni.GestoreEccezioniSql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object per la tabella {@code prenotazione}.
 * <p>
 * I controlli di capienza sala e di ruolo del cliente (deve essere
 * {@code CLIENTE}) NON sono ripetuti qui in Java: sono gia' garantiti dai
 * trigger {@code trg_verifica_capienza} e {@code trg_verifica_ruolo_cliente}
 * di {@code schema.sql}, che operano atomicamente anche in presenza di piu'
 * connessioni concorrenti (a differenza di un doppio controllo "leggi poi
 * scrivi" fatto qui in Java, che sarebbe soggetto a race condition). Questo
 * DAO si limita quindi a eseguire l'operazione e a lasciare che
 * un'eventuale violazione emerga come eccezione SQL, classificata da
 * {@link GestoreEccezioniSql}.
 *
 * @author CineMax Team
 */
public class PrenotazioneDAO {

    private final Connection connessione;
    private final ProiezioneDAO proiezioneDAO;

    public PrenotazioneDAO(Connection connessione) {
        this.connessione = connessione;
        this.proiezioneDAO = new ProiezioneDAO(connessione);
    }

    private static final String SELECT_BASE =
            "SELECT pr.codice, pr.username_cliente, u.nome, u.cognome, "
          + "       pr.id_proiezione, pr.numero_biglietti, pr.data_creazione "
          + "  FROM prenotazione pr "
          + "  JOIN utente u ON u.username = pr.username_cliente ";

    /**
     * Crea una nuova prenotazione. Il codice (formato {@code PRN-0001}) e'
     * generato dal trigger {@code trg_genera_codice_prenotazione}, quindi
     * non viene passato in input.
     *
     * @return la prenotazione appena creata, gia' completa di proiezione e dati cliente
     * @throws EccezioneApplicativa se non ci sono abbastanza posti liberi,
     *         se lo username non e' un cliente, o se la proiezione non esiste
     */
    public PrenotazioneDTO crea(String usernameCliente, int idProiezione, int numeroBiglietti) {
        String sql = "INSERT INTO prenotazione (username_cliente, id_proiezione, numero_biglietti) "
                + "VALUES (?, ?, ?)";
        try (PreparedStatement ps = connessione.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, usernameCliente);
            ps.setInt(2, idProiezione);
            ps.setInt(3, numeroBiglietti);
            ps.executeUpdate();
            try (ResultSet chiavi = ps.getGeneratedKeys()) {
                chiavi.next();
                String codice = chiavi.getString("codice");
                return trovaPerCodice(codice);
            }
        } catch (SQLException e) {
            throw GestoreEccezioniSql.classifica(e, "creazione prenotazione");
        }
    }

    /** Prenotazione per codice, oppure {@code null} se non esiste. */
    public PrenotazioneDTO trovaPerCodice(String codice) {
        String sql = SELECT_BASE + " WHERE pr.codice = ?";
        try (PreparedStatement ps = connessione.prepareStatement(sql)) {
            ps.setString(1, codice);
            List<PrenotazioneDTO> risultati = eseguiEMappa(ps);
            return risultati.isEmpty() ? null : risultati.get(0);
        } catch (SQLException e) {
            throw GestoreEccezioniSql.classifica(e, "ricerca prenotazione per codice");
        }
    }

    /** Prenotazioni attive (relative a proiezioni future) di un cliente. */
    public List<PrenotazioneDTO> attivePerCliente(String usernameCliente) {
        String sql = SELECT_BASE
                + "  JOIN proiezione p ON p.id_proiezione = pr.id_proiezione "
                + " WHERE pr.username_cliente = ? AND p.data_ora > now() "
                + " ORDER BY p.data_ora";
        try (PreparedStatement ps = connessione.prepareStatement(sql)) {
            ps.setString(1, usernameCliente);
            return eseguiEMappa(ps);
        } catch (SQLException e) {
            throw GestoreEccezioniSql.classifica(e, "ricerca prenotazioni attive cliente");
        }
    }

    /**
     * Cambia la proiezione associata a una prenotazione esistente (il
     * "cambio data" richiesto dalla traccia). Il controllo "sia la vecchia
     * che la nuova data devono essere future" va fatto dal livello di
     * servizio (Step 4) leggendo prima le due proiezioni con
     * {@link ProiezioneDAO#trovaPerId(int)}, perche' coinvolge una regola
     * di business specifica del dominio (non e' un vincolo di integrita'
     * dei dati in se', quindi resta nel livello di servizio invece che nel
     * DAO o nel trigger).
     */
    public void modifica(String codice, int nuovoIdProiezione) {
        String sql = "UPDATE prenotazione SET id_proiezione = ? WHERE codice = ?";
        try (PreparedStatement ps = connessione.prepareStatement(sql)) {
            ps.setInt(1, nuovoIdProiezione);
            ps.setString(2, codice);
            int righe = ps.executeUpdate();
            if (righe == 0) {
                throw new EccezioneApplicativa("Prenotazione non trovata: " + codice);
            }
        } catch (SQLException e) {
            throw GestoreEccezioniSql.classifica(e, "modifica prenotazione");
        }
    }

    /** Elimina una prenotazione. */
    public void elimina(String codice) {
        String sql = "DELETE FROM prenotazione WHERE codice = ?";
        try (PreparedStatement ps = connessione.prepareStatement(sql)) {
            ps.setString(1, codice);
            int righe = ps.executeUpdate();
            if (righe == 0) {
                throw new EccezioneApplicativa("Prenotazione non trovata: " + codice);
            }
        } catch (SQLException e) {
            throw GestoreEccezioniSql.classifica(e, "eliminazione prenotazione");
        }
    }

    /** Ricerca combinata per il bigliettaio: codice / nome / cognome / titolo / intervallo date. */
    public List<PrenotazioneDTO> cerca(String codice, String nome, String cognome,
                                        String titoloFilm, LocalDate dataDa, LocalDate dataA) {
        String sql = SELECT_BASE
                + "  JOIN proiezione p ON p.id_proiezione = pr.id_proiezione "
                + "  JOIN film f ON f.id_film = p.id_film "
                + " WHERE (?::text IS NULL OR pr.codice ILIKE ?)"
                + "   AND (?::text IS NULL OR u.nome ILIKE '%' || ? || '%')"
                + "   AND (?::text IS NULL OR u.cognome ILIKE '%' || ? || '%')"
                + "   AND (?::text IS NULL OR f.titolo ILIKE '%' || ? || '%')"
                + "   AND (?::date IS NULL OR p.data_ora::date >= ?)"
                + "   AND (?::date IS NULL OR p.data_ora::date <= ?)"
                + " ORDER BY p.data_ora";
        try (PreparedStatement ps = connessione.prepareStatement(sql)) {
            impostaTestoDoppio(ps, 1, codice);
            impostaTestoDoppio(ps, 3, nome);
            impostaTestoDoppio(ps, 5, cognome);
            impostaTestoDoppio(ps, 7, titoloFilm);
            impostaDataDoppio(ps, 9, dataDa);
            impostaDataDoppio(ps, 11, dataA);
            return eseguiEMappa(ps);
        } catch (SQLException e) {
            throw GestoreEccezioniSql.classifica(e, "ricerca prenotazioni");
        }
    }

    /** Prenotazioni relative a proiezioni della data odierna (schermata bigliettaio). */
    public List<PrenotazioneDTO> diOggi() {
        String sql = SELECT_BASE
                + "  JOIN proiezione p ON p.id_proiezione = pr.id_proiezione "
                + " WHERE p.data_ora::date = CURRENT_DATE "
                + " ORDER BY p.data_ora";
        try (PreparedStatement ps = connessione.prepareStatement(sql)) {
            return eseguiEMappa(ps);
        } catch (SQLException e) {
            throw GestoreEccezioniSql.classifica(e, "ricerca prenotazioni di oggi");
        }
    }

    // ------------------------------------------------------------------
    // Utility private
    // ------------------------------------------------------------------

    private List<PrenotazioneDTO> eseguiEMappa(PreparedStatement ps) throws SQLException {
        List<PrenotazioneDTO> risultati = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                risultati.add(mappa(rs));
            }
        }
        return risultati;
    }

    private PrenotazioneDTO mappa(ResultSet rs) throws SQLException {
        int idProiezione = rs.getInt("id_proiezione");
        ProiezioneDTO proiezione = proiezioneDAO.trovaPerId(idProiezione);
        String nomeCompleto = rs.getString("nome") + " " + rs.getString("cognome");
        return new PrenotazioneDTO(
                rs.getString("codice"),
                rs.getString("username_cliente"),
                nomeCompleto,
                proiezione,
                rs.getInt("numero_biglietti"),
                rs.getTimestamp("data_creazione").toLocalDateTime());
    }

    private void impostaTestoDoppio(PreparedStatement ps, int primoIndice, String valore) throws SQLException {
        if (valore == null) {
            ps.setNull(primoIndice, Types.VARCHAR);
            ps.setNull(primoIndice + 1, Types.VARCHAR);
        } else {
            ps.setString(primoIndice, valore);
            ps.setString(primoIndice + 1, valore);
        }
    }

    private void impostaDataDoppio(PreparedStatement ps, int primoIndice, LocalDate valore) throws SQLException {
        if (valore == null) {
            ps.setNull(primoIndice, Types.DATE);
            ps.setNull(primoIndice + 1, Types.DATE);
        } else {
            ps.setDate(primoIndice, java.sql.Date.valueOf(valore));
            ps.setDate(primoIndice + 1, java.sql.Date.valueOf(valore));
        }
    }
}
