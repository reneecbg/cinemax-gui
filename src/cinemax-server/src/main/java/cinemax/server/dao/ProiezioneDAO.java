/*
 * ============================================================================
 *  CineMax - Laboratorio Interdisciplinare B
 *  Autori:
 *    - <Renee Angelica Cabigting> - Matricola <756997> - Sede <CO>
 *  File: ProiezioneDAO.java
 * ============================================================================
 */
package cinemax.server.dao;

import cinemax.common.dto.FilmDTO;
import cinemax.common.dto.ProiezioneDTO;
import cinemax.server.eccezioni.GestoreEccezioniSql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object per la tabella {@code proiezione}, in join con
 * {@code film} e {@code sala}.
 * <p>
 * Le query di ricerca sono costruite ricalcando fedelmente
 * {@code query-esempi.sql} (Step 1): i parametri opzionali sono gestiti con
 * la stessa forma {@code (? IS NULL OR colonna ...)}, cosi' un singolo
 * metodo copre sia la ricerca con un solo criterio sia quella con tutti i
 * criteri combinati, senza costruire la query dinamicamente con
 * concatenazione di stringhe (che aprirebbe la porta a SQL injection).
 *
 * @author CineMax Team
 */
public class ProiezioneDAO {

    private final Connection connessione;

    public ProiezioneDAO(Connection connessione) {
        this.connessione = connessione;
    }

    private static final String SELECT_BASE =
            "SELECT p.id_proiezione, p.data_ora, p.costo_biglietto, "
          + "       f.id_film, f.titolo, f.genere, f.regista, f.anno, f.durata_minuti, f.eta_minima, "
          + "       s.capienza - COALESCE(( "
          + "           SELECT SUM(pr.numero_biglietti) FROM prenotazione pr "
          + "            WHERE pr.id_proiezione = p.id_proiezione "
          + "       ), 0) AS posti_liberi "
          + "  FROM proiezione p "
          + "  JOIN film f ON f.id_film = p.id_film "
          + "  JOIN sala  s ON s.id_sala = p.id_sala ";

    /**
     * Ricerca combinata di proiezioni. Ogni parametro puo' essere
     * {@code null} per non essere applicato come filtro.
     */
    public List<ProiezioneDTO> cerca(String titoloParziale, String genere,
                                      LocalDate dataDa, LocalDate dataA,
                                      Double costoMin, Double costoMax) {
        String sql = SELECT_BASE
                + " WHERE (?::text IS NULL OR f.titolo ILIKE '%' || ? || '%')"
                + "   AND (?::text IS NULL OR f.genere ILIKE '%' || ? || '%')"
                + "   AND (?::date IS NULL OR p.data_ora::date >= ?)"
                + "   AND (?::date IS NULL OR p.data_ora::date <= ?)"
                + "   AND (?::numeric IS NULL OR p.costo_biglietto >= ?)"
                + "   AND (?::numeric IS NULL OR p.costo_biglietto <= ?)"
                + " ORDER BY p.data_ora";
        try (PreparedStatement ps = connessione.prepareStatement(sql)) {
            impostaParametroDoppio(ps, 1, titoloParziale);
            impostaParametroDoppio(ps, 3, genere);
            impostaParametroDataDoppio(ps, 5, dataDa);
            impostaParametroDataDoppio(ps, 7, dataA);
            impostaParametroNumericoDoppio(ps, 9, costoMin);
            impostaParametroNumericoDoppio(ps, 11, costoMax);
            return eseguiEMappa(ps);
        } catch (SQLException e) {
            throw GestoreEccezioniSql.classifica(e, "ricerca proiezioni");
        }
    }

    /**
     * Proiezioni di un film (titolo anche parziale) nei tre mesi successivi
     * alla data odierna — schermata guest richiesta dal Lab B.
     */
    public List<ProiezioneDTO> cercaFilmProssimiTreMesi(String titoloParziale) {
        String sql = SELECT_BASE
                + " WHERE f.titolo ILIKE '%' || ? || '%'"
                + "   AND p.data_ora BETWEEN now() AND now() + INTERVAL '3 months'"
                + " ORDER BY p.data_ora";
        try (PreparedStatement ps = connessione.prepareStatement(sql)) {
            ps.setString(1, titoloParziale);
            return eseguiEMappa(ps);
        } catch (SQLException e) {
            throw GestoreEccezioniSql.classifica(e, "ricerca proiezioni prossimi 3 mesi");
        }
    }

    /** Proiezioni pianificate: data futura. */
    public List<ProiezioneDTO> pianificate() {
        String sql = SELECT_BASE + " WHERE p.data_ora > now() ORDER BY p.data_ora";
        try (PreparedStatement ps = connessione.prepareStatement(sql)) {
            return eseguiEMappa(ps);
        } catch (SQLException e) {
            throw GestoreEccezioniSql.classifica(e, "ricerca proiezioni pianificate");
        }
    }

    /** Proiezioni storiche: data passata. */
    public List<ProiezioneDTO> storiche() {
        String sql = SELECT_BASE + " WHERE p.data_ora <= now() ORDER BY p.data_ora DESC";
        try (PreparedStatement ps = connessione.prepareStatement(sql)) {
            return eseguiEMappa(ps);
        } catch (SQLException e) {
            throw GestoreEccezioniSql.classifica(e, "ricerca proiezioni storiche");
        }
    }

