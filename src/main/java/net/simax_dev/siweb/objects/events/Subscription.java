package net.simax_dev.siweb.objects.events;

import java.util.function.Consumer;

public class Subscription<T> {
    private final EventEmitter<T> eventEmitter;
    private final Consumer<T> consumer;

    public Subscription(EventEmitter<T> emitter, Consumer<T> consumer) {
        this.eventEmitter = emitter;
        this.consumer = consumer;
    }

    /**
     * Unsubscribe from current event
     * -> The consumer won't be called anymore
     */
    public void unsubscribe() {
        this.eventEmitter.unsubscribe(this);
    }

    public Consumer<T> getConsumer() {
        return this.consumer;
    }
}
