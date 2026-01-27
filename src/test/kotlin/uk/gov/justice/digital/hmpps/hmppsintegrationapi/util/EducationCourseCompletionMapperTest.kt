package uk.gov.justice.digital.hmpps.hmppsintegrationapi.util

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.shouldBe
import jakarta.validation.Validation
import jakarta.validation.Validator
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CourseCompletion
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CourseDetails
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.EducationCourseCompletionRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HmppsMessage
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HmppsMessageEventType
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PersonDetails
import java.time.LocalDate

class EducationCourseCompletionMapperTest :
  DescribeSpec({

    lateinit var validator: Validator
    lateinit var mapper: ObjectMapper

    beforeTest {
      validator = Validation.buildDefaultValidatorFactory().validator

      mapper =
        ObjectMapper()
          .registerModule(KotlinModule.Builder().build())
          .registerModule(JavaTimeModule())
          .setSerializationInclusion(JsonInclude.Include.NON_NULL)
    }

    fun validPerson(
      crn: String = "A123456",
      firstName: String = "John",
      lastName: String = "Doe",
      dateOfBirth: LocalDate = LocalDate.of(1990, 1, 1),
      region: String = "East of England",
      email: String = "John@example.org",
    ) = PersonDetails(crn, firstName, lastName, dateOfBirth, region, email)

    fun validCourse(
      courseName: String = "Intro to litter picking",
      courseType: String = "Digital",
      provider: String = "Acme Learning",
      status: String = "Completed",
      totalTime: String = "02:30",
      attempts: Int? = 3,
      expectedMinutes: Double = 10.5,
    ) = CourseDetails(courseName, courseType, provider, status, totalTime, attempts, expectedMinutes)

    fun validRequest(
      externalReference: String = "EXT-123",
      person: PersonDetails = validPerson(),
      course: CourseDetails = validCourse(),
    ) = EducationCourseCompletionRequest(
      CourseCompletion(externalReference, person, course),
    )

    describe("EducationCourseCompletionRequest validation") {

      it("valid object should produce no violations") {
        val violations = validator.validate(validRequest())
        violations.shouldBeEmpty()
      }

      describe("PersonDetails validation") {

        it("CRN must match [A-Z][0-9]{6}") {
          val person = validPerson(crn = "ZZ123") // invalid
          val violations = validator.validate(validRequest(person = person))
          violations.map { it.propertyPath.toString() }.shouldContain("courseCompletion.person.crn")
        }

        it("email must match the regex") {
          val person = validPerson(email = "nope")
          val violations = validator.validate(validRequest(person = person))
          violations.map { it.propertyPath.toString() }.shouldContain("courseCompletion.person.email")
        }

        it("firstName, lastName, region must not be blank") {
          val person = validPerson(firstName = " ", lastName = " ", region = " ")
          val violations = validator.validate(validRequest(person = person))
          violations.map { it.propertyPath.toString() }.shouldContainAll(
            "courseCompletion.person.firstName",
            "courseCompletion.person.lastName",
            "courseCompletion.person.region",
          )
        }
      }

      describe("CourseDetails validation") {

        it("courseName, courseType, provider, status must not be blank") {
          val course =
            validCourse(
              courseName = " ",
              courseType = " ",
              provider = " ",
              status = " ",
            )
          val violations = validator.validate(validRequest(course = course))
          violations.map { it.propertyPath.toString() }.shouldContainAll(
            "courseCompletion.course.courseName",
            "courseCompletion.course.courseType",
            "courseCompletion.course.provider",
            "courseCompletion.course.status",
          )
        }

        it("totalTime must match HH:MM") {
          val course = validCourse(totalTime = "5:3")
          val violations = validator.validate(validRequest(course = course))
          violations
            .map { it.propertyPath.toString() }
            .shouldContain("courseCompletion.course.totalTime")
        }

        it("expectedMinutes must be positive") {
          val course = validCourse(expectedMinutes = 0.0)
          val violations = validator.validate(validRequest(course = course))
          violations
            .map { it.propertyPath.toString() }
            .shouldContain("courseCompletion.course.expectedMinutes")
        }

        it("attempts may be null") {
          val course = validCourse(attempts = null)
          val violations = validator.validate(validRequest(course = course))
          violations.shouldBeEmpty()
        }
      }
    }

    describe("JSON mapping to HmppsMessage") {

      it("serializes correct messageAttributes") {
        val person =
          validPerson(
            crn = "B123456",
            firstName = "Jane",
            lastName = "Doe",
            dateOfBirth = LocalDate.of(1992, 5, 14),
            region = "London",
            email = "jane@example.org",
          )

        val course =
          validCourse(
            courseName = "Health & Safety",
            courseType = "Compliance",
            provider = "Gov Academy",
            status = "Completed",
            totalTime = "10:45",
            attempts = 5,
            expectedMinutes = 12.0,
          )

        val cc = CourseCompletion("EXT-XYZ", person, course)

        val msg =
          HmppsMessage(
            eventType = HmppsMessageEventType.EDUCATION_COURSE_COMPLETION_CREATED,
            messageAttributes =
              mapOf(
                "crn" to person.crn,
                "firstName" to person.firstName,
                "lastName" to person.lastName,
                "dateOfBirth" to person.dateOfBirth.toString(),
                "region" to person.region,
                "email" to person.email,
                "courseName" to course.courseName,
                "courseType" to course.courseType,
                "provider" to course.provider,
                "status" to course.status,
                "totalTime" to course.totalTime,
                "attempts" to course.attempts.toString(),
                "expectedMinutes" to course.expectedMinutes.toString(),
                "externalReference" to cc.externalReference,
              ),
          )

        val json = mapper.writeValueAsString(msg)
        val roundTrip = mapper.readValue(json, HmppsMessage::class.java)

        roundTrip.messageAttributes.shouldContainExactly(
          mapOf(
            "crn" to "B123456",
            "firstName" to "Jane",
            "lastName" to "Doe",
            "dateOfBirth" to "1992-05-14",
            "region" to "London",
            "email" to "jane@example.org",
            "courseName" to "Health & Safety",
            "courseType" to "Compliance",
            "provider" to "Gov Academy",
            "status" to "Completed",
            "totalTime" to "10:45",
            "attempts" to "5",
            "expectedMinutes" to "12.0",
            "externalReference" to "EXT-XYZ",
          ),
        )
      }

      it("null attempts becomes empty string") {
        val course = validCourse(attempts = null)
        val person = validPerson()
        val cc = CourseCompletion("EXT-NULL", person, course)

        val message =
          HmppsMessage(
            eventType = HmppsMessageEventType.EDUCATION_COURSE_COMPLETION_CREATED,
            messageAttributes =
              mapOf(
                "crn" to person.crn,
                "firstName" to person.firstName,
                "lastName" to person.lastName,
                "dateOfBirth" to person.dateOfBirth.toString(),
                "region" to person.region,
                "email" to person.email,
                "courseName" to course.courseName,
                "courseType" to course.courseType,
                "provider" to course.provider,
                "status" to course.status,
                "totalTime" to course.totalTime,
                "attempts" to course.attempts?.toString().orEmpty(),
                "expectedMinutes" to course.expectedMinutes.toString(),
                "externalReference" to cc.externalReference,
              ),
          )

        message.messageAttributes["attempts"]!!.shouldBe("")
      }
    }
  })
