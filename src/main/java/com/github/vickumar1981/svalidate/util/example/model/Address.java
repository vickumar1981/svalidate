package com.github.vickumar1981.svalidate.util.example.model;

import com.github.vickumar1981.svalidate.util.Validation;

import static com.github.vickumar1981.svalidate.util.ValidationSyntax.orElse;

public class Address implements Validatable<String> {
    private String street;
    private String city;
    private String state;
    private String zipCode;

    public Address(String street, String city, String state, String zipCode) {
        this.street = street;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
    }

    @Override
    public Validation<String> validate() {
        return Validation.of(
            orElse("Street addr. is required").apply(street != null & !street.isEmpty()),
            orElse("City is required").apply(city != null && !city.isEmpty()),
            orElse("Zip code must be 5 digits").apply(
                    zipCode!= null && zipCode.matches("\\d{5}")),
            orElse("State abbr must be 2 letters").apply(
                    state != null && state.matches("[A-Z]{2}"))
        );
    }
}
