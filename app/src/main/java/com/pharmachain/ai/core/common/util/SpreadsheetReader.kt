package com.pharmachain.ai.core.common.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.EncryptedDocumentException
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.jsoup.Jsoup
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets

/**
 * Supported spreadsheet formats detected by magic numbers and content inspection.
 */
enum class SpreadsheetFormat(val displayName: String, val extension: String) {
    XLSX("Excel Workbook (.xlsx)", ".xlsx"),
    XLS_BINARY("Excel 97-2003 (.xls)", ".xls"),
    HTML_TABLE("HTML Table POS Export (.html/.xls)", ".html"),
    CSV_DELIMITED("CSV / Delimited Text", ".csv")
}

/**
 * Unified parsed representation of any uploaded spreadsheet file.
 */
data class ParsedSpreadsheet(
    val fileName: String,
    val detectedFormat: SpreadsheetFormat,
    val headers: List<String>,
    val rows: List<List<String>>,
    val warnings: List<String> = emptyList()
)

class SpreadsheetParseException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Enterprise Production Spreadsheet Reader powered by Apache POI (5.2.5) and Jsoup (1.17.2).
 * Handles OpenXML (.xlsx), Legacy BIFF (.xls), Egyptian POS HTML-table exports, and UTF-8/BOM CSVs.
 */
object SpreadsheetReader {

    private val dataFormatter by lazy { DataFormatter() }

    /**
     * Reads and parses an input stream asynchronously on Dispatchers.IO.
     */
    suspend fun read(
        inputStream: InputStream,
        fileName: String = "spreadsheet.xlsx",
        declaredMimeType: String? = null,
        maxBytes: Int = 30 * 1024 * 1024 // 30MB safety limit
    ): ParsedSpreadsheet = withContext(Dispatchers.IO) {
        val rawBytes = inputStream.readBytes()

        if (rawBytes.isEmpty()) {
            throw SpreadsheetParseException("File is empty (0 bytes). Please select a valid spreadsheet.")
        }
        if (rawBytes.size > maxBytes) {
            throw SpreadsheetParseException("File size (${rawBytes.size / (1024 * 1024)}MB) exceeds the 30MB limit.")
        }

        val format = detectFormat(rawBytes, fileName, declaredMimeType)

        try {
            when (format) {
                SpreadsheetFormat.XLSX -> parseXlsxWithPoi(rawBytes, fileName)
                SpreadsheetFormat.XLS_BINARY -> parseXlsWithPoi(rawBytes, fileName)
                SpreadsheetFormat.HTML_TABLE -> parseHtmlTableWithJsoup(rawBytes, fileName)
                SpreadsheetFormat.CSV_DELIMITED -> parseDelimitedText(rawBytes, fileName)
            }
        } catch (e: EncryptedDocumentException) {
            throw SpreadsheetParseException("This spreadsheet is password-protected. Please upload an unprotected file.", e)
        } catch (e: SpreadsheetParseException) {
            throw e
        } catch (e: Exception) {
            // Fallback attempt for edge cases (e.g. MISLABELED CSV/HTML as .xlsx)
            if (format == SpreadsheetFormat.XLSX || format == SpreadsheetFormat.XLS_BINARY) {
                try {
                    return@withContext parseHtmlOrDelimitedFallback(rawBytes, fileName)
                } catch (_: Exception) {
                    // rethrow original POI error
                }
            }
            throw SpreadsheetParseException("Failed to parse ${format.displayName}: ${e.localizedMessage ?: e.javaClass.simpleName}", e)
        }
    }

