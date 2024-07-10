package dev.joseluisgs.notification;

import dev.joseluisgs.dto.TenistaDto;
import dev.joseluisgs.mapper.TenistaMapper;
import dev.joseluisgs.models.Tenista;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TenistasNotificationsTest {
    private final TenistasNotifications tenistasNotifications = new TenistasNotifications();


    @Test
    @DisplayName("Envío de notificación es correcto")
    void enviarNotificacion() {
        var tenistaTest = TenistaMapper.toTenistaDto(Tenista.builder()
                .id(1L)
                .nombre("Roger Federer")
                .pais("Suiza")
                .altura(185)
                .peso(85)
                .puntos(9600)
                .mano(Tenista.Mano.DIESTRO)
                .fechaNacimiento(LocalDate.of(1981, 8, 8))
                .build());
        Notification<TenistaDto> notification = new Notification<>(Notification.Type.CREATE, tenistaTest);

        tenistasNotifications.send(notification);
        var result = tenistasNotifications.getNotifications().blockFirst();

        assertAll("Verificación de notificación",
                () -> assertEquals(Notification.Type.CREATE, result.type(), "El tipo de notificación debe ser CREATE"),
                () -> assertEquals(tenistaTest, result.item(), "Los datos de la notificación deben ser los mismos")
        );
    }

    @Test
    @DisplayName("Se descarta la primera notificación si hay más de una y no se ha procesado")
    void obtenerNotificaciones() {
        var tenistaTest = TenistaMapper.toTenistaDto(Tenista.builder()
                .id(1L)
                .nombre("TenistaTest")
                .pais("Suiza")
                .altura(185)
                .peso(85)
                .puntos(9600)
                .mano(Tenista.Mano.DIESTRO)
                .fechaNacimiento(LocalDate.of(1981, 8, 8))
                .build());

        Notification<TenistaDto> notification1 = new Notification<>(Notification.Type.CREATE, tenistaTest, "test1");
        Notification<TenistaDto> notification2 = new Notification<>(Notification.Type.UPDATE, tenistaTest, "test2");

        tenistasNotifications.send(notification1);
        tenistasNotifications.send(notification2);

        var result = tenistasNotifications.getNotifications().blockFirst();

        assertAll("Verificación de notificación",
                () -> assertEquals(Notification.Type.UPDATE, result.type(), "El tipo de notificación debe ser UPDATE"),
                () -> assertEquals(tenistaTest, result.item(), "Los datos de la notificación deben ser los mismos"),
                () -> assertEquals("test2", result.message(), "El mensaje de la notificación debe ser el mismo")
        );
    }


}