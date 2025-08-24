import kotlin.test.Test
import kotlin.test.assertEquals

class CsvParserTest {

    @Test
    fun testParseSimpleCsv() {
        val csvData = """Name,Age,Status
John,25,Active
Jane,30,Inactive"""

        val parsed = CsvParser.parse(csvData)
        
        assertEquals(3, parsed.size)
        assertEquals(listOf("Name", "Age", "Status"), parsed[0])
        assertEquals(listOf("John", "25", "Active"), parsed[1])
        assertEquals(listOf("Jane", "30", "Inactive"), parsed[2])
    }

    @Test
    fun testParseWithQuotes() {
        val csvData = """"Name","Description"
"John Doe","A person, with commas"
"Jane Smith","Another ""quoted"" description""""

        val parsed = CsvParser.parse(csvData)
        
        assertEquals(3, parsed.size)
        assertEquals(listOf("Name", "Description"), parsed[0])
        assertEquals(listOf("John Doe", "A person, with commas"), parsed[1])
        assertEquals(listOf("Jane Smith", """Another "quoted" description"""), parsed[2])
    }

    @Test
    fun testParseWithMultilineFields() {
        val csvData = """"Name","Description","Status"
"John Doe","This is a description
that spans multiple
lines","Active"
"Jane Smith","Single line description","Inactive""""

        val parsed = CsvParser.parse(csvData)
        
        assertEquals(3, parsed.size)
        assertEquals(listOf("Name", "Description", "Status"), parsed[0])
        assertEquals(listOf("John Doe", "This is a description\nthat spans multiple\nlines", "Active"), parsed[1])
        assertEquals(listOf("Jane Smith", "Single line description", "Inactive"), parsed[2])
    }

    @Test
    fun testParseEmptyFields() {
        val csvData = """Name,Description,Status
John,,Active
,Empty name,Inactive
,,"""

        val parsed = CsvParser.parse(csvData)
        
        assertEquals(4, parsed.size)
        assertEquals(listOf("Name", "Description", "Status"), parsed[0])
        assertEquals(listOf("John", "", "Active"), parsed[1])
        assertEquals(listOf("", "Empty name", "Inactive"), parsed[2])
        assertEquals(listOf("", "", ""), parsed[3])
    }

    @Test
    fun testParseQuotedEmptyFields() {
        val csvData = """"Name","Description","Status"
"John","","Active"
"","Empty name","Inactive""""

        val parsed = CsvParser.parse(csvData)
        
        assertEquals(3, parsed.size)
        assertEquals(listOf("Name", "Description", "Status"), parsed[0])
        assertEquals(listOf("John", "", "Active"), parsed[1])
        assertEquals(listOf("", "Empty name", "Inactive"), parsed[2])
    }

    @Test
    fun testParseWithCarriageReturns() {
        val csvData = "Name,Age,Status\r\nJohn,25,Active\r\nJane,30,Inactive"

        val parsed = CsvParser.parse(csvData)
        
        assertEquals(3, parsed.size)
        assertEquals(listOf("Name", "Age", "Status"), parsed[0])
        assertEquals(listOf("John", "25", "Active"), parsed[1])
        assertEquals(listOf("Jane", "30", "Inactive"), parsed[2])
    }

    @Test
    fun testParseSingleRow() {
        val csvData = "Name,Age,Status"

        val parsed = CsvParser.parse(csvData)
        
        assertEquals(1, parsed.size)
        assertEquals(listOf("Name", "Age", "Status"), parsed[0])
    }

    @Test
    fun testParseEmptyString() {
        val csvData = ""

        val parsed = CsvParser.parse(csvData)
        
        assertEquals(1, parsed.size)
        assertEquals(listOf(""), parsed[0])
    }

    @Test
    fun testParseComplexQuotedField() {
        val csvData = """"Name","Complex Field"
"John","Field with ""quotes"", 
newlines, and commas!""""

        val parsed = CsvParser.parse(csvData)
        
        assertEquals(2, parsed.size)
        assertEquals(listOf("Name", "Complex Field"), parsed[0])
        assertEquals(listOf("John", "Field with \"quotes\", \nnewlines, and commas!"), parsed[1])
    }
}