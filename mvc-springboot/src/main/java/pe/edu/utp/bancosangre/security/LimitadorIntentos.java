package pe.edu.utp.bancosangre.security;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Freno simple contra adivinar contraseñas: tras {@value #MAX_FALLOS} intentos fallidos seguidos de la misma
 * clave (usuario + IP) en {@link #VENTANA}, bloquea nuevos intentos hasta que pase la ventana.
 * Es en memoria (se reinicia con la app); suficiente para una sola instancia.
 */
@Component
public class LimitadorIntentos {

    static final int MAX_FALLOS = 5;
    static final Duration VENTANA = Duration.ofMinutes(15);

    private final Map<String, Deque<Instant>> fallos = new ConcurrentHashMap<>();

    public boolean bloqueado(String clave) {
        return bloqueado(clave, Instant.now());
    }

    boolean bloqueado(String clave, Instant ahora) {
        Deque<Instant> lista = fallos.get(clave);
        if (lista == null) return false;
        synchronized (lista) {
            purgar(lista, ahora);
            return lista.size() >= MAX_FALLOS;
        }
    }

    public void registrarFallo(String clave) {
        registrarFallo(clave, Instant.now());
    }

    void registrarFallo(String clave, Instant ahora) {
        Deque<Instant> lista = fallos.computeIfAbsent(clave, k -> new ArrayDeque<>());
        synchronized (lista) {
            purgar(lista, ahora);
            lista.addLast(ahora);
        }
    }

    public void limpiar(String clave) {
        fallos.remove(clave);
    }

    private static void purgar(Deque<Instant> lista, Instant ahora) {
        Instant limite = ahora.minus(VENTANA);
        while (!lista.isEmpty() && lista.peekFirst().isBefore(limite)) lista.removeFirst();
    }
}
