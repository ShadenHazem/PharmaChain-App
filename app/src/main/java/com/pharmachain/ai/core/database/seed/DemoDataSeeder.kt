package com.pharmachain.ai.core.database.seed

import androidx.room.withTransaction
import com.pharmachain.ai.core.database.PharmaChainDatabase
import com.pharmachain.ai.core.database.entity.DistributorProfileEntity
import com.pharmachain.ai.core.database.entity.MedicationEntity
import com.pharmachain.ai.core.database.entity.ProductListingEntity
import com.pharmachain.ai.core.database.entity.UserEntity
import com.pharmachain.ai.core.model.Role
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * ============================================================================
 * PHARMACHAIN AI — DEMO SEED DATA: DISTRIBUTOR CATALOG POPULATION
 * 
 * NOTE: This is DEMO / SEED DATA representing a realistic inventory catalog
 * for a typical Egyptian community pharmacy. Pricing (in EGP), stock levels,
 * and distributor listings are illustrative for demonstration and offline
 * validation purposes and are NOT sourced from real distributor price lists.
 * ============================================================================
 */
object DemoDataSeeder {

    const val DEMO_DISTRIBUTOR_ID = "dist_cairo_drugs"
    const val DEMO_USER_ID = "user_cairo_drugs"
    const val DEMO_DISTRIBUTOR_NAME = "Cairo Drugs"
    const val DEMO_REPUTATION_SCORE = 4.7
    const val DEMO_TOTAL_ORDERS = 1240
    const val DEMO_COMMISSION_RATE = 4.5
    const val DEMO_TAX_REG_ID = "DEMO-000000"

    /**
     * Internal definition for catalog seed items.
     */
    data class DemoProductItem(
        val id: String,
        val genericName: String,
        val brandName: String,
        val form: String,
        val strength: String,
        val category: String,
        val priceEgp: Double,
        val stockQuantity: Int,
        val expiryOffsetDays: Int
    )

