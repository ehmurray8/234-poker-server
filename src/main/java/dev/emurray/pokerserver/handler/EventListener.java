package dev.emurray.pokerserver.handler;

import com.google.common.eventbus.Subscribe;
import dev.emurray.pokerserver.model.SocketEvent;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class EventListener {

    private final List<RequestHandler> handlers;

    public EventListener(List<RequestHandler> requestHandlers) {
        this.handlers = requestHandlers;
    }

    @Subscribe
    public void handleEvent(SocketEvent event) {
        handlers.stream().filter(it -> it.canHandle(event.messageType()))
            .forEach(it -> it.handle(event.sessionId(), event.session(), event.messageDetails()));
    }
}
