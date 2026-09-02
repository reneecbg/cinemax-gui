-- ============================================================================
--  CineMax - Dati di popolamento iniziale (dbCM)
--  File: seed.sql
--
--  Tutti gli utenti di test condividono la stessa password in chiaro
--  "prova123" (hash SHA-256 verificato: fb7e0d7b6792cdd30a0d464f4137ffba
--  41dfb09f02e66b4f5248badeb64c437a). Usare sempre questa password per
--  accedere con uno qualsiasi degli account elencati qui sotto.
-- ============================================================================

BEGIN;

-- Sala unica, capienza 200 posti (come da traccia)
INSERT INTO sala (nome, capienza)
VALUES ('Sala unica', 200)
ON CONFLICT DO NOTHING;

-- 2 proiezionisti + 5 bigliettai richiesti dalla traccia (password: prova123)
INSERT INTO utente (username, nome, cognome, password_hash, data_nascita, domicilio, ruolo) VALUES
('proiezionista1', 'Luca',   'Verdi',   'fb7e0d7b6792cdd30a0d464f4137ffba41dfb09f02e66b4f5248badeb64c437a', '1985-03-12', 'Varese',       'PROIEZIONISTA'),
('proiezionista2', 'Sara',   'Colombo', 'fb7e0d7b6792cdd30a0d464f4137ffba41dfb09f02e66b4f5248badeb64c437a', '1990-07-25', 'Como',         'PROIEZIONISTA'),
('bigliettaio1',   'Marco',  'Ferrari', 'fb7e0d7b6792cdd30a0d464f4137ffba41dfb09f02e66b4f5248badeb64c437a', '1995-01-30', 'Varese',       'BIGLIETTAIO'),
('bigliettaio2',   'Giulia', 'Rizzo',   'fb7e0d7b6792cdd30a0d464f4137ffba41dfb09f02e66b4f5248badeb64c437a', '1998-11-05', 'Gallarate',    'BIGLIETTAIO'),
('bigliettaio3',   'Paolo',  'Greco',   'fb7e0d7b6792cdd30a0d464f4137ffba41dfb09f02e66b4f5248badeb64c437a', '1992-06-18', 'Como',         'BIGLIETTAIO'),
('bigliettaio4',   'Elena',  'Conti',   'fb7e0d7b6792cdd30a0d464f4137ffba41dfb09f02e66b4f5248badeb64c437a', '2000-09-22', 'Busto Arsizio','BIGLIETTAIO'),
('bigliettaio5',   'Davide', 'Moretti', 'fb7e0d7b6792cdd30a0d464f4137ffba41dfb09f02e66b4f5248badeb64c437a', '1996-04-14', 'Saronno',      'BIGLIETTAIO')
ON CONFLICT (username) DO NOTHING;

-- Un cliente di esempio per i test (password: prova123)
INSERT INTO utente (username, nome, cognome, password_hash, data_nascita, domicilio, ruolo) VALUES
('mrossi', 'Mario', 'Rossi', 'fb7e0d7b6792cdd30a0d464f4137ffba41dfb09f02e66b4f5248badeb64c437a', '1999-02-10', 'Milano', 'CLIENTE')
ON CONFLICT (username) DO NOTHING;

-- Alcuni film e proiezioni di esempio per popolare l'interfaccia durante lo sviluppo
INSERT INTO film (titolo, genere, regista, anno, durata_minuti, eta_minima) VALUES
('A Beautiful Mind',    'Biography', 'Ron Howard',    2001, 135, 12),
('Good Will Hunting',   'Drama',     'Gus Van Sant',  1997, 126, 0)
ON CONFLICT (titolo, regista, anno) DO NOTHING;

INSERT INTO proiezione (id_film, id_sala, data_ora, costo_biglietto)
SELECT f.id_film, s.id_sala, ts.data_ora, ts.costo
  FROM (VALUES
        ('A Beautiful Mind',  TIMESTAMP '2027-12-30 10:30:00', 8.50),
        ('Good Will Hunting', TIMESTAMP '2027-12-29 16:00:00', 8.50)
       ) AS ts(titolo, data_ora, costo)
  JOIN film f ON f.titolo = ts.titolo
  CROSS JOIN (SELECT id_sala FROM sala ORDER BY id_sala LIMIT 1) s
  ON CONFLICT DO NOTHING;

COMMIT;
