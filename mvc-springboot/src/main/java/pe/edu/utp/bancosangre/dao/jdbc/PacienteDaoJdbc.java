package pe.edu.utp.bancosangre.dao.jdbc;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pe.edu.utp.bancosangre.dao.PacienteDao;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Implementación JDBC (PostgreSQL) de PacienteDao. */
@Repository
public class PacienteDaoJdbc extends JdbcDaoBase implements PacienteDao {

    public PacienteDaoJdbc(JdbcTemplate jdbc) {
        super(jdbc);
    }

    @Override
    public String coleccion() {
        return "pacientes";
    }

    @Override
    public List<Object> listar() {
        return leer("pacientes");
    }

    @Override
    public Optional<Map<String, Object>> buscar(String identificador) {
        String campo = identificador.startsWith("HC-") ? "historia_clinica" : "dni";
        return jdbc.queryForList("SELECT dni, nombres AS nombre, historia_clinica AS hc FROM pacientes WHERE " + campo + " = ?", identificador)
                .stream().findFirst();
    }

    @Override
    public String reservarHistoria() {
        String historia;
        do {
            historia = jdbc.queryForObject("SELECT 'HC-' || to_char(current_date, 'YYYY') || '-' || lpad(nextval('numero_historia_local')::text, 5, '0')", String.class);
        } while (buscar(historia).isPresent());
        return historia;
    }

    @Override
    public void guardar(List<Map<String, Object>> registros, String correoActor, String rolActor) {
        escribir("pacientes", registros, correoActor, rolActor);
    }
}
