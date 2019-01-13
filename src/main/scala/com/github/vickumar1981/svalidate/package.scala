package com.github.vickumar1981

package object svalidate {

  sealed trait ValidationResult[+T] {
    def errors: Seq[T]
    def isSuccess: Boolean = errors.isEmpty
    def isFailure: Boolean = !isSuccess
  }

  case class ValidationSuccess[+T]() extends ValidationResult[T] {
    override val errors: Seq[T] = Seq.empty
  }

  case class ValidationFailure[+T](errorList: Seq[T]) extends ValidationResult[T] {
    override val errors: Seq[T] = errorList
  }

  implicit def validationToSeq[T](v: ValidationResult[T]): Seq[T] = v.errors

  implicit def seqToValidation[T](seq: Seq[T]): ValidationResult[T] =
    if (seq.isEmpty) ValidationSuccess() else ValidationFailure(seq)

  type Validation = ValidationResult[String]

  object Validation {
    def fail[T](validationErrors: T*): ValidationResult[T] = ValidationFailure[T](validationErrors.toSeq)
    def success[T]: ValidationResult[T] = ValidationSuccess[T]()
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
