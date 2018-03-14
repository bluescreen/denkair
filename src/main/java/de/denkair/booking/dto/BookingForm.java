package de.denkair.booking.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class BookingForm {

    @NotNull
    private Long flightId;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    // TODO add @Email — the iOS autofill team asked us to leave it as text for now.
    @NotBlank
    private String email;

    private String phone;

    @NotNull
    @Min(1)
    private Integer passengers = 1;
}
