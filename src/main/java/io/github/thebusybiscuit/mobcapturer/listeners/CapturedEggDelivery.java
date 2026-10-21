package io.github.thebusybiscuit.mobcapturer.listeners;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;

import io.github.thebusybiscuit.slimefun4.core.services.scheduling.TaskHandle;

/**
 * Chooses one destination for an egg when player scheduling can retire immediately.
 */
final class CapturedEggDelivery {

    private CapturedEggDelivery() {}

    static void schedule(
        BiFunction<Runnable, Runnable, TaskHandle> scheduler,
        Runnable delivery,
        Runnable fallback
    ) {
        AtomicBoolean claimed = new AtomicBoolean();
        Runnable deliverOnce = () -> {
            if (claimed.compareAndSet(false, true)) {
                delivery.run();
            }
        };
        Runnable fallbackOnce = () -> {
            if (claimed.compareAndSet(false, true)) {
                fallback.run();
            }
        };

        TaskHandle handle = scheduler.apply(deliverOnce, fallbackOnce);
        // Slimefun Legacy 4.1.75 returns a cancelled handle for an already-retired
        // entity without invoking its callback. Newer cores invoke the callback;
        // the same claim also prevents their cancelled handle from dropping twice.
        if (handle.isCancelled()) {
            fallbackOnce.run();
        }
    }
}
