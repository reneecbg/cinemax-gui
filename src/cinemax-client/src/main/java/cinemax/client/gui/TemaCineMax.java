/*
 * ============================================================================
 *  CineMax - Laboratorio Interdisciplinare B
 *  Autori:
 *    - <Renee Angelica Cabigting> - Matricola <756997> - Sede <CO>
 *  File: TemaCineMax.java
 * ============================================================================
 */
package cinemax.client.gui;

import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Font;

/**
 * Applica un tema visivo coerente a tutta l'applicazione, impostando i
 * colori e il font di default a livello di {@link UIManager} invece che
 * pannello per pannello. Va chiamato una sola volta, prima di creare
 * qualsiasi componente Swing (tipicamente all'avvio di {@code ClientCM}).
 * <p>
 * Si usa il Look &amp; Feel Nimbus (incluso nel JDK standard, nessuna
 * libreria esterna) come base, personalizzandone la palette di colori
 * verso un tema blu/grigio professionale. Centralizzare il tema in
 * un'unica classe evita di dover impostare colori sparsi in ogni
 * {@code JPanel}, e permette di cambiare l'aspetto dell'intera
 * applicazione modificando un solo file.
 *
 * @author CineMax Team
 */
public final class TemaCineMax {

    /** Blu principale: intestazioni, pulsanti, elementi di enfasi. */
    public static final Color BLU_PRINCIPALE = new Color(0x1F, 0x4E, 0x79);

    /** Blu più chiaro: hover, selezione, focus. */
    public static final Color BLU_CHIARO = new Color(0x4A, 0x77, 0xA8);

    /** Grigio molto chiaro: sfondo generale delle schermate. */
    public static final Color GRIGIO_SFONDO = new Color(0xF2, 0xF4, 0xF7);

    /** Grigio scuro: testo principale. */
    public static final Color GRIGIO_TESTO = new Color(0x33, 0x33, 0x33);

    private TemaCineMax() {
        // utility class
    }

    /**
     * Applica il Look &amp; Feel Nimbus e la palette di colori personalizzata.
     * Se Nimbus non è disponibile nell'ambiente di esecuzione (raro), la
     * chiamata non lancia eccezioni: l'applicazione prosegue con il
     * Look &amp; Feel di default del sistema operativo.
     */
    public static void applica() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // Nimbus non disponibile: si prosegue con il Look & Feel di default,
            // l'applicazione resta comunque perfettamente funzionante.
        }

        UIManager.put("nimbusBase", BLU_PRINCIPALE);
        UIManager.put("nimbusBlueGrey", GRIGIO_SFONDO);
        UIManager.put("control", GRIGIO_SFONDO);
        UIManager.put("nimbusLightBackground", Color.WHITE);
        UIManager.put("text", GRIGIO_TESTO);
        UIManager.put("nimbusSelectionBackground", BLU_CHIARO);
        UIManager.put("nimbusFocus", BLU_CHIARO);
        UIManager.put("nimbusSelectedText", Color.WHITE);

        Font fontBase = new Font("Segoe UI", Font.PLAIN, 13);
        UIManager.put("defaultFont", fontBase);
    }
}
