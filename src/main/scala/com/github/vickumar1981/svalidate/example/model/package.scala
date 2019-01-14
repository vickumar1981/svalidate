package com.github.vickumar1981.svalidate.example

import java.util.Optional

import com.github.vickumar1981.svalidate.{Validatable, ValidatableWith, Validation}
import com.github.vickumar1981.svalidate.util.example.model.{Address => JavaAddress, Person => JavaPerson}

package object model {
  private def toJavaOption[T](option: Option[T]): Optional[T] =
    if (option.isDefined) Optional.of(option.get) else Optional.empty()

  case class Address(street: String,
                     city: String,
                     state: String,
                     zipCode: String) {
    def asJava: JavaAddress = new JavaAddress(street, city, state, zipCode)
  }

  case class Person(firstName: String,
                    lastName: String,
                    hasContactInfo: Boolean,
                    address: Option[Address] = None,
                    phone: Option[String] = None) {
    def asJava: JavaPerson = new JavaPerson(firstName, lastName, hasContactInfo,
      toJavaOption(address.map { _.asJava }), toJavaOption(phone))
  }

  implicit object AddressValidator extends Validatable[Address] {
    override def validate(value: Address): Validation = {
      (value.street.nonEmpty orElse "Street addr. is required") ++
        (value.city.nonEmpty orElse "City is required") ++
        (value.zipCode.matches("\\d{5}") orElse "Zip code must be 5 digits") ++
        (value.state.matches("[A-Z]{2}") orElse "State abbr must be 2 letters")
    }
  }

  implicit object PersonValidator extends Validatable[Person] {
    def validateContactInfo(value: Person): Validation = {
      (value.address errorIfEmpty "Address is required") ++
        (value.phone errorIfEmpty "Phone # is required") ++
        value.address.maybeValidate() ++
        value.phone.maybeValidate(_.matches("\\d{10}") orElse "Phone # must be 10 digits")
    }

    override def validate(value: Person): Validation = {
      (value.firstName.nonEmpty orElse "First name is required") ++
        (value.lastName.nonEmpty orElse "Last name is required") ++
        (value.hasContactInfo andThen validateContactInfo(value)) ++
        value.hasContactInfo.orElse {
          (value.address errorIfDefined "Address must be empty") ++
            (value.phone errorIfDefined "Phone # must be empty")
        }
    }
  }

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
}
