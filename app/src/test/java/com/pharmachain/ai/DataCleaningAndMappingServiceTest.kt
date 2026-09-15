package com.pharmachain.ai

import com.pharmachain.ai.core.model.MappingField
import com.pharmachain.ai.feature.forecasting.engine.ArabicTextNormalizer
import com.pharmachain.ai.feature.forecasting.engine.DataCleaningAndMappingService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DataCleaningAndMappingServiceTest {

    @Test
    fun `test Arabic text normalization and digit conversion`() {
        val inputWithDiacritics = "تَارِيخُ البَيْعِ"
        val normalized = ArabicTextNormalizer.normalizeArabic(inputWithDiacritics)
        assertEquals("تاريخ البيع", normalized)

        val arabicIndicDate = "١٢/٠٣/٢٠٢٥"
        val normalizedDigits = ArabicTextNormalizer.normalizeDigits(arabicIndicDate)
        assertEquals("12/03/2025", normalizedDigits)
    }

    @Test
    fun `test date normalization variants to canonical ISO`() {
        // DD/MM/YYYY
        val d1 = DataCleaningAndMappingService.normalizeDateFormat("15/08/2026")
        assertEquals("2026-08-15", d1)

        // Arabic-Indic digits: ١٢/٠٣/٢٠٢٥ -> 2025-03-12
        val d2 = DataCleaningAndMappingService.normalizeDateFormat("١٢/٠٣/٢٠٢٥")
        assertEquals("2025-03-12", d2)

        // YYYY-MM-DD
        val d3 = DataCleaningAndMappingService.normalizeDateFormat("2026-01-01")
        assertEquals("2026-01-01", d3)

        // Excel serial date 46000 (~2025-12-09)
        val d4 = DataCleaningAndMappingService.normalizeDateFormat("45678")
        assertNotNull(d4)
    }

    @Test
    fun `test numeric and currency normalization`() {
        // English currency + thousands separator
        val n1 = DataCleaningAndMappingService.normalizeNumericValue("1,200 EGP")
        assertEquals("1200", n1)

        // Arabic currency + Arabic thousands separator
        val n2 = DataCleaningAndMappingService.normalizeNumericValue("١٬٢٠٠ ج.م")
        assertEquals("1200", n2)

        // Decimal price with currency
        val n3 = DataCleaningAndMappingService.normalizeNumericValue("45.50 L.E.")
        assertEquals("45.50", n3)
    }

    @Test
    fun `test bilingual fuzzy header matching`() {
        // Arabic headers
        val matchDateAr = DataCleaningAndMappingService.matchHeaderToField("تاريخ البيع")
        assertEquals(MappingField.DATE, matchDateAr.suggestedField)
        assertTrue(matchDateAr.confidenceScore >= 0.85)

        val matchSkuAr = DataCleaningAndMappingService.matchHeaderToField("اسم الصنف")
        assertEquals(MappingField.SKU_OR_NAME, matchSkuAr.suggestedField)
        assertTrue(matchSkuAr.confidenceScore >= 0.85)

        val matchQtyAr = DataCleaningAndMappingService.matchHeaderToField("الكمية المباعة")
        assertEquals(MappingField.QUANTITY_SOLD, matchQtyAr.suggestedField)
        assertTrue(matchQtyAr.confidenceScore >= 0.85)

        val matchPriceAr = DataCleaningAndMappingService.matchHeaderToField("سعر الجمهور (ج.م)")
        assertEquals(MappingField.UNIT_PRICE, matchPriceAr.suggestedField)
        assertTrue(matchPriceAr.confidenceScore >= 0.80)

        // English headers
        val matchDateEn = DataCleaningAndMappingService.matchHeaderToField("Transaction_Date")
        assertEquals(MappingField.DATE, matchDateEn.suggestedField)
        assertTrue(matchDateEn.confidenceScore >= 0.85)

        val matchSkuEn = DataCleaningAndMappingService.matchHeaderToField("Item_Description")
        assertEquals(MappingField.SKU_OR_NAME, matchSkuEn.suggestedField)
        assertTrue(matchSkuEn.confidenceScore >= 0.85)

        val matchQtyEn = DataCleaningAndMappingService.matchHeaderToField("Units_Sold")
        assertEquals(MappingField.QUANTITY_SOLD, matchQtyEn.suggestedField)
        assertTrue(matchQtyEn.confidenceScore >= 0.85)

        val matchPriceEn = DataCleaningAndMappingService.matchHeaderToField("Unit_Cost_EGP")
        assertEquals(MappingField.UNIT_PRICE, matchPriceEn.suggestedField)
        assertTrue(matchPriceEn.confidenceScore >= 0.85)
    }

    @Test
    fun `test dataset cleaning drops empty rows and flags partial rows`() {
        val headers = listOf("تاريخ البيع", "اسم الصنف", "الكمية", "السعر")
        val rawRows = listOf(
            mapOf("تاريخ البيع" to "١٥/٠٨/٢٠٢٦", "اسم الصنف" to "Panadol Extra", "الكمية" to "10", "السعر" to "45 EGP"),
            mapOf("تاريخ البيع" to "   ", "اسم الصنف" to "", "الكمية" to "", "السعر" to ""), // Empty row
            mapOf("تاريخ البيع" to "2026-08-16", "اسم الصنف" to "Augmentin 1g", "الكمية" to "", "السعر" to "115") // Partial row
        )

        val cleaned = DataCleaningAndMappingService.cleanAndNormalizeDataset(headers, rawRows)

        assertEquals(1, cleaned.droppedEmptyRows)
        assertEquals(2, cleaned.cleanedRows.size)
        assertEquals(1, cleaned.partialRowsCount)
        assertEquals("2026-08-15", cleaned.cleanedRows[0]["تاريخ البيع"])
        assertEquals("45", cleaned.cleanedRows[0]["السعر"])
    }

    @Test
    fun `test format aware parser correctly unzips and parses XLSX spreadsheet with shared strings`() {
        val workbook = org.apache.poi.xssf.usermodel.XSSFWorkbook()
        val sheet = workbook.createSheet("Sales")
        val r0 = sheet.createRow(0)
        r0.createCell(0).setCellValue("Transaction_Date")
        r0.createCell(1).setCellValue("Product_Name")
        r0.createCell(2).setCellValue("Quantity_Sold")
        val r1 = sheet.createRow(1)
        r1.createCell(0).setCellValue("2026-08-15")
        r1.createCell(1).setCellValue("Panadol Extra 500mg")
        r1.createCell(2).setCellValue(45.0)

        val byteOut = java.io.ByteArrayOutputStream()
        workbook.write(byteOut)
        workbook.close()

        val xlsxBytes = byteOut.toByteArray()
        val parsed = kotlinx.coroutines.runBlocking {
            com.pharmachain.ai.core.common.util.SpreadsheetReader.read(
                java.io.ByteArrayInputStream(xlsxBytes),
                "forecasting_batch.xlsx"
            )
        }

        assertEquals("Transaction_Date", parsed.headers[0])
        assertEquals("Product_Name", parsed.headers[1])
        assertEquals("Quantity_Sold", parsed.headers[2])
        assertEquals(1, parsed.rows.size)
        assertEquals("2026-08-15", parsed.rows[0][0])
        assertEquals("Panadol Extra 500mg", parsed.rows[0][1])
        assertEquals("45", parsed.rows[0][2])
    }

    @Test
    fun `test format aware parser handles UTF-8 CSV with BOM and semicolon delimiter`() {
        val bomCsv = "\uFEFFتاريخ البيع;اسم الصنف;الكمية المباعة;السعر\n2026-08-10;Congestal;25;32.50\n"
        val parsed = kotlinx.coroutines.runBlocking {
            com.pharmachain.ai.core.common.util.SpreadsheetReader.read(
                java.io.ByteArrayInputStream(bomCsv.toByteArray(Charsets.UTF_8)),
                "sales.csv"
            )
        }

        assertEquals(4, parsed.headers.size)
        assertEquals("تاريخ البيع", parsed.headers[0])
        assertEquals("اسم الصنف", parsed.headers[1])
        assertEquals(1, parsed.rows.size)
        assertEquals("Congestal", parsed.rows[0][1])
    }
}
