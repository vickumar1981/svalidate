package com.github.vickumar1981.svalidate.util.example.model;

import com.github.vickumar1981.svalidate.util.Validation;

public interface Validatable<T> {
    Validation<T> validate();
}
