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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.AuthorisationConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.dsl.objectMapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.AuthorisationConfigReader
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.FileManager
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SubscriptionFilterPolicyManagerTest {
  val fileManager = Mockito.spy(FileManager::class.java)!!

  lateinit var service: SubscriptionFilterPolicyManager
  lateinit var testConfig: AuthorisationConfig

  @BeforeEach
  fun setup() {
    Mockito.reset(fileManager)
    doNothing().whenever(fileManager).write(any(), any())
    doNothing().whenever(fileManager).checkOrCreateDirectory(any())
    testConfig = AuthorisationConfigReader(fileManager).read("test")
    service = SubscriptionFilterPolicyManager(fileManager)
  }

  @Test
  fun `Should generate ALL subscription policy config files for the test environment when none exist`() {
    setUpConsumers(noPolicies = true)
    val numberOfConsumers = testConfig.consumersWithQueue().size
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

  @Test
  fun `the isDifferentTo method should report false if there is only an order change in the filter policy lists`() {
    val thisPolicy = FilterPolicy(listOf("EVENT_1", "EVENT_2", "EVENT_3"), listOf("PRISON_1", "PRISON_2"))
    val newPolicy = FilterPolicy(listOf("EVENT_3", "EVENT_2", "EVENT_1"), listOf("PRISON_2", "PRISON_1"))
    assertFalse(thisPolicy.isDifferentTo(newPolicy))
  }

  @Test
  fun `the isDifferentTo method should report true if there is a change other than an order change in the filter policy lists`() {
    val thisPolicy = FilterPolicy(listOf("EVENT_1", "EVENT_2", "EVENT_3"), listOf("PRISON_1", "PRISON_2"))
    val newPolicy = FilterPolicy(listOf("EVENT_3", "EVENT_2", "EVENT_4"), listOf("PRISON_2", "PRISON_1"))
    assertTrue(thisPolicy.isDifferentTo(newPolicy))
  }

  @Test
  fun `Should remove policy files when consumer is no longer associated with a queue `() {
    setUpConsumers(noPolicies = false, changedConsumers = listOf("limited-prisons"))
    val policy = FilterPolicy(listOf("NEEDS_CHANGED"))
    val json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(policy)
    whenever(fileManager.readFileContentsFromResourcesFolder("event-filter-policies/test/limited-prisons-subscription-filter.json")).thenReturn(json)
    service.generatePolicyFiles(listOf("test"))
    verify(fileManager, times(1)).delete(any())
  }

  @Test
  fun `Should also return the prisonId within the policy file`() {
    setUpConsumers(noPolicies = false, changedConsumers = listOf("config-v2-test"))
    service.generatePolicyFiles(listOf("test"))
    val jsonString = argumentCaptor<String>()
    verify(fileManager, times(1)).write(any(), jsonString.capture())
    val updatedPolicy = service.objectMapper.readValue(jsonString.firstValue, FilterPolicy::class.java)
    assertEquals("XYZ", updatedPolicy.prisonId?.get(0))
  }

  fun setUpConsumers(
    noPolicies: Boolean = true,
    changedConsumers: List<String>? = null,
    throwsExceptionConsumers: List<String>? = null,
  ) {
    val testGenerator = SubscriptionFilterPolicyManager(fileManager)

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
