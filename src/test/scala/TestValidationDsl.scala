
import com.github.vickumar1981.svalidate.{Validation, ValidationDsl, ValidationFailure, ValidationSuccess}
import org.scalatest.{FlatSpec, Matchers}

class TestValidationDsl extends FlatSpec with Matchers {
  import TestFixtures._
  import com.github.vickumar1981.svalidate.example.model._
  import com.github.vickumar1981.svalidate.ValidationSyntax._
  import scala.collection.JavaConverters._

  private def checkFailure(v: Validation) = {
    v.isFailure should be(true)
    v match {
      case ValidationSuccess() => fail("ValidationSuccess was not equal to ValidationFailure")
      case ValidationFailure(_) => succeed
    }
  }

  private def checkSuccess(v: Validation) = {
    v should be(Validation.success)
    v.isSuccess should be(true)
    v match {
      case ValidationSuccess() => succeed
      case ValidationFailure(_) => fail("ValidationFailure was not equal to ValidationSuccess")
    }
  }

  private val validationDsl = new ValidationDsl[String] {
    def testErrorIfEmpty(value: Option[String], errs: String): Validation = value errorIfEmpty errs
    def testErrorIfDefined(value: Option[String], errs: String): Validation = value errorIfDefined errs
    def testMaybeValidateWith(value: Option[Contacts],
                              settings: ContactSettings): Validation = value.maybeValidateWith(settings)
  }

  "validate" should "return a ValidationSuccess if no implementation is defined" in {
    val result = BlankObject().validate()
    checkSuccess(result)
  }

  "validateWith" should "return a ValidationSuccess if no implementation is defined" in {
    val result = BlankObject().validateWith("")
    checkSuccess(result)
  }

  "validate" should "return a success if no validation errors occur" in {
    val result = validPerson.validate()
    checkSuccess(result)
  }

  "orElse" should "return a single validation result error" in {
    val firstNameRequired = validPerson.copy(firstName = "").validate()
    val lastNameRequired = validPerson.copy(lastName = "").validate()
    firstNameRequired should be(Validation.fail("First name is required"))
    checkFailure(firstNameRequired)
    lastNameRequired should be(Validation.fail("Last name is required"))
    checkFailure(lastNameRequired)
  }

  "andThen" should "perform validation conditionally" in {
    val emptyPersonInfo = validPerson.copy(address = None, phone = None).validate()
    emptyPersonInfo should be(
      Validation.fail("Address is required", "Phone # is required"))
    checkFailure(emptyPersonInfo)

    val emptyPersonInfoOkay = validPerson.copy(
      address = None,
      phone = None,
      hasContactInfo = false).validate()
    emptyPersonInfoOkay should be(Validation.success)
    checkSuccess(emptyPersonInfoOkay)
  }

  "validateWith" should "accept a generic type parameter when implicit is in scope" in {
    val contactInfo = validContactInfo.validateWith(ContactSettings())
    checkSuccess(contactInfo)
    val invalidFacebookContacts = validContactInfo.copy(facebook = None).validateWith(ContactSettings())
    invalidFacebookContacts should be(Validation.fail("Facebook contacts are required"))
    checkFailure(invalidFacebookContacts)
    val invalidTwitterContacts = validContactInfo.copy(twitter = None).validateWith(ContactSettings())
    invalidTwitterContacts should be(Validation.fail("Twitter contacts are required"))
    checkFailure(invalidTwitterContacts)
  }

  "maybeValidate" should "return complementary values for when option is present and when option is empty" in {
    val invalidFacebookContacts = validContactInfo.copy(facebook = None).validateWith(ContactSettings())
    invalidFacebookContacts should be(Validation.fail("Facebook contacts are required"))
    checkFailure(invalidFacebookContacts)

    val invalidFacebookContacts2 =
      validContactInfo.copy(facebook = Some(mkContactList))
        .validateWith(ContactSettings(hasFacebookContacts = Some(false)))
    invalidFacebookContacts2 should be(Validation.fail("Facebook contacts must be empty"))
    checkFailure(invalidFacebookContacts2)

    val invalidTwitterContacts = validContactInfo.copy(twitter = None).validateWith(ContactSettings())
    invalidTwitterContacts should be(Validation.fail("Twitter contacts are required"))
    checkFailure(invalidTwitterContacts)

    val invalidTwitterContacts2 =
      validContactInfo.copy(twitter = Some(mkContactList))
        .validateWith(ContactSettings(hasTwitterContacts = Some(false)))
    invalidTwitterContacts2 should be(Validation.fail("Twitter contacts must be empty"))
    checkFailure(invalidTwitterContacts2)
  }

  "maybeValidateWith" should "return a success for an empty option" in {
    val result = validationDsl.testMaybeValidateWith(None, ContactSettings())
    checkSuccess(result)
  }

  "maybeValidateWith" should "return validateWith for an option with a validatable value" in {
    val (invalidContact, invalidSettings) = invalidContactAndSettings
    val result1 = invalidContact.validateWith(invalidSettings)
    val result2 = validationDsl.testMaybeValidateWith(Some(invalidContact), invalidSettings)
    checkFailure(result1)
    checkFailure(result2)
    result1 should be(result2)
    result1.errors should be(result2.errors)
  }

  "errorIfEmpty" should "return a ValidationSuccess when the value is defined" in {
    val result = validationDsl.testErrorIfEmpty(Some(""), "value is required")
    checkSuccess(result)
  }

  "errorIfEmpty" should "return a ValidationFailure when the value is empty" in {
    val result = validationDsl.testErrorIfEmpty(None, "value is required")
    result should be(Validation.fail("value is required"))
    checkFailure(result)
  }

  "errorIfDefined" should "return a ValidationSuccess when the value is empty" in {
    val result = validationDsl.testErrorIfDefined(None, "value should be empty")
    checkSuccess(result)
  }

  "errorIfDefined" should "return a ValidationFailure when the value is defined" in {
    val result = validationDsl.testErrorIfDefined(Some(""), "value should be empty")
    result should be(Validation.fail("value should be empty"))
    checkFailure(result)
  }

  "java static functions" should "return the same validation results as the Scala DSL" in {
    val emptyAddress = Address("", "", "", "")
    val emptyAddressErrors = emptyAddress.asJava.validate()
    emptyAddressErrors.isFailure should be(true)
    emptyAddressErrors.isSuccess should be(false)
    emptyAddress.validate().errors.asJava should be(emptyAddressErrors.errors())

    val validPersonInfo = validPerson.asJava.validate()
    validPersonInfo.isFailure should be(false)
    validPersonInfo.isSuccess should be(true)

    val emptyPersonInfo = validPerson.copy(address = None, phone = None)
    val emptyPersonInfoErrors = emptyPersonInfo.asJava.validate()
    emptyPersonInfoErrors.isFailure should be(true)
    emptyPersonInfoErrors.isSuccess should be(false)
    emptyPersonInfo.validate().errors.asJava should be(emptyPersonInfoErrors.errors())

    val emptyPersonInfoOkay = validPerson.copy(address = None,
      phone = None,
      hasContactInfo = false)
    val emptyPersonInfoOkayErrors = emptyPersonInfoOkay.asJava.validate()
    emptyPersonInfoOkayErrors.isSuccess should be(true)
    emptyPersonInfoOkayErrors.isFailure should be(false)
  }
}
