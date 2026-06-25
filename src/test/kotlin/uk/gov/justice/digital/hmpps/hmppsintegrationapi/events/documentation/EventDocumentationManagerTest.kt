package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.documentation

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.FileManager
import kotlin.test.Test
import kotlin.test.assertEquals

class EventDocumentationManagerTest {
  val fileManager: FileManager = mock()
  lateinit var generator: EventDocumentationManager
  val testFile =
    """
    ----
    blah
    blah
    blah
    <!---GENERATED-EVENTS-BEGIN--->
    Generated line 1
    Generated line 2
    Generated line 3
    Generated line 4
    <!---GENERATED-EVENTS-END--->
    ---
    """.trimIndent()

  @BeforeEach
  fun setup() {
    whenever(fileManager.getDocumentationPath(any())).thenReturn("path")
    whenever(fileManager.getDocumentationContents(any(), any())).thenReturn(testFile)
    doNothing().whenever(fileManager).write(any(), any())
    generator = EventDocumentationManager(fileManager)
  }

  @Test
  fun `Generates an updated file`() {
    generator.generate()
    val updated = argumentCaptor<String>()
    verify(fileManager).write(any(), updated.capture())
    val updatedContent = updated.firstValue
    assertThat(updatedContent).contains("blah")
    assertThat(updatedContent).doesNotContain("Generated line 1")
    assertThat(updatedContent).contains("Dynamic Risks Changed")
  }

  @Test
  fun `Does not update the file when there are no changes`() {
    val generated = generator.generateEventsSection()
    val currentTestFileEvents = generator.extractEventSection(testFile)
    val noChanges = testFile.replace(currentTestFileEvents, generated)
    whenever(fileManager.getDocumentationContents(any(), any())).thenReturn(noChanges)
    generator.generate()
    verify(fileManager, times(0)).write(any(), any())
  }

  @Test
  fun `Retrieves the events section`() {
    val expected =
      """
      Generated line 1
      Generated line 2
      Generated line 3
      Generated line 4
      """.trimIndent()
    val actual = generator.extractEventSection(testFile)
    assertEquals(actual, expected)
  }
}
