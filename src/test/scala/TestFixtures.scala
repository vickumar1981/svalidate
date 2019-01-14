import com.github.javafaker.Faker
import com.github.vickumar1981.svalidate.{Validatable, ValidatableWith}
import com.github.vickumar1981.svalidate.example.model.{Address, ContactSettings, Contacts, Person}

import scala.util.Random

object TestFixtures {
  private final val zipCodeLength = 5
  private final val phoneNumberLength = 10
  private final val maxContacts = 10
  private val faker = new Faker()

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

  def invalidContactAndSettings: (Contacts, ContactSettings) = {
    val generateTrueFalse = () => Random.nextInt(maxContacts) % 2
    val hasContacts = (x: Int) => if (x == 1) Some(true) else Some(false)
    val hasFacebook = hasContacts(generateTrueFalse())
    val hasTwitter = hasContacts(generateTrueFalse())
    val contacts = Contacts(
      if (hasFacebook.getOrElse(true)) None else Some(mkContactList),
      if (hasTwitter.getOrElse(true)) None else Some(mkContactList)
    )
    (contacts, ContactSettings(hasFacebook, hasTwitter))
  }
}
