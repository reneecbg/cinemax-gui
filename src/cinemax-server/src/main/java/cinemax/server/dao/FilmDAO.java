/*
 * ============================================================================
 *  CineMax - Laboratorio Interdisciplinare B
 *  Autori:
 *    - <Renee Angelica Cabigting> - Matricola <756997> - Sede <CO>
 *  File: FilmDAO.java
 * ============================================================================
 */
package cinemax.server.dao;

import cinemax.common.dto.FilmDTO;
import cinemax.server.eccezioni.GestoreEccezioniSql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Data Access Object per la tabella {@code film}.
 * <p>
 * Ogni istanza opera su una {@link Connection} fornita dal chiamante (di
 * norma la connessione dedicata al client corrente, vedi
 * {@link cinemax.server.db.ConnessioneFactory}): il DAO stesso non apre ne'
 * chiude connessioni, per lasciare al chiamante il controllo delle
 * transazioni quando un'operazione coinvolge piu' DAO (es. creare un film e
 * poi la sua prima proiezione nella stessa transazione).
 *
 * @author CineMax Team
 */
public class FilmDAO {

    private final Connection connessione;

    public FilmDAO(Connection connessione) {
        this.connessione = connessione;
    }

    /**
     * Cerca un film per titolo, regista e anno esatti (la chiave candidata
     * definita dal vincolo UNIQUE di {@code schema.sql}).
     *
     * @return il film trovato, oppure {@code null} se non esiste
     */
    public FilmDTO trovaPerTitoloRegistaAnno(String titolo, String regista, int anno) {
        String sql = "SELECT id_film, titolo, genere, regista, anno, durata_minuti, eta_minima "
                + "FROM film WHERE titolo = ? AND regista = ? AND anno = ?";
        try (PreparedStatement ps = connessione.prepareStatement(sql)) {
            ps.setString(1, titolo);
            ps.setString(2, regista);
            ps.setInt(3, anno);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mappa(rs) : null;
            }
        } catch (SQLException e) {
            throw GestoreEccezioniSql.classifica(e, "ricerca film per titolo/regista/anno");
        }
    }

    /**
     * Inserisce un nuovo film.
     *
     * @return il film appena inserito, con l'id assegnato dal database
     */
    public FilmDTO inserisci(String titolo, String genere, String regista,
                              int anno, int durataMinuti, int etaMinima) {
        String sql = "INSERT INTO film (titolo, genere, regista, anno, durata_minuti, eta_minima) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connessione.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, titolo);
            ps.setString(2, genere);
            ps.setString(3, regista);
            ps.setInt(4, anno);
            ps.setInt(5, durataMinuti);
            ps.setInt(6, etaMinima);
            ps.executeUpdate();
            try (ResultSet chiavi = ps.getGeneratedKeys()) {
                chiavi.next();
                int id = chiavi.getInt(1);
                return new FilmDTO(id, titolo, genere, regista, anno, durataMinuti, etaMinima);
            }
        } catch (SQLException e) {
            throw GestoreEccezioniSql.classifica(e, "inserimento film");
        }
    }

    /**
     * Restituisce il film con l'id indicato, se esiste. Usato tipicamente
     * per costruire un {@code ProiezioneDTO} completo a partire da una riga
     * della tabella {@code proiezione}.
     */
    public FilmDTO trovaPerId(int idFilm) {
        String sql = "SELECT id_film, titolo, genere, regista, anno, durata_minuti, eta_minima "
                + "FROM film WHERE id_film = ?";
        try (PreparedStatement ps = connessione.prepareStatement(sql)) {
            ps.setInt(1, idFilm);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mappa(rs) : null;
            }
        } catch (SQLException e) {
            throw GestoreEccezioniSql.classifica(e, "ricerca film per id");
        }
    }

    private FilmDTO mappa(ResultSet rs) throws SQLException {
        return new FilmDTO(
                rs.getInt("id_film"),
                rs.getString("titolo"),
                rs.getString("genere"),
                rs.getString("regista"),
                rs.getInt("anno"),
                rs.getInt("durata_minuti"),
                rs.getInt("eta_minima"));
    }
}
