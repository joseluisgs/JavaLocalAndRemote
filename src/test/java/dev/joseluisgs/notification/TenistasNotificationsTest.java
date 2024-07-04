package dev.joseluisgs.notification;

import dev.joseluisgs.models.Tenista;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TenistasNotificationsTest {
    private final TenistasNotifications tenistasNotifications = new TenistasNotifications();

    private final Tenista tenistaTest = Tenista.builder()
            .id(1L)
            .nombre("Roger Federer")
            .pais("Suiza")
            .altura(185)
            .peso(85)
            .puntos(9600)
            .mano(Tenista.Mano.DIESTRO)
            .fechaNacimiento(LocalDate.of(1981, 8, 8))
            .build();

    @Test
    @DisplayName("Envío de notificación")
    void enviarNotificacion() {
        Notification<Tenista> notification = new Notification<>(Notification.Type.CREATE, tenistaTest);

        tenistasNotifications.send(notification);
        var result = tenistasNotifications.getNotifications().blockFirst();

        assertAll("Verificación de notificación",
                () -> assertEquals(Notification.Type.CREATE, result.type(), "El tipo de notificación debe ser CREATE"),
                () -> assertEquals(tenistaTest, result.item(), "Los datos de la notificación deben ser los mismos")
        );
    }

}