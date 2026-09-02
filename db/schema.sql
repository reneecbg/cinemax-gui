-- ============================================================================
--  CineMax - Schema relazionale del database (dbCM)
--  Laboratorio Interdisciplinare B - Sviluppo GUI / Client-Server
--  Autori:
--    - <Renee Angelica Cabigting> - Matricola <756997> - Sede <CO>
--  File: schema.sql
--
--  Note di progettazione:
--  - Schema in Terza Forma Normale (3NF): ogni attributo non chiave dipende
--    unicamente dalla chiave primaria della propria tabella, senza dipendenze
--    transitive tra colonne non chiave.
--  - Il numero di posti liberi NON viene memorizzato: e' un dato derivato
--    (capienza_sala - somma biglietti prenotati per la proiezione) e viene
--    sempre calcolato con una query aggregata, per evitare anomalie di
--    aggiornamento tipiche degli attributi derivati memorizzati.
--  - I controlli di integrita' che richiedono di guardare piu' righe/tabelle
--    (sovrapposizione proiezioni, capienza sala, ruolo del cliente) non sono
--    esprimibili con semplici CHECK constraint di PostgreSQL: sono quindi
--    implementati con trigger, che sono anche il meccanismo con cui si
--    gestisce la concorrenza tra piu' client connessi contemporaneamente
--    (vedi commenti nelle singole funzioni trigger).
-- ============================================================================

BEGIN;

-- ----------------------------------------------------------------------------
-- Estensioni
-- ----------------------------------------------------------------------------
-- pg_trgm: indici su similarita' di stringhe, usati per velocizzare le
-- ricerche "per titolo anche parziale" (query con ILIKE '%...%').
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- ----------------------------------------------------------------------------
-- Tipi
-- ----------------------------------------------------------------------------
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'ruolo_utente') THEN
        CREATE TYPE ruolo_utente AS ENUM ('CLIENTE', 'PROIEZIONISTA', 'BIGLIETTAIO');
    END IF;
END$$;

-- ----------------------------------------------------------------------------
-- Tabella: sala
-- ----------------------------------------------------------------------------
-- Il cinema descritto dalla traccia e' monosala, ma modellare la sala come
-- entita' a se' stante (invece di una costante applicativa, come nella
-- versione TUI del Lab A) rende lo schema corretto dal punto di vista della
-- normalizzazione ed estendibile in futuro a un cinema multisala, senza
-- toccare lo schema. Per l'uso attuale e' sufficiente una sola riga.
CREATE TABLE IF NOT EXISTS sala (
    id_sala     SERIAL PRIMARY KEY,
    nome        VARCHAR(50) NOT NULL DEFAULT 'Sala unica',
    capienza    INTEGER NOT NULL CHECK (capienza > 0)
);

COMMENT ON TABLE sala IS 'Sala del cinema. Il progetto attuale prevede una sola riga (monosala).';

-- ----------------------------------------------------------------------------
-- Tabella: film
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS film (
    id_film         SERIAL PRIMARY KEY,
    titolo          VARCHAR(200) NOT NULL,
    genere          VARCHAR(80)  NOT NULL,
    regista         VARCHAR(120) NOT NULL,
    anno            INTEGER NOT NULL CHECK (anno BETWEEN 1888 AND 2100),
    durata_minuti   INTEGER NOT NULL CHECK (durata_minuti > 0),
    eta_minima      INTEGER NOT NULL DEFAULT 0 CHECK (eta_minima >= 0),

    -- Evita film duplicati: stesso criterio gia' usato da Film.equals() nella
    -- versione precedente dell'applicazione (titolo + regista + anno).
    CONSTRAINT uq_film_titolo_regista_anno UNIQUE (titolo, regista, anno)
);

-- Indice per velocizzare la ricerca "per titolo (anche parziale)"
CREATE INDEX IF NOT EXISTS idx_film_titolo_trgm
    ON film USING gin (titolo gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_film_genere ON film (genere);

-- ----------------------------------------------------------------------------
-- Tabella: utente
-- ----------------------------------------------------------------------------
-- Si usa lo username come chiave primaria naturale (invece di un id
-- surrogato): e' gia' l'identificatore univoco richiesto dalla traccia per il
-- login, ed e' l'attributo con cui tutte le altre tabelle fanno riferimento
-- all'utente, quindi evita un ulteriore livello di indirezione nei join.
CREATE TABLE IF NOT EXISTS utente (
    username            VARCHAR(50) PRIMARY KEY,
    nome                VARCHAR(80)  NOT NULL,
    cognome             VARCHAR(80)  NOT NULL,
    -- Impronta SHA-256 in esadecimale (64 caratteri): la password in chiaro
    -- non viene MAI memorizzata.
    password_hash       CHAR(64) NOT NULL,
    data_nascita         DATE NULL,
    domicilio           VARCHAR(150) NOT NULL,
    ruolo               ruolo_utente NOT NULL,
    data_registrazione  TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT ck_username_non_vuoto CHECK (btrim(username) <> '')
);

CREATE INDEX IF NOT EXISTS idx_utente_ruolo ON utente (ruolo);
CREATE INDEX IF NOT EXISTS idx_utente_nome_cognome ON utente (nome, cognome);

-- ----------------------------------------------------------------------------
-- Tabella: proiezione
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS proiezione (
    id_proiezione   SERIAL PRIMARY KEY,
    id_film         INTEGER NOT NULL REFERENCES film(id_film) ON DELETE RESTRICT,
    id_sala         INTEGER NOT NULL REFERENCES sala(id_sala) ON DELETE RESTRICT,
    data_ora        TIMESTAMP NOT NULL,
    costo_biglietto NUMERIC(6,2) NOT NULL CHECK (costo_biglietto > 0)
);

CREATE INDEX IF NOT EXISTS idx_proiezione_data_ora ON proiezione (data_ora);
CREATE INDEX IF NOT EXISTS idx_proiezione_id_film ON proiezione (id_film);

-- ----------------------------------------------------------------------------
-- Tabella: prenotazione
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS prenotazione (
    -- Il codice viene generato automaticamente dal trigger sottostante
    -- (formato PRN-0001, PRN-0002, ...) usando una sequence: in questo modo
    -- la generazione e' atomica anche con piu' client connessi in parallelo
    -- (a differenza di un "SELECT MAX(...) + 1" fatto lato applicazione, che
    -- sotto concorrenza puo' generare codici duplicati).
    codice              VARCHAR(10) PRIMARY KEY,
    username_cliente    VARCHAR(50) NOT NULL REFERENCES utente(username) ON DELETE RESTRICT,
    id_proiezione       INTEGER NOT NULL REFERENCES proiezione(id_proiezione) ON DELETE RESTRICT,
    numero_biglietti    INTEGER NOT NULL CHECK (numero_biglietti > 0),
    data_creazione      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_prenotazione_username ON prenotazione (username_cliente);
CREATE INDEX IF NOT EXISTS idx_prenotazione_id_proiezione ON prenotazione (id_proiezione);

CREATE SEQUENCE IF NOT EXISTS seq_prenotazione START 1;

-- ----------------------------------------------------------------------------
-- TRIGGER 1 - Generazione automatica del codice prenotazione
-- ----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_genera_codice_prenotazione()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.codice IS NULL OR btrim(NEW.codice) = '' THEN
        NEW.codice := 'PRN-' || lpad(nextval('seq_prenotazione')::text, 4, '0');
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_genera_codice_prenotazione ON prenotazione;
CREATE TRIGGER trg_genera_codice_prenotazione
    BEFORE INSERT ON prenotazione
    FOR EACH ROW
    EXECUTE FUNCTION fn_genera_codice_prenotazione();

-- ----------------------------------------------------------------------------
-- TRIGGER 2 - Il cliente di una prenotazione deve avere ruolo CLIENTE
-- ----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_verifica_ruolo_cliente()
RETURNS TRIGGER AS $$
DECLARE
    v_ruolo ruolo_utente;
BEGIN
    SELECT ruolo INTO v_ruolo FROM utente WHERE username = NEW.username_cliente;
    IF v_ruolo IS DISTINCT FROM 'CLIENTE' THEN
        RAISE EXCEPTION 'Utente % non ha ruolo CLIENTE (ruolo attuale: %)',
            NEW.username_cliente, v_ruolo;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_verifica_ruolo_cliente ON prenotazione;
CREATE TRIGGER trg_verifica_ruolo_cliente
    BEFORE INSERT OR UPDATE ON prenotazione
    FOR EACH ROW
    EXECUTE FUNCTION fn_verifica_ruolo_cliente();

-- ----------------------------------------------------------------------------
-- TRIGGER 3 - Capienza sala (gestione della concorrenza sulle prenotazioni)
-- ----------------------------------------------------------------------------
-- La riga di 'proiezione' interessata viene bloccata con FOR UPDATE prima di
-- contare i biglietti gia' prenotati: se due client tentano di prenotare
-- contemporaneamente gli ultimi posti della stessa proiezione, la seconda
-- transazione resta in attesa del lock e, quando riparte, vede gia' i posti
-- occupati dalla prima e viene correttamente rifiutata se non ce ne sono
-- piu' a sufficienza. Questo rende il controllo sicuro sotto concorrenza
-- senza bisogno di lock applicativi nel server Java.
CREATE OR REPLACE FUNCTION fn_verifica_capienza_prenotazione()
RETURNS TRIGGER AS $$
DECLARE
    v_capienza  INTEGER;
    v_prenotati INTEGER;
BEGIN
    SELECT s.capienza INTO v_capienza
      FROM proiezione p
      JOIN sala s ON s.id_sala = p.id_sala
     WHERE p.id_proiezione = NEW.id_proiezione
     FOR UPDATE OF p;

    IF v_capienza IS NULL THEN
        RAISE EXCEPTION 'Proiezione % inesistente', NEW.id_proiezione;
    END IF;

    SELECT COALESCE(SUM(numero_biglietti), 0) INTO v_prenotati
      FROM prenotazione
     WHERE id_proiezione = NEW.id_proiezione
       AND codice <> COALESCE(NEW.codice, '');

    IF v_prenotati + NEW.numero_biglietti > v_capienza THEN
        RAISE EXCEPTION 'Posti insufficienti per la proiezione %: richiesti %, liberi %',
            NEW.id_proiezione, NEW.numero_biglietti, (v_capienza - v_prenotati);
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_verifica_capienza ON prenotazione;
CREATE TRIGGER trg_verifica_capienza
    BEFORE INSERT OR UPDATE ON prenotazione
    FOR EACH ROW
    EXECUTE FUNCTION fn_verifica_capienza_prenotazione();

-- ----------------------------------------------------------------------------
-- TRIGGER 4 - Divieto di sovrapposizione tra proiezioni nella stessa sala
-- ----------------------------------------------------------------------------
-- Stesso principio del trigger precedente: si bloccano (FOR UPDATE) le righe
-- di 'proiezione' gia' esistenti nella stessa sala prima di verificare la
-- sovrapposizione, cosi' due proiezionisti che aggiungono proiezioni in
-- parallelo non possono creare comunque un conflitto orario.
CREATE OR REPLACE FUNCTION fn_verifica_sovrapposizione_proiezione()
RETURNS TRIGGER AS $$
DECLARE
    v_durata     INTEGER;
    v_fine       TIMESTAMP;
    v_conflitto  RECORD;
BEGIN
    SELECT durata_minuti INTO v_durata FROM film WHERE id_film = NEW.id_film;
    IF v_durata IS NULL THEN
        RAISE EXCEPTION 'Film % inesistente', NEW.id_film;
    END IF;
    v_fine := NEW.data_ora + (v_durata || ' minutes')::interval;

    PERFORM 1
      FROM proiezione p
     WHERE p.id_sala = NEW.id_sala
       AND p.id_proiezione <> COALESCE(NEW.id_proiezione, -1)
     FOR UPDATE OF p;

    SELECT p.id_proiezione, p.data_ora INTO v_conflitto
      FROM proiezione p
      JOIN film f ON f.id_film = p.id_film
     WHERE p.id_sala = NEW.id_sala
       AND p.id_proiezione <> COALESCE(NEW.id_proiezione, -1)
       AND NEW.data_ora < (p.data_ora + (f.durata_minuti || ' minutes')::interval)
       AND p.data_ora < v_fine
     LIMIT 1;

    IF FOUND THEN
        RAISE EXCEPTION 'La proiezione si sovrappone con la proiezione # % (%)',
            v_conflitto.id_proiezione, v_conflitto.data_ora;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_verifica_sovrapposizione ON proiezione;
CREATE TRIGGER trg_verifica_sovrapposizione
    BEFORE INSERT OR UPDATE ON proiezione
    FOR EACH ROW
    EXECUTE FUNCTION fn_verifica_sovrapposizione_proiezione();

-- ----------------------------------------------------------------------------
-- TRIGGER 5 - Divieto di modifica/eliminazione di proiezioni con prenotazioni
-- ----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_blocca_modifica_proiezione_con_prenotazioni()
RETURNS TRIGGER AS $$
DECLARE
    v_id INTEGER := COALESCE(OLD.id_proiezione, NEW.id_proiezione);
BEGIN
    IF EXISTS (SELECT 1 FROM prenotazione WHERE id_proiezione = v_id) THEN
        RAISE EXCEPTION 'Esistono prenotazioni per la proiezione %: operazione non consentita', v_id;
    END IF;
    RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_blocca_update_proiezione ON proiezione;
CREATE TRIGGER trg_blocca_update_proiezione
    BEFORE UPDATE ON proiezione
    FOR EACH ROW
    EXECUTE FUNCTION fn_blocca_modifica_proiezione_con_prenotazioni();

DROP TRIGGER IF EXISTS trg_blocca_delete_proiezione ON proiezione;
CREATE TRIGGER trg_blocca_delete_proiezione
    BEFORE DELETE ON proiezione
    FOR EACH ROW
    EXECUTE FUNCTION fn_blocca_modifica_proiezione_con_prenotazioni();

COMMIT;
