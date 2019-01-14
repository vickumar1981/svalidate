import com.github.vickumar1981.svalidate.{Validation, ValidationSuccess, ValidationFailure}
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

  "validateWith" should "accept a generic type parameter" in {
    val contactInfo = validContactInfo.validateWith(ContactSettings())
    checkSuccess(contactInfo)
    val invalidFacebookContacts = validContactInfo.copy(facebook = None).validateWith(ContactSettings())
    invalidFacebookContacts should be(Validation.fail("Facebook contacts are required"))
    checkFailure(invalidFacebookContacts)
    val invalidTwitterContacts = validContactInfo.copy(twitter = None).validateWith(ContactSettings())
    invalidTwitterContacts should be(Validation.fail("Twitter contacts are required"))
    checkFailure(invalidTwitterContacts)
  }
}
