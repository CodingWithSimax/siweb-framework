package net.simax_dev.objects.events;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class EventEmitter<T> {
    private final List<Subscription<T>> listeners = new ArrayList<>();

    /**
     * Subscribe to the custom event, using a consumer
     * @param consumer the consumer which will get called on event emit
     * @return returns the subscribtion, which can be cancelled
     */
    public Subscription<T> subscribe(Consumer<T> consumer) {
        Subscription<T> subscription = new Subscription<>(
                this,
                consumer
        );
        this.listeners.add(subscription);
        return subscription;
    }

    public void unsubscribe(Subscription<T> subscription) {
        this.listeners.remove(subscription);
    }

    /**
     * Emit a signal to the emitter, all subscriptions will be called
     */
    public void emit(T data) {
        for (Subscription<T> listener : this.listeners) {
            listener.getConsumer().accept(data);
        }
    }
}
