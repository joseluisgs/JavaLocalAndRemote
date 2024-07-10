package dev.joseluisgs.notification;

import dev.joseluisgs.dto.TenistaDto;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import javax.inject.Singleton;

@Singleton
public class TenistasNotifications implements Notifications<TenistaDto> {

    private static final Logger logger = LoggerFactory.getLogger(TenistasNotifications.class);

    /**
     * Otra forma es Utiliza ReplayProcessor de Project Reactor para manejar las notificaciones.
     * ReplayProcessor es una clase que permite emitir eventos a múltiples suscriptores.
     * create(1) limita el número de eventos almacenados en el buffer a 1. Solo recibimos la última notificación.
     */

    // ReplayProcessor es una clase que permite emitir eventos a múltiples suscriptores
    // private final ReplayProcessor<Notification<TenistaDto>> notificationsProcessor = ReplayProcessor.create(1);

    /**
     * Clase que utiliza Sinks.Many de Project Reactor para manejar las notificaciones.
     * Many es una clase que permite emitir eventos a múltiples suscriptores.
     * Raplay() permite que los nuevos suscriptores reciban los eventos anteriores.
     * Limit(1) limita el número de eventos almacenados en el buffer a 1. Solo recibimos la última notificación.
     */

    // Sinks.Many es una clase que permite emitir eventos a múltiples suscriptores, l
    private final Sinks.Many<Notification<TenistaDto>> notificationsSink = Sinks.many().replay().limit(1);

    // El método asFlux() devuelve un Flux que se suscribe a los eventos emitidos por notificationsSink.
    // Por lo tanto, cualquier notificación emitida por notificationsSink se reenvía a los suscriptores de notifications.
    // onBackpressureDrop() descarta los eventos si los suscriptores no pueden consumirlos de inmediato.
    @Getter
    private final Flux<Notification<TenistaDto>> notifications = notificationsSink.asFlux().onBackpressureDrop();

    @Override
    public void send(Notification<TenistaDto> notification) {
        logger.debug("Enviando notificación: {}", notification);
        notificationsSink.tryEmitNext(notification).orThrow();
    }

}
