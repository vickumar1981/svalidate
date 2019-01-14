package fixtures

import com.github.javafaker.Faker
import com.github.vickumar1981.svalidate.{Validatable, ValidatableWith, Validation}

import scala.util.Random

object TestData {
  private final val zipCodeLength = 5
  private final val phoneNumberLength = 10
  private final val maxContacts = 10
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

  def mkContactList: List[String] = (1 to Random.nextInt(maxContacts))
    .toList.map { _ => faker.internet().emailAddress() }

  def validContactInfo: Contacts = Contacts(
    Some(mkContactList),
    Some(mkContactList))

  case class Contacts(facebook: Option[List[String]] = None, twitter: Option[List[String]] = None)
  case class ContactSettings(hasFacebookContacts: Option[Boolean] = Some(true),
                             hasTwitterContacts: Option[Boolean] = Some(true))

  implicit object ContactInfoValidator extends ValidatableWith[Contacts, ContactSettings] {
    override def validateWith(value: Contacts, contactSettings: ContactSettings): Validation = {
      contactSettings.hasFacebookContacts.maybeValidate {
        contacts =>
          (contacts andThen { value.facebook errorIfEmpty "Facebook contacts are required" }) ++
          (contacts orElse { value.facebook errorIfDefined "Facebook contacts must be empty"})
      } ++
      contactSettings.hasTwitterContacts.maybeValidate {
        contacts =>
          (contacts andThen { value.twitter errorIfEmpty "Twitter contacts are required" }) ++
          (contacts orElse { value.twitter errorIfDefined "Twitter contacts must be empty" })
      }
    }
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
      (value.address errorIfEmpty "Address is required") ++
        (value.phone errorIfEmpty "Phone # is required") ++
        value.address.maybeValidate() ++
        value.phone.maybeValidate(_.matches("\\d{10}") orElse "Phone # must be 10 digits") ++
        value.hasContactInfo.orElse {
          (value.address errorIfDefined "Address must be empty") ++
            (value.phone errorIfDefined "Phone # must be empty")
        }
    }

    override def validate(value: Person): Validation = {
      (value.firstName.nonEmpty orElse "First name is required") ++
        (value.lastName.nonEmpty orElse "Last name is required") ++
        (value.hasContactInfo andThen validateContactInfo(value))
    }
  }
}
