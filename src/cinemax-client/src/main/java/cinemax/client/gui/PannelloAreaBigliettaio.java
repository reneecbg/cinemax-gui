/*
 * ============================================================================
 *  CineMax - Laboratorio Interdisciplinare B
 *  Autori:
 *    - <Renee Angelica Cabigting> - Matricola <756997> - Sede <CO>
 *  File: PannelloAreaBigliettaio.java
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
 * Area riservata bigliettaio: le due schermate richieste dalla traccia
 * (prenotazioni di oggi, ricerca prenotazioni) più il logout.
 *
 * @author CineMax Team
 */
public class PannelloAreaBigliettaio extends JPanel {

    public PannelloAreaBigliettaio(FinestraPrincipale finestra) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JLabel titolo = new JLabel("Area bigliettaio - " + finestra.getUtenteCorrente().getNomeCompleto());
        titolo.setFont(titolo.getFont().deriveFont(Font.BOLD, 20f));
        titolo.setForeground(TemaCineMax.BLU_PRINCIPALE);
        titolo.setAlignmentX(CENTER_ALIGNMENT);
        add(titolo);
        add(Box.createVerticalStrut(20));

        JButton bottoneOggi = new JButton("Prenotazioni di oggi");
        JButton bottoneCerca = new JButton("Cerca prenotazioni");
        JButton bottoneLogout = new JButton("Logout");
        for (JButton b : new JButton[]{bottoneOggi, bottoneCerca, bottoneLogout}) {
            b.setAlignmentX(CENTER_ALIGNMENT);
            add(b);
            add(Box.createVerticalStrut(10));
        }

        bottoneOggi.addActionListener(e -> finestra.mostraPannello(new PannelloPrenotazioniOggi(finestra)));
        bottoneCerca.addActionListener(e -> finestra.mostraPannello(new PannelloCercaPrenotazioni(finestra)));
        bottoneLogout.addActionListener(e -> finestra.logout());
    }
}
