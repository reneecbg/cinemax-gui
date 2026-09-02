/*
 * ============================================================================
 *  CineMax - Laboratorio Interdisciplinare B
 *  Autori:
 *    - <Renee Angelica Cabigting> - Matricola <756997> - Sede <CO>
 *  File: PannelloAreaProiezionista.java
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
 * Area riservata proiezionista: le tre schermate richieste dalla traccia
 * (inserisci proiezione, pianificate, storiche) più il logout.
 *
 * @author CineMax Team
 */
public class PannelloAreaProiezionista extends JPanel {

    public PannelloAreaProiezionista(FinestraPrincipale finestra) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JLabel titolo = new JLabel("Area proiezionista - " + finestra.getUtenteCorrente().getNomeCompleto());
        titolo.setFont(titolo.getFont().deriveFont(Font.BOLD, 20f));
        titolo.setForeground(TemaCineMax.BLU_PRINCIPALE);
        titolo.setAlignmentX(CENTER_ALIGNMENT);
        add(titolo);
        add(Box.createVerticalStrut(20));

        JButton bottoneAggiungi = new JButton("Inserisci nuova proiezione");
        JButton bottonePianificate = new JButton("Proiezioni pianificate");
        JButton bottoneStoriche = new JButton("Proiezioni storiche");
        JButton bottoneLogout = new JButton("Logout");
        for (JButton b : new JButton[]{bottoneAggiungi, bottonePianificate, bottoneStoriche, bottoneLogout}) {
            b.setAlignmentX(CENTER_ALIGNMENT);
            add(b);
            add(Box.createVerticalStrut(10));
        }

        bottoneAggiungi.addActionListener(e -> finestra.mostraPannello(new PannelloAggiungiProiezione(finestra)));
        bottonePianificate.addActionListener(e -> finestra.mostraPannello(new PannelloProiezioniPianificate(finestra)));
        bottoneStoriche.addActionListener(e -> finestra.mostraPannello(new PannelloProiezioniStoriche(finestra)));
        bottoneLogout.addActionListener(e -> finestra.logout());
    }
}
