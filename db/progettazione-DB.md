# CineMax — Progettazione del database (dbCM)

## 1. Metodologia

La progettazione segue l'approccio classico a tre fasi: schema concettuale
(ER), ristrutturazione dello schema ER, traduzione in schema logico
relazionale. Le entità sono state individuate direttamente dai requisiti
funzionali della traccia ("Strutture dati: Proiezioni, Utenti, Prenotazioni"),
a cui si aggiunge l'entità **Sala**, introdotta in fase di ristrutturazione
(vedi §3) e non esplicitamente richiesta dal testo.

## 2. Schema concettuale ER

Vedi `ER-diagram.mermaid` per il diagramma. Riassunto di entità, attributi e
associazioni:

| Entità | Attributi | Chiave |
|---|---|---|
| Film | titolo, genere, regista, anno, durata_minuti, eta_minima | id_film (surrogata); (titolo, regista, anno) chiave candidata |
| Sala | nome, capienza | id_sala (surrogata) |
| Proiezione | data_ora, costo_biglietto | id_proiezione (surrogata) |
| Utente | nome, cognome, password_hash, data_nascita, domicilio, ruolo | username (naturale) |
| Prenotazione | numero_biglietti, data_creazione | codice (naturale, generato dal sistema) |

Associazioni e cardinalità:

- **Film — Proiezione** (1:N): un film può essere proiettato in più
  proiezioni; ogni proiezione fa riferimento a esattamente un film.
  Partecipazione di Proiezione totale (una proiezione non esiste senza un
  film), di Film parziale (un film può non avere ancora proiezioni).
- **Sala — Proiezione** (1:N): analogo al precedente. Con una sola sala
  attiva, in pratica tutte le proiezioni fanno riferimento alla stessa riga.
- **Utente — Prenotazione** (1:N): un cliente effettua più prenotazioni;
  ogni prenotazione fa riferimento a esattamente un cliente. Partecipazione
  di Prenotazione totale, di Utente parziale.
- **Proiezione — Prenotazione** (1:N): una proiezione riceve più
  prenotazioni; ogni prenotazione fa riferimento a esattamente una
  proiezione.

### Vincoli in linguaggio naturale (non esprimibili graficamente in ER)

1. Solo un utente con ruolo `CLIENTE` può essere associato a una
   prenotazione (un proiezionista o un bigliettaio non prenotano).
2. La somma dei biglietti prenotati per una proiezione non può superare la
   capienza della sala che la ospita.
3. Due proiezioni nella stessa sala non possono sovrapporsi temporalmente,
   considerando l'orario di inizio e la durata del film proiettato.
4. La password non è mai memorizzata in chiaro, solo la sua impronta
   crittografica (SHA-256).
5. Una proiezione con almeno una prenotazione non può essere modificata né
   eliminata.
6. Lo username è univoco a livello di intero sistema, indipendentemente dal
   ruolo dell'utente.

## 3. Ristrutturazione dello schema ER

Rispetto a una prima stesura "letterale" dei requisiti, sono state fatte
queste scelte, motivate di seguito:

- **Introduzione dell'entità Sala.** I requisiti parlano di un cinema
  monosala con una capienza fissa (200 posti). Trattare la capienza come
  attributo di Sala, invece che come costante applicativa (come nella
  versione TUI del Lab A) o come attributo ripetuto su ogni Proiezione,
  evita ridondanza e apre alla naturale estensione a un cinema multisala
  senza modificare lo schema.
- **Nessun attributo derivato memorizzato.** Il numero di "posti liberi" di
  una proiezione è funzionalmente determinato da altri dati già presenti
  (capienza della sala e somma dei biglietti prenotati): memorizzarlo come
  colonna introdurrebbe una dipendenza aggiornabile in modo incoerente
  (anomalia di aggiornamento) ogni volta che si crea/modifica/elimina una
  prenotazione. Viene quindi sempre calcolato con una query aggregata (vedi
  `query-esempi.sql`).
- **Chiavi naturali dove già uniche per requisito.** Username per Utente e
  codice per Prenotazione sono già richiesti come identificativi univoci
  dall'applicazione: usarli come chiave primaria evita di introdurre una
  chiave surrogata ridondante e semplifica i join (tutte le tabelle
  collegate li referenziano già come tali nella versione precedente su
  file).
- **Chiavi surrogate per Film, Sala e Proiezione.** Non esiste un
  sottoinsieme di attributi naturalmente stabile e sempre noto a priori per
  questi tre casi (il titolo di un film da solo non è univoco, ad es.
  remake); si è preferita una chiave surrogata, con un vincolo di unicità
  aggiuntivo su Film (titolo, regista, anno) per prevenire i duplicati.

