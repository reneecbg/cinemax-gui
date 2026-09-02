/*
 * ============================================================================
 *  CineMax - Laboratorio Interdisciplinare B
 *  Autori:
 *    - <Renee Angelica Cabigting> - Matricola <756997> - Sede <CO>
 *  File: RuoloDTO.java
 * ============================================================================
 */
package cinemax.common.dto;

/**
 * Ruolo di un utente. Corrisponde 1:1 al tipo enumerato {@code ruolo_utente}
 * definito nel database (vedi {@code schema.sql}).
 *
 * @author CineMax Team
 */
public enum RuoloDTO {
    CLIENTE,
    PROIEZIONISTA,
    BIGLIETTAIO
}
