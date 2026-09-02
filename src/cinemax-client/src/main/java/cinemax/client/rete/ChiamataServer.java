/*
 * ============================================================================
 *  CineMax - Laboratorio Interdisciplinare B
 *  Autori:
 *    - <Renee Angelica Cabigting> - Matricola <756997> - Sede <CO>
 *  File: ChiamataServer.java
 * ============================================================================
 */
package cinemax.client.rete;

import cinemax.common.protocol.Risposta;

import java.io.IOException;

/**
 * Rappresenta una singola chiamata a {@link ConnessioneServer#invia}, da
 * eseguire fuori dall'Event Dispatch Thread. Vedi
 * {@code cinemax.client.gui.FinestraPrincipale#eseguiRichiesta} per come
 * viene usata.
 *
 * @author CineMax Team
 */
@FunctionalInterface
public interface ChiamataServer {
    Risposta chiama() throws IOException;
}
