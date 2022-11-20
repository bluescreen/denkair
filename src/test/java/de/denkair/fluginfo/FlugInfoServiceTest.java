package de.denkair.fluginfo;

import junit.framework.TestCase;

/**
 * Legacy-Test fuer FlugInfoService.
 * JUnit 3 Style, extends TestCase. Laeuft ueber den Vintage-Runner.
 *
 * Das Package hiess mal de.condor.fluginfo (bis zur Umfirmierung); nach dem Rename
 * 2015 ist es de.denkair.fluginfo — der Test wurde mit umgezogen.
 *
 * Migration auf JUnit 5 ist offen (HA-2210), geht aber nur zusammen mit dem
 * SAP-Connector-Reflection-Refactor.
 *
 * @author jens, 2014-04
 */
public class FlugInfoServiceTest extends TestCase {

    public void testSingletonIsSameInstance() {
        FlugInfoService a = FlugInfoService.getInstance();
        FlugInfoService b = FlugInfoService.getInstance();
        assertNotNull(a);
        assertSame(a, b);
    }

    public void testPartnerCodeNotEmpty() {
        String code = FlugInfoService.getInstance().getPartnerCode();
        assertNotNull(code);
        assertTrue(code.length() > 0);
    }

    public void testGetNowReturnsRecent() {
        long before = System.currentTimeMillis();
        java.util.Date now = FlugInfoService.getInstance().getNow();
        long after = System.currentTimeMillis();
        assertTrue(now.getTime() >= before);
        assertTrue(now.getTime() <= after + 1000);
    }

    // Tests fuer convert(Flight) fehlen — wuerden einen geladenen Flight-
    // EntityManager brauchen, und JUnit 3 + @SpringBootTest ist keine Freundschaft.
}
