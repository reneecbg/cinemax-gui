/*
 * ============================================================================
 *  CineMax - Laboratorio Interdisciplinare B
 *  Autori:
 *    - <Renee Angelica Cabigting> - Matricola <756997> - Sede <CO>
 *  File: PannelloMenuIniziale.java
 * ============================================================================
 */
package cinemax.client.gui;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Font;

/**
 * Menu iniziale dell'applicazione (schermata richiesta esplicitamente dalla
 * traccia): permette di accedere, registrarsi, oppure proseguire come guest
 * indicando il nome (anche parziale) di un film.
 *
 * @author CineMax Team
 */
public class PannelloMenuIniziale extends JPanel {

    public PannelloMenuIniziale(FinestraPrincipale finestra) {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JLabel titolo = new JLabel("CineMax", SwingConstants.CENTER);
        titolo.setFont(titolo.getFont().deriveFont(Font.BOLD, 28f));
        titolo.setForeground(TemaCineMax.BLU_PRINCIPALE);
        add(titolo, BorderLayout.NORTH);

        JPanel centro = new JPanel();
        centro.setLayout(new BoxLayout(centro, BoxLayout.Y_AXIS));

        JButton bottoneLogin = new JButton("Accedi");
        JButton bottoneRegistrati = new JButton("Registrati");
        bottoneLogin.setAlignmentX(CENTER_ALIGNMENT);
        bottoneRegistrati.setAlignmentX(CENTER_ALIGNMENT);
        bottoneLogin.addActionListener(e -> finestra.mostraPannello(new PannelloLogin(finestra)));
        bottoneRegistrati.addActionListener(e -> finestra.mostraPannello(new PannelloRegistrazione(finestra)));

        centro.add(bottoneLogin);
        centro.add(Box.createVerticalStrut(10));
        centro.add(bottoneRegistrati);
        centro.add(Box.createVerticalStrut(30));

        JLabel etichettaGuest = new JLabel("Oppure continua come ospite:");
        etichettaGuest.setAlignmentX(CENTER_ALIGNMENT);
        centro.add(etichettaGuest);
        centro.add(Box.createVerticalStrut(8));

        JPanel rigaGuest = new JPanel();
        JTextField campoTitolo = new JTextField(20);
        JButton bottoneGuest = new JButton("Cerca proiezioni del film");
        rigaGuest.add(new JLabel("Nome del film (anche parziale):"));
        rigaGuest.add(campoTitolo);
        rigaGuest.add(bottoneGuest);
        rigaGuest.setAlignmentX(CENTER_ALIGNMENT);
        centro.add(rigaGuest);

        bottoneGuest.addActionListener(e -> {
            String titoloParziale = campoTitolo.getText().trim();
            if (titoloParziale.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Indica almeno una parte del titolo del film.",
                        "Campo mancante", JOptionPane.WARNING_MESSAGE);
                return;
            }
            finestra.mostraPannello(new PannelloGuestRisultati(finestra, titoloParziale));
        });

        centro.add(Box.createVerticalStrut(20));
        JButton bottoneRicercaLibera = new JButton("Cerca proiezioni (ricerca avanzata)");
        bottoneRicercaLibera.setAlignmentX(CENTER_ALIGNMENT);
        bottoneRicercaLibera.addActionListener(e -> finestra.mostraPannello(new PannelloRicercaProiezioni(finestra)));
        centro.add(bottoneRicercaLibera);

        add(centro, BorderLayout.CENTER);
    }
}
