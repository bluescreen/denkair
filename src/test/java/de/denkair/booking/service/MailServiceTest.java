package de.denkair.booking.service;

import de.denkair.booking.domain.Airport;
import de.denkair.booking.domain.Booking;
import de.denkair.booking.domain.Customer;
import de.denkair.booking.domain.Flight;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MailServiceTest {

    @Mock private JavaMailSender mailSender;
    @InjectMocks private MailService mail;

    private Booking booking;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(mail, "fromAddress", "no-reply@denkair.de");
        ReflectionTestUtils.setField(mail, "mailHost", "smtp.denkair.de");

        Airport ham = new Airport(); ham.setIata("HAM");
        Airport pmi = new Airport(); pmi.setIata("PMI");
        Flight f = new Flight();
        f.setFlightNumber("HA4021");
        f.setOrigin(ham); f.setDestination(pmi);

        Customer c = new Customer();
        c.setEmail("k@example.de");

        booking = new Booking();
        booking.setReferenceCode("HA-ABC12");
        booking.setFlight(f);
        booking.setCustomer(c);
        booking.setPassengers(2);
        booking.setTotalPreis(new BigDecimal("238.00"));
    }

    @Test
    void sendsMessageWithExpectedContents() {
        mail.sendBookingConfirmation(booking);

        ArgumentCaptor<SimpleMailMessage> cap = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(cap.capture());
        SimpleMailMessage msg = cap.getValue();
        assertEquals("no-reply@denkair.de", msg.getFrom());
        assertArrayEquals(new String[]{"k@example.de"}, msg.getTo());
        assertTrue(msg.getSubject().contains("HA-ABC12"));
        assertNotNull(msg.getText());
        assertTrue(msg.getText().contains("HA4021"));
        assertTrue(msg.getText().contains("HAM"));
        assertTrue(msg.getText().contains("PMI"));
        assertTrue(msg.getText().contains("238.00"));
    }

    @Test
    void senderExceptionIsSwallowed() {
        doThrow(new RuntimeException("smtp down")).when(mailSender).send(any(SimpleMailMessage.class));
        mail.sendBookingConfirmation(booking);
    }
}