    /**
     * Determines format by inspecting MAGIC BYTES first (not just file extension).
     */
    fun detectFormat(bytes: ByteArray, fileName: String, declaredMimeType: String? = null): SpreadsheetFormat {
        val lowerName = fileName.lowercase()

        // 1. FIRST PRIORITY: ZIP local file header (PK\x03\x04, PK\x05\x06, PK\x07\x08) -> OpenXML (.xlsx / .xlsm)
        if (isZipArchive(bytes)) {
            return SpreadsheetFormat.XLSX
        }

        // 2. SECOND PRIORITY: OLE2 compound document signature (0xD0 0xCF 0x11 0xE0 0xA1 0xB1 0x1A 0xE1) -> Legacy XLS
        if (isOle2CompoundDocument(bytes)) {
            return SpreadsheetFormat.XLS_BINARY
        }

        // 3. THIRD PRIORITY: Sniff first ~2KB as text for HTML table POS exports (common in Egyptian pharmacies)
        val sampleLen = minOf(bytes.size, 2048)
        val textSample = String(bytes, 0, sampleLen, StandardCharsets.UTF_8).lowercase()

        if (textSample.contains("<html") || textSample.contains("<table") || textSample.contains("xmlns:x=\"urn:schemas-microsoft-com:office:excel\"")) {
            return SpreadsheetFormat.HTML_TABLE
        }

        // 4. FOURTH PRIORITY: Extension hints for text-based formats
        if (lowerName.endsWith(".html") || lowerName.endsWith(".htm")) {
            return SpreadsheetFormat.HTML_TABLE
        }
        if (lowerName.endsWith(".xlsx") || lowerName.endsWith(".xlsm")) {
            return SpreadsheetFormat.XLSX
        }
        if (lowerName.endsWith(".xls")) {
            return SpreadsheetFormat.XLS_BINARY
        }

        // 5. LAST RESORT: CSV / Delimited text
        return SpreadsheetFormat.CSV_DELIMITED
    }

    private fun isZipArchive(bytes: ByteArray): Boolean {
        if (bytes.size < 4) return false
        val isPk = bytes[0] == 0x50.toByte() && bytes[1] == 0x4B.toByte()
        val isHeader = (bytes[2] == 0x03.toByte() && bytes[3] == 0x04.toByte()) ||
                       (bytes[2] == 0x05.toByte() && bytes[3] == 0x06.toByte()) ||
                       (bytes[2] == 0x07.toByte() && bytes[3] == 0x08.toByte())
        return isPk && isHeader
    }

    private fun isOle2CompoundDocument(bytes: ByteArray): Boolean {
        if (bytes.size < 8) return false
        return bytes[0] == 0xD0.toByte() &&
               bytes[1] == 0xCF.toByte() &&
               bytes[2] == 0x11.toByte() &&
               bytes[3] == 0xE0.toByte() &&
               bytes[4] == 0xA1.toByte() &&
               bytes[5] == 0xB1.toByte() &&
               bytes[6] == 0x1A.toByte() &&
               bytes[7] == 0xE1.toByte()
    }

    // =========================================================================
    // Apache POI Parsers (.xlsx and .xls)
    // =========================================================================

    private fun parseXlsxWithPoi(bytes: ByteArray, fileName: String): ParsedSpreadsheet {
        ByteArrayInputStream(bytes).use { inputStream ->
            XSSFWorkbook(inputStream).use { workbook ->
                return extractFirstSheetData(workbook.getSheetAt(0), fileName, SpreadsheetFormat.XLSX)
            }
        }
    }

    private fun parseXlsWithPoi(bytes: ByteArray, fileName: String): ParsedSpreadsheet {
        ByteArrayInputStream(bytes).use { inputStream ->
            HSSFWorkbook(inputStream).use { workbook ->
                return extractFirstSheetData(workbook.getSheetAt(0), fileName, SpreadsheetFormat.XLS_BINARY)
            }
        }
    }