    /** Dettaglio di una proiezione per id, oppure {@code null} se non esiste. */
    public ProiezioneDTO trovaPerId(int idProiezione) {
        String sql = SELECT_BASE + " WHERE p.id_proiezione = ?";
        try (PreparedStatement ps = connessione.prepareStatement(sql)) {
            ps.setInt(1, idProiezione);
            List<ProiezioneDTO> risultati = eseguiEMappa(ps);
            return risultati.isEmpty() ? null : risultati.get(0);
        } catch (SQLException e) {
            throw GestoreEccezioniSql.classifica(e, "ricerca proiezione per id");
        }
    }

    /**
     * Inserisce una nuova proiezione per il film indicato, nell'unica sala
     * esistente. Se la proiezione si sovrappone a una gia' esistente, il
     * trigger {@code trg_verifica_sovrapposizione} di {@code schema.sql}
     * fa fallire l'INSERT: l'eccezione risultante viene classificata da
     * {@link GestoreEccezioniSql} come {@link cinemax.server.eccezioni.EccezioneApplicativa}.
     *
     * @return la proiezione creata, con id assegnato e posti liberi = capienza sala
     */
    public ProiezioneDTO inserisci(int idFilm, LocalDateTime dataOra, double costoBiglietto) {
        String sql = "INSERT INTO proiezione (id_film, id_sala, data_ora, costo_biglietto) "
                + "SELECT ?, id_sala, ?, ? FROM sala ORDER BY id_sala LIMIT 1";
        try (PreparedStatement ps = connessione.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, idFilm);
            ps.setTimestamp(2, Timestamp.valueOf(dataOra));
            ps.setDouble(3, costoBiglietto);
            ps.executeUpdate();
            try (ResultSet chiavi = ps.getGeneratedKeys()) {
                chiavi.next();
                int id = chiavi.getInt(1);
                return trovaPerId(id);
            }
        } catch (SQLException e) {
            throw GestoreEccezioniSql.classifica(e, "inserimento proiezione");
        }
    }

    /**
     * Modifica data/ora e costo di una proiezione esistente. Il trigger
     * {@code trg_blocca_update_proiezione} impedisce la modifica se
     * esistono gia' prenotazioni per quella proiezione.
     */
    public void modifica(int idProiezione, LocalDateTime nuovaDataOra, double nuovoCosto) {
        String sql = "UPDATE proiezione SET data_ora = ?, costo_biglietto = ? WHERE id_proiezione = ?";
        try (PreparedStatement ps = connessione.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(nuovaDataOra));
            ps.setDouble(2, nuovoCosto);
            ps.setInt(3, idProiezione);
            int righe = ps.executeUpdate();
            if (righe == 0) {
                throw new cinemax.server.eccezioni.EccezioneApplicativa("Proiezione non trovata: " + idProiezione);
            }
        } catch (SQLException e) {
            throw GestoreEccezioniSql.classifica(e, "modifica proiezione");
        }
    }

    /**
     * Elimina una proiezione. Il trigger {@code trg_blocca_delete_proiezione}
     * impedisce l'eliminazione se esistono gia' prenotazioni.
     */
    public void elimina(int idProiezione) {
        String sql = "DELETE FROM proiezione WHERE id_proiezione = ?";
        try (PreparedStatement ps = connessione.prepareStatement(sql)) {
            ps.setInt(1, idProiezione);
            int righe = ps.executeUpdate();
            if (righe == 0) {
                throw new cinemax.server.eccezioni.EccezioneApplicativa("Proiezione non trovata: " + idProiezione);
            }
        } catch (SQLException e) {
            throw GestoreEccezioniSql.classifica(e, "eliminazione proiezione");
        }
    }

    // ------------------------------------------------------------------
    // Utility private
    // ------------------------------------------------------------------

    private List<ProiezioneDTO> eseguiEMappa(PreparedStatement ps) throws SQLException {
        List<ProiezioneDTO> risultati = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                risultati.add(mappa(rs));
            }
        }
        return risultati;
    }

    private ProiezioneDTO mappa(ResultSet rs) throws SQLException {
        FilmDTO film = new FilmDTO(
                rs.getInt("id_film"),
                rs.getString("titolo"),
                rs.getString("genere"),
                rs.getString("regista"),
                rs.getInt("anno"),
                rs.getInt("durata_minuti"),
                rs.getInt("eta_minima"));
        return new ProiezioneDTO(
                rs.getInt("id_proiezione"),
                film,
                rs.getTimestamp("data_ora").toLocalDateTime(),
                rs.getDouble("costo_biglietto"),
                rs.getInt("posti_liberi"));
    }

    /** Imposta lo stesso valore testuale su due segnaposto consecutivi (cast + confronto). */
    private void impostaParametroDoppio(PreparedStatement ps, int primoIndice, String valore) throws SQLException {
        if (valore == null) {
            ps.setNull(primoIndice, Types.VARCHAR);
            ps.setNull(primoIndice + 1, Types.VARCHAR);
        } else {
            ps.setString(primoIndice, valore);
            ps.setString(primoIndice + 1, valore);
        }
    }

    private void impostaParametroDataDoppio(PreparedStatement ps, int primoIndice, LocalDate valore) throws SQLException {
        if (valore == null) {
            ps.setNull(primoIndice, Types.DATE);
            ps.setNull(primoIndice + 1, Types.DATE);
        } else {
            ps.setDate(primoIndice, java.sql.Date.valueOf(valore));
            ps.setDate(primoIndice + 1, java.sql.Date.valueOf(valore));
        }
    }

    private void impostaParametroNumericoDoppio(PreparedStatement ps, int primoIndice, Double valore) throws SQLException {
        if (valore == null) {
            ps.setNull(primoIndice, Types.NUMERIC);
            ps.setNull(primoIndice + 1, Types.NUMERIC);
        } else {
            ps.setDouble(primoIndice, valore);
            ps.setDouble(primoIndice + 1, valore);
        }
    }
}
