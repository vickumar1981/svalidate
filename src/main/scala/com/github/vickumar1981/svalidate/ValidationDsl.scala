package com.github.vickumar1981.svalidate

trait ValidationDsl[T] {
  case class ConditionAndValidation(condition: Boolean,
                                    validation: () => ValidationResult[T])

  case class ConditionAndError(condition: Boolean, error: T)

  private def throwErrorsIfConditionsAreMet(conditions: ConditionAndError*): ValidationResult[T] =
    ValidationResult(
      conditions
        .filter(_.condition)
        .map(c => c.error)
        .toList)

  private def performValidationsWithConditions(conditions: ConditionAndValidation*):ValidationResult[T] = {
    ValidationResult(conditions.filter(_.condition).flatMap(_.validation().errors))
  }

  implicit class BoolsToValidation(val cond: Boolean) {
    def thenThrow(err: T): ValidationResult[T] =
      throwErrorsIfConditionsAreMet(ConditionAndError(cond, err))

    def thenCheck(validation: => ValidationResult[T]): ValidationResult[T] =
      performValidationsWithConditions(
        ConditionAndValidation(cond, () => validation))
  }

  implicit class OptionToValidatable[A](validatable: Option[A]) {
    def maybeValidate()(implicit validator: ValidatableResult[A, T]): ValidationResult[T] =
      validatable.map(v => validator.validate(v)).getOrElse(Validation.success)

    def maybeValidate(validation: A => ValidationResult[T]): ValidationResult[T] =
      validatable.map(validation).getOrElse(Validation.success)

    def maybeThrow(errMsg: T): ValidationResult[T] =
      validatable.map(_ => ValidationResult(errMsg :: Nil)).getOrElse(Validation.success)
  }

  implicit class OptionToValidatableWith[A, B](validatable: Option[A])
                                              (implicit validator: ValidatableWithResult[A, B, T]) {
    def maybeValidateWith(b: B): ValidationResult[T] =
      validatable.map(v => validator.validateWith(v, b)).getOrElse(Validation.success)
  }
}