    private fun extractFirstSheetData(sheet: Sheet?, fileName: String, format: SpreadsheetFormat): ParsedSpreadsheet {
        if (sheet == null || sheet.physicalNumberOfRows == 0) {
            throw SpreadsheetParseException("The first sheet in this workbook is completely empty.")
        }

        val rowsList = mutableListOf<List<String>>()
        var emptyRowsSkipped = 0

        val rowIterator = sheet.rowIterator()
        var headerRow: Row? = null

        // Find first non-empty row as header
        while (rowIterator.hasNext() && headerRow == null) {
            val r = rowIterator.next()
            if (isRowNotEmpty(r)) {
                headerRow = r
            } else {
                emptyRowsSkipped++
            }
        }

        if (headerRow == null) {
            throw SpreadsheetParseException("No valid header row found in the workbook.")
        }

        val lastCellNum = maxOf(headerRow.lastCellNum.toInt(), 1)
        val headers = mutableListOf<String>()

        for (colIdx in 0 until lastCellNum) {
            val cell = headerRow.getCell(colIdx)
            val formatted = cell?.let { dataFormatter.formatCellValue(it) }?.trim() ?: ""
            headers.add(if (formatted.isNotBlank()) formatted else "Column_${colIdx + 1}")
        }

        val uniqueHeaders = makeHeadersUnique(headers)
        val colCount = uniqueHeaders.size

        while (rowIterator.hasNext()) {
            val row = rowIterator.next()
            if (isRowNotEmpty(row)) {
                val rowCells = ArrayList<String>(colCount)
                for (c in 0 until colCount) {
                    val cell = row.getCell(c)
                    val formatted = cell?.let { dataFormatter.formatCellValue(it) }?.trim() ?: ""
                    rowCells.add(formatted)
                }
                if (rowCells.any { it.isNotBlank() }) {
                    rowsList.add(rowCells)
                } else {
                    emptyRowsSkipped++
                }
            } else {
                emptyRowsSkipped++
            }
        }

        val warnings = mutableListOf<String>()
        if (emptyRowsSkipped > 0) {
            warnings.add("$emptyRowsSkipped empty row(s) were automatically skipped.")
        }

        return ParsedSpreadsheet(
            fileName = fileName,
            detectedFormat = format,
            headers = uniqueHeaders,
            rows = rowsList,
            warnings = warnings
        )
    }

    private fun isRowNotEmpty(row: Row?): Boolean {
        if (row == null) return false
        val first = row.firstCellNum.toInt()
        val last = row.lastCellNum.toInt()
        if (first < 0 || last <= first) return false

        for (c in first until last) {
            val cell = row.getCell(c)
            if (cell != null) {
                val text = dataFormatter.formatCellValue(cell).trim()
                if (text.isNotBlank()) return true
            }
        }
        return false
    }

    // =========================================================================
    // Jsoup HTML Table POS Parser (Egyptian Pharmacy POS Exports)
    // =========================================================================

    private fun parseHtmlTableWithJsoup(bytes: ByteArray, fileName: String): ParsedSpreadsheet {
        val htmlString = String(bytes, StandardCharsets.UTF_8)
        val doc = Jsoup.parse(htmlString)
        val tables = doc.select("table")

        if (tables.isEmpty()) {
            throw SpreadsheetParseException("HTML export contains no <table> elements.")
        }

        // Pick largest table by row count
        val targetTable = tables.maxByOrNull { it.select("tr").size } ?: tables.first()
            ?: throw SpreadsheetParseException("HTML export contains no valid <table> elements.")
        val trElements = targetTable.select("tr")

        if (trElements.isEmpty()) {
            throw SpreadsheetParseException("Selected HTML table contains no rows.")
        }

        val rawHeaders = mutableListOf<String>()
        val headerTr = trElements.first()
            ?: throw SpreadsheetParseException("HTML table contains no header row.")
        val thCells = headerTr.select("th, td")

        for ((idx, cell) in thCells.withIndex()) {
            val text = cell.text().trim()
            rawHeaders.add(if (text.isNotBlank()) text else "Column_${idx + 1}")
        }

        val uniqueHeaders = makeHeadersUnique(rawHeaders)
        val colCount = uniqueHeaders.size
        val dataRows = mutableListOf<List<String>>()

        for (i in 1 until trElements.size) {
            val tr = trElements[i]
            val cells = tr.select("th, td")
            val rowCells = ArrayList<String>(colCount)
            for (c in 0 until colCount) {
                val text = if (c < cells.size) cells[c].text().trim() else ""
                rowCells.add(text)
            }
            if (rowCells.any { it.isNotBlank() }) {
                dataRows.add(rowCells)
            }
        }

        return ParsedSpreadsheet(
            fileName = fileName,
            detectedFormat = SpreadsheetFormat.HTML_TABLE,
            headers = uniqueHeaders,
            rows = dataRows,
            warnings = listOf("Parsed from HTML Table POS export format.")
        )
    }

