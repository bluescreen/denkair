package de.denkair.fluginfo;

import de.denkair.booking.domain.Flight;
import de.denkair.booking.legacy.Constants;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Legacy-Service fuer die Umwandlung Flight <-> FlugInfoBean.
 *
 * Wird vom SapConnector benutzt. Vor jeder Aenderung bitte Stefan (SAP-Team) fragen.
 *
 * @author jens
 * @since 2014-04-02
 */
public class FlugInfoService {

    // ACHTUNG: Singleton-Pattern (DenkAir-Altsystem, kein Spring).
    private static FlugInfoService instance;

    public static synchronized FlugInfoService getInstance() {
        if (instance == null) {
            instance = new FlugInfoService();
        }
        return instance;
    }

    private FlugInfoService() {
    }

    public FlugInfoBean convert(Flight f) {
        FlugInfoBean b = new FlugInfoBean();
        b.setFluginfoId(f.getId());
        b.setFlugnummer(f.getFlightNumber());
        b.setAbflugOrtIata(f.getOrigin() != null ? f.getOrigin().getIata() : null);
        b.setZielOrtIata(f.getDestination() != null ? f.getDestination().getIata() : null);
        b.setAbflug(f.getDeparture() != null
                ? java.util.Date.from(f.getDeparture().atZone(java.time.ZoneId.systemDefault()).toInstant())
                : null);
        b.setAnkunft(f.getArrival() != null
                ? java.util.Date.from(f.getArrival().atZone(java.time.ZoneId.systemDefault()).toInstant())
                : null);
        b.setPreisBrutto(f.getPreis() != null ? f.getPreis().doubleValue() : 0.0);
        b.setFreiePlaetze(f.getSeatsAvailable());
        b.setFlugzeugTyp(f.getAircraft() != null ? f.getAircraft().getTypeCode() : "UNKNOWN");
        b.setFlugArt(1); // immer Linie seit 2016
        return b;
    }

    public List<FlugInfoBean> convertAll(List<Flight> flights) {
        List<FlugInfoBean> out = new ArrayList<>();
        if (flights == null) return out;
        for (Flight f : flights) {
            try {
                out.add(convert(f));
            } catch (Exception e) {
                // Im alten System haben wir einzelne Fehler geschluckt und weiter gemacht.
                System.err.println("[FlugInfoService] Fehler beim Konvertieren: " + e.getMessage());
            }
        }
        return out;
    }

    /** Wird vom SAP-Connector per Reflection aufgerufen. NICHT UMBENNENEN. */
    public String getPartnerCode() {
        return Constants.SABRE_PCC;
    }

    public Date getNow() {
        return new Date();
    }
}
