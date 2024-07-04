package dev.joseluisgs.validator;

import dev.joseluisgs.error.TenistaError;
import dev.joseluisgs.models.Tenista;
import io.vavr.control.Either;

import java.time.LocalDateTime;

public class TenistaValidator {
    public static Either<TenistaError.ValidationError, Tenista> validate(Tenista tenista) {
        if (tenista.getNombre().isBlank()) {
            return Either.left(new TenistaError.ValidationError("El nombre del tenista no puede estar vacío"));
        }
        if (tenista.getPais().isBlank()) {
            return Either.left(new TenistaError.ValidationError("El país del tenista no puede estar vacío"));
        }
        if (tenista.getAltura() <= 0) {
            return Either.left(new TenistaError.ValidationError("La altura del tenista no puede ser nula o menor o igual a 0"));
        }
        if (tenista.getPeso() <= 0) {
            return Either.left(new TenistaError.ValidationError("El peso del tenista no puede ser nulo o menor o igual a 0"));
        }
        if (tenista.getPuntos() < 0) {
            return Either.left(new TenistaError.ValidationError("Los puntos del tenista no pueden ser nulos o menores a 0"));
        }
        if (tenista.getFechaNacimiento().isAfter(
                LocalDateTime.now().toLocalDate())) {
            return Either.left(new TenistaError.ValidationError("La fecha de nacimiento del tenista no puede ser mayor a la fecha actual"));
        }
        return Either.right(tenista);
    }
}
