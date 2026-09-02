/*
 * ============================================================================
 *  CineMax - Laboratorio Interdisciplinare B
 *  Autori:
 *    - <Renee Angelica Cabigting> - Matricola <756997> - Sede <CO>
 *  File: PannelloAreaCliente.java
 * ============================================================================
 */
package cinemax.client.gui;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Font;

/**
 * Area riservata cliente: menu con accesso alle due funzionalità che
 * richiedono login lato cliente (la prenotazione vera e propria avviene
 * nel dettaglio di una proiezione, raggiungibile dalla ricerca).
 *
 * @author CineMax Team
 */
public class PannelloAreaCliente extends JPanel {

    public PannelloAreaCliente(FinestraPrincipale finestra) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JLabel titolo = new JLabel("Area cliente - " + finestra.getUtenteCorrente().getNomeCompleto());
        titolo.setFont(titolo.getFont().deriveFont(Font.BOLD, 20f));
        titolo.setForeground(TemaCineMax.BLU_PRINCIPALE);
        titolo.setAlignmentX(CENTER_ALIGNMENT);
        add(titolo);
        add(Box.createVerticalStrut(20));

        JButton bottoneCerca = new JButton("Cerca proiezioni e prenota");
        JButton bottoneMiePrenotazioni = new JButton("Le mie prenotazioni");
        JButton bottoneLogout = new JButton("Logout");
        for (JButton b : new JButton[]{bottoneCerca, bottoneMiePrenotazioni, bottoneLogout}) {
            b.setAlignmentX(CENTER_ALIGNMENT);
            add(b);
            add(Box.createVerticalStrut(10));
        }

        bottoneCerca.addActionListener(e -> finestra.mostraPannello(new PannelloRicercaProiezioni(finestra, this)));
        bottoneMiePrenotazioni.addActionListener(e -> finestra.mostraPannello(new PannelloPrenotazioniAttive(finestra)));
        bottoneLogout.addActionListener(e -> finestra.logout());
    }
}
