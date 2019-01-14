package com.github.vickumar1981.svalidate.util;

import com.github.vickumar1981.svalidate.util.example.model.Validatable;

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

    public static <A> Function<Optional<?>, Validation<A>> errorIfEmpty(A ...errors) {
        return (validatable) -> validatable.map(v -> Validation.<A>success()).orElse(Validation.fail(errors));
    }

    public static <A> Function<Optional<?>, Validation<A>> errorIfDefined(A ...errors) {
        return (validatable) -> validatable.map(v -> Validation.fail(errors)).orElse(Validation.success());
    }

    public static <A> Validation<A> maybeValidate(Optional<? extends Validatable> condition) {
        return condition.map(c -> c.validate()).orElse(Validation.<A>success());
    }

    public static <A, B> Validation<A> maybeValidate(Optional<B> condition, Function<B, Validation<A>> check) {
        return condition.map(check).orElse(Validation.success());
    }
}