    /**
     * Comprehensive representative dataset (~200 medications) across 10 key categories.
     * Contains varied stock levels (including low-stock items < 10 for alert testing)
     * and varied expiry horizons (including near-expiry items 60-90 days out).
     */
    val DEMO_MEDICATIONS: List<DemoProductItem> = listOf(
        // ====================================================================
        // 1. ANALGESICS, ANTIPYRETICS & NSAIDs (23 items)
        // ====================================================================
        DemoProductItem("med_101", "Paracetamol", "Panadol Advance 500mg", "Tablet", "500mg", "Analgesics & Antipyretics", 38.00, 4, 450), // Low stock demo
        DemoProductItem("med_102", "Paracetamol + Caffeine", "Panadol Extra 500mg", "Tablet", "500mg/65mg", "Analgesics & Antipyretics", 45.00, 320, 520),
        DemoProductItem("med_103", "Paracetamol", "Abimol 500mg", "Tablet", "500mg", "Analgesics & Antipyretics", 18.00, 240, 600),
        DemoProductItem("med_104", "Paracetamol", "Paramol 500mg", "Tablet", "500mg", "Analgesics & Antipyretics", 20.00, 180, 480),
        DemoProductItem("med_105", "Paracetamol + Caffeine + Propyphenazone", "Cetafen Tablets", "Tablet", "Standard", "Analgesics & Antipyretics", 26.50, 140, 510),
        DemoProductItem("med_106", "Ibuprofen", "Brufen 400mg", "Tablet", "400mg", "Analgesics & NSAIDs", 34.00, 210, 420),
        DemoProductItem("med_107", "Ibuprofen", "Brufen 600mg", "Tablet", "600mg", "Analgesics & NSAIDs", 46.00, 160, 490),
        DemoProductItem("med_108", "Ibuprofen", "Marcofen 200mg/5ml Suspension", "Syrup", "100mg/5ml", "Analgesics & NSAIDs", 22.00, 85, 360),
        DemoProductItem("med_109", "Diclofenac Potassium", "Cataflam 50mg", "Tablet", "50mg", "Analgesics & NSAIDs", 52.00, 190, 540),
        DemoProductItem("med_110", "Diclofenac Potassium", "Clofast 50mg", "Tablet", "50mg", "Analgesics & NSAIDs", 31.00, 110, 460),
        DemoProductItem("med_111", "Diclofenac Sodium", "Voltaren 75mg Ampoules", "Injection", "75mg/3ml", "Analgesics & NSAIDs", 68.00, 95, 380),
        DemoProductItem("med_112", "Diclofenac Sodium", "Voltaren 100mg Retard", "Tablet", "100mg", "Analgesics & NSAIDs", 58.00, 140, 500),
        DemoProductItem("med_113", "Diclofenac Sodium", "Olfen 100mg SR", "Capsule", "100mg", "Analgesics & NSAIDs", 36.00, 130, 440),
        DemoProductItem("med_114", "Ketoprofen", "Ketofan 75mg", "Capsule", "75mg", "Analgesics & NSAIDs", 28.00, 175, 490),
        DemoProductItem("med_115", "Ketoprofen", "Bi-Profenid 150mg", "Tablet", "150mg", "Analgesics & NSAIDs", 62.00, 120, 520),
        DemoProductItem("med_116", "Ketoprofen", "Ketolgin 100mg Ampoules", "Injection", "100mg/2ml", "Analgesics & NSAIDs", 32.00, 80, 410),
        DemoProductItem("med_117", "Meloxicam", "Mobic 15mg", "Tablet", "15mg", "Analgesics & NSAIDs", 48.00, 90, 530),
        DemoProductItem("med_118", "Meloxicam", "Anti-Cox II 15mg", "Tablet", "15mg", "Analgesics & NSAIDs", 33.00, 115, 470),
        DemoProductItem("med_119", "Celecoxib", "Celebrex 200mg", "Capsule", "200mg", "Analgesics & NSAIDs", 118.00, 65, 560),
        DemoProductItem("med_120", "Ketorolac Tromethamine", "Ketolac 30mg Ampoules", "Injection", "30mg/2ml", "Analgesics & NSAIDs", 42.00, 70, 390),
        DemoProductItem("med_121", "Paracetamol + Orphenadrine", "Norgesic Tablets", "Tablet", "450mg/35mg", "Analgesics & Muscle Relaxants", 38.50, 150, 480),
        DemoProductItem("med_122", "Acetylsalicylic Acid", "Aspocid 75mg Chewable", "Tablet", "75mg", "Analgesics & Antiplatelet", 16.00, 380, 620),
        DemoProductItem("med_123", "Acetylsalicylic Acid", "Aspirin Protect 100mg", "Tablet", "100mg", "Analgesics & Antiplatelet", 32.00, 260, 580),

        // ====================================================================
        // 2. ANTIBIOTICS & ANTIMICROBIALS (22 items)
        // ====================================================================
        DemoProductItem("med_124", "Amoxicillin + Clavulanate", "Augmentin 1g Tablets", "Tablet", "1000mg", "Antibiotics", 112.00, 6, 420), // Low stock demo
        DemoProductItem("med_125", "Amoxicillin + Clavulanate", "Augmentin 625mg Tablets", "Tablet", "625mg", "Antibiotics", 82.00, 110, 440),
        DemoProductItem("med_126", "Amoxicillin + Clavulanate", "Hibiotic 1g Tablets", "Tablet", "1000mg", "Antibiotics", 88.00, 190, 460),
        DemoProductItem("med_127", "Amoxicillin + Clavulanate", "Curam 1g Tablets", "Tablet", "1000mg", "Antibiotics", 94.00, 140, 430),
        DemoProductItem("med_128", "Amoxicillin + Clavulanate", "Megamox 1g Tablets", "Tablet", "1000mg", "Antibiotics", 79.00, 120, 390),
        DemoProductItem("med_129", "Amoxicillin Trihydrate", "Amoxil 500mg", "Capsule", "500mg", "Antibiotics", 24.00, 80, 75), // Near-expiry demo (75 days)
        DemoProductItem("med_130", "Ciprofloxacin", "Ciprofar 500mg", "Tablet", "500mg", "Antibiotics", 48.00, 160, 480),
        DemoProductItem("med_131", "Ciprofloxacin", "Ciprofar 750mg", "Tablet", "750mg", "Antibiotics", 64.00, 4, 510), // Low stock demo
        DemoProductItem("med_132", "Ciprofloxacin", "Serviflox 500mg", "Tablet", "500mg", "Antibiotics", 42.00, 130, 470),
        DemoProductItem("med_133", "Levofloxacin", "Tavanic 500mg", "Tablet", "500mg", "Antibiotics", 135.00, 50, 520),
        DemoProductItem("med_134", "Levofloxacin", "Levoflox 500mg", "Tablet", "500mg", "Antibiotics", 58.00, 90, 460),
        DemoProductItem("med_135", "Azithromycin", "Zithrokan 500mg", "Capsule", "500mg", "Antibiotics", 46.00, 175, 450),
        DemoProductItem("med_136", "Azithromycin", "Zithromax 500mg", "Tablet", "500mg", "Antibiotics", 86.00, 80, 490),
        DemoProductItem("med_137", "Azithromycin", "Xithrone 500mg", "Tablet", "500mg", "Antibiotics", 42.00, 115, 430),
        DemoProductItem("med_138", "Ceftriaxone", "Cefaxone 1g IV/IM", "Injection", "1000mg", "Antibiotics", 54.00, 130, 400),
        DemoProductItem("med_139", "Ceftriaxone", "Rocephin 1g Injection", "Injection", "1000mg", "Antibiotics", 98.00, 45, 440),
        DemoProductItem("med_140", "Cefotaxime", "Cefotax 1g Injection", "Injection", "1000mg", "Antibiotics", 38.00, 120, 380),
        DemoProductItem("med_141", "Cefuroxime Axetil", "Zinnat 500mg", "Tablet", "500mg", "Antibiotics", 105.00, 75, 510),
        DemoProductItem("med_142", "Cefixime", "Suprax 400mg", "Capsule", "400mg", "Antibiotics", 92.00, 60, 470),
        DemoProductItem("med_143", "Clarithromycin", "Klacid 500mg", "Tablet", "500mg", "Antibiotics", 128.00, 55, 530),
        DemoProductItem("med_144", "Metronidazole", "Flagyl 500mg", "Tablet", "500mg", "Antiprotozoal & Antibacterial", 28.00, 210, 70), // Near-expiry demo (70 days)
        DemoProductItem("med_145", "Doxycycline", "Vibramycin 100mg", "Capsule", "100mg", "Antibiotics", 36.00, 95, 490),

        // ====================================================================
        // 3. ANTIHISTAMINES, ALLERGY & COLD (18 items)
        // ====================================================================
        DemoProductItem("med_146", "Cetirizine HCl", "Zyrtec 10mg", "Tablet", "10mg", "Antihistamines & Allergy", 44.00, 180, 85), // Near-expiry demo (85 days)
        DemoProductItem("med_147", "Cetirizine HCl", "Histazine 10mg", "Tablet", "10mg", "Antihistamines & Allergy", 22.00, 140, 520),
        DemoProductItem("med_148", "Levocetirizine", "Xyzal 5mg", "Tablet", "5mg", "Antihistamines & Allergy", 56.00, 110, 480),
        DemoProductItem("med_149", "Levocetirizine", "Levocet 5mg", "Tablet", "5mg", "Antihistamines & Allergy", 32.00, 95, 460),
        DemoProductItem("med_150", "Fexofenadine HCl", "Telfast 120mg", "Tablet", "120mg", "Antihistamines & Allergy", 64.00, 130, 510),
        DemoProductItem("med_151", "Fexofenadine HCl", "Telfast 180mg", "Tablet", "180mg", "Antihistamines & Allergy", 78.00, 160, 540),
        DemoProductItem("med_152", "Fexofenadine HCl", "Fexodine 180mg", "Tablet", "180mg", "Antihistamines & Allergy", 45.00, 105, 470),
        DemoProductItem("med_153", "Loratadine", "Claritin 10mg", "Tablet", "10mg", "Antihistamines & Allergy", 48.00, 120, 500),
        DemoProductItem("med_154", "Loratadine", "Mosidatin 10mg", "Tablet", "10mg", "Antihistamines & Allergy", 25.00, 90, 440),
        DemoProductItem("med_155", "Desloratadine", "Aerius 5mg", "Tablet", "5mg", "Antihistamines & Allergy", 72.00, 85, 530),
        DemoProductItem("med_156", "Desloratadine", "Deslor 5mg", "Tablet", "5mg", "Antihistamines & Allergy", 38.00, 110, 460),
        DemoProductItem("med_157", "Bilastine", "Bilaxten 20mg", "Tablet", "20mg", "Antihistamines & Allergy", 84.00, 70, 520),
        DemoProductItem("med_158", "Chlorpheniramine", "Allergyl 4mg", "Tablet", "4mg", "Antihistamines & Allergy", 14.00, 220, 600),
        DemoProductItem("med_159", "Cold Combination", "Panadol Cold & Flu All in One", "Tablet", "Standard", "Cold & Flu Formulations", 48.00, 240, 490),
        DemoProductItem("med_160", "Cold Combination", "Congestal Tablets", "Tablet", "Standard", "Cold & Flu Formulations", 31.00, 310, 510),
        DemoProductItem("med_161", "Cold Combination", "1, 2, 3 Cold Tablets", "Tablet", "Standard", "Cold & Flu Formulations", 24.00, 290, 470),
        DemoProductItem("med_162", "Cold Combination", "Flustop Tablets", "Tablet", "Standard", "Cold & Flu Formulations", 21.00, 160, 450),
        DemoProductItem("med_163", "Clemastine", "Tavegyl 1mg", "Tablet", "1mg", "Antihistamines & Allergy", 26.00, 80, 430),

        // ====================================================================
        // 4. GASTROINTESTINAL & DIGESTIVE (23 items)
        // ====================================================================
        DemoProductItem("med_164", "Esomeprazole", "Nexium 40mg", "Tablet", "40mg", "Gastrointestinal & PPI", 148.00, 5, 540), // Low stock demo
        DemoProductItem("med_165", "Esomeprazole", "Nexium 20mg", "Tablet", "20mg", "Gastrointestinal & PPI", 98.00, 120, 520),
        DemoProductItem("med_166", "Esomeprazole", "Ezogast 40mg", "Capsule", "40mg", "Gastrointestinal & PPI", 62.00, 140, 480),
        DemoProductItem("med_167", "Pantoprazole", "Controloc 40mg", "Tablet", "40mg", "Gastrointestinal & PPI", 112.00, 160, 560),
        DemoProductItem("med_168", "Pantoprazole", "Controloc 20mg", "Tablet", "20mg", "Gastrointestinal & PPI", 74.00, 115, 530),
        DemoProductItem("med_169", "Pantoprazole", "Zurcal 40mg", "Tablet", "40mg", "Gastrointestinal & PPI", 68.00, 130, 490),
        DemoProductItem("med_170", "Omeprazole", "Gastrazole 40mg", "Capsule", "40mg", "Gastrointestinal & PPI", 48.00, 175, 460),
        DemoProductItem("med_171", "Omeprazole", "Omez 20mg", "Capsule", "20mg", "Gastrointestinal & PPI", 32.00, 190, 470),
        DemoProductItem("med_172", "Famotidine", "Antodine 40mg", "Tablet", "40mg", "Gastrointestinal & H2 Blockers", 34.00, 150, 500),
        DemoProductItem("med_173", "Domperidone", "Motilium 10mg", "Tablet", "10mg", "Antiemetic & Prokinetic", 46.00, 140, 520),
        DemoProductItem("med_174", "Domperidone", "Gastromotil 10mg", "Tablet", "10mg", "Antiemetic & Prokinetic", 28.00, 110, 450),
        DemoProductItem("med_175", "Metoclopramide", "Primperan 10mg", "Tablet", "10mg", "Antiemetic & Prokinetic", 18.00, 160, 480),
        DemoProductItem("med_176", "Ondansetron", "Zofran 8mg", "Tablet", "8mg", "Antiemetic & 5-HT3 Antagonist", 165.00, 40, 550),
        DemoProductItem("med_177", "Ondansetron", "Danset 4mg", "Tablet", "4mg", "Antiemetic & 5-HT3 Antagonist", 52.00, 75, 490),
        DemoProductItem("med_178", "Mebeverine HCl", "Duspatalin Retard 200mg", "Capsule", "200mg", "Antispasmodic & IBS", 78.00, 180, 510),
        DemoProductItem("med_179", "Mebeverine + Sulpiride", "Colona Tablets", "Tablet", "100mg/25mg", "Antispasmodic & IBS", 38.00, 220, 530),
        DemoProductItem("med_180", "Mebeverine HCl", "Coloverin D", "Tablet", "Standard", "Antispasmodic & IBS", 35.00, 145, 460),
        DemoProductItem("med_181", "Nifuroxazide", "Antinal 200mg", "Capsule", "200mg", "Intestinal Antiseptic", 32.00, 260, 60), // Near-expiry demo (60 days)
        DemoProductItem("med_182", "Loperamide", "Imodium 2mg", "Capsule", "2mg", "Antidiarrheal", 36.00, 110, 490),
        DemoProductItem("med_183", "Lactulose", "Duphalac Syrup 200ml", "Syrup", "3.34g/5ml", "Laxative", 58.00, 95, 420),
        DemoProductItem("med_184", "Simethicone", "Disflatyl Chewable", "Tablet", "40mg", "Antiflatulent", 24.00, 190, 550),
        DemoProductItem("med_185", "Hyoscine Butylbromide", "Buscopan 10mg", "Tablet", "10mg", "Antispasmodic", 32.00, 210, 480),
        DemoProductItem("med_186", "Antacid Suspension", "Epicogel 150ml Suspension", "Liquid", "Standard", "Antacids", 22.50, 140, 430),

        // ====================================================================
        // 5. CARDIOVASCULAR & LIPID REGULATORS (24 items)
        // ====================================================================
        DemoProductItem("med_187", "Bisoprolol Fumarate", "Concor 5mg Plus", "Tablet", "5mg/12.5mg", "Cardiovascular & Antihypertensive", 68.00, 230, 540),
        DemoProductItem("med_188", "Bisoprolol Fumarate", "Concor 5mg", "Tablet", "5mg", "Cardiovascular & Antihypertensive", 60.00, 280, 520),
        DemoProductItem("med_189", "Bisoprolol Fumarate", "Concor 10mg", "Tablet", "10mg", "Cardiovascular & Antihypertensive", 78.00, 3, 560), // Low stock demo
        DemoProductItem("med_190", "Bisoprolol Fumarate", "Bisocard 5mg", "Tablet", "5mg", "Cardiovascular & Antihypertensive", 32.00, 160, 470),
        DemoProductItem("med_191", "Amlodipine Besylate", "Norvasc 5mg", "Tablet", "5mg", "Cardiovascular & Calcium Blocker", 54.00, 170, 510),
        DemoProductItem("med_192", "Amlodipine Besylate", "Norvasc 10mg", "Tablet", "10mg", "Cardiovascular & Calcium Blocker", 76.00, 130, 530),
        DemoProductItem("med_193", "Amlodipine Besylate", "Amlodip 5mg", "Tablet", "5mg", "Cardiovascular & Calcium Blocker", 28.00, 140, 460),
        DemoProductItem("med_194", "Amlodipine + Valsartan", "Exforge 5/160mg", "Tablet", "5mg/160mg", "Cardiovascular & Combination", 152.00, 85, 550),
        DemoProductItem("med_195", "Amlodipine + Valsartan", "Exforge 10/160mg", "Tablet", "10mg/160mg", "Cardiovascular & Combination", 168.00, 70, 540),
        DemoProductItem("med_196", "Valsartan", "Tareg 80mg", "Tablet", "80mg", "Cardiovascular & ARB", 72.00, 110, 490),
        DemoProductItem("med_197", "Valsartan", "Disartan 160mg", "Capsule", "160mg", "Cardiovascular & ARB", 56.00, 95, 480),
        DemoProductItem("med_198", "Losartan Potassium", "Cozaar 50mg", "Tablet", "50mg", "Cardiovascular & ARB", 65.00, 125, 520),
        DemoProductItem("med_199", "Candesartan Cilexetil", "Atacand 16mg", "Tablet", "16mg", "Cardiovascular & ARB", 88.00, 90, 540),
        DemoProductItem("med_200", "Telmisartan", "Micardis 80mg", "Tablet", "80mg", "Cardiovascular & ARB", 110.00, 65, 560),
        DemoProductItem("med_201", "Ramipril", "Tritace 5mg", "Tablet", "5mg", "Cardiovascular & ACE Inhibitor", 58.00, 120, 500),
        DemoProductItem("med_202", "Enalapril Maleate", "Ezapril 10mg", "Tablet", "10mg", "Cardiovascular & ACE Inhibitor", 26.00, 140, 450),
        DemoProductItem("med_203", "Captopril", "Capoten 25mg", "Tablet", "25mg", "Cardiovascular & ACE Inhibitor", 24.00, 190, 470),
        DemoProductItem("med_204", "Atorvastatin Calcium", "Lipitor 20mg", "Tablet", "20mg", "Cardiovascular & Statins", 125.00, 110, 580),
        DemoProductItem("med_205", "Atorvastatin Calcium", "Lipitor 40mg", "Tablet", "40mg", "Cardiovascular & Statins", 165.00, 5, 560), // Low stock demo
        DemoProductItem("med_206", "Atorvastatin Calcium", "Ator 20mg", "Tablet", "20mg", "Cardiovascular & Statins", 58.00, 220, 510),
        DemoProductItem("med_207", "Rosuvastatin Calcium", "Crestor 10mg", "Tablet", "10mg", "Cardiovascular & Statins", 145.00, 80, 570),
        DemoProductItem("med_208", "Rosuvastatin Calcium", "Rosutor 20mg", "Tablet", "20mg", "Cardiovascular & Statins", 72.00, 130, 490),
        DemoProductItem("med_209", "Clopidogrel Bisulfate", "Plavix 75mg", "Tablet", "75mg", "Cardiovascular & Antiplatelet", 185.00, 90, 590),
        DemoProductItem("med_210", "Furosemide", "Lasix 40mg", "Tablet", "40mg", "Cardiovascular & Diuretics", 22.00, 260, 510),

        // ====================================================================
        // 6. DIABETES & ENDOCRINE (21 items)
        // ====================================================================
        DemoProductItem("med_211", "Metformin HCl", "Glucophage 500mg", "Tablet", "500mg", "Antidiabetic & Biguanides", 27.50, 310, 540),
        DemoProductItem("med_212", "Metformin HCl", "Glucophage 1000mg", "Tablet", "1000mg", "Antidiabetic & Biguanides", 42.00, 280, 560),
        DemoProductItem("med_213", "Metformin HCl", "Glucophage XR 1000mg", "Tablet", "1000mg", "Antidiabetic & Biguanides", 54.00, 190, 530),
        DemoProductItem("med_214", "Metformin HCl", "Cidophage 850mg", "Tablet", "850mg", "Antidiabetic & Biguanides", 20.00, 250, 490),
        DemoProductItem("med_215", "Glimepiride", "Amaryl 2mg", "Tablet", "2mg", "Antidiabetic & Sulfonylureas", 46.00, 180, 520),
        DemoProductItem("med_216", "Glimepiride", "Amaryl 3mg", "Tablet", "3mg", "Antidiabetic & Sulfonylureas", 58.00, 150, 510),
        DemoProductItem("med_217", "Glimepiride", "Glimepir 4mg", "Tablet", "4mg", "Antidiabetic & Sulfonylureas", 36.00, 110, 470),
        DemoProductItem("med_218", "Gliclazide", "Diamicron MR 60mg", "Tablet", "60mg", "Antidiabetic & Sulfonylureas", 64.00, 140, 530),
        DemoProductItem("med_219", "Gliclazide", "Diamicron MR 30mg", "Tablet", "30mg", "Antidiabetic & Sulfonylureas", 44.00, 120, 500),
        DemoProductItem("med_220", "Sitagliptin Phosphate", "Januvia 100mg", "Tablet", "100mg", "Antidiabetic & DPP-4 Inhibitor", 220.00, 7, 570), // Low stock demo
        DemoProductItem("med_221", "Sitagliptin + Metformin", "Janumet 50/1000mg", "Tablet", "50mg/1000mg", "Antidiabetic & Combination", 235.00, 45, 550),
        DemoProductItem("med_222", "Vildagliptin", "Galvus 50mg", "Tablet", "50mg", "Antidiabetic & DPP-4 Inhibitor", 175.00, 60, 540),
        DemoProductItem("med_223", "Vildagliptin + Metformin", "Galvus Met 50/1000mg", "Tablet", "50mg/1000mg", "Antidiabetic & Combination", 195.00, 50, 530),
        DemoProductItem("med_224", "Empagliflozin", "Jardiance 10mg", "Tablet", "10mg", "Antidiabetic & SGLT2 Inhibitor", 310.00, 35, 600),
        DemoProductItem("med_225", "Empagliflozin", "Jardiance 25mg", "Tablet", "25mg", "Antidiabetic & SGLT2 Inhibitor", 345.00, 30, 590),
        DemoProductItem("med_226", "Dapagliflozin", "Forxiga 10mg", "Tablet", "10mg", "Antidiabetic & SGLT2 Inhibitor", 295.00, 40, 580),
        DemoProductItem("med_227", "Insulin Glargine", "Lantus SoloStar 100 IU/ml", "Injection", "100 IU/ml", "Antidiabetic & Insulins", 420.00, 25, 360),
        DemoProductItem("med_228", "Insulin Aspart", "NovoRapid FlexPen 100 U/ml", "Injection", "100 U/ml", "Antidiabetic & Insulins", 390.00, 28, 380),
        DemoProductItem("med_229", "Levothyroxine Sodium", "Euthyrox 50mcg", "Tablet", "50mcg", "Endocrine & Thyroid", 42.00, 160, 520),
        DemoProductItem("med_230", "Levothyroxine Sodium", "Euthyrox 100mcg", "Tablet", "100mcg", "Endocrine & Thyroid", 55.00, 140, 540),
        DemoProductItem("med_231", "Carbimazole", "Carbimazole 5mg", "Tablet", "5mg", "Endocrine & Antithyroid", 32.00, 85, 480),

        // ====================================================================
        // 7. RESPIRATORY & PULMONOLOGY (18 items)
        // ====================================================================
        DemoProductItem("med_232", "Salbutamol Sulfate", "Ventolin Inhaler 100mcg", "Inhaler", "100mcg/dose", "Respiratory & Bronchodilators", 52.00, 2, 490), // Low stock demo
        DemoProductItem("med_233", "Salbutamol Sulfate", "Ventolin Syrup 120ml", "Syrup", "2mg/5ml", "Respiratory & Bronchodilators", 24.00, 110, 440),
        DemoProductItem("med_234", "Salbutamol", "Farcolin Respirator Solution", "Liquid", "0.5%", "Respiratory & Bronchodilators", 22.00, 95, 460),
        DemoProductItem("med_235", "Budesonide", "Pulmicort 0.5mg/2ml Respules", "Liquid", "0.5mg/2ml", "Respiratory & Corticosteroids", 185.00, 45, 480),
        DemoProductItem("med_236", "Fluticasone + Salmeterol", "Seretide Diskus 250mcg", "Inhaler", "50/250mcg", "Respiratory & Inhalers", 240.00, 35, 520),
        DemoProductItem("med_237", "Fluticasone + Salmeterol", "Seretide Diskus 500mcg", "Inhaler", "50/500mcg", "Respiratory & Inhalers", 280.00, 28, 510),
        DemoProductItem("med_238", "Montelukast Sodium", "Singulair 10mg", "Tablet", "10mg", "Respiratory & Leukotriene Blocker", 160.00, 50, 550),
        DemoProductItem("med_239", "Montelukast Sodium", "Sedokast 10mg", "Tablet", "10mg", "Respiratory & Leukotriene Blocker", 75.00, 85, 490),
        DemoProductItem("med_240", "Montelukast Sodium", "Delmonkast 5mg Chewable", "Tablet", "5mg", "Respiratory & Leukotriene Blocker", 58.00, 70, 470),
        DemoProductItem("med_241", "Ipratropium Bromide", "Atrovent 250mcg Unit Dose", "Liquid", "250mcg", "Respiratory & Anticholinergics", 72.00, 60, 460),
        DemoProductItem("med_242", "Ambroxol HCl", "Mucosolvan 30mg", "Tablet", "30mg", "Respiratory & Mucolytics", 34.00, 130, 500),
        DemoProductItem("med_243", "Ambroxol HCl", "Ambroxol Syrup 100ml", "Syrup", "15mg/5ml", "Respiratory & Mucolytics", 18.50, 140, 430),
        DemoProductItem("med_244", "Carbocisteine", "Mucoplex 375mg", "Capsule", "375mg", "Respiratory & Mucolytics", 26.00, 115, 450),
        DemoProductItem("med_245", "Acetylcysteine", "ACC 600mg Effervescent", "Tablet", "600mg", "Respiratory & Mucolytics", 62.00, 170, 520),
        DemoProductItem("med_246", "Acetylcysteine", "Fluimucil 200mg Sachets", "Powder", "200mg", "Respiratory & Mucolytics", 44.00, 125, 480),
        DemoProductItem("med_247", "Herbal Cough Complex", "Bronchicum Elixir 100ml", "Syrup", "Herbal", "Respiratory & Cough Syrups", 48.00, 180, 410),
        DemoProductItem("med_248", "Ivy Leaf Extract", "Prospan Cough Syrup 100ml", "Syrup", "Dried Ivy Leaf", "Respiratory & Cough Syrups", 65.00, 150, 430),
        DemoProductItem("med_249", "Butamirate Citrate", "Sinecod Drops 20ml", "Liquid", "5mg/ml", "Respiratory & Antitussives", 38.00, 90, 460),

        // ====================================================================
        // 8. VITAMINS, MINERALS & HEMATOLOGY (22 items)
        // ====================================================================
        DemoProductItem("med_250", "Vitamin B Complex", "Milga Advance Tablets", "Tablet", "High Potency", "Vitamins & Minerals", 78.00, 6, 540), // Low stock demo
        DemoProductItem("med_251", "Vitamin B Complex", "Neurobion Ampoules", "Injection", "B1+B6+B12", "Vitamins & Minerals", 42.00, 210, 490),
        DemoProductItem("med_252", "Vitamin B Complex", "Neurobion Coated Tablets", "Tablet", "B1+B6+B12", "Vitamins & Minerals", 36.00, 190, 520),
        DemoProductItem("med_253", "Vitamin B Complex", "Neuroton Ampoules", "Injection", "High Potency", "Vitamins & Minerals", 48.00, 160, 470),
        DemoProductItem("med_254", "Vitamin B12", "Betolvex 1mg Ampoules", "Injection", "1mg/ml", "Vitamins & Minerals", 45.00, 140, 510),
        DemoProductItem("med_255", "Cholecalciferol", "Devarol-S 200,000 IU Ampoule", "Injection", "200,000 IU", "Vitamins & Minerals", 25.00, 310, 580),
        DemoProductItem("med_256", "Vitamin D3", "Vidrop Oral Drops 15ml", "Liquid", "2800 IU/ml", "Vitamins & Minerals", 19.50, 340, 520),
        DemoProductItem("med_257", "Vitamin D3", "Ossofortin 10,000 IU", "Tablet", "10,000 IU", "Vitamins & Minerals", 52.00, 180, 560),
        DemoProductItem("med_258", "Vitamin C", "C-Retard 500mg Sustained Release", "Capsule", "500mg", "Vitamins & Minerals", 32.00, 240, 500),
        DemoProductItem("med_259", "Vitamin C + Paracetamol", "Cevamol Effervescent Tablets", "Tablet", "Standard", "Vitamins & Cold Formulations", 26.00, 280, 480),
        DemoProductItem("med_260", "Zinc Gluconate", "Zincron 50mg", "Capsule", "50mg", "Vitamins & Minerals", 28.00, 190, 530),
        DemoProductItem("med_261", "Calcium + Vitamin D3", "Osteocare Tablets", "Tablet", "Combination", "Vitamins & Bone Health", 72.00, 140, 550),
        DemoProductItem("med_262", "Calcium Carbonate", "Calcimate 500mg", "Tablet", "500mg", "Vitamins & Bone Health", 35.00, 120, 490),
        DemoProductItem("med_263", "Ferrous Fumarate + Folic", "Feroglobin B12 Capsules", "Capsule", "Combination", "Hematology & Hematinics", 68.00, 160, 520),
        DemoProductItem("med_264", "Ferrous Fumarate", "Fumafer 200mg", "Tablet", "200mg", "Hematology & Hematinics", 22.00, 180, 470),
        DemoProductItem("med_265", "Folic Acid", "Folic Acid 5mg", "Tablet", "5mg", "Vitamins & Minerals", 15.00, 320, 600),
        DemoProductItem("med_266", "Omega-3 Fatty Acids", "Omega 3 Plus Capsules", "Capsule", "1000mg", "Supplements & Lipids", 85.00, 130, 540),
        DemoProductItem("med_267", "Alpha-Lipoic Acid", "Thiotacid 600mg", "Tablet", "600mg", "Supplements & Neuropathy", 92.00, 110, 560),
        DemoProductItem("med_268", "Multivitamin Complex", "Kerovit Soft Gelatin Capsules", "Capsule", "Comprehensive", "Vitamins & Tonics", 74.00, 150, 530),
        DemoProductItem("med_269", "Multivitamin Complex", "Vitamount for Men/Women", "Capsule", "Complete", "Vitamins & Tonics", 48.00, 120, 490),
        DemoProductItem("med_270", "Coenzyme Q10", "CoQ10 100mg Capsules", "Capsule", "100mg", "Supplements & Antioxidants", 120.00, 55, 570),
        DemoProductItem("med_271", "Iron + Vitamin Complex", "Haemoton Capsules", "Capsule", "Complex", "Hematology & Hematinics", 38.00, 140, 480),

        // ====================================================================
        // 9. TOPICALS, ANTISEPTICS & DERMATOLOGY (20 items)
        // ====================================================================
        DemoProductItem("med_272", "Fusidic Acid + Betamethasone", "Fucicort Lipid Cream 30g", "Cream", "2% + 0.1%", "Dermatology & Antibiotic Creams", 52.00, 8, 480), // Low stock demo
        DemoProductItem("med_273", "Fusidic Acid", "Fucidin 2% Cream 30g", "Cream", "2%", "Dermatology & Antibiotic Creams", 44.00, 190, 510),
        DemoProductItem("med_274", "Fusidic Acid", "Fucidin 2% Ointment 30g", "Ointment", "2%", "Dermatology & Antibiotic Creams", 44.00, 170, 510),
        DemoProductItem("med_275", "Mupirocin", "Bactroban 2% Ointment 15g", "Ointment", "2%", "Dermatology & Antibiotic Creams", 48.00, 120, 470),
        DemoProductItem("med_276", "Clotrimazole", "Canesten 1% Cream 20g", "Cream", "1%", "Dermatology & Antifungals", 32.00, 150, 530),
        DemoProductItem("med_277", "Clotrimazole", "Dermatin 1% Powder", "Powder", "1%", "Dermatology & Antifungals", 24.00, 130, 490),
        DemoProductItem("med_278", "Ketoconazole", "Nizoral 2% Cream 30g", "Cream", "2%", "Dermatology & Antifungals", 46.00, 110, 520),
        DemoProductItem("med_279", "Ketoconazole", "Nizoral 2% Anti-Dandruff Shampoo", "Liquid", "2%", "Dermatology & Medicated Shampoos", 72.00, 95, 540),
        DemoProductItem("med_280", "Miconazole Nitrate", "Daktarin 2% Oral Gel 40g", "Gel", "2%", "Antifungal & Oral Gels", 38.00, 140, 460),
        DemoProductItem("med_281", "Miconazole Nitrate", "Miconaz 2% Cream 20g", "Cream", "2%", "Dermatology & Antifungals", 20.00, 160, 480),
        DemoProductItem("med_282", "Terbinafine HCl", "Lamisil 1% Cream 15g", "Cream", "1%", "Dermatology & Antifungals", 56.00, 85, 550),
        DemoProductItem("med_283", "Hydrocortisone", "Microcort 1% Cream 15g", "Cream", "1%", "Dermatology & Corticosteroids", 18.00, 140, 460),
        DemoProductItem("med_284", "Betamethasone", "Betnovate Cream 30g", "Cream", "0.1%", "Dermatology & Corticosteroids", 26.00, 160, 490),
        DemoProductItem("med_285", "Betamethasone", "Diprosone Ointment 30g", "Ointment", "0.05%", "Dermatology & Corticosteroids", 28.00, 135, 500),
        DemoProductItem("med_286", "Povidone-Iodine", "Betadine Antiseptic Solution 120ml", "Liquid", "10%", "Antiseptics & Wound Care", 42.00, 210, 600),
        DemoProductItem("med_287", "Povidone-Iodine", "Betadine Antiseptic Ointment 60g", "Ointment", "10%", "Antiseptics & Wound Care", 36.00, 180, 580),
        DemoProductItem("med_288", "Silver Sulfadiazine", "Dermazin 1% Burn Cream 50g", "Cream", "1%", "Antiseptics & Burn Therapy", 34.00, 160, 520),
        DemoProductItem("med_289", "D-Panthenol", "Panthenol 5% Skin Cream 50g", "Cream", "5%", "Dermatology & Skin Barrier", 28.00, 290, 570),
        DemoProductItem("med_290", "D-Panthenol + Chlorhexidine", "Healosol Skin Spray 100ml", "Spray", "Standard", "Antiseptics & Healing", 38.00, 110, 480),
        DemoProductItem("med_291", "Zinc Oxide + Olive Oil", "Zincoderm Barrier Cream 50g", "Cream", "Zinc Oxide", "Dermatology & Diaper Rash", 22.00, 230, 540),

        // ====================================================================
        // 10. OPHTHALMOLOGY, ENT & PHARMACY OTC ESSENTIALS (16 items)
        // ====================================================================
        DemoProductItem("med_292", "Carboxymethylcellulose", "Tears Guard Eye Drops 15ml", "Liquid", "0.5%", "Ophthalmology & Artificial Tears", 32.00, 180, 460),
        DemoProductItem("med_293", "Sodium Hyaluronate", "Systane Ultra Lubricant Drops 10ml", "Liquid", "High Quality", "Ophthalmology & Artificial Tears", 115.00, 70, 510),
        DemoProductItem("med_294", "Tobramycin + Dexamethasone", "Tobradex Eye Drops 5ml", "Liquid", "0.3% + 0.1%", "Ophthalmology & Anti-infective", 54.00, 130, 430),
        DemoProductItem("med_295", "Olopatadine HCl", "Pataday 0.2% Eye Drops 2.5ml", "Liquid", "0.2%", "Ophthalmology & Anti-Allergy", 88.00, 60, 490),
        DemoProductItem("med_296", "Oxymetazoline HCl", "Otrivin Adult 0.1% Nasal Drops 10ml", "Liquid", "0.1%", "ENT & Nasal Decongestants", 18.00, 260, 470),
        DemoProductItem("med_297", "Xylometazoline HCl", "Otrivin Pediatric 0.05% Drops 10ml", "Liquid", "0.05%", "ENT & Nasal Decongestants", 16.50, 210, 480),
        DemoProductItem("med_298", "Sea Water Saline", "Physiomer Normal Jet Nasal Spray 135ml", "Spray", "Isotonic Sea Water", "ENT & Nasal Hygiene", 135.00, 80, 590),
        DemoProductItem("med_299", "Sodium Chloride", "Free-Nose Pediatric Saline Spray", "Spray", "0.9% NaCl", "ENT & Nasal Hygiene", 45.00, 140, 520),
        DemoProductItem("med_300", "Chlorhexidine Gluconate", "Orovex Extra Mint Mouthwash 250ml", "Liquid", "0.12%", "Dental & Oral Hygiene", 36.00, 190, 540),
        DemoProductItem("med_301", "Chlorhexidine Gluconate", "Hexitol Antiseptic Mouthwash 125ml", "Liquid", "0.1%", "Dental & Oral Hygiene", 22.00, 170, 510),
        DemoProductItem("med_302", "Povidone-Iodine", "Betadine Antiseptic Gargle 120ml", "Liquid", "1%", "ENT & Antiseptic Gargles", 38.00, 150, 560),
        DemoProductItem("med_303", "Oral Rehydration Salts", "Rehydran Electrolyte Sachets (Pack of 10)", "Powder", "WHO Formula", "OTC & Electrolytes", 18.00, 310, 620),
        DemoProductItem("med_304", "Antiseptic Throat Lozenges", "Strepsils Honey & Lemon Lozenges (24s)", "Lozenge", "Standard", "ENT & Throat Care", 48.00, 220, 500),
        DemoProductItem("med_305", "Antiseptic Throat Lozenges", "Larypro Lozenges (Pack of 20)", "Lozenge", "Lysozyme + Dequalinium", "ENT & Throat Care", 28.00, 190, 480),
        DemoProductItem("med_306", "Benzocaine + Antiseptic", "Bradosol Mint Throat Lozenges", "Lozenge", "Standard", "ENT & Throat Care", 22.00, 160, 460),
        DemoProductItem("med_307", "Ear Drops Complex", "Otocalm Ear Drops 15ml", "Liquid", "Analgesic & Decongestant", "ENT & Ear Drops", 20.00, 140, 450)
    )

