package com.github.vickumar1981.svalidate.util.example.model;

import com.github.vickumar1981.svalidate.util.Validation;

import java.util.Optional;

import static com.github.vickumar1981.svalidate.util.ValidationSyntax.orElse;
import static com.github.vickumar1981.svalidate.util.ValidationSyntax.errorIfEmpty;
import static com.github.vickumar1981.svalidate.util.ValidationSyntax.errorIfDefined;
import static com.github.vickumar1981.svalidate.util.ValidationSyntax.maybeValidate;

public class Person implements Validatable<String> {
    private String firstName;
    private String lastName;
    private Boolean hasContactInfo;
    private Optional<Address> address;
    private Optional<String> phone;

    public Person(String firstName,
                  String lastName,
                  Boolean hasContactInfo,
                  Optional<Address> address,
                  Optional<String> phone) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.hasContactInfo = hasContactInfo;
        this.address = address;
        this.phone = phone;
    }

    private Validation<String> validateContactInfo() {
        return errorIfEmpty("Address is required").apply(address)
                .append(errorIfEmpty("Phone # is required").apply(phone))
                .append(maybeValidate(address))
                .append(maybeValidate(phone.map(p -> p.matches("\\d{10}")),
                        orElse("Phone # must be 10 digits")));
    }

    @Override
    public Validation<String> validate() {
        return orElse("First name is required").apply(
                firstName != null && !firstName.isEmpty())
                .append(orElse("Last name is required").apply(
                        lastName != null && !lastName.isEmpty()))
                .andThen(hasContactInfo, this::validateContactInfo)
                .orElse(hasContactInfo, () ->
                    errorIfDefined("Address must be empty").apply(address)
                        .append(errorIfDefined("Phone # must be empty").apply(phone))
                );
    }
}
