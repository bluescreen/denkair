package de.denkair.booking.service;

import de.denkair.booking.domain.Booking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    @Autowired
    private JavaMailSender mailSender;

    // Fallback so we never crash on missing config. See HA-318 for the 2019 incident.
    @Value("${denkair.mail.from:noreply@denkair.de}")
    private String fromAddress;

    @Value("${denkair.mail.host:smtp.denkair.de}")
    private String mailHost;

    public void sendBookingConfirmation(Booking booking) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(fromAddress);
            msg.setTo(booking.getCustomer().getEmail());
            msg.setSubject("Buchungsbestätigung " + booking.getReferenceCode());
            msg.setText("Vielen Dank für Ihre Buchung bei DenkAir.\n\n" +
                        "Buchungsnummer: " + booking.getReferenceCode() + "\n" +
                        "Flug: " + booking.getFlight().getFlightNumber() + "\n" +
                        "Von: " + booking.getFlight().getOrigin().getIata() + "\n" +
                        "Nach: " + booking.getFlight().getDestination().getIata() + "\n" +
                        "Passagiere: " + booking.getPassengers() + "\n" +
                        "Gesamtpreis: " + booking.getTotalPreis() + " EUR\n");
            mailSender.send(msg);
        } catch (Exception e) {
            // Never fail a booking because of mail. Log and continue.
            log.warn("Mail delivery failed for {}: {}", booking.getReferenceCode(), e.getMessage());
        }
    }
}