    /**
     * Seeds the demo distributor "Cairo Drugs", its parent User row, all medications,
     * and their associated ProductListingEntity rows within a single atomic Room transaction.
     * 
     * Idempotent: If "Cairo Drugs" already exists in the database, skips duplicate insertions.
     */
    suspend fun seedDemoDistributorCatalog(database: PharmaChainDatabase): Boolean = withContext(Dispatchers.IO) {
        try {
            // Idempotent check: Check if Cairo Drugs profile already exists
            val existingDistributor = database.distributorDao().getProfileById(DEMO_DISTRIBUTOR_ID)
            if (existingDistributor != null) {
                return@withContext true
            }

            database.withTransaction {
                val now = System.currentTimeMillis()

                // Step 1: Insert Parent UserEntity row FIRST (Foreign Key parent of DistributorProfile)
                val userEntity = UserEntity(
                    id = DEMO_USER_ID,
                    role = Role.DISTRIBUTOR,
                    fullName = "Cairo Drugs Distribution Co.",
                    email = "demo@cairodrugs.eg",
                    phone = "+20 2 2790 0000",
                    createdAt = now
                )
                database.userDao().insertUser(userEntity)

                // Step 2: Insert DistributorProfileEntity SECOND (Foreign Key child of UserEntity, parent of ProductListing)
                val distributorEntity = DistributorProfileEntity(
                    id = DEMO_DISTRIBUTOR_ID,
                    userId = DEMO_USER_ID,
                    companyName = DEMO_DISTRIBUTOR_NAME,
                    taxRegistrationId = DEMO_TAX_REG_ID,
                    reputationScore = DEMO_REPUTATION_SCORE,
                    totalOrdersFulfilled = DEMO_TOTAL_ORDERS,
                    defaultCommissionRatePct = DEMO_COMMISSION_RATE,
                    isVerified = true
                )
                database.distributorDao().insertProfile(distributorEntity)

                // Step 3: Insert MedicationEntities
                val medicationEntities = DEMO_MEDICATIONS.map { item ->
                    MedicationEntity(
                        id = item.id,
                        genericName = item.genericName,
                        brandName = item.brandName,
                        form = item.form,
                        strength = item.strength,
                        category = item.category,
                        imageUrl = null
                    )
                }
                database.medicationDao().insertMedications(medicationEntities)

                // Step 4: Insert ProductListingEntities tied to "Cairo Drugs"
                val listingEntities = DEMO_MEDICATIONS.map { item ->
                    ProductListingEntity(
                        id = "list_cairo_${item.id.removePrefix("med_")}",
                        medicationId = item.id,
                        distributorId = DEMO_DISTRIBUTOR_ID,
                        distributorName = DEMO_DISTRIBUTOR_NAME,
                        distributorReputation = DEMO_REPUTATION_SCORE,
                        price = item.priceEgp,
                        stockQuantity = item.stockQuantity,
                        expiryDate = now + (item.expiryOffsetDays.toLong() * 86400000L),
                        updatedAt = now
                    )
                }
                database.medicationDao().insertProductListings(listingEntities)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
