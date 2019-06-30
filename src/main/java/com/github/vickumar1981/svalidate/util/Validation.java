package com.github.vickumar1981.svalidate.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;;

public class Validation<T> {

    private ArrayList<T> exceptions;

    private Validation(ArrayList<T> errors) {
        this.exceptions = errors;
    }

    public static <T> Validation<T> of(Validation<T> ...validations) {
        ArrayList<T> errs = new ArrayList<>();
        for (Validation<T> v: validations) {
            errs.addAll(v.errors());
        }
        return new Validation<>(errs);
    }

    public List<T> errors() {
        return exceptions;
    }

    public Validation<T> append(Validation<T> other) {
        this.exceptions.addAll(other.errors());
        return this;
    }

    public Validation<T> andThen(Boolean cond, Supplier<Validation<T>> other) {
        if (cond) {
            Validation <T> otherValidation = other.get();
            append(otherValidation);
        }
        return this;
    }

    public Validation<T> orElse(Boolean cond, Supplier<Validation<T>> other) {
        if (!cond) {
            Validation <T> otherValidation = other.get();
            append(otherValidation);
        }
        return this;
    }

    public boolean isSuccess() {
        return exceptions.isEmpty();
    }

    public boolean isFailure() {
        return !isSuccess();
    }

    public static <A> Validation<A> fail(A ...value) {
        ArrayList<A> errors = new ArrayList<>(Arrays.asList(value));
        return new Validation<>(errors);
    }

    public static <A> Validation<A> success() {
        return new Validation<>(new ArrayList<>());
    }
}
