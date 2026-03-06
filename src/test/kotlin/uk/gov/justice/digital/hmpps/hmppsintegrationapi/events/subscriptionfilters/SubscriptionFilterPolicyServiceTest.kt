package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.subscriptionfilters

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ConfigAuthorisation
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.dsl.objectMapper
import kotlin.test.assertEquals

class SubscriptionFilterPolicyServiceTest {
  val fileManager = Mockito.spy(FileManager::class.java)!!

  lateinit var service: SubscriptionFilterPolicyService
  lateinit var testConfig: Map<String, ConfigAuthorisation>

  @BeforeEach
  fun setup() {
    Mockito.reset(fileManager)
    doNothing().whenever(fileManager).write(any(), any())
    doNothing().whenever(fileManager).checkOrCreateDirectory(any())
    testConfig = AuthorisationConfigReader(fileManager).read("test")
    service = SubscriptionFilterPolicyService(FilterPolicyReader(fileManager), FilterPolicyWriter(fileManager), AuthorisationConfigReader(fileManager))
  }

  @Test
  fun `Should generate ALL subscription policy config files for the test environment when none exist`() {
    setUpConsumers(noPolicies = true)
    val numberOfConsumers = testConfig.size
    val filterPolicyJson = argumentCaptor<String>()
    service.generatePolicyFiles(listOf("test"))
    verify(fileManager, times(numberOfConsumers)).write(any(), filterPolicyJson.capture())
    val savedJson = filterPolicyJson.allValues
    assertEquals(numberOfConsumers, savedJson.size)
  }

  @Test
  fun `Should generate no subscription policy config files for the test environment when no changes`() {
    setUpConsumers(noPolicies = false)
    service.generatePolicyFiles(listOf("test"))
    verify(fileManager, times(0)).write(any(), any())
  }

  @Test
  fun `Should generate only where consumers config files require changes`() {
    setUpConsumers(noPolicies = false, changedConsumers = listOf("automated-test-client"))
    service.generatePolicyFiles(listOf("test"))
    verify(fileManager, times(1)).write(any(), any())
  }

  @Test
  fun `Should not generate any files only when there has been an exception during the read process`() {
    setUpConsumers(noPolicies = false, changedConsumers = listOf("automated-test-client"), throwsExceptionConsumers = listOf("no-include-or-roles"))
    assertThrows<RuntimeException> { service.generatePolicyFiles(listOf("test")) }
    verify(fileManager, times(0)).write(any(), any())
  }

  fun setUpConsumers(
    noPolicies: Boolean = true,
    changedConsumers: List<String>? = null,
    throwsExceptionConsumers: List<String>? = null,
  ) {
    val testGenerator =
      SubscriptionFilterPolicyService(
        FilterPolicyReader(fileManager),
        FilterPolicyWriter(fileManager),
        AuthorisationConfigReader(fileManager),
      )

    testGenerator
      .generatePoliciesForEnvironment("test")
      .forEach {
        val consumer = it.key
        val throwException = throwsExceptionConsumers?.contains(consumer) == true
        val policy = if (changedConsumers?.contains(consumer) == true) FilterPolicy(listOf("NEEDS_CHANGED")) else it.value.first
        val json = if (noPolicies) null else objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(policy)
        val path = "event-filter-policies/test/$consumer-subscription-filter.json"

        if (throwException) {
          whenever(fileManager.readFileContentsFromResourcesFolder(path)).doThrow(RuntimeException("An exception occurred during read"))
        } else {
          whenever(fileManager.readFileContentsFromResourcesFolder(path)).thenReturn(json)
        }
      }
  }
}
