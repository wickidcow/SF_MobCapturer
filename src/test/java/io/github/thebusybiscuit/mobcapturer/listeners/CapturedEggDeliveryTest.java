package io.github.thebusybiscuit.mobcapturer.listeners;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import io.github.thebusybiscuit.slimefun4.core.services.scheduling.TaskHandle;

class CapturedEggDeliveryTest {

    @Test
    void alreadyRetiredPlayerOnReleasedCoreKeepsOneEgg() {
        AtomicInteger inventory = new AtomicInteger();
        AtomicInteger dropped = new AtomicInteger();

        // Released 4.1.75: native submission returns null and the wrapper returns
        // a cancelled handle without invoking either callback.
        CapturedEggDelivery.schedule(
            (deliver, retired) -> new Handle(true),
            inventory::incrementAndGet,
            dropped::incrementAndGet
        );

        assertEquals(0, inventory.get());
        assertEquals(1, dropped.get());
    }

    @Test
    void immediateRetirementAndCancelledHandleDoNotDropTwice() {
        AtomicInteger inventory = new AtomicInteger();
        AtomicInteger dropped = new AtomicInteger();

        // Newer cores invoke retirement before returning the cancelled handle.
        CapturedEggDelivery.schedule(
            (deliver, retired) -> {
                retired.run();
                return new Handle(true);
            },
            inventory::incrementAndGet,
            dropped::incrementAndGet
        );

        assertEquals(0, inventory.get());
        assertEquals(1, dropped.get());
    }

    @Test
    void retirementAfterAcceptedSubmissionKeepsOneEgg() {
        AtomicInteger inventory = new AtomicInteger();
        AtomicInteger dropped = new AtomicInteger();
        AtomicReference<Runnable> retireLater = new AtomicReference<>();

        CapturedEggDelivery.schedule(
            (deliver, retired) -> {
                retireLater.set(retired);
                return new Handle(false);
            },
            inventory::incrementAndGet,
            dropped::incrementAndGet
        );
        assertEquals(0, dropped.get());
        retireLater.get().run();
        retireLater.get().run();

        assertEquals(0, inventory.get());
        assertEquals(1, dropped.get());
    }

    @Test
    void successfulDeliveryCannotAlsoRunRetirementFallback() {
        AtomicInteger inventory = new AtomicInteger();
        AtomicInteger dropped = new AtomicInteger();
        AtomicReference<Runnable> deliverLater = new AtomicReference<>();
        AtomicReference<Runnable> retireLater = new AtomicReference<>();

        CapturedEggDelivery.schedule(
            (deliver, retired) -> {
                deliverLater.set(deliver);
                retireLater.set(retired);
                return new Handle(false);
            },
            inventory::incrementAndGet,
            dropped::incrementAndGet
        );
        deliverLater.get().run();
        retireLater.get().run();
        deliverLater.get().run();

        assertEquals(1, inventory.get());
        assertEquals(0, dropped.get());
    }

    @Test
    void competingCallbacksCannotCreateTwoEggs() throws InterruptedException {
        AtomicInteger inventory = new AtomicInteger();
        AtomicInteger dropped = new AtomicInteger();
        AtomicReference<Runnable> deliverLater = new AtomicReference<>();
        AtomicReference<Runnable> retireLater = new AtomicReference<>();
        CountDownLatch start = new CountDownLatch(1);

        CapturedEggDelivery.schedule(
            (deliver, retired) -> {
                deliverLater.set(deliver);
                retireLater.set(retired);
                return new Handle(false);
            },
            inventory::incrementAndGet,
            dropped::incrementAndGet
        );
        Thread delivery = new Thread(() -> awaitAndRun(start, deliverLater.get()));
        Thread retirement = new Thread(() -> awaitAndRun(start, retireLater.get()));
        delivery.start();
        retirement.start();
        start.countDown();
        delivery.join();
        retirement.join();

        assertEquals(1, inventory.get() + dropped.get());
    }

    @Test
    void uncertainInventoryFailureIsNotRetriedAsAnotherEgg() {
        AtomicInteger inventory = new AtomicInteger();
        AtomicInteger dropped = new AtomicInteger();
        AtomicReference<Runnable> deliverLater = new AtomicReference<>();
        AtomicReference<Runnable> retireLater = new AtomicReference<>();

        CapturedEggDelivery.schedule(
            (deliver, retired) -> {
                deliverLater.set(deliver);
                retireLater.set(retired);
                return new Handle(false);
            },
            () -> {
                inventory.incrementAndGet();
                throw new IllegalStateException("Failure after inventory mutation");
            },
            dropped::incrementAndGet
        );
        assertThrows(IllegalStateException.class, deliverLater.get()::run);
        retireLater.get().run();

        assertEquals(1, inventory.get());
        assertEquals(0, dropped.get());
    }

    private static void awaitAndRun(CountDownLatch start, Runnable task) {
        try {
            start.await();
            task.run();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new AssertionError(exception);
        }
    }

    private static final class Handle implements TaskHandle {
        private boolean cancelled;

        private Handle(boolean cancelled) {
            this.cancelled = cancelled;
        }

        @Override
        public void cancel() {
            cancelled = true;
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }
    }
}