## 4. Traduzione in schema relazionale

Applicando le regole standard di traduzione (entità forte → tabella con
chiave primaria; associazione 1:N → chiave esterna nella tabella che
rappresenta il lato "N"; attributo con dominio ristretto e stabile → tipo
enumerato) si ottiene lo schema in `schema.sql`:

```
film(id_film, titolo, genere, regista, anno, durata_minuti, eta_minima)
sala(id_sala, nome, capienza)
proiezione(id_proiezione, id_film→film, id_sala→sala, data_ora, costo_biglietto)
utente(username, nome, cognome, password_hash, data_nascita, domicilio, ruolo)
prenotazione(codice, username_cliente→utente, id_proiezione→proiezione, numero_biglietti, data_creazione)
```

I vincoli in linguaggio naturale del §2 che la traduzione meccanica non
cattura (2, 3, 5, oltre in parte al vincolo 1) sono implementati con trigger
PostgreSQL (dettagliati nei commenti di `schema.sql`), perché richiedono di
esaminare righe diverse da quella in inserimento/modifica — cosa che un
semplice `CHECK` constraint di PostgreSQL non può fare.

## 5. Verifica della forma normale (3NF)

Per ciascuna tabella, ogni attributo non chiave dipende funzionalmente
dall'intera chiave primaria e non esistono dipendenze transitive tra
attributi non chiave:

- **film**: tutti gli attributi (titolo, genere, regista, anno,
  durata_minuti, eta_minima) dipendono solo da `id_film`. Nessuna
  dipendenza transitiva (es. `genere` non dipende da `regista`).
- **sala**: `nome` e `capienza` dipendono solo da `id_sala`.
- **proiezione**: `data_ora` e `costo_biglietto` dipendono dalla chiave
  intera `id_proiezione`; `id_film` e `id_sala` sono chiavi esterne, non
  attributi derivati da altre colonne della stessa tabella.
- **utente**: tutti gli attributi anagrafici dipendono solo da `username`;
  nessuno di essi dipende da un altro attributo non chiave (es. `domicilio`
  non dipende da `nome`).
- **prenotazione**: `numero_biglietti` e `data_creazione` dipendono solo da
  `codice`; `username_cliente` e `id_proiezione` sono chiavi esterne
  indipendenti tra loro (una prenotazione non "deriva" la proiezione dal
  cliente o viceversa).

Non essendoci ripetizione di gruppi di attributi né dipendenze parziali da
chiavi composte (le chiavi primarie sono tutte singole), lo schema soddisfa
anche 1NF e 2NF banalmente.

## 6. Gestione della concorrenza a livello di database

Il Lab B richiede esplicitamente che il server supporti più client connessi
in parallelo, con possibili accessi concorrenti alle stesse risorse. Oltre
alla gestione a livello applicativo (thread per connessione nel server,
vedi documentazione dell'architettura client/server), lo schema del
database applica già una prima linea di difesa:

- I trigger `fn_verifica_capienza_prenotazione` e
  `fn_verifica_sovrapposizione_proiezione` acquisiscono un lock di riga
  (`SELECT ... FOR UPDATE`) sulla proiezione coinvolta **prima** di
  contare/confrontare i dati. Se due transazioni concorrenti tentano di
  operare sulla stessa proiezione, PostgreSQL serializza l'accesso: la
  seconda transazione attende il commit/rollback della prima e rivaluta la
  condizione con i dati aggiornati, evitando quindi race condition come la
  doppia prenotazione dell'ultimo posto disponibile.
- Il codice di prenotazione è generato con una `SEQUENCE` (atomica per
  definizione in PostgreSQL), non con un calcolo `MAX(...) + 1` lato
  applicazione, che sotto concorrenza potrebbe produrre codici duplicati.

Questo verrà completato, nel livello server, con transazioni esplicite
(`Connection.setAutoCommit(false)` + `commit()`/`rollback()`) attorno a ogni
operazione che coinvolge questi trigger, in modo che l'eventuale eccezione
sollevata dal trigger annulli in modo pulito l'intera operazione lato
Java.

## 7. File di questa consegna

- `schema.sql` — script DDL completo (tabelle, vincoli, trigger, indici).
- `seed.sql` — dati iniziali (sala, 2 proiezionisti, 5 bigliettai, dati di
  esempio per test).
- `query-esempi.sql` — query parametriche a supporto di ogni funzionalità
  richiesta, da usare come riferimento per il livello DAO del server.
- `ER-diagram.mermaid` — diagramma ER (visualizzabile con qualunque
  strumento compatibile Mermaid, incluso VS Code con l'estensione
  "Markdown Preview Mermaid Support").
