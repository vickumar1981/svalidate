package com.github.vickumar1981.svalidate.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Validation<T> {

    private List<T> exceptions;

    private Validation(List<T> errors) {
        this.exceptions = errors;
    }

    public List<T> errors() {
        return exceptions;
    }

    public Validation<T> append(Validation<T> other) {
        this.exceptions.addAll(other.errors());
        return this;
    }

    public Validation<T> andThen(Supplier<Validation<T>> other) {
        if (isSuccess()) {
            return other.get();
        }
        return this;
    }

    public boolean isSuccess() {
        return exceptions.isEmpty();
    }

    public boolean isFailure() {
        return !isSuccess();
    }

    public static <A> Validation fail(A ...value) {
        return new Validation<>(
                Arrays.stream(value).collect(Collectors.toList())
        );
    }

    public static <A> Validation success() {
        return new Validation<>(Collections.emptyList());
    }
}
