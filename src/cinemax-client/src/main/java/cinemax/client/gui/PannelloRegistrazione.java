/*
 * ============================================================================
 *  CineMax - Laboratorio Interdisciplinare B
 *  Autori:
 *    - <Renee Angelica Cabigting> - Matricola <756997> - Sede <CO>
 *  File: PannelloRegistrazione.java
 * ============================================================================
 */
package cinemax.client.gui;

import cinemax.common.protocol.Comando;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Schermata di registrazione di un nuovo cliente (l'unico ruolo che un
 * utente può auto-registrarsi: proiezionisti e bigliettai sono precaricati
 * nel database, vedi {@code seed.sql}).
 *
 * @author CineMax Team
 */
public class PannelloRegistrazione extends JPanel {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public PannelloRegistrazione(FinestraPrincipale finestra) {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        JTextField campoUsername = new JTextField(18);
        JTextField campoNome = new JTextField(18);
        JTextField campoCognome = new JTextField(18);
        JPasswordField campoPassword = new JPasswordField(18);
        JTextField campoDataNascita = new JTextField(18);
        JTextField campoDomicilio = new JTextField(18);

        int riga = 0;
        riga = aggiungiCampo(c, riga, "Username:", campoUsername);
        riga = aggiungiCampo(c, riga, "Nome:", campoNome);
        riga = aggiungiCampo(c, riga, "Cognome:", campoCognome);
        riga = aggiungiCampo(c, riga, "Password:", campoPassword);
        riga = aggiungiCampo(c, riga, "Data di nascita (gg/mm/aaaa, facoltativa):", campoDataNascita);
        riga = aggiungiCampo(c, riga, "Domicilio:", campoDomicilio);

        JButton bottoneRegistrati = new JButton("Registrati");
        JButton bottoneIndietro = new JButton("Indietro");
        c.gridx = 0; c.gridy = riga; add(bottoneIndietro, c);
        c.gridx = 1; add(bottoneRegistrati, c);

        bottoneIndietro.addActionListener(e -> finestra.mostraPannello(new PannelloMenuIniziale(finestra)));

        bottoneRegistrati.addActionListener(e -> {
            String username = campoUsername.getText().trim();
            String nome = campoNome.getText().trim();
            String cognome = campoCognome.getText().trim();
            String password = new String(campoPassword.getPassword());
            String domicilio = campoDomicilio.getText().trim();
            String testoData = campoDataNascita.getText().trim();

            LocalDate dataNascita = null;
            if (!testoData.isEmpty()) {
                try {
                    dataNascita = LocalDate.parse(testoData, FORMATO_DATA);
                } catch (DateTimeParseException ex) {
                    JOptionPane.showMessageDialog(this,
                            "Data di nascita non valida: usa il formato gg/mm/aaaa",
                            "Formato non valido", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            LocalDate dataNascitaFinale = dataNascita;
            finestra.eseguiRichiesta(
                    () -> finestra.getConnessione().invia(Comando.REGISTRA_CLIENTE,
                            username, nome, cognome, password, dataNascitaFinale, domicilio),
                    dato -> {
                        JOptionPane.showMessageDialog(this,
                                "Registrazione completata. Ora puoi accedere.",
                                "Registrazione riuscita", JOptionPane.INFORMATION_MESSAGE);
                        finestra.mostraPannello(new PannelloLogin(finestra));
                    });
        });
    }

    private int aggiungiCampo(GridBagConstraints c, int riga, String etichetta, javax.swing.JComponent campo) {
        c.gridx = 0; c.gridy = riga; add(new JLabel(etichetta), c);
        c.gridx = 1; add(campo, c);
        return riga + 1;
    }
}
