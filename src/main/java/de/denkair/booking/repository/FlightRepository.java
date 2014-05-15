package de.denkair.booking.repository;

import de.denkair.booking.domain.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface FlightRepository extends JpaRepository<Flight, Long> {

    @Query("SELECT f FROM Flight f " +
           "WHERE f.origin.iata = :origin " +
           "AND f.destination.iata = :destination " +
           "AND f.departure BETWEEN :from AND :to " +
           "AND f.aktiv = true " +
           "ORDER BY f.departure ASC")
    List<Flight> searchFlights(@Param("origin") String origin,
                               @Param("destination") String destination,
                               @Param("from") LocalDateTime from,
                               @Param("to") LocalDateTime to);

    List<Flight> findByAktivTrueOrderByDepartureAsc();

    // Von liza (2018) fuer das Call-Center-Tool. Bitte nicht verwenden in neuem Code.
    // @deprecated use searchFlights(...) statt dieser Methode.
    @Deprecated
    @Query("SELECT f FROM Flight f WHERE f.flightNumber = :flugnummer AND f.aktiv = true")
    List<Flight> findeNachFlugnummer(@Param("flugnummer") String flugnummer);
}
