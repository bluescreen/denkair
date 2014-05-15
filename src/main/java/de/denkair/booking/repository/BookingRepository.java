package de.denkair.booking.repository;

import de.denkair.booking.domain.Booking;
import de.denkair.booking.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByReferenceCode(String referenceCode);

    List<Booking> findByCustomerOrderByCreatedAtDesc(Customer customer);

    // Legacy naming, still used by the /customer/mine endpoint.
    List<Booking> findAllByCustomer(Customer customer);

    // Kept for the 2018 CSV export job. TODO confirm whether still called.
    List<Booking> findByCustomer(Customer customer);
}
