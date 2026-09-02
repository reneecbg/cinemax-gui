-- ============================================================================
--  CineMax - Query di esempio a supporto dei servizi applicativi
--  File: query-esempi.sql
--  Queste query documentano come ogni funzionalita' richiesta dalla traccia
--  viene realizzata sul database. Nel server Java saranno eseguite come
--  PreparedStatement con parametri (qui sostituiti da segnaposto $1, $2, ...
--  a scopo di documentazione).
-- ============================================================================

-- ----------------------------------------------------------------------------
-- cercaProiezione(): ricerca combinata (titolo parziale, genere, intervallo
-- date, intervallo costo). Ogni parametro puo' essere NULL per essere ignorato.
-- ----------------------------------------------------------------------------
SELECT p.id_proiezione, f.titolo, f.genere, f.regista, f.anno, f.durata_minuti,
       f.eta_minima, p.data_ora, p.costo_biglietto,
       s.capienza - COALESCE((
           SELECT SUM(pr.numero_biglietti) FROM prenotazione pr
            WHERE pr.id_proiezione = p.id_proiezione
       ), 0) AS posti_liberi
  FROM proiezione p
  JOIN film f ON f.id_film = p.id_film
  JOIN sala  s ON s.id_sala = p.id_sala
 WHERE ($1::text IS NULL OR f.titolo ILIKE '%' || $1 || '%')
   AND ($2::text IS NULL OR f.genere ILIKE '%' || $2 || '%')
   AND ($3::date IS NULL OR p.data_ora::date >= $3)
   AND ($4::date IS NULL OR p.data_ora::date <= $4)
   AND ($5::numeric IS NULL OR p.costo_biglietto >= $5)
   AND ($6::numeric IS NULL OR p.costo_biglietto <= $6)
 ORDER BY p.data_ora;

-- ----------------------------------------------------------------------------
-- Proiezioni nei tre mesi successivi per un dato film (schermata guest, Lab B)
-- ----------------------------------------------------------------------------
SELECT p.id_proiezione, f.titolo, p.data_ora, p.costo_biglietto
  FROM proiezione p
  JOIN film f ON f.id_film = p.id_film
 WHERE f.titolo ILIKE '%' || $1 || '%'
   AND p.data_ora BETWEEN now() AND now() + INTERVAL '3 months'
 ORDER BY p.data_ora;

-- ----------------------------------------------------------------------------
-- Proiezioni pianificate (proiezionista) / storiche (proiezionista)
-- ----------------------------------------------------------------------------
-- pianificate:
SELECT * FROM proiezione WHERE data_ora > now() ORDER BY data_ora;
-- storiche:
SELECT * FROM proiezione WHERE data_ora <= now() ORDER BY data_ora DESC;

-- ----------------------------------------------------------------------------
-- registraCliente(): l'username univoco e' garantito dalla PRIMARY KEY;
-- si intercetta la violazione (SQLState 23505) lato server.
-- ----------------------------------------------------------------------------
INSERT INTO utente (username, nome, cognome, password_hash, data_nascita, domicilio, ruolo)
VALUES ($1, $2, $3, $4, $5, $6, 'CLIENTE');

-- ----------------------------------------------------------------------------
-- login(): il confronto della password avviene lato server (si calcola
-- l'hash della password digitata e lo si confronta con quello restituito).
-- ----------------------------------------------------------------------------
SELECT username, nome, cognome, password_hash, ruolo
  FROM utente
 WHERE username = $1;

-- ----------------------------------------------------------------------------
-- creaPrenotazione(): il controllo di capienza e il codice progressivo sono
-- garantiti dai trigger dello schema, non serve verificarli prima in Java.
-- ----------------------------------------------------------------------------
INSERT INTO prenotazione (username_cliente, id_proiezione, numero_biglietti)
VALUES ($1, $2, $3)
RETURNING codice;

-- ----------------------------------------------------------------------------
-- Prenotazioni attive di un cliente (relative a proiezioni future)
-- ----------------------------------------------------------------------------
SELECT pr.codice, f.titolo, p.data_ora, pr.numero_biglietti,
       p.costo_biglietto, (pr.numero_biglietti * p.costo_biglietto) AS totale
  FROM prenotazione pr
  JOIN proiezione p ON p.id_proiezione = pr.id_proiezione
  JOIN film f ON f.id_film = p.id_film
 WHERE pr.username_cliente = $1
   AND p.data_ora > now()
 ORDER BY p.data_ora;

-- ----------------------------------------------------------------------------
-- modificaPrenotazione() (cambio proiezione): entrambe le date (vecchia e
-- nuova) devono essere future - controllo lato server prima dell'UPDATE.
-- ----------------------------------------------------------------------------
UPDATE prenotazione
   SET id_proiezione = $2
 WHERE codice = $1;

-- ----------------------------------------------------------------------------
-- eliminaPrenotazione()
-- ----------------------------------------------------------------------------
DELETE FROM prenotazione WHERE codice = $1;

-- ----------------------------------------------------------------------------
-- cercaPrenotazione() (bigliettaio): per codice / nome+cognome cliente /
-- titolo (parziale) / intervallo di date
-- ----------------------------------------------------------------------------
SELECT pr.codice, u.nome, u.cognome, f.titolo, p.data_ora, pr.numero_biglietti
  FROM prenotazione pr
  JOIN utente u ON u.username = pr.username_cliente
  JOIN proiezione p ON p.id_proiezione = pr.id_proiezione
  JOIN film f ON f.id_film = p.id_film
 WHERE ($1::text IS NULL OR pr.codice ILIKE $1)
   AND ($2::text IS NULL OR u.nome ILIKE '%' || $2 || '%')
   AND ($3::text IS NULL OR u.cognome ILIKE '%' || $3 || '%')
   AND ($4::text IS NULL OR f.titolo ILIKE '%' || $4 || '%')
   AND ($5::date IS NULL OR p.data_ora::date >= $5)
   AND ($6::date IS NULL OR p.data_ora::date <= $6);

-- ----------------------------------------------------------------------------
-- Prenotazioni della data odierna (bigliettaio)
-- ----------------------------------------------------------------------------
SELECT pr.codice, u.nome, u.cognome, f.titolo, p.data_ora, pr.numero_biglietti
  FROM prenotazione pr
  JOIN utente u ON u.username = pr.username_cliente
  JOIN proiezione p ON p.id_proiezione = pr.id_proiezione
  JOIN film f ON f.id_film = p.id_film
 WHERE p.data_ora::date = CURRENT_DATE;
