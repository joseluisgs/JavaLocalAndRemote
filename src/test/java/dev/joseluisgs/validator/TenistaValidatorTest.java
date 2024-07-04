package dev.joseluisgs.validator;

import dev.joseluisgs.error.TenistaError;
import dev.joseluisgs.models.Tenista;
import io.vavr.control.Either;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class TenistaValidatorTest {
    @Test
    @DisplayName("Validar tenista con nombre vacío")
    void nombreNoPuedeEstarVacio() {
        Tenista tenista = new Tenista();
        tenista.setNombre("");
        tenista.setPais("España");
        tenista.setAltura(180);
        tenista.setPeso(75);
        tenista.setPuntos(1000);
        tenista.setFechaNacimiento(LocalDate.of(1990, 1, 1));

        Either<TenistaError.ValidationError, Tenista> resultado = TenistaValidator.validate(tenista);

        assertAll("Validar nombre vacío",
                () -> assertTrue(resultado.isLeft(), "Resultado debe ser un error"),
                () -> assertEquals("ERROR: El nombre del tenista no puede estar vacío", resultado.getLeft().getMessage())
        );
    }

    @Test
    @DisplayName("Validar tenista con país vacío")
    void paisNoPuedeEstarVacio() {
        Tenista tenista = new Tenista();
        tenista.setNombre("Rafael Nadal");
        tenista.setPais("");
        tenista.setAltura(185);
        tenista.setPeso(85);
        tenista.setPuntos(1520);
        tenista.setFechaNacimiento(LocalDate.of(1986, 6, 3));

        Either<TenistaError.ValidationError, Tenista> resultado = TenistaValidator.validate(tenista);

        assertAll("Validar país vacío",
                () -> assertTrue(resultado.isLeft(), "Resultado debe ser un error"),
                () -> assertEquals("ERROR: El país del tenista no puede estar vacío", resultado.getLeft().getMessage())
        );
    }

    @Test
    @DisplayName("Validar tenista con altura no válida")
    void alturaDebeSerValida() {
        Tenista tenista = new Tenista();
        tenista.setNombre("Rafael Nadal");
        tenista.setPais("España");
        tenista.setAltura(0);
        tenista.setPeso(85);
        tenista.setPuntos(1520);
        tenista.setFechaNacimiento(LocalDate.of(1986, 6, 3));

        Either<TenistaError.ValidationError, Tenista> resultado = TenistaValidator.validate(tenista);

        assertAll("Validar altura no válida",
                () -> assertTrue(resultado.isLeft(), "Resultado debe ser un error"),
                () -> assertEquals("ERROR: La altura del tenista no puede ser nula o menor o igual a 0", resultado.getLeft().getMessage())
        );
    }

    @Test
    @DisplayName("Validar tenista con peso no válido")
    void pesoDebeSerValido() {
        Tenista tenista = new Tenista();
        tenista.setNombre("Roger Federer");
        tenista.setPais("Suiza");
        tenista.setAltura(185);
        tenista.setPeso(0);
        tenista.setPuntos(1260);
        tenista.setFechaNacimiento(LocalDate.of(1981, 8, 8));

        Either<TenistaError.ValidationError, Tenista> resultado = TenistaValidator.validate(tenista);

        assertAll("Validar peso no válido",
                () -> assertTrue(resultado.isLeft(), "Resultado debe ser un error"),
                () -> assertEquals("ERROR: El peso del tenista no puede ser nulo o menor o igual a 0", resultado.getLeft().getMessage())
        );
    }

    @Test
    @DisplayName("Validar tenista con puntos negativos")
    void puntosNoPuedenSerNegativos() {
        Tenista tenista = new Tenista();
        tenista.setNombre("Novak Djokovic");
        tenista.setPais("Serbia");
        tenista.setAltura(188);
        tenista.setPeso(77);
        tenista.setPuntos(-10);
        tenista.setFechaNacimiento(LocalDate.of(1987, 5, 22));

        Either<TenistaError.ValidationError, Tenista> resultado = TenistaValidator.validate(tenista);

        assertAll("Validar puntos negativos",
                () -> assertTrue(resultado.isLeft(), "Resultado debe ser un error"),
                () -> assertEquals("ERROR: Los puntos del tenista no pueden ser nulos o menores a 0", resultado.getLeft().getMessage())
        );
    }

    @Test
    @DisplayName("Validar tenista con fecha de nacimiento futura")
    void fechaDeNacimientoNoPuedeSerFutura() {
        Tenista tenista = new Tenista();
        tenista.setNombre("Andy Murray");
        tenista.setPais("Reino Unido");
        tenista.setAltura(190);
        tenista.setPeso(80);
        tenista.setPuntos(1400);
        tenista.setFechaNacimiento(LocalDate.now().plusDays(1));

        Either<TenistaError.ValidationError, Tenista> resultado = TenistaValidator.validate(tenista);

        assertAll("Validar fecha de nacimiento futura",
                () -> assertTrue(resultado.isLeft(), "Resultado debe ser un error"),
                () -> assertEquals("ERROR: La fecha de nacimiento del tenista no puede ser mayor a la fecha actual", resultado.getLeft().getMessage())
        );
    }

    @Test
    @DisplayName("Validar tenista válido")
    void tenistaValido() {
        Tenista tenista = new Tenista();
        tenista.setNombre("Juan Martín del Potro");
        tenista.setPais("Argentina");
        tenista.setAltura(198);
        tenista.setPeso(97);
        tenista.setPuntos(1120);
        tenista.setFechaNacimiento(LocalDate.of(1988, 9, 23));

        Either<TenistaError.ValidationError, Tenista> resultado = TenistaValidator.validate(tenista);

        assertAll("Validar tenista válido",
                () -> assertTrue(resultado.isRight(), "Resultado debe ser un tenista válido"),
                () -> assertEquals(tenista, resultado.get(), "El tenista debería coincidir")
        );
    }
}