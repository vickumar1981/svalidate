package fixtures

import com.github.javafaker.Faker
import com.github.vickumar1981.svalidate.{Validatable, Validation}

object TestData {
  private final val zipCodeLength = 5
  val faker = new Faker()

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
    Some(faker.phoneNumber.cellPhone.filter(_.isDigit)))

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
      (!value.zipCode.matches("\\d{5}") thenThrow "Zip code must be 5 digits") ++
        (!value.state.matches("[A-Z]{2}") thenThrow "State abbr must be 2 letters")
    }
  }

  implicit object PersonValidator extends Validatable[Person] {
    def validateContactInfo(value: Person): Validation = {
      (value.address.isEmpty thenThrow "Address is required") ++
        (value.phone.isEmpty thenThrow "Phone # is required") ++
        value.address.maybeValidate() ++
        value.phone.maybeValidate(p => (!p.matches("\\d{10}")) thenThrow "Phone # must be 10 digits")
    }

    override def validate(value: Person): Validation = {
      (value.firstName.isEmpty thenThrow "First name is required") ++
        (value.lastName.isEmpty thenThrow "Last name is required") ++
        (value.hasContactInfo thenCheck validateContactInfo(value))
    }
  }
}
