CineMax (CM) - README
=====================

Progetto: piattaforma CineMax (Lab B - sviluppo GUI, architettura client/server)
Autori: vedi autori.txt

------------------------------------------------------------------------------
1. PREREQUISITI
------------------------------------------------------------------------------
- JDK 17 o superiore (consigliato: JDK 21, Eclipse Temurin)
- Apache Maven 3.9+
- PostgreSQL 16+ in esecuzione (locale o raggiungibile in rete)

Verificare le installazioni con:
    java -version
    mvn -version
    psql --version

------------------------------------------------------------------------------
2. STRUTTURA DEL REPOSITORY
------------------------------------------------------------------------------
    autori.txt        - dati degli autori del progetto
    pom.xml            - POM Maven padre (multi-modulo)
    src/
        cinemax-common - protocollo di comunicazione e DTO condivisi
        cinemax-server - modulo serverCM (DAO, servizi, rete, main)
        cinemax-client - modulo clientCM (GUI Swing, main)
    db/
        schema.sql         - script DDL completo (tabelle, vincoli, trigger)
        seed.sql           - dati di popolamento iniziale
        query-esempi.sql   - query di riferimento per ogni funzionalità
        ER-diagram.mermaid - diagramma ER del database
        progettazione-DB.md- documentazione della progettazione del database
    bin/               - eseguibili .jar (generati con build.sh / build.bat)
    doc/               - manuali, diagrammi, Javadoc (vedi doc/DA-COMPLETARE.txt)
    lib/               - vedi lib/PERCHE-VUOTA.txt
    build.sh           - script di build per macOS/Linux
    build.bat          - script di build per Windows

------------------------------------------------------------------------------
3. CREAZIONE DEL DATABASE
------------------------------------------------------------------------------
Con PostgreSQL in esecuzione:

    createdb -U postgres cinemax
    psql -U postgres -d cinemax -f db/schema.sql
    psql -U postgres -d cinemax -f db/seed.sql

Lo script seed.sql crea automaticamente:
    - la sala unica (capienza 200 posti)
    - 2 utenti con ruolo PROIEZIONISTA e 5 con ruolo BIGLIETTAIO
    - un cliente di esempio e due film/proiezioni di prova

NOTA SULLE PASSWORD: tutti gli utenti creati da seed.sql (proiezionisti,
bigliettai, e il cliente di esempio "mrossi") condividono la stessa
password in chiaro:

    prova123

------------------------------------------------------------------------------
4. COMPILAZIONE (MAVEN)
------------------------------------------------------------------------------
Dalla radice del repository (dove si trova questo file e pom.xml):

    mvn -pl src/cinemax-common,src/cinemax-server,src/cinemax-client -am package

Questo comando compila tutti e tre i moduli e produce due jar eseguibili
"fat" (con tutte le dipendenze incluse, incl. il driver JDBC PostgreSQL):

    src/cinemax-server/target/CineMaxServer.jar
    src/cinemax-client/target/CineMaxClient.jar

In alternativa, usare gli script che compilano e copiano automaticamente
i jar nella cartella bin/:

    ./build.sh        (macOS / Linux)
    build.bat         (Windows)

Per generare la documentazione Javadoc aggregata di tutti i moduli:

    mvn org.apache.maven.plugins:maven-javadoc-plugin:3.10.1:aggregate

L'output viene creato in target/site/apidocs e va copiato in doc/javadoc/.

------------------------------------------------------------------------------
5. AVVIO
------------------------------------------------------------------------------
5.1 Avviare il server (in un terminale, deve restare in esecuzione):

    java -jar bin/CineMaxServer.jar

    Verranno richiesti in sequenza: host del database, porta (default 5432),
    nome del database (cinemax), utente e password PostgreSQL, e infine la
    porta su cui il server deve mettersi in ascolto (default 5000).

5.2 Avviare uno o più client (in altri terminali, con il server già attivo):

    java -jar bin/CineMaxClient.jar

    Verrà richiesto l'host e la porta del server (default localhost:5000).
    È possibile avviare più istanze del client contemporaneamente da
    postazioni diverse per verificare il supporto multiutente.

------------------------------------------------------------------------------
6. NOTE SU LIBRERIE NON STANDARD
------------------------------------------------------------------------------
- org.postgresql:postgresql (driver JDBC), usato solo dal modulo server,
  gestito interamente da Maven (dichiarato in src/cinemax-server/pom.xml).
- Nessuna libreria GUI esterna: il client usa esclusivamente Swing,
  incluso nel JDK standard.
- Nessuna libreria va scaricata o posizionata manualmente: si veda
  lib/PERCHE-VUOTA.txt per il dettaglio.
