package de.denkair.booking.dto;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BookingFormTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll static void init() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    @AfterAll static void close() { factory.close(); }

    private BookingForm valid() {
        BookingForm f = new BookingForm();
        f.setFlightId(1L); f.setFirstName("Max"); f.setLastName("M"); f.setEmail("a@b.de"); f.setPassengers(2);
        return f;
    }

    @Test
    void validFormHasNoViolations() {
        assertTrue(validator.validate(valid()).isEmpty());
    }

    @Test
    void missingFlightIdFails() {
        BookingForm f = valid(); f.setFlightId(null);
        Set<ConstraintViolation<BookingForm>> v = validator.validate(f);
        assertEquals(1, v.size());
    }

    @Test
    void blankFirstNameFails() {
        BookingForm f = valid(); f.setFirstName("");
        assertEquals(1, validator.validate(f).size());
    }

    @Test
    void blankEmailFails() {
        BookingForm f = valid(); f.setEmail("");
        assertEquals(1, validator.validate(f).size());
    }

    @Test
    void zeroPassengersFails() {
        BookingForm f = valid(); f.setPassengers(0);
        assertEquals(1, validator.validate(f).size());
    }

    @Test
    void lombokGettersAndSettersWork() {
        BookingForm f = valid();
        assertEquals("Max", f.getFirstName());
        assertEquals(2, f.getPassengers());
        f.setPhone("+49");
        assertEquals("+49", f.getPhone());
    }
}
