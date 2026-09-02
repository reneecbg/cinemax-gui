/*
 * ============================================================================
 *  CineMax - Laboratorio Interdisciplinare B
 *  Autori:
 *    - <Renee Angelica Cabigting> - Matricola <756997> - Sede <CO>
 *  File: ServerCM.java
 * ============================================================================
 */
package cinemax.server;

import cinemax.server.db.ConnessioneFactory;
import cinemax.server.rete.GestoreClient;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Scanner;

/**
 * Punto di ingresso del modulo serverCM.
 * <p>
 * All'avvio chiede all'operatore host/credenziali del database (come
 * richiesto dalla traccia) e la porta su cui restare in ascolto, verifica
 * subito che la connessione al database funzioni (per non scoprire un
 * problema di configurazione solo al primo client connesso), poi entra nel
 * ciclo principale: per ogni connessione accettata dal {@link ServerSocket},
 * avvia un nuovo thread {@link GestoreClient} dedicato e torna subito in
 * ascolto della connessione successiva. Questo e' cio' che rende il server
 * capace di servire piu' client in parallelo, come richiesto dalla traccia.
 *
 * @author CineMax Team
 */
public class ServerCM {

    private static final int PORTA_PREDEFINITA = 5000;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("==============================================");
        System.out.println(" CineMax - serverCM");
        System.out.println("==============================================");

        ConnessioneFactory connessioneFactory = chiediParametriDbFinoAValidi(scanner);

        int porta = chiediPortaAscolto(scanner);

        try (ServerSocket serverSocket = new ServerSocket(porta)) {
            System.out.println("[serverCM] In ascolto sulla porta " + porta + "...");

            while (true) {
                Socket socketClient = serverSocket.accept();
                Thread thread = new Thread(new GestoreClient(socketClient, connessioneFactory));
                // Thread "daemon": non impedisce la chiusura della JVM se il
                // server viene terminato mentre ci sono client ancora connessi.
                thread.setDaemon(true);
                thread.start();
            }

        } catch (IOException e) {
            System.err.println("[serverCM] Impossibile avviare il server sulla porta " + porta + ": " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Chiede ripetutamente host/porta/database/credenziali finche' non si
     * riesce a stabilire una connessione di prova, cosi' un errore di
     * configurazione (host sbagliato, credenziali errate) viene segnalato
     * subito e non al momento in cui si connette il primo client.
     */
    private static ConnessioneFactory chiediParametriDbFinoAValidi(Scanner scanner) {
        while (true) {
            System.out.print("Host del database [localhost]: ");
            String host = leggiConDefault(scanner, "localhost");

            System.out.print("Porta del database [5432]: ");
            int portaDb = leggiInteroConDefault(scanner, 5432);

            System.out.print("Nome del database [cinemax]: ");
            String nomeDb = leggiConDefault(scanner, "cinemax");

            System.out.print("Utente PostgreSQL: ");
            String utente = scanner.nextLine().trim();

            System.out.print("Password PostgreSQL: ");
            String password = scanner.nextLine();

            ConnessioneFactory factory = new ConnessioneFactory(host, portaDb, nomeDb, utente, password);
            try {
                factory.verificaConnessione();
                System.out.println("[serverCM] Connessione al database riuscita.");
                return factory;
            } catch (SQLException e) {
                System.out.println("[serverCM] Connessione al database fallita: " + e.getMessage());
                System.out.println("Riprova.");
            }
        }
    }

    private static int chiediPortaAscolto(Scanner scanner) {
        System.out.print("Porta su cui mettersi in ascolto [" + PORTA_PREDEFINITA + "]: ");
        return leggiInteroConDefault(scanner, PORTA_PREDEFINITA);
    }

    private static String leggiConDefault(Scanner scanner, String valorePredefinito) {
        String riga = scanner.nextLine().trim();
        return riga.isEmpty() ? valorePredefinito : riga;
    }

    private static int leggiInteroConDefault(Scanner scanner, int valorePredefinito) {
        String riga = scanner.nextLine().trim();
        if (riga.isEmpty()) {
            return valorePredefinito;
        }
        try {
            return Integer.parseInt(riga);
        } catch (NumberFormatException e) {
            System.out.println("Valore non valido, uso il predefinito (" + valorePredefinito + ").");
            return valorePredefinito;
        }
    }
}
