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
  */
package object svalidate {
  /**
    * A generic interface that holds a validation result of type T, interoperable with Seq[T]
    * @tparam T the type of ValidationResult
    */
  sealed trait ValidationResult[+T] {
    /**
      * List of errors from the validation
      * @return the list of errors from the validation result
      */
    def errors: Seq[T]

    /**
      * Check if the validation succeeded
      * @return if the validation succeded
      */
    def isSuccess: Boolean = errors.isEmpty

    /**
      * Check if the validation failed
      * @return if the validation failed
      */
    def isFailure: Boolean = !isSuccess
  }

  /**
    * A case class that represents a validation success of type T.
    * Extends a [[ValidationResult]][T] and implements the errors function with an empty list
    * @tparam T the type of ValidationResult
    */
  case class ValidationSuccess[+T]() extends ValidationResult[T] {
    /**
      * Returns an empty list
      */
    override val errors: Seq[T] = Seq.empty
  }

  /**
    * A case class that represents a validation failure of type T
    * Extends a [[ValidationResult]][T] and implements errors with the list of errors passed in
    * @tparam T the type of [[ValidationResult]]
    */
  case class ValidationFailure[+T](errorList: Seq[T]) extends ValidationResult[T] {
    override val errors: Seq[T] = errorList
  }

  /**
    * Implicit conversion from [[ValidationResult]][T] to Seq[T].
    * Allows [[ValidationResult]][T] to be substituted for Seq[T]
    * @param v the input [[ValidationResult]][T]
    * @tparam T the type of [[ValidatableResult]]
    * @return a Seq[T] returned the errors of the input validation
    */
  implicit def validationToSeq[T](v: ValidationResult[T]): Seq[T] = v.errors

  /**
    * Implicit conversion from Seq[T] to [[ValidationResult]][T].
    * Allows Seq[T] to be substituted for [[ValidationResult]][T]
    * @param seq the input Seq
    * @tparam T the type of [[ValidationResult]]
    * @return a [[ValidationFailure]] containing errors from the input Seq
    */
  implicit def seqToValidation[T](seq: Seq[T]): ValidationResult[T] =
    if (seq.isEmpty) ValidationSuccess() else ValidationFailure(seq)

  /**
    * Validation is the same as [[ValidationResult]][String].
    * Allows the default return type to be a list of strings
    */
  type Validation = ValidationResult[String]

  /**
    * Companion object containing factory methods to create a [[ValidationSuccess]][T] or
    * a [[ValidationFailure]][T]
    *
    * {{{
    *   val validationSuccess = Validation.success
    *   val validationFailure = Validation.fail("The validation failed.")
    * }}}
    */
  object Validation {
    /**
      * Creates a validation failure from a list of T
      * @param validationErrors a list of type T
      * @tparam T the type of [[ValidationFailure]] to create
      * @return a [[ValidationFailure]][T] containing the list of errors
      */
    def fail[T](validationErrors: T*): ValidationResult[T] = ValidationFailure[T](validationErrors.toSeq)

    /**
      * Creates a validation success of type T
      * @tparam T the type of success to create
      * @return a [[ValidationSuccess]][T]
      */
    def success[T]: ValidationResult[T] = ValidationSuccess[T]()
  }

  /**
    * A generic type class that defines which class to validate and what type of validation to return
    * @tparam A the class to validate
    * @tparam B the type of [[ValidationResult]] to use with [[ValidationDsl]]
    */
  trait ValidatableResult[-A, B] extends ValidationDsl[B] {
    /**
      * Override this class to validate type A using a [[ValidationResult]][B]
      * @param value
      * @return
      */
    def validate(value: A): ValidationResult[B] = Validation.success
  }

  /**
    * A generic type class that defines which class to use validateWith and what type of validation to return
    * @tparam A the class to validate
    * @tparam B the class to pass into validateWith
    * @tparam C the type of [[ValidationResult]] to use with [[ValidationDsl]]
    */
  trait ValidatableWithResult[-A, B, C] extends ValidationDsl[C] {
    /**
      *
      * @param value
      * @param b
      * @return
      */
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
