import com.github.vickumar1981.svalidate.Validation
import org.scalatest.{FlatSpec, Matchers}

class TestValidationDsl extends FlatSpec with Matchers {
  import fixtures.TestData._
  import com.github.vickumar1981.svalidate.ValidationSyntax._

  private def checkFailure(v: Validation) = {
    v.isFailure should be(true)
  }

  private def checkSuccess(v: Validation) = {
    v should be(Validation.success)
    v.isSuccess should be(true)
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
    val emptyContactInfo = validPerson.copy(address = None, phone = None).validate()
    emptyContactInfo should be(
      Validation.fail("Address is required", "Phone # is required"))
    checkFailure(emptyContactInfo)

    val emptyContactInfoOkay = validPerson.copy(
      address = None,
      phone = None,
      hasContactInfo = false).validate()
    emptyContactInfoOkay should be(Validation.success)
    checkSuccess(emptyContactInfoOkay)
  }
}
