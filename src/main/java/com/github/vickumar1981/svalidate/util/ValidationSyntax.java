package com.github.vickumar1981.svalidate.util;

import java.util.Optional;
import java.util.function.Function;

public class ValidationSyntax {
    public static <T> Function<Boolean, Validation<T>> orElse(T ...errors) {
        return (cond) -> {
            if (!cond) {
                return Validation.fail(errors);
            } else {
                return Validation.success();
            }
        };
    }

    public static <A> Function<Optional<A>, Validation<A>> errorIfEmpty(A ...errors) {
        return (validatable) -> validatable.map(v -> Validation.success()).orElse(Validation.fail(errors));
    }

    public static <A> Function<Optional<A>, Validation<A>> errorIfDefined(A ...errors) {
        return (validatable) -> validatable.map(v -> Validation.fail(errors)).orElse(Validation.success());
    }
}
