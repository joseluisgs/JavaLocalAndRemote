package dev.joseluisgs.notification;

import dev.joseluisgs.models.Tenista;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import javax.inject.Singleton;

@Singleton
public class TenistasNotifications implements Notifications<Tenista> {

    private static final Logger logger = LoggerFactory.getLogger(TenistasNotifications.class);

    /**
     * Clase que utiliza Sinks.Many de Project Reactor para manejar las notificaciones.
     * Se emplea onBackpressureBuffer para gestionar la sobrecarga de eventos y asegurar
     * que los eventos adicionales se almacenen temporalmente si los suscriptores no
     * pueden consumirlos de inmediato.
     * La emisión y consumo de notificaciones se realizan mediante un Flux.
     */

    private final Sinks.Many<Notification<Tenista>> notificationsSink = Sinks.many().multicast().onBackpressureBuffer();
    @Getter
    private final Flux<Notification<Tenista>> notifications = notificationsSink.asFlux().onBackpressureDrop();

    @Override
    public void send(Notification<Tenista> notification) {
        logger.debug("Enviando notificación: {}", notification);
        notificationsSink.tryEmitNext(notification).orThrow();
    }

}
