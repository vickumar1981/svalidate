package com.github.vickumar1981.svalidate

trait ValidationDsl[T] {
  private def collapseErrors(errors: Seq[T]): ValidationResult[T] =
    if (errors.isEmpty) Validation.success else ValidationFailure(errors)

  implicit class BoolsToValidation(cond: Boolean) {
    def orElse(errors: T*): ValidationResult[T] =
      if (!cond) collapseErrors(errors.toSeq) else Validation.success

    def orElse(validation: => ValidationResult[T]): ValidationResult[T] =
      if (!cond) collapseErrors(validation.errors) else Validation.success

    def andThen(validation: => ValidationResult[T]): ValidationResult[T] =
      if (cond) validation else Validation.success
  }

  implicit class OptionToValidatable[A](validatable: Option[A]) {
    def maybeValidate()(implicit validator: ValidatableResult[A, T]): ValidationResult[T] =
      validatable.map(validator.validate).getOrElse(Validation.success)

    def maybeValidate(validation: A => ValidationResult[T]): ValidationResult[T] =
      validatable.map(validation).getOrElse(Validation.success)

    def errorIfDefined(errors: T*): ValidationResult[T] =
      validatable.map(_ => collapseErrors(errors.toSeq)).getOrElse(Validation.success)

    def errorIfEmpty(errors: T*): ValidationResult[T] =
      validatable.map(_ => Validation.success).getOrElse(collapseErrors(errors.toSeq))
  }

  implicit class OptionToValidatableWith[A, B](validatable: Option[A])
                                              (implicit validator: ValidatableWithResult[A, B, T]) {
    def maybeValidateWith(b: B): ValidationResult[T] =
      validatable.map(v => validator.validateWith(v, b)).getOrElse(Validation.success)
  }
}
