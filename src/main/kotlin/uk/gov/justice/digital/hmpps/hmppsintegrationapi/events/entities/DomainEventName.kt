package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities

object DomainEventName {
  object ProbabtionCase {
    object Registration {
      const val ADDED = "probation-case.registration.added"
      const val UPDATED = "probation-case.registration.updated"
      const val DELETED = "probation-case.registration.deleted"
      const val DEREGISTERED = "probation-case.registration.deregistered"
    }

    object Engagement {
      const val CREATED = "probation-case.engagement.created"
    }

    object PrisonIdentifier {
      const val ADDED = "probation-case.prison-identifier.added"
    }

    object Address {
      const val CREATED = "probation-case.address.created"
      const val UPDATED = "probation-case.address.updated"
      const val DELETED = "probation-case.address.deleted"
    }

    object RiskScores {
      object OGRS {
        const val MANUAL_CALCULATION = "probation-case.risk-scores.ogrs.manual-calculation"
      }
    }

    object MappaInformation {
      const val CREATED = "probation-case.mappa-information.created"
      const val UPDATED = "probation-case.mappa-information.updated"
      const val DELETED = "probation-case.mappa-information.deleted"
    }

    object MappaExport {
      const val CREATED = "probation-case.mappa-export.created"
      const val TERMINATED = "probation-case.mappa-export.terminated"
    }

    object AssessmentSummary {
      const val CREATED = "probation-case.assessment-summary.created"
    }

    object SupervisionAppointment {
      const val CREATED = "probation-case.supervision-appointment.created"
    }

    object Supervision {
      const val CREATED = "probation-case.supervision.created"
    }

    object Cas3Booking {
      const val CREATED = "probation-case.cas3-booking.created"
    }

    object Exclusion {
      const val UPDATED = "probation-case.exclusion.updated"
    }

    object Restriction {
      const val UPDATED = "probation-case.restriction.updated"
    }
  }

  object Probation {
    object Staff {
      const val UPDATED = "probation.staff.updated"
    }
  }

  object PLP {
    object InductionSchedule {
      const val UPDATED = "plp.induction-schedule.updated"
    }

    object ReviewSchedule {
      const val UPDATED = "plp.review-schedule.updated"
    }
  }

  object SAN {
    object PlanCreationSchedule {
      const val UPDATED = "san.plan-creation-schedule.updated"
    }

    object ReviewSchedule {
      const val UPDATED = "san.review-schedule.updated"
    }
  }

  object CreateAndVaryALicence {
    object Licence {
      const val ACTIVATED = "create-and-vary-a-licence.licence.activated"
      const val INACTIVATED = "create-and-vary-a-licence.licence.inactivated"
    }
  }

  object Person {
    object Alert {
      const val CREATED = "person.alert.created"
      const val CHANGED = "person.alert.changed"
      const val UPDATED = "person.alert.updated"
      const val DELETED = "person.alert.deleted"
    }

    object Community {
      object Manager {
        const val ALLOCATED = "person.community.manager.allocated"
        const val TRANSFERRED = "person.community.manager.transferred"
      }
    }

    object CaseNote {
      const val CREATED = "person.case-note.created"
      const val UPDATED = "person.case-note.updated"
      const val DELETED = "person.case-note.deleted"
    }
  }

  object PrisonerOffenderSearch {
    object Prisoner {
      const val CREATED = "prisoner-offender-search.prisoner.created"
      const val RECEIVED = "prisoner-offender-search.prisoner.received"
      const val UPDATED = "prisoner-offender-search.prisoner.updated"
      const val RELEASED = "prisoner-offender-search.prisoner.released"
    }
  }

  object PrisonOffenderEvents {
    object Prisoner {
      const val RELEASED = "prison-offender-events.prisoner.released"
      const val RECEIVED = "prison-offender-events.prisoner.received"
      const val CONTACT_ADDED = "prison-offender-events.prisoner.contact-added"
      const val CONTACT_APPROVED = "prison-offender-events.prisoner.contact-approved"
      const val CONTACT_UNAPPROVED = "prison-offender-events.prisoner.contact-unapproved"
      const val CONTACT_REMOVED = "prison-offender-events.prisoner.contact-removed"
      const val MERGED = "prison-offender-events.prisoner.merged"

      object Restriction {
        const val CHANGED = "prison-offender-events.prisoner.restriction.changed"
      }

      object PersonRestriction {
        const val UPSERTED = "prison-offender-events.prisoner.person-restriction.upserted"
        const val DELETED = "prison-offender-events.prisoner.person-restriction.deleted"
      }

      object NonAssociationDetail {
        const val CHANGED = "prison-offender-events.prisoner.non-association-detail.changed"
      }
    }
  }

  object CalculateReleaseDates {
    object Prisoner {
      const val CHANGED = "calculate-release-dates.prisoner.changed"
    }
  }

  object RiskAssessment {
    object Scores {
      object OGRS {
        const val DETERMINED = "risk-assessment.scores.ogrs.determined"
      }

      object RSR {
        const val DETERMINED = "risk-assessment.scores.rsr.determined"
      }
    }
  }

  object Assessment {
    object Summary {
      const val PRODUCED = "assessment.summary.produced"
    }
  }

  object Incentives {
    object IEPReview {
      const val INSERTED = "incentives.iep-review.inserted"
      const val UPDATED = "incentives.iep-review.updated"
      const val DELETED = "incentives.iep-review.deleted"
    }
  }

  object PrisonVisit {
    const val BOOKED = "prison-visit.booked"
    const val CHANGED = "prison-visit.changed"
    const val CANCELLED = "prison-visit.cancelled"
  }

  object Adjudication {
    object Hearing {
      const val CREATED = "adjudication.hearing.created"
      const val COMPLETED = "adjudication.hearingCompleted.created"
      const val DELETED = "adjudication.hearing.deleted"
    }

    object Punishments {
      const val CREATED = "adjudication.punishments.created"
    }

    object Report {
      const val CREATED = "adjudication.report.created"
    }
  }

  object NonAssociations {
    const val CREATED = "non-associations.created"
    const val AMENDED = "non-associations.amended"
    const val CLOSED = "non-associations.closed"
    const val DELETED = "non-associations.deleted"
  }

  object LocationsInsidePrison {
    object Location {
      const val CREATED = "location.inside.prison.created"
      const val AMENDED = "location.inside.prison.amended"
      const val DELETED = "location.inside.prison.deleted"
      const val DEACTIVATED = "location.inside.prison.deactivated"
      const val REACTIVATED = "location.inside.prison.reactivated"
    }

    object SignedOpCapacity {
      const val AMENDED = "location.inside.prison.signed-op-cap.amended"
    }
  }
}
