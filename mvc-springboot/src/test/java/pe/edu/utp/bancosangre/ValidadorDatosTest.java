package pe.edu.utp.bancosangre;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidadorDatosTest {

    private static final String DONANTE_OK = """
            {"donantes":[{"nombre":"Ana Pérez","dni":"12345678","sexo":"F","telefono":"987654321",
            "correo":"ana@correo.com","direccion":"Av. Lima 123","fechaNacimiento":"15/03/1990",
            "pesoKg":60,"hemoglobina":13.5,"antecedentes":"Ninguno","estado":"Apto"}]}""";

    @Test
    void donanteValidoPasa() {
        assertNull(ValidadorDatos.validar(DONANTE_OK));
    }

    @Test
    void jsonInvalidoSeRechaza() {
        assertNotNull(ValidadorDatos.validar("{no es json"));
        assertNotNull(ValidadorDatos.validar("[1,2]"));
    }

    @Test
    void dniConLetrasSeRechaza() {
        String error = ValidadorDatos.validar(DONANTE_OK.replace("12345678", "1234567A"));
        assertTrue(error.contains("DNI"), error);
    }

    @Test
    void pesoFueraDeRangoSeRechaza() {
        assertTrue(ValidadorDatos.validar(DONANTE_OK.replace("\"pesoKg\":60", "\"pesoKg\":20")).contains("peso"));
    }

    @Test
    void fechaImposibleSeRechaza() {
        assertNotNull(ValidadorDatos.validar(DONANTE_OK.replace("15/03/1990", "31/02/1990")));
    }

    @Test
    void donanteDiferidoExigeMotivo() {
        String error = ValidadorDatos.validar(DONANTE_OK.replace("\"estado\":\"Apto\"", "\"estado\":\"Diferido\""));
        assertTrue(error.contains("motivo"), error);
    }
}
