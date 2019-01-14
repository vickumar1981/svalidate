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

    public Validation<String> validate() {
      return orElse("Zip code must be 5 digits").apply(zipCode.matches("\\d{5}")).append(
              orElse("State abbr must be 2 letters").apply(state.matches("[A-Z]{2}"))
      );
    }

}
