package de.denkair.booking;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// HA-2099: context-loads-Test faellt seit Kafka-Client-Dependency (2023) auf CI
// zufaellig durch — der Broker-Connect-Retry blockt den Test-Start. Logback
// versucht ausserdem /var/log/denkair/ zu schreiben, was im Test-Profil nicht da ist.
// Wurde auf Klassen-Ebene deaktiviert, bis HA-2099 + HA-B6 geloest sind.
@Disabled("Class-level skip: context load bricht durch logback file-appender + kafka broker lookup")
@SpringBootTest
class BookingApplicationTests {

    @Test
    void contextLoads() {
    }
}
