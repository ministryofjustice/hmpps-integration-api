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
      firstName: String = "John",
      lastName: String = "Doe",
      dateOfBirth: LocalDate = LocalDate.of(1990, 1, 1),
      region: String = "East of England",
      office: String = "The Lighthouse",
      email: String = "John@example.org",
    ) = PersonDetails(firstName, lastName, dateOfBirth, region, office, email)

    fun validCourse(
      courseName: String = "Intro to litter picking",
      courseType: String = "Digital",
      provider: String = "Acme Learning",
      completionDateTime: LocalDate = LocalDate.of(2021, 1, 1),
      status: String = "Completed",
      totalTimeMinutes: Long = 150,
      attempts: Int? = 3,
      expectedMinutes: Long = 120,
    ) = CourseDetails(
      courseName,
      courseType,
      provider,
      completionDateTime,
      status,
      totalTimeMinutes,
      attempts,
      expectedMinutes,
    )

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

        it("expectedTimeMinutes must be positive") {
          val course = validCourse(expectedMinutes = 0L)
          val violations = validator.validate(validRequest(course = course))
          violations
            .map { it.propertyPath.toString() }
            .shouldContain("courseCompletion.course.expectedTimeMinutes")
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
            completionDateTime = LocalDate.of(2023, 12, 31),
            status = "Completed",
            totalTimeMinutes = 150,
            attempts = 5,
            expectedMinutes = 120,
          )

        val cc = CourseCompletion("EXT-XYZ", person, course)

        val msg =
          HmppsMessage(
            eventType = HmppsMessageEventType.EDUCATION_COURSE_COMPLETION_CREATED,
            messageAttributes =
              mapOf(
                "firstName" to person.firstName,
                "lastName" to person.lastName,
                "dateOfBirth" to person.dateOfBirth.toString(),
                "region" to person.region,
                "email" to person.email,
                "courseName" to course.courseName,
                "courseType" to course.courseType,
                "provider" to course.provider,
                "completionDate" to course.completionDate.toString(),
                "status" to course.status,
                "totalTimeMinutes" to course.totalTimeMinutes,
                "attempts" to course.attempts,
                "expectedTimeMinutes" to course.expectedTimeMinutes,
                "externalReference" to cc.externalReference,
              ),
          )

        val json = mapper.writeValueAsString(msg)
        val roundTrip = mapper.readValue(json, HmppsMessage::class.java)

        roundTrip.messageAttributes.shouldContainExactly(
          mapOf(
            "firstName" to "Jane",
            "lastName" to "Doe",
            "dateOfBirth" to "1992-05-14",
            "region" to "London",
            "email" to "jane@example.org",
            "courseName" to "Health & Safety",
            "courseType" to "Compliance",
            "provider" to "Gov Academy",
            "completionDate" to "2023-12-31",
            "status" to "Completed",
            "totalTimeMinutes" to 150,
            "attempts" to 5,
            "expectedTimeMinutes" to 120,
            "externalReference" to "EXT-XYZ",
          ),
        )
      }
    }
  })
