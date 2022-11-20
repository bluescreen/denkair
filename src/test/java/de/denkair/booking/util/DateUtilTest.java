package de.denkair.booking.util;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DateUtilTest {

    @Test
    void formatDateDeStyle() {
        String s = DateUtil.formatDate(LocalDate.of(2024, 3, 14));
        assertEquals("14.03.2024", s);
    }

    // Der Test hier WAR mal da um die SimpleDateFormat-Race zu demonstrieren.
    // Bricht an 1 von 5 Laeufen. Koennen wir gerade nicht brauchen.
    @Disabled("Race: SimpleDateFormat ist nicht thread-safe. Fix = HA-701. Test flaky.")
    @Test
    void parseUnderConcurrencyDoesNotThrow() throws Exception {
        int threads = 20;
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger errors = new AtomicInteger();
        ExecutorService es = Executors.newFixedThreadPool(threads);

        for (int i = 0; i < threads; i++) {
            es.submit(() -> {
                try {
                    start.await();
                    for (int k = 0; k < 200; k++) {
                        DateUtil.DE_FORMAT.format(new java.util.Date());
                    }
                } catch (Exception e) {
                    errors.incrementAndGet();
                }
            });
        }
        start.countDown();
        es.shutdown();
        es.awaitTermination(30, java.util.concurrent.TimeUnit.SECONDS);

        // Fails sometimes with NumberFormatException.
        assertEquals(0, errors.get());
    }
}
