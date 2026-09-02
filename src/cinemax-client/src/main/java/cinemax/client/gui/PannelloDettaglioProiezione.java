/*
 * ============================================================================
 *  CineMax - Laboratorio Interdisciplinare B
 *  Autori:
 *    - <Renee Angelica Cabigting> - Matricola <756997> - Sede <CO>
 *  File: PannelloDettaglioProiezione.java
 * ============================================================================
 */
package cinemax.client.gui;

import cinemax.common.dto.ProiezioneDTO;
import cinemax.common.dto.RuoloDTO;
import cinemax.common.dto.UtenteDTO;
import cinemax.common.protocol.Comando;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.format.DateTimeFormatter;

/**
 * Schermata di dettaglio di una proiezione — richiesta esplicitamente dalla
 * traccia, ed è la stessa per tutti (guest, cliente non loggato in quel
 * momento, cliente loggato): solo se {@link FinestraPrincipale#getUtenteCorrente()}
 * è un {@link RuoloDTO#CLIENTE} viene mostrato anche il modulo di
 * prenotazione, che è quindi anche "la schermata in cui un cliente può
 * inserire una prenotazione per una proiezione" richiesta dalla traccia,
 * senza bisogno di una schermata separata.
 *
 * @author CineMax Team
 */
public class PannelloDettaglioProiezione extends JPanel {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public PannelloDettaglioProiezione(FinestraPrincipale finestra, int idProiezione, JPanel pannelloPrecedente) {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel corpo = new JPanel();
        corpo.setLayout(new BoxLayout(corpo, BoxLayout.Y_AXIS));
        add(corpo, BorderLayout.CENTER);

        JPanel barraInferiore = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton bottoneIndietro = new JButton("Indietro");
        barraInferiore.add(bottoneIndietro);
        add(barraInferiore, BorderLayout.SOUTH);
        bottoneIndietro.addActionListener(e -> finestra.mostraPannello(pannelloPrecedente));

        // Il dettaglio si carica dal server: fino ad allora si mostra solo "Caricamento...".
        JLabel etichettaCaricamento = new JLabel("Caricamento dettagli...");
        corpo.add(etichettaCaricamento);

        finestra.eseguiRichiesta(
                () -> finestra.getConnessione().invia(Comando.DETTAGLIO_PROIEZIONE, idProiezione),
                dato -> {
                    corpo.remove(etichettaCaricamento);
                    ProiezioneDTO p = (ProiezioneDTO) dato;
                    popolaDettaglio(finestra, corpo, p);
                    corpo.revalidate();
                    corpo.repaint();
                });
    }

    private void popolaDettaglio(FinestraPrincipale finestra, JPanel corpo, ProiezioneDTO p) {
        JLabel titolo = new JLabel(p.getFilm().getTitolo());
        titolo.setFont(titolo.getFont().deriveFont(Font.BOLD, 22f));
        corpo.add(titolo);
        corpo.add(Box.createVerticalStrut(10));

        corpo.add(new JLabel("Genere: " + p.getFilm().getGenere()));
        corpo.add(new JLabel("Regista: " + p.getFilm().getRegista()));
        corpo.add(new JLabel("Anno: " + p.getFilm().getAnno()));
        corpo.add(new JLabel("Durata: " + p.getFilm().getDurataMinuti() + " minuti"));
        corpo.add(new JLabel("Età minima consigliata: " + p.getFilm().getEtaMinima()));
        corpo.add(Box.createVerticalStrut(10));
        corpo.add(new JLabel("Data e ora: " + p.getDataOra().format(FORMATO)));
        corpo.add(new JLabel(String.format("Costo biglietto: %.2f €", p.getCostoBiglietto())));
        corpo.add(new JLabel("Posti liberi: " + p.getPostiLiberi()));

        UtenteDTO utente = finestra.getUtenteCorrente();
        if (utente != null && utente.getRuolo() == RuoloDTO.CLIENTE) {
            corpo.add(Box.createVerticalStrut(20));
            corpo.add(aggiungiModuloPrenotazione(finestra, p, utente));
        }
    }

    /**
     * Costruisce il modulo di prenotazione: è qui che si realizza la
     * schermata "un cliente registrato può inserire una prenotazione per
     * una proiezione" richiesta dalla traccia.
     */
    private JPanel aggiungiModuloPrenotazione(FinestraPrincipale finestra, ProiezioneDTO p, UtenteDTO utente) {
        JPanel modulo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        modulo.add(new JLabel("Numero biglietti:"));
        SpinnerNumberModel modelloSpinner = new SpinnerNumberModel(1, 1, Math.max(1, p.getPostiLiberi()), 1);
        JSpinner spinnerBiglietti = new JSpinner(modelloSpinner);
        modulo.add(spinnerBiglietti);

        JButton bottonePrenota = new JButton("Prenota");
        modulo.add(bottonePrenota);
        bottonePrenota.setEnabled(p.getPostiLiberi() > 0);
        if (p.getPostiLiberi() == 0) {
            modulo.add(new JLabel("(nessun posto disponibile)"));
        }

        bottonePrenota.addActionListener(e -> {
            int numeroBiglietti = (Integer) spinnerBiglietti.getValue();
            finestra.eseguiRichiesta(
                    () -> finestra.getConnessione().invia(Comando.CREA_PRENOTAZIONE,
                            utente.getUsername(), p.getIdProiezione(), numeroBiglietti),
                    dato -> {
                        JOptionPane.showMessageDialog(this,
                                "Prenotazione creata con successo.",
                                "Prenotazione confermata", JOptionPane.INFORMATION_MESSAGE);
                        finestra.mostraPannello(new PannelloAreaCliente(finestra));
                    });
        });

        return modulo;
    }
}
