package com.github.vickumar1981

/** Provides classes for doing simple validations in Java and Scala.
  *
  * ==Overview==
  *  The validation wrapper is provided as a simple wrapper around a list
  *
  *  Results of a validation are stored in a [[com.github.vickumar1981.svalidate.ValidationResult]]
  *  A [[com.github.vickumar1981.svalidate.ValidationResult]] can be chained to make rules
  *  using the syntax provided from [[com.github.vickumar1981.svalidate.ValidationSyntax]].
  *
  *  A validator for a class can be defined by implementing a [[com.github.vickumar1981.svalidate.ValidatableResult]]
  *
  *  Analogous classes for Java can be found in the [[com.github.vickumar1981.svalidate.util]] package
  *
  *
  * | Class | Description |
  * | :---  | :--- |
  * | [[com.github.vickumar1981.svalidate.ValidationResult]] | Holds the return value from a validation |
  * | [[com.github.vickumar1981.svalidate.ValidationSyntax]] | Provides DSL syntax for validations |
  * | [[com.github.vickumar1981.svalidate.ValidatableResult]] | Interface to implement for defining a validation |
  *
  *
  */
package object svalidate {

  /**
    * A generic interface that holds a validation result of type T, interoperable with Seq[T]
    */
  sealed trait ValidationResult[+T] {
    /**
      * the list of errors from the validation
      * @return the list of errors from the validation result
      */
    def errors: Seq[T]

    /**
      * check if the validation succeeded
      * @return if the validation succeded
      */
    def isSuccess: Boolean = errors.isEmpty

    /**
      * check if the validation failed
      * @return if the validation failed
      */
    def isFailure: Boolean = !isSuccess
  }

  /**
    * A case class that represents a validation success of type T.
    * Extends a [[ValidationResult]] and implements it with an empty list
    */
  case class ValidationSuccess[+T]() extends ValidationResult[T] {
    /**
      * Returns an empty list
      */
    override val errors: Seq[T] = Seq.empty
  }

  /**
    * A case class that represents a validation failure of type T
    * Extends a [[ValidationResult]] and implements it with the list of errors passed in
    */
  case class ValidationFailure[+T](errorList: Seq[T]) extends ValidationResult[T] {
    override val errors: Seq[T] = errorList
  }

  /**
    * Implicit conversion from [[ValidationResult]] to Seq[T]
    */
  implicit def validationToSeq[T](v: ValidationResult[T]): Seq[T] = v.errors

  /**
    * Implicit conversion from Seq[T] to [[ValidationResult]]
    */
  implicit def seqToValidation[T](seq: Seq[T]): ValidationResult[T] =
    if (seq.isEmpty) ValidationSuccess() else ValidationFailure(seq)

  /**
    * Validation is the same as [[ValidationResult]][String].
    * Allows the default return type to be a list of strings
    */
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
