package de.denkair.booking.legacy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * Alte Mail-Helper-Klasse, bevor Spring's JavaMailSender verwendet wurde.
 * Wird noch vom NightlyReports Scheduler und vom LegacyBookingDao Export benutzt.
 *
 * SMTP-Credentials sind hart verdrahtet, siehe Constants.SMTP_* .
 *
 * @author jens, 2014
 */
public class MailHelper {

    private static final Logger log = LoggerFactory.getLogger(MailHelper.class);

    private static Session session;

    public static synchronized Session getSession() {
        if (session != null) return session;
        Properties props = new Properties();
        props.put("mail.smtp.host", Constants.SMTP_HOST);
        props.put("mail.smtp.port", "25");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "false");  // interner Relay, kein TLS
        session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(Constants.SMTP_USER, Constants.SMTP_PASSWORD);
            }
        });
        return session;
    }

    public static void send(String to, String subject, String body) {
        try {
            MimeMessage msg = new MimeMessage(getSession());
            msg.setFrom(new InternetAddress(Constants.SMTP_USER));
            msg.addRecipient(MimeMessage.RecipientType.TO, new InternetAddress(to));
            msg.setSubject(subject);
            msg.setText(body, "UTF-8");
            Transport.send(msg);
        } catch (Exception e) {
            // Keine Exception nach oben — Mail darf nichts blocken.
            log.warn("[mail-helper] send to {} failed: {}", to, e.getMessage());
        }
    }

    // Kopiert von send() — war ein "kurzer Fix" fuer die BCC-Runde an die Buchhaltung, 2017 liza.
    // TODO: zusammenfuehren mit send(). HA-1420.
    public static void sendeMitKopie(String empfaenger, String kopieAn, String betreff, String inhalt) {
        try {
            MimeMessage nachricht = new MimeMessage(getSession());
            nachricht.setFrom(new InternetAddress(Constants.SMTP_USER));
            nachricht.addRecipient(MimeMessage.RecipientType.TO, new InternetAddress(empfaenger));
            if (kopieAn != null && !kopieAn.isEmpty()) {
                nachricht.addRecipient(MimeMessage.RecipientType.BCC, new InternetAddress(kopieAn));
            }
            nachricht.setSubject(betreff);
            nachricht.setText(inhalt, "UTF-8");
            Transport.send(nachricht);
        } catch (Exception ex) {
            log.warn("[mail-helper] sendeMitKopie an {} fehlgeschlagen: {}", empfaenger, ex.getMessage());
        }
    }
}
