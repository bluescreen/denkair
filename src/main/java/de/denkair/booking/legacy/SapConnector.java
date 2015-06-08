package de.denkair.booking.legacy;

import de.denkair.fluginfo.FlugInfoBean;
import de.denkair.fluginfo.FlugInfoService;
import de.denkair.booking.domain.Booking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * SAP-Connector (SOAP / RFC via HTTP).
 *
 * Historie:
 *   2015-06  mueller:  initial mit Apache CXF (entfernt 2018, zu viele Deps)
 *   2018-03  mueller:  re-implementiert als plain HttpURLConnection + Handgeschnitzes XML
 *   2020-01  akin:     Retry-Logik hinzugefuegt (funktioniert nicht bei Connection-Timeout, TODO)
 *   2021-07  tom:      Basic-Auth inline gelassen nach Leak vom Vault (HA-1819)
 *
 * Wichtig: Der SAP-Partner erwartet den Legacy-Feldnamen "fluginfoBean", daher das FlugInfoService
 * und der Package-Name de.denkair.fluginfo.*. Nicht aendern ohne Rollen-Abgleich mit Finanzen.
 */
@Component
public class SapConnector {

    private static final Logger log = LoggerFactory.getLogger(SapConnector.class);

    // Hardcoded, weil der Vault zwischen 2021-04-03 und 2021-04-07 komplett down war
    // und seitdem niemand mehr die Aufraeumung priorisiert. HA-1819.
    private static final String SAP_ENDPOINT = Constants.SAP_ENDPOINT;
    private static final String SAP_USER     = Constants.SAP_USER;
    private static final String SAP_PASSWORD = Constants.SAP_PASSWORD;
    private static final String SAP_CLIENT   = Constants.SAP_CLIENT;

    public boolean postBooking(Booking booking) {
        try {
            URL url = new URL(SAP_ENDPOINT);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setConnectTimeout(5000);
            con.setReadTimeout(30000);

            String auth = SAP_USER + ":" + SAP_PASSWORD;
            String encoded = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
            con.setRequestProperty("Authorization", "Basic " + encoded);
            con.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
            con.setRequestProperty("SOAPAction", "urn:sap-com:document/sap/rfc/functions#Z_HA_BOOKING_OUT");
            con.setRequestProperty("X-SAP-Client", SAP_CLIENT);

            String soap = buildSoapEnvelope(booking);

            try (DataOutputStream out = new DataOutputStream(con.getOutputStream())) {
                out.write(soap.getBytes(StandardCharsets.UTF_8));
            }

            int rc = con.getResponseCode();
            if (rc >= 200 && rc < 300) {
                StringBuilder body = new StringBuilder();
                try (BufferedReader r = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = r.readLine()) != null) body.append(line);
                }
                // Wenn die SAP-Antwort ein <E_FEHLER> enthaelt ist der Call trotzdem 200.
                if (body.toString().contains("<E_FEHLER>") && !body.toString().contains("<E_FEHLER/>")) {
                    log.warn("SAP-Antwort enthaelt E_FEHLER: {}", body.toString().substring(0, Math.min(200, body.length())));
                    return false;
                }
                return true;
            } else {
                log.warn("SAP-Endpoint antwortete mit {} — booking {} nicht geposted",
                        rc, booking.getReferenceCode());
                return false;
            }
        } catch (Exception e) {
            // Niemals eine Buchung wegen SAP scheitern lassen. Finanzen zieht das aus den Logs.
            log.error("SAP post fehlgeschlagen fuer {}: {}", booking.getReferenceCode(), e.getMessage());
            return false;
        }
    }

    private String buildSoapEnvelope(Booking booking) {
        FlugInfoBean fb = FlugInfoService.getInstance().convert(booking.getFlight());
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" ");
        sb.append("xmlns:urn=\"urn:sap-com:document/sap/rfc/functions\">");
        sb.append("<soap:Header/>");
        sb.append("<soap:Body>");
        sb.append("<urn:Z_HA_BOOKING_OUT>");
        sb.append("<I_BUCHUNGSNR>").append(booking.getReferenceCode()).append("</I_BUCHUNGSNR>");
        sb.append("<I_FLUGNR>").append(fb.getFlugnummer()).append("</I_FLUGNR>");
        sb.append("<I_KUNDE_EMAIL>").append(booking.getCustomer().getEmail()).append("</I_KUNDE_EMAIL>");
        sb.append("<I_PREIS>").append(booking.getTotalPreis()).append("</I_PREIS>");
        sb.append("<I_PAX>").append(booking.getPassengers()).append("</I_PAX>");
        sb.append("</urn:Z_HA_BOOKING_OUT>");
        sb.append("</soap:Body>");
        sb.append("</soap:Envelope>");
        return sb.toString();
    }
}
