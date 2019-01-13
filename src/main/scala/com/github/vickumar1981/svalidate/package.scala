package com.github.vickumar1981

package object svalidate {

  case class ValidationResult[T](errors: Seq[T] = Seq.empty) {
    // scalastyle:off
    def ++(other: ValidationResult[T]): ValidationResult[T] = ValidationResult(errors ++ other.errors)

    // scalastyle:on

    def isSuccess: Boolean = errors.isEmpty
    def isFailure: Boolean = !isSuccess
  }

  type Validation = ValidationResult[String]

  object Validation {
    //def fail[T](validationError: T): ValidationResult[T] = ValidationResult(validationError :: Nil)
    def fail[T](validationErrors: T*): ValidationResult[T] = ValidationResult(validationErrors)
    def success[T]: ValidationResult[T] = ValidationResult()
  }

  trait ValidatableResult[-A, B] extends ValidationDsl[B] {
    def validate(value: A): ValidationResult[B] = Validation.success
  }

  trait ValidatableWithResult[-A, B, C] extends ValidationDsl[C] {
    def validateWith(value: A, b: B): ValidationResult[C] = Validation.success
  }

  trait Validatable[-A] extends ValidatableResult[A, String]
  trait ValidatableWith[-A, B] extends ValidatableWithResult[A, B, String]

  object ValidationSyntax {
    implicit class ValidatableOps[+A, B](value: A)(implicit v: ValidatableResult[A, B]) {
      def validate(): ValidationResult[B] = v.validate(value)
    }

    implicit class ValidatableWithOps[+A, B, C](value: A)(
      implicit v: ValidatableWith[A, B]) {
      def validateWith(b: B): Validation = v.validateWith(value, b)
    }
  }
}
