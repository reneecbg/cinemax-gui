/*
 * ============================================================================
 *  CineMax - Laboratorio Interdisciplinare B
 *  Autori:
 *    - <Renee Angelica Cabigting> - Matricola <756997> - Sede <CO>
 *  File: PannelloAggiungiProiezione.java
 * ============================================================================
 */
package cinemax.client.gui;

import cinemax.common.protocol.Comando;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Schermata "inserisci una nuova proiezione" del proiezionista: inserisce
 * contestualmente anche il film, se non già presente (vedi
 * {@code ServizioProiezioni#aggiungiProiezione}, che riusa il film se
 * titolo/regista/anno coincidono con uno già esistente).
 *
 * @author CineMax Team
 */
public class PannelloAggiungiProiezione extends JPanel {

    private static final DateTimeFormatter FORMATO_DATA_ORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public PannelloAggiungiProiezione(FinestraPrincipale finestra) {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        JTextField campoTitolo = new JTextField(20);
        JTextField campoGenere = new JTextField(20);
        JTextField campoRegista = new JTextField(20);
        JTextField campoAnno = new JTextField(20);
        JTextField campoDurata = new JTextField(20);
        JTextField campoEtaMinima = new JTextField(20);
        JTextField campoDataOra = new JTextField(20);
        JTextField campoCosto = new JTextField(20);

        int riga = 0;
        riga = aggiungiCampo(c, riga, "Titolo:", campoTitolo);
        riga = aggiungiCampo(c, riga, "Genere:", campoGenere);
        riga = aggiungiCampo(c, riga, "Regista:", campoRegista);
        riga = aggiungiCampo(c, riga, "Anno:", campoAnno);
        riga = aggiungiCampo(c, riga, "Durata (minuti):", campoDurata);
        riga = aggiungiCampo(c, riga, "Età minima:", campoEtaMinima);
        riga = aggiungiCampo(c, riga, "Data e ora (gg/mm/aaaa hh:mm):", campoDataOra);
        riga = aggiungiCampo(c, riga, "Costo biglietto (€):", campoCosto);

        JButton bottoneSalva = new JButton("Aggiungi proiezione");
        JButton bottoneIndietro = new JButton("Indietro");
        c.gridx = 0; c.gridy = riga; add(bottoneIndietro, c);
        c.gridx = 1; add(bottoneSalva, c);

        bottoneIndietro.addActionListener(e -> finestra.mostraPannello(new PannelloAreaProiezionista(finestra)));

        bottoneSalva.addActionListener(e -> {
            String titolo = campoTitolo.getText().trim();
            String genere = campoGenere.getText().trim();
            String regista = campoRegista.getText().trim();

            int anno, durata, etaMinima;
            double costo;
            LocalDateTime dataOra;
            try {
                anno = Integer.parseInt(campoAnno.getText().trim());
                durata = Integer.parseInt(campoDurata.getText().trim());
                etaMinima = Integer.parseInt(campoEtaMinima.getText().trim());
                costo = Double.parseDouble(campoCosto.getText().trim());
                dataOra = LocalDateTime.parse(campoDataOra.getText().trim(), FORMATO_DATA_ORA);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Controlla anno, durata, età minima e costo: devono essere numeri.");
                return;
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Formato data/ora non valido: usa gg/mm/aaaa hh:mm");
                return;
            }

            finestra.eseguiRichiesta(
                    () -> finestra.getConnessione().invia(Comando.AGGIUNGI_PROIEZIONE,
                            titolo, genere, regista, anno, durata, etaMinima, dataOra, costo),
                    dato -> {
                        JOptionPane.showMessageDialog(this, "Proiezione aggiunta con successo.");
                        finestra.mostraPannello(new PannelloAreaProiezionista(finestra));
                    });
        });
    }

    private int aggiungiCampo(GridBagConstraints c, int riga, String etichetta, JComponent campo) {
        c.gridx = 0; c.gridy = riga; add(new JLabel(etichetta), c);
        c.gridx = 1; add(campo, c);
        return riga + 1;
    }
}