    // =========================================================================
    // UTF-8 & BOM-Aware CSV / Delimited Text Parser
    // =========================================================================

    private fun parseDelimitedText(bytes: ByteArray, fileName: String): ParsedSpreadsheet {
        // Strip UTF-8 Byte Order Mark (\uFEFF / EF BB BF) if present
        var rawText = String(bytes, StandardCharsets.UTF_8)
        if (rawText.startsWith("\uFEFF")) {
            rawText = rawText.substring(1)
        }

        val lines = rawText.lines().map { it.trimEnd() }.filter { it.isNotBlank() }
        if (lines.isEmpty()) {
            throw SpreadsheetParseException("CSV file is empty.")
        }

        val delimiter = detectDelimiter(lines.take(10))
        val parsedRows = lines.map { parseCsvLine(it, delimiter) }

        val rawHeaders = parsedRows.first().mapIndexed { idx, h ->
            val cleaned = h.trim()
            if (cleaned.isNotBlank()) cleaned else "Column_${idx + 1}"
        }

        val uniqueHeaders = makeHeadersUnique(rawHeaders)
        val colCount = uniqueHeaders.size
        val dataRows = mutableListOf<List<String>>()

        for (i in 1 until parsedRows.size) {
            val row = parsedRows[i]
            val padded = ArrayList<String>(colCount)
            for (c in 0 until colCount) {
                padded.add(if (c < row.size) row[c].trim() else "")
            }
            if (padded.any { it.isNotBlank() }) {
                dataRows.add(padded)
            }
        }

        return ParsedSpreadsheet(
            fileName = fileName,
            detectedFormat = SpreadsheetFormat.CSV_DELIMITED,
            headers = uniqueHeaders,
            rows = dataRows
        )
    }

    private fun detectDelimiter(sampleLines: List<String>): Char {
        val candidates = listOf(',', ';', '\t', '|')
        val counts = candidates.associateWith { delim ->
            sampleLines.sumOf { line -> line.count { it == delim } }
        }
        return counts.maxByOrNull { it.value }?.key ?: ','
    }

    private fun parseCsvLine(line: String, delimiter: Char): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0

        while (i < line.length) {
            val ch = line[i]
            when {
                ch == '"' -> {
                    if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                        current.append('"')
                        i++
                    } else {
                        inQuotes = !inQuotes
                    }
                }
                ch == delimiter && !inQuotes -> {
                    result.add(current.toString())
                    current.setLength(0)
                }
                else -> current.append(ch)
            }
            i++
        }
        result.add(current.toString())
        return result
    }

    private fun parseHtmlOrDelimitedFallback(bytes: ByteArray, fileName: String): ParsedSpreadsheet {
        val sample = String(bytes, 0, minOf(bytes.size, 2048), StandardCharsets.UTF_8).lowercase()
        return if (sample.contains("<html") || sample.contains("<table")) {
            parseHtmlTableWithJsoup(bytes, fileName)
        } else {
            parseDelimitedText(bytes, fileName)
        }
    }

    private fun makeHeadersUnique(headers: List<String>): List<String> {
        val unique = mutableListOf<String>()
        val counts = mutableMapOf<String, Int>()
        for (h in headers) {
            val count = counts.getOrDefault(h, 0)
            if (count == 0) {
                unique.add(h)
                counts[h] = 1
            } else {
                counts[h] = count + 1
                unique.add("${h}_${count + 1}")
            }
        }
        return unique
    }
}
