/*
 * ============================================================================
 *  CineMax - Laboratorio Interdisciplinare B
 *  Autori:
 *    - <Renee Angelica Cabigting> - Matricola <756997> - Sede <CO>
 *  File: PannelloLogin.java
 * ============================================================================
 */
package cinemax.client.gui;

import cinemax.common.dto.UtenteDTO;
import cinemax.common.protocol.Comando;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * Schermata di login. In base al {@code ruolo} restituito dal server dopo
 * l'autenticazione, naviga verso l'area corrispondente (cliente,
 * proiezionista o bigliettaio) — le tre aree condividono lo stesso punto di
 * ingresso invece di avere tre schermate di login separate.
 *
 * @author CineMax Team
 */
public class PannelloLogin extends JPanel {

    public PannelloLogin(FinestraPrincipale finestra) {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        JTextField campoUsername = new JTextField(18);
        JPasswordField campoPassword = new JPasswordField(18);

        c.gridx = 0; c.gridy = 0; add(new JLabel("Username:"), c);
        c.gridx = 1; add(campoUsername, c);
        c.gridx = 0; c.gridy = 1; add(new JLabel("Password:"), c);
        c.gridx = 1; add(campoPassword, c);

        JButton bottoneAccedi = new JButton("Accedi");
        JButton bottoneIndietro = new JButton("Indietro");
        c.gridx = 0; c.gridy = 2; add(bottoneIndietro, c);
        c.gridx = 1; add(bottoneAccedi, c);

        bottoneIndietro.addActionListener(e -> finestra.mostraPannello(new PannelloMenuIniziale(finestra)));

        bottoneAccedi.addActionListener(e -> {
            String username = campoUsername.getText().trim();
            String password = new String(campoPassword.getPassword());
            finestra.eseguiRichiesta(
                    () -> finestra.getConnessione().invia(Comando.LOGIN, username, password),
                    dato -> {
                        UtenteDTO utente = (UtenteDTO) dato;
                        finestra.setUtenteCorrente(utente);
                        finestra.mostraPannello(pannelloAreaPerRuolo(finestra, utente));
                    });
        });
    }

    /** Sceglie la schermata "area riservata" giusta in base al ruolo dell'utente. */
    private JPanel pannelloAreaPerRuolo(FinestraPrincipale finestra, UtenteDTO utente) {
        switch (utente.getRuolo()) {
            case PROIEZIONISTA:
                return new PannelloAreaProiezionista(finestra);
            case BIGLIETTAIO:
                return new PannelloAreaBigliettaio(finestra);
            case CLIENTE:
            default:
                return new PannelloAreaCliente(finestra);
        }
    }
}
