![Logo](logo.png)

## sValidate

[![Build Status](https://api.travis-ci.org/vickumar1981/svalidate.svg?branch=master)](https://travis-ci.org/vickumar1981/svalidate/builds) [![Coverage Status](https://coveralls.io/repos/github/vickumar1981/svalidate/badge.svg?branch=master)](https://coveralls.io/github/vickumar1981/svalidate?branch=master) [![Read the Docs](https://img.shields.io/readthedocs/pip.svg)](https://vickumar1981.github.io/stringdistance/api/com/github/vickumar1981/svalidate/index.html) [![Maven metadata URI](https://img.shields.io/maven-metadata/v/http/central.maven.org/maven2/com/github/vickumar1981/svalidate_2.12/maven-metadata.xml.svg)](https://mvnrepository.com/artifact/com.github.vickumar1981/stringdistance) [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE.md)

A simple and easy validation syntax for Scala and Java classes

For more detailed information, please refer to the [API Documentation](https://vickumar1981.github.io/stringdistance/api/com/github/vickumar1981/svalidate/index.html "API Documentation").

Requires: Scala 2.12, 2.11

---
### Contents

1.  [Add it to your project](https://github.com/vickumar1981/svalidate/#add-it-to-your-project-)
2.  [Basic Usage](https://github.com/vickumar1981/svalidate#basic-usage)
2.  [Nested Classes](https://github.com/vickumar1981/svalidate#nested-classes)
3.  [Validating With](https://github.com/vickumar1981/svalidate#validating-with)
5.  [Reporting an Issue](https://github.com/vickumar1981/svalidate#reporting-an-issue)
6.  [Contributing](https://github.com/vickumar1981/svalidate#contributing)
7.  [License](https://github.com/vickumar1981/svalidate#license)

---
### 1. Add it to your project ...

__Using sbt:__

In `build.sbt`:
```scala
libraryDependencies += "com.github.vickumar1981" %% "svalidate" % "1.0.0-SNAPSHOT"
```

__Using gradle:__

In `build.gradle`:
```groovy
dependencies {
    compile 'com.github.vickumar1981:stringdistance_2.12:1.0.0-SNAPSHOT'
}
```

__Using Maven:__

In `pom.xml`:
```xml
<dependency>
    <groupId>com.github.vickumar1981</groupId>
    <artifactId>svalidate_2.12</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

Note: For Java 7 or Scala 2.11, please use the `svalidate_2.11` artifact as a dependency instead.

---
### 2. Basic Usage

Let's create a simple class called `Address` that looks like:

```scala
package test.example

case class Address(street: String,
                   city: String,
                   state: String,
                   zipCode: String)
```

The rules for validating an `Address` are:
 -  street address is required
 -  city is required
 -  state abbr. must be exactly two capital letters
 -  zip code must be exactly 5 digits


Using `sValidate`'s validation syntax, we can add a new validator of the type `Validatable<Address>`

```scala
package test.example

object ModelValidations {
  implicit object AddressValidator extends Validatable[Address] {
      override def validate(value: Address): Validation = {
        (value.street.nonEmpty orElse "Street addr. is required") ++
          (value.city.nonEmpty orElse "City is required") ++
          (value.zipCode.matches("\\d{5}") orElse "Zip code must be 5 digits") ++
          (value.state.matches("[A-Z]{2}") orElse "State abbr must be 2 letters")
      }
    }
}
```

To extend validation to the `Address` class, we import our validator and the validation syntax.

```scala
import test.example.Address

object TestValidation {
  import test.example.ModelValidations._
  import com.github.vickumar1981.ValidationSyntax._
  
  def main(args: Array[String]): Unit = {
    val addr = Address("", "", "", "")
    val errors = addr.validate().errors
    println(errors)
    // ArrayBuffer(Street addr. is required, City is required, Zip code must be 5 digits, State abbr must be 2 letters)
  }
}
```

[See Scala Address validation example](https://github.com/vickumar1981/svalidate/blob/master/src/main/scala/com/github/vickumar1981/svalidate/example/model/package.scala#L28)
 
[See Java Address validation example](https://github.com/vickumar1981/svalidate/blob/master/src/main/java/com/github/vickumar1981/svalidate/util/example/model/Address.java#L7)

---
### 3. Nested Classes

Let's say we have a `Person` class which contains an `Address` instance, 
and whose validation depends upon the validation of the `address` member instance.

Additionally, a `Person` also has a `hasContact: Boolean` indicator

The class might look like:

```scala
case class Person(firstName: String,
                  lastName: String,
                  hasContactInfo: Boolean,
                  address: Option[Address] = None,
                  phone: Option[String] = None)

```

The rules for validating a `Person` are:
 -  first name is required
 -  last name is required
 -  phone number and address are both optional, and their
    validation depends upon the `hasContactInfo` indicator
 -  a phone number must be exactly 10 numbers
 -  if the `hasContactInfo` flag is true, then both `phone` and `address`
    should be validated
 -  if the `hasContactInfo` flag is false, then both `phone` and `address`
    should be empty

An example validator for `Address` might look like: 

```scala
package text.example

object ModelValidations {
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
}
```

[See Scala Person validation example](https://github.com/vickumar1981/svalidate/blob/master/src/main/scala/com/github/vickumar1981/svalidate/example/model/package.scala#L37)
 
[See Java Person validation example](https://github.com/vickumar1981/svalidate/blob/master/src/main/java/com/github/vickumar1981/svalidate/util/example/model/Person.java#L12)

---
### 4. Validating With

Sometimes, validation depends on an external value.  This is where we can use the `.validateWith[T](t: T)` syntax.

Let's say we have a `Contacts` class which contains an optional list of Facebook and Twitter emails.

Each user in our system also has a `ContactSettings` object, that determines the validation the user's `Contacts`

The two classes might look like:

```scala
case class Contacts(facebook: Option[List[String]] = None, twitter: Option[List[String]] = None)
case class ContactSettings(hasFacebookContacts: Option[Boolean] = Some(true),
                           hasTwitterContacts: Option[Boolean] = Some(true))
```

The rules for validating a user's `Contacts` are:
 -  If the `hasFacebookContacts` or `hasTwitterContacts` indicators are set to `true`,
    then the respective `facebook` or `twitter` list of emails for a user must be supplied
 -  If the `hasFacebookContacts` or `hasTwitterContacts` indicators are set to `false`,
    then the respective `facebook` or `twitter` list of emails for a user must be empty
 -  If the `hasFacebookContacts` or `hasTwitterContacts` indicators are empty,
    then the respective `facebook` or `twitter` list of emails can be empty or supplied

We will use a `ValidatableWith[Contacts, ContactSettings]` validator. 

An example implementation might look like:

```scala
package text.example

object ModelValidations {
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
```

[See Scala Contacts validation example](https://github.com/vickumar1981/svalidate/blob/master/src/main/scala/com/github/vickumar1981/svalidate/example/model/package.scala#L60)

Note:  There is currently no `.validateWith` syntax for Java

---
### 5. Reporting an Issue

Please report any issues or bugs to the [Github issues page](https://github.com/vickumar1981/svalidate/issues).

---
### 6. Contributing

Please view the [contributing guidelines](CONTRIBUTING.md) 

---
### 7. License

This project is licensed under the [Apache 2 License](LICENSE.md).