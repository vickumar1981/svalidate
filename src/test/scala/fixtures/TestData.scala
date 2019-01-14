package fixtures

import com.github.javafaker.Faker
import com.github.vickumar1981.svalidate.{Validatable, ValidatableWith, Validation}

import scala.util.Random

object TestData {
  private final val zipCodeLength = 5
  private final val phoneNumberLength = 10
  val faker = new Faker()

  case class BlankObject()

  implicit object BlankObjectValidator extends Validatable[BlankObject]
  implicit object BlankObjectValidatorWith extends ValidatableWith[BlankObject, String]

  def validAddress: Address = Address(
    faker.address.streetAddress,
    faker.address.city,
    faker.address.stateAbbr,
    faker.address.zipCode.take(zipCodeLength))

  def validPerson: Person = Person(
    faker.name.firstName,
    faker.name.lastName,
    true,
    Some(validAddress),
    Some(Random.alphanumeric.filter(_.isDigit).take(phoneNumberLength).mkString))

  def validContactInfo: ContactInfo = ContactInfo(
    faker.name.firstName,
    faker.name.lastName)

  case class ContactInfo(firstName: String, lastName: String)

  implicit object ContactInfoValidator extends ValidatableWith[ContactInfo, Boolean] {
    override def validateWith(value: ContactInfo, isRequired: Boolean): Validation =
      if (isRequired) {
        (value.firstName.nonEmpty orElse "First name is required") ++
          (value.lastName.nonEmpty orElse "Last name is required")
      } else { Validation.success }
  }

  case class Address(street: String,
                     city: String,
                     state: String,
                     zipCode: String)

  case class Person(firstName: String,
                    lastName: String,
                    hasContactInfo: Boolean,
                    address: Option[Address] = None,
                    phone: Option[String] = None)

  implicit object AddressValidator extends Validatable[Address] {
    override def validate(value: Address): Validation = {
      (value.zipCode.matches("\\d{5}") orElse "Zip code must be 5 digits") ++
        (value.state.matches("[A-Z]{2}") orElse "State abbr must be 2 letters")
    }
  }

  implicit object PersonValidator extends Validatable[Person] {
    def validateContactInfo(value: Person): Validation = {
      (value.address.isDefined orElse "Address is required") ++
        (value.phone.isDefined orElse "Phone # is required") ++
        value.address.maybeValidate() ++
        value.phone.maybeValidate(_.matches("\\d{10}") orElse "Phone # must be 10 digits")
    }

    override def validate(value: Person): Validation = {
      (value.firstName.nonEmpty orElse "First name is required") ++
        (value.lastName.nonEmpty orElse "Last name is required") ++
        (value.hasContactInfo andThen validateContactInfo(value))
    }
  }
}
