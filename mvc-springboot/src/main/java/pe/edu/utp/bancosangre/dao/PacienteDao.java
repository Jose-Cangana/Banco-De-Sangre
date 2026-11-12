package pe.edu.utp.bancosangre.dao;

/** DAO de la entidad: Pacientes (receptores de sangre) con su DNI e historia clínica. */
public interface PacienteDao extends Dao {
    java.util.Optional<java.util.Map<String, Object>> buscar(String identificador);
    String reservarHistoria();
}
