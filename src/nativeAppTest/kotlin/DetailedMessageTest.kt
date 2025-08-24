import util.generateDetailedMessages
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DetailedMessageTest {

    @Test
    fun testGenerateDetailedMessagesWithCellUpdate() {
        val config = createTestConfig(headerRow = 0, titleColumn = 0)
        
        val previousData = """Name,Age,Status
John,25,Active
Jane,30,Inactive"""

        val currentData = """Name,Age,Status
John,26,Active
Jane,30,Active"""

        val messages = generateDetailedMessages(config, previousData, currentData)
        
        assertEquals(2, messages.size)
        assertTrue(messages.contains("Age of John was updated to 26 from 25"))
        assertTrue(messages.contains("Status of Jane was updated to Active from Inactive"))
    }

    @Test
    fun testGenerateDetailedMessagesWithNewEntry() {
        val config = createTestConfig(headerRow = 0, titleColumn = 0)
        
        val previousData = """Name,Age,Status
John,25,Active"""

        val currentData = """Name,Age,Status
John,25,Active
Jane,30,Active"""

        val messages = generateDetailedMessages(config, previousData, currentData)
        
        assertEquals(1, messages.size)
        assertEquals("New entry added: Jane", messages[0])
    }

    @Test
    fun testGenerateDetailedMessagesWithRemovedEntry() {
        val config = createTestConfig(headerRow = 0, titleColumn = 0)
        
        val previousData = """Name,Age,Status
John,25,Active
Jane,30,Inactive"""

        val currentData = """Name,Age,Status
John,25,Active"""

        val messages = generateDetailedMessages(config, previousData, currentData)
        
        assertEquals(1, messages.size)
        assertEquals("Entry removed: Jane", messages[0])
    }

    @Test
    fun testGenerateDetailedMessagesWithQuotedCsv() {
        val config = createTestConfig(headerRow = 0, titleColumn = 0)
        
        val previousData = """"Name","Description","Status"
"John Doe","A person, with commas","Active""""

        val currentData = """"Name","Description","Status"
"John Doe","A different person, with commas","Active""""

        val messages = generateDetailedMessages(config, previousData, currentData)
        
        assertEquals(1, messages.size)
        assertEquals("Description of John Doe was updated to A different person, with commas from A person, with commas", messages[0])
    }

    @Test
    fun testGenerateDetailedMessagesWithInvalidConfig() {
        val config = createTestConfig(headerRow = -1, titleColumn = -1)
        
        val previousData = """Name,Age,Status
John,25,Active"""

        val currentData = """Name,Age,Status
John,26,Active"""

        val messages = generateDetailedMessages(config, previousData, currentData)
        
        assertTrue(messages.isEmpty())
    }

    @Test
    fun testGenerateDetailedMessagesWithMultilineUpdate() {
        val config = createTestConfig(headerRow = 0, titleColumn = 0)
        
        val previousData = """"Name","Description","Status"
"John Doe","Old description
with multiple lines","Active""""

        val currentData = """"Name","Description","Status"
"John Doe","New description
with different lines","Active""""

        val messages = generateDetailedMessages(config, previousData, currentData)
        
        assertEquals(1, messages.size)
        assertEquals("Description of John Doe was updated to New description\nwith different lines from Old description\nwith multiple lines", messages[0])
    }

    private fun createTestConfig(headerRow: Int = 0, titleColumn: Int = 0): AppConfig {
        return AppConfig(
            sheet = SheetConfig(
                id = "test-sheet-id",
                index = 0,
                headerRow = headerRow,
                titleColumn = titleColumn
            ),
            git = GitConfig(
                repoUrl = "https://github.com/test/repo",
                token = "test-token"
            ),
            general = GeneralConfig(
                intervalMinutes = 15,
                dataPath = "./test-data",
            )
        )
    }
}