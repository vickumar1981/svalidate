import com.github.vickumar1981.svalidate.{Validation, ValidationDsl, ValidationFailure, ValidationSuccess}
import org.scalatest.{FlatSpec, Matchers}

class TestValidationDsl extends FlatSpec with Matchers {
  import fixtures.TestData._
  import com.github.vickumar1981.svalidate.ValidationSyntax._

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

  "thenThrow" should "return a single validation result error" in {
    val firstNameRequired = validPerson.copy(firstName = "").validate()
    val lastNameRequired = validPerson.copy(lastName = "").validate()
    firstNameRequired should be(Validation.fail("First name is required"))
    checkFailure(firstNameRequired)
    lastNameRequired should be(Validation.fail("Last name is required"))
    checkFailure(lastNameRequired)
  }

  "thenCheck" should "perform validation conditionally" in {
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
}
