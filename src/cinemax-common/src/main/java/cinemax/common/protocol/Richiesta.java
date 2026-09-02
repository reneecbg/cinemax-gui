/*
 * ============================================================================
 *  CineMax - Laboratorio Interdisciplinare B
 *  Autori:
 *    - <Renee Angelica Cabigting> - Matricola <756997> - Sede <CO>
 *  File: Richiesta.java
 * ============================================================================
 */
package cinemax.common.protocol;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Messaggio inviato dal clientCM al serverCM su socket TCP, tramite
 * {@link java.io.ObjectOutputStream}.
 * <p>
 * E' intenzionalmente generico (un {@link Comando} piu' un array di
 * parametri) invece di avere una sottoclasse per ogni comando: questo tiene
 * il protocollo compatto e facilmente estendibile (aggiungere un comando
 * non richiede una nuova classe), al prezzo di un cast esplicito ai
 * parametri lato server. I parametri sono sempre tipi semplici o DTO del
 * package {@code cinemax.common.dto}, mai le classi di persistenza del
 * server: questo mantiene il protocollo indipendente dall'implementazione
 * interna del server (che potrebbe cambiare DB o ORM senza impattare il
 * client).
 *
 * @author CineMax Team
 */
public class Richiesta implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Comando richiesto. */
    private final Comando comando;

    /** Parametri posizionali del comando (puo' essere vuoto). */
    private final Object[] parametri;

    /**
     * Costruisce una richiesta.
     *
     * @param comando   il comando da eseguire
     * @param parametri i parametri posizionali richiesti dal comando
     *                  (l'ordine e il significato sono documentati sul
     *                  metodo del servizio server corrispondente)
     */
    public Richiesta(Comando comando, Object... parametri) {
        this.comando = comando;
        this.parametri = parametri;
    }

    /**
     * @return il comando richiesto
     */
    public Comando getComando() {
        return comando;
    }

    /**
     * @return i parametri posizionali del comando
     */
    public Object[] getParametri() {
        return parametri;
    }

    /**
     * Estrae, con cast, il parametro posizionale indicato.
     *
     * @param indice indice del parametro (a partire da 0)
     * @param tipo   classe attesa del parametro
     * @param <T>    tipo atteso del parametro
     * @return il parametro convertito al tipo richiesto
     */
    public <T> T getParametro(int indice, Class<T> tipo) {
        return tipo.cast(parametri[indice]);
    }

    @Override
    public String toString() {
        return "Richiesta{" + comando + ", parametri=" + Arrays.toString(parametri) + '}';
    }
}
