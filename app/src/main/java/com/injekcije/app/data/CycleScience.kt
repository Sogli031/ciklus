package com.injekcije.app.data

/**
 * Evidence-Based Cycle Science System
 *
 * Based on PubMed research including:
 * - Bhasin S, et al. Am J Physiol Endocrinol Metab 2001;281:E1172-81 (Testosterone dose-response)
 *   PMID: 11701431
 * - Sattler FR, et al. JCEM 1999;84:1268-1276 (Nandrolone studies)
 *   PMID: 10199766
 * - Grunfeld C, et al. J Acquir Immune Defic Syndr 2006;41:304-14 (Oxandrolone HIV wasting)
 *   PMID: 16540931
 * - BJU International 2026 (PCT protocols - CC and hCG recovery rates)
 * - Kiracofe et al., Burns 2019 (Oxandrolone hepatotoxicity)
 *   PMID: 30545697
 * - Schroeder ET, et al. J Gerontol A Biol Sci Med Sci 2005;60:1586-92 (Oxandrolone LBM gains)
 *   PMID: 16424293
 * - Basaria S, et al. NEJM 2010;363:109-122 (AAS adverse events in elderly)
 *   PMID: 20592293
 * - Kanayama G, et al. Drug Alcohol Depend 2009;98:1-12 (Long-term AAS cardiac effects)
 *   PMID: 18599224
 */

// =============================================================================
// SCIENTIFIC DATA ENUMS
// =============================================================================

/**
 * Level of evidence from clinical research
 */
enum class EvidenceLevel(val displayName: String) {
    RCT_STRONG("Snažni RCT dokazi"),
    RCT_MODERATE("Umereni RCT dokazi"),
    OBSERVATIONAL("Opservacione studije"),
    ANIMAL_ONLY("Samo životinjske studije"),
    ANECDOTAL("Anegdotalno/bez studija")
}

/**
 * Aromatization potential - how much a compound converts to estrogen
 */
enum class AromatizationLevel(
    val displayName: String,
    val aiLikelihood: String
) {
    NONE("Bez aromatizacije", "AI nije potreban"),
    LOW("Niska", "AI retko potreban"),
    MODERATE("Umerena", "AI možda potreban"),
    HIGH("Visoka", "AI verovatno potreban"),
    VERY_HIGH("Veoma visoka", "AI skoro sigurno potreban")
}

/**
 * Hepatotoxicity risk level
 */
enum class HepatotoxicityLevel(val displayName: String) {
    MINIMAL("Minimalna"),
    LOW("Niska"),
    MODERATE("Umerena"),
    HIGH("Visoka"),
    VERY_HIGH("Veoma visoka (17-aa)")
}

/**
 * Prolactin increase risk (relevant for 19-nors)
 */
enum class ProlactinRisk(val displayName: String) {
    NONE("Bez rizika"),
    LOW("Nizak rizik"),
    MODERATE("Umeren rizik"),
    HIGH("Visok rizik - praćenje potrebno")
}

/**
 * Cardiovascular risk level
 */
enum class CardiovascularRisk(val displayName: String) {
    LOW("Nizak"),
    MODERATE("Umeren"),
    HIGH("Visok"),
    VERY_HIGH("Veoma visok")
}

// =============================================================================
// COMPOUND SCIENTIFIC DATA
// =============================================================================

/**
 * Scientific data for each compound based on PubMed research.
 * Contains optimal doses, side effect profiles, and evidence levels.
 */
data class CompoundScientificData(
    val compoundType: CompoundType,

    // Dosing (from RCT studies)
    val optimalDoseMgWeek: Int?,           // Optimal dose from studies (if known)
    val maxStudiedDoseMgWeek: Int?,        // Max dose studied in RCTs
    val minEffectiveDoseMgWeek: Int?,      // Minimum effective dose
    val doseResponseNote: String,           // Note about dose-response

    // Side effect profile
    val aromatization: AromatizationLevel,
    val hepatotoxicity: HepatotoxicityLevel,
    val prolactinRisk: ProlactinRisk,
    val cardiovascularRisk: CardiovascularRisk,

    // Evidence
    val evidenceLevel: EvidenceLevel,
    val keyStudies: List<String>,          // Key study citations

    // Specific warnings
    val specificWarnings: List<String>,

    // Bloodwork recommendations
    val bloodworkMarkers: List<String>,    // What to test
    val bloodworkFrequencyWeeks: Int       // How often to test
)

/**
 * Repository of scientific data for all compounds
 */
object CompoundScienceRepository {

    private val data = mapOf(
        // PubMed: Bhasin S, et al. Am J Physiol Endocrinol Metab 2001;281:E1172-81 (PMID 11701431)
        CompoundType.TESTOSTERONE to CompoundScientificData(
            compoundType = CompoundType.TESTOSTERONE,
            optimalDoseMgWeek = 125,       // Tačka infleksije iz Bhasin 2001
            maxStudiedDoseMgWeek = 600,    // Maksimalna doza u RCT-u
            minEffectiveDoseMgWeek = 50,   // Minimalna suprafiziološka doza
            doseResponseNote = "125mg/ned je tačka infleksije: ~40% maksimalnih dobitaka uz <10% neželjenih efekata. " +
                    "Iznad 125mg kriva dobiti se spljošćuje dok nuspojave rastu eksponencijalno. " +
                    "Na 600mg: +7.9kg mišićne mase (FFM); androgene nuspojave (akne, retencija) rastu sa dozom, ali studija ne daje incidencu po dozi.",
            aromatization = AromatizationLevel.HIGH,
            hepatotoxicity = HepatotoxicityLevel.MINIMAL,
            prolactinRisk = ProlactinRisk.NONE,
            cardiovascularRisk = CardiovascularRisk.MODERATE,
            evidenceLevel = EvidenceLevel.RCT_STRONG,
            keyStudies = listOf(
                "Bhasin S, et al. Am J Physiol Endocrinol Metab 2001;281:E1172-81 (PMID 11701431)"
            ),
            specificWarnings = listOf(
                "Rizik porasta hematokrita raste sa dozom (izraženo na 300-600mg) - redovno praćenje",
                "HDL pada linearno sa dozom",
                "E2 raste sa dozom - praćenje estrogena preporučeno"
            ),
            bloodworkMarkers = listOf("Testosteron", "Estradiol", "Hematokrit", "HDL/LDL", "PSA"),
            bloodworkFrequencyWeeks = 6
        ),

        // PubMed: Sattler FR, et al. JCEM 1999;84:1268-1276 (PMID: 10199766)
        CompoundType.NANDROLONE to CompoundScientificData(
            compoundType = CompoundType.NANDROLONE,
            optimalDoseMgWeek = 100,       // Optimalna doza iz Sattler 1999
            maxStudiedDoseMgWeek = 200,    // Maksimalna proučavana doza
            minEffectiveDoseMgWeek = 50,   // Minimalna efektivna doza
            doseResponseNote = "100mg/ned: +3.2kg telesne mase (DXA čista masa ~3.9kg) u HIV studijama. " +
                    "Iznad 100mg rizik/korist opada. " +
                    "19-nor metaboliti se mogu detektovati ~9-12 meseci!",
            aromatization = AromatizationLevel.LOW,
            hepatotoxicity = HepatotoxicityLevel.LOW,
            prolactinRisk = ProlactinRisk.HIGH,
            cardiovascularRisk = CardiovascularRisk.HIGH,
            evidenceLevel = EvidenceLevel.RCT_MODERATE,
            keyStudies = listOf(
                "Sattler FR, et al. JCEM 1999"
            ),
            specificWarnings = listOf(
                "⚠️ 19-nor: metaboliti se detektuju ~9-12 meseci",
                "⚠️ Prolaktin monitoring OBAVEZAN (Cabergoline pri ruci)",
                "⚠️ Snažnija HPTA supresija od testosterona",
                "⚠️ Ne koristiti ako je fertilitet prioritet u skorijoj budućnosti"
            ),
            bloodworkMarkers = listOf("Testosteron", "Estradiol", "Prolaktin", "Progesteron", "HDL/LDL"),
            bloodworkFrequencyWeeks = 4
        ),

        // Nema humanih RCT studija - samo životinjski podaci (veterinarski preparat)
        // Trenbolon acetat je odobren samo za veterinarsku upotrebu (Finaplix-H)
        CompoundType.TRENBOLONE to CompoundScientificData(
            compoundType = CompoundType.TRENBOLONE,
            optimalDoseMgWeek = null, // Nema humanih RCT podataka
            maxStudiedDoseMgWeek = null,
            minEffectiveDoseMgWeek = null,
            doseResponseNote = "⚠️ NEMA HUMANIH RCT STUDIJA! " +
                    "Svi podaci iz životinjskih studija. " +
                    "5x androgena potentnost testosterona (in vitro). " +
                    "Korišćenje = preuzimanje potpuno nepoznatog rizika.",
            aromatization = AromatizationLevel.NONE,
            hepatotoxicity = HepatotoxicityLevel.MODERATE,
            prolactinRisk = ProlactinRisk.HIGH,
            cardiovascularRisk = CardiovascularRisk.VERY_HIGH,
            evidenceLevel = EvidenceLevel.ANIMAL_ONLY,
            keyStudies = listOf(
                "Nema humanih RCT studija",
                "Životinjske studije pokazuju izuzetnu potentnost"
            ),
            specificWarnings = listOf(
                "⚠️ NEMA HUMANIH STUDIJA - svi rizici nepoznati",
                "⚠️ 19-nor: metaboliti se detektuju godinu+ dana",
                "⚠️ Izuzetno supresivan - možda najteži oporavak",
                "⚠️ Prolaktin monitoring OBAVEZAN",
                "⚠️ Poznate nuspojave: noćno znojenje, nesanica, agresija, kardio stres"
            ),
            bloodworkMarkers = listOf("Testosteron", "Prolaktin", "HDL/LDL", "Jetreni enzimi", "Krvni pritisak"),
            bloodworkFrequencyWeeks = 4
        ),

        // Nema humanih RCT studija - veterinarski preparat (Equipoise)
        // Poluživot undecylenate estera: ~14 dana (farmakokinetički podaci iz veterine)
        CompoundType.BOLDENONE to CompoundScientificData(
            compoundType = CompoundType.BOLDENONE,
            optimalDoseMgWeek = null,
            maxStudiedDoseMgWeek = null,
            minEffectiveDoseMgWeek = null,
            doseResponseNote = "⚠️ NEMA HUMANIH RCT STUDIJA! " +
                    "Dugo poluvreme undecylenate estera (~14 dana). " +
                    "Veterinarski steroid - minimalni podaci o bezbednosti kod ljudi.",
            aromatization = AromatizationLevel.LOW,
            hepatotoxicity = HepatotoxicityLevel.LOW,
            prolactinRisk = ProlactinRisk.NONE,
            cardiovascularRisk = CardiovascularRisk.MODERATE,
            evidenceLevel = EvidenceLevel.ANIMAL_ONLY,
            keyStudies = listOf(
                "Nema humanih RCT studija",
                "Veterinarska primena dokumentovana"
            ),
            specificWarnings = listOf(
                "⚠️ Nema humanih podataka o bezbednosti",
                "Dugo poluvreme - čekaj 6-8 nedelja pre PCT",
                "Može povećati eritropoezu (hematokrit)"
            ),
            bloodworkMarkers = listOf("Testosteron", "Estradiol", "Hematokrit", "HDL/LDL"),
            bloodworkFrequencyWeeks = 6
        ),

        // Drostanolon propionat - originalno za karcinom dojke
        // Ograničeni savremeni klinički podaci; bazira se na istorijskoj upotrebi
        CompoundType.MASTERON to CompoundScientificData(
            compoundType = CompoundType.MASTERON,
            optimalDoseMgWeek = null,
            maxStudiedDoseMgWeek = null,
            minEffectiveDoseMgWeek = null,
            doseResponseNote = "DHT derivat - ne aromatizuje. " +
                    "Originalno razvijen za karcinom dojke. " +
                    "Blaga supresija, brzo izlazi iz sistema.",
            aromatization = AromatizationLevel.NONE,
            hepatotoxicity = HepatotoxicityLevel.LOW,
            prolactinRisk = ProlactinRisk.NONE,
            cardiovascularRisk = CardiovascularRisk.MODERATE,
            evidenceLevel = EvidenceLevel.OBSERVATIONAL,
            keyStudies = listOf(
                "Originalno kliničko korišćenje za karcinom dojke",
                "Limitirani savremeni podaci"
            ),
            specificWarnings = listOf(
                "DHT derivat - može ubrzati gubitak kose kod predisponiranih",
                "Ne aromatizuje - može 'maskirati' visok E2 simptome"
            ),
            bloodworkMarkers = listOf("Testosteron", "Estradiol", "DHT", "HDL/LDL"),
            bloodworkFrequencyWeeks = 6
        ),

        // Methenolon enanthat - istorijska klinička primena za anemiju i kaheksiju
        // Ograničeni savremeni RCT podaci
        CompoundType.PRIMOBOLAN to CompoundScientificData(
            compoundType = CompoundType.PRIMOBOLAN,
            optimalDoseMgWeek = null,
            maxStudiedDoseMgWeek = null,
            minEffectiveDoseMgWeek = null,
            doseResponseNote = "DHT derivat sa blagim anaboličkim efektom. " +
                    "Historijski korišćen za kaheksiju i anemiju. " +
                    "Relativno blaga supresija.",
            aromatization = AromatizationLevel.NONE,
            hepatotoxicity = HepatotoxicityLevel.LOW,
            prolactinRisk = ProlactinRisk.NONE,
            cardiovascularRisk = CardiovascularRisk.LOW,
            evidenceLevel = EvidenceLevel.OBSERVATIONAL,
            keyStudies = listOf(
                "Historijska klinička primena",
                "Limitirani savremeni RCT podaci"
            ),
            specificWarnings = listOf(
                "DHT derivat - potencijal za gubitak kose",
                "Blaga supresija - lakši oporavak"
            ),
            bloodworkMarkers = listOf("Testosteron", "HDL/LDL"),
            bloodworkFrequencyWeeks = 8
        ),

        // PubMed: Grunfeld C, et al. J Acquir Immune Defic Syndr 2006;41:304-14 (PMID 16540931) - Oxandrolone
        // PubMed: Schroeder ET, et al. J Gerontol A Biol Sci Med Sci 2005;60:1586-92 (PMID 16424293) - Oxandrolone
        // PubMed: Kiracofe et al., Burns 2019 (PMID 30545697) - Oxandrolone hepatotoksičnost
        // PubMed: Bhasin S, et al. NEJM 1996 (PMID: 8637535) - Testosteron
        // PubMed: Hervey GR, et al. Clin Sci 1981 (PMID 7018798) - Methandienone (Dianabol)
        // HAE studije za Stanozolol terapijske doze
        CompoundType.ORAL_17AA to CompoundScientificData(
            compoundType = CompoundType.ORAL_17AA,
            optimalDoseMgWeek = null, // Varira po komponenti
            maxStudiedDoseMgWeek = null,
            minEffectiveDoseMgWeek = null,
            doseResponseNote = """
**OXANDROLONE (Anavar):** 10-20mg/dan optimalno. Bubrežni metabolizam (28% nepromenjen u urinu) -
najsigurniji oralni. Prolazan porast transaminaza (transaminitis kod ~42% u opekotinskoj studiji - Kiracofe), reverzibilan po prestanku.
Telesna masa +1.8 kg / BCM +0.9 kg na 20mg (12 ned, HIV RCT - Grunfeld).

**OXYMETHOLONE (Anadrol):** NAJHEPATOTOKSIČNIJI! 5x ALT povišenje.
1-1.5 mg/kg/dan maksimum, 4-6 nedelja. LBM: 2.9 kg.

**STANOZOLOL (Winstrol):** FATALNI slučajevi na 50mg EOD.
HDL supresija 49-90%. Samo 0.5-2mg/dan za HAE.

**METHANDIENONE (Dianabol):** 27% pacijenata ima povišen AST.
≤25mg/dan, 4 nedelje max. LBM: 2.3 kg/6 ned na 100mg.

**TURINABOL:** Poluživot 16h. Najizraženija HPTA supresija.
30-50mg/dan, 6 nedelja. 2 nedelje čekanja pre PCT.
            """.trimIndent(),
            aromatization = AromatizationLevel.LOW,
            hepatotoxicity = HepatotoxicityLevel.VERY_HIGH,
            prolactinRisk = ProlactinRisk.NONE,
            cardiovascularRisk = CardiovascularRisk.HIGH,
            evidenceLevel = EvidenceLevel.RCT_MODERATE,
            keyStudies = listOf(
                "Grunfeld C, et al. J Acquir Immune Defic Syndr 2006;41:304-14 (Oxandrolone HIV)",
                "Schroeder ET, et al. J Gerontol A Biol Sci Med Sci 2005 (Oxandrolone)",
                "Kiracofe et al., Burns 2019 (Oxandrolone hepatotoksičnost)",
                "Bhasin S, et al. NEJM 1996 (Testosteron)",
                "Hervey GR, et al. Clin Sci 1981 (Methandienone/Dianabol)",
                "HAE studije (Stanozolol terapijske doze)"
            ),
            specificWarnings = listOf(
                "⚠️ HEPATOTOKSIČNO - 17-alpha-alkilacija za oralnu bioraspoloživost",
                "⚠️ Ograniči na 6-8 nedelja maksimalno",
                "⚠️ Oxandrolone: izuzetak - bubrežni metabolizam, minimalna hepatotoksičnost",
                "⚠️ Oxymetholone: NAJHEPATOTOKSIČNIJI - fatalni slučajevi dokumentovani",
                "⚠️ Stanozolol: HDL pad 49-90% - najgori lipidni profil",
                "SHBG drastično pada - može maskirati laboratorijske vrednosti"
            ),
            bloodworkMarkers = listOf("ALT", "AST", "GGT", "Bilirubin", "HDL/LDL", "SHBG"),
            bloodworkFrequencyWeeks = 3
        ),

        CompoundType.UNKNOWN to CompoundScientificData(
            compoundType = CompoundType.UNKNOWN,
            optimalDoseMgWeek = null,
            maxStudiedDoseMgWeek = null,
            minEffectiveDoseMgWeek = null,
            doseResponseNote = "Nepoznata komponenta - primeni oprez.",
            aromatization = AromatizationLevel.MODERATE,
            hepatotoxicity = HepatotoxicityLevel.MODERATE,
            prolactinRisk = ProlactinRisk.LOW,
            cardiovascularRisk = CardiovascularRisk.MODERATE,
            evidenceLevel = EvidenceLevel.ANECDOTAL,
            keyStudies = emptyList(),
            specificWarnings = listOf("Nepoznata komponenta - preporučuje se oprez"),
            bloodworkMarkers = listOf("Kompletna krvna slika", "Jetreni panel", "Lipidni panel"),
            bloodworkFrequencyWeeks = 4
        )
    )

    fun getScientificData(compoundType: CompoundType): CompoundScientificData {
        return data[compoundType] ?: data[CompoundType.UNKNOWN]!!
    }

    fun getAllData(): Map<CompoundType, CompoundScientificData> = data
}

// =============================================================================
// CYCLE INSIGHTS SYSTEM
// =============================================================================

/**
 * Severity of an insight/warning
 */
enum class InsightSeverity {
    INFO,       // Informational
    TIP,        // Helpful tip
    WARNING,    // Warning - should address
    CRITICAL    // Critical - must address
}

/**
 * Category of insight
 */
enum class InsightCategory {
    DOSING,
    ESTROGEN,
    PROLACTIN,
    LIVER,
    CARDIOVASCULAR,
    PCT,
    BLOODWORK,
    DURATION,
    COMPOUND_INTERACTION,
    GENERAL
}

/**
 * A single insight/recommendation about the cycle
 */
data class CycleInsight(
    val id: String,
    val severity: InsightSeverity,
    val category: InsightCategory,
    val title: String,
    val message: String,
    val action: String? = null,   // Recommended action
    val reference: String? = null  // Study reference
)

/**
 * Complete analysis of a cycle
 */
data class CycleAnalysis(
    val insights: List<CycleInsight>,
    val overallRiskLevel: CardiovascularRisk,
    val recommendedBloodworkMarkers: Set<String>,
    val recommendedBloodworkFrequencyWeeks: Int,
    val pctRecommendation: String,
    val estimatedPctStartDays: Int
)

/**
 * Analyzes cycles and generates evidence-based insights
 */
object CycleAnalyzer {

    /**
     * Analyze a cycle and generate insights
     *
     * @param substances List of substance IDs in the cycle
     * @param weeklyDoses Map of substance ID to weekly dose in mg
     * @param cycleDurationWeeks Planned cycle duration
     * @param isFirstCycle Whether this is the user's first cycle
     */
    fun analyzeCycle(
        substances: List<String>,
        weeklyDoses: Map<String, Double> = emptyMap(),
        cycleDurationWeeks: Int = 12,
        isFirstCycle: Boolean = false
    ): CycleAnalysis {
        val insights = mutableListOf<CycleInsight>()
        val bloodworkMarkers = mutableSetOf<String>()
        var bloodworkFrequency = 8 // Default

        // Get compound types and their scientific data
        val compoundTypes = substances.map { CompoundType.fromSubstanceId(it) }.distinct()
        val scienceData = compoundTypes.map { CompoundScienceRepository.getScientificData(it) }

        // Collect all bloodwork markers
        scienceData.forEach { data ->
            bloodworkMarkers.addAll(data.bloodworkMarkers)
            if (data.bloodworkFrequencyWeeks < bloodworkFrequency) {
                bloodworkFrequency = data.bloodworkFrequencyWeeks
            }
        }

        // === DOSING INSIGHTS ===
        generateDosingInsights(substances, weeklyDoses, scienceData, insights)

        // === FIRST CYCLE INSIGHTS ===
        if (isFirstCycle) {
            generateFirstCycleInsights(compoundTypes, insights)
        }

        // === ESTROGEN INSIGHTS ===
        generateEstrogenInsights(scienceData, insights)

        // === PROLACTIN INSIGHTS ===
        generateProlactinInsights(compoundTypes, scienceData, insights)

        // === LIVER INSIGHTS ===
        generateLiverInsights(scienceData, cycleDurationWeeks, insights)

        // === CARDIOVASCULAR INSIGHTS ===
        generateCardiovascularInsights(scienceData, insights)

        // === COMPOUND INTERACTION INSIGHTS ===
        generateInteractionInsights(compoundTypes, insights)

        // === DURATION INSIGHTS ===
        generateDurationInsights(cycleDurationWeeks, insights)

        // === EVIDENCE LEVEL INSIGHTS ===
        generateEvidenceInsights(scienceData, insights)

        // === BLOODWORK REMINDER ===
        insights.add(CycleInsight(
            id = "bloodwork_schedule",
            severity = InsightSeverity.INFO,
            category = InsightCategory.BLOODWORK,
            title = "Raspored krvne slike",
            message = "Na osnovu komponenti u ciklusu, preporučeno je praćenje krvne slike svake $bloodworkFrequency nedelje.",
            action = "Testiraj: ${bloodworkMarkers.joinToString(", ")}"
        ))

        // Calculate overall risk
        val overallRisk = scienceData.maxOfOrNull { it.cardiovascularRisk } ?: CardiovascularRisk.MODERATE

        // Calculate PCT timing
        val esters = substances.map { EsterType.fromSubstanceId(it) }
        val longestWait = esters.maxOfOrNull { it.pctWaitDays } ?: 45

        // PCT recommendation
        val pctRecommendation = if (compoundTypes.any { it.requiresHCG }) {
            "hCG 1500IU 3x/ned + Clomiphene 25mg dnevno (najbolji ishod: 87.5% normozoospermija)"
        } else {
            "Clomiphene 25mg dnevno (69.2% normozoospermija)"
        }

        return CycleAnalysis(
            insights = insights.sortedByDescending {
                when (it.severity) {
                    InsightSeverity.CRITICAL -> 4
                    InsightSeverity.WARNING -> 3
                    InsightSeverity.TIP -> 2
                    InsightSeverity.INFO -> 1
                }
            },
            overallRiskLevel = overallRisk,
            recommendedBloodworkMarkers = bloodworkMarkers,
            recommendedBloodworkFrequencyWeeks = bloodworkFrequency,
            pctRecommendation = pctRecommendation,
            estimatedPctStartDays = longestWait
        )
    }

    private fun generateDosingInsights(
        substances: List<String>,
        weeklyDoses: Map<String, Double>,
        scienceData: List<CompoundScientificData>,
        insights: MutableList<CycleInsight>
    ) {
        // Check testosterone dose specifically
        val testSubstance = substances.find {
            CompoundType.fromSubstanceId(it) == CompoundType.TESTOSTERONE
        }

        if (testSubstance != null && weeklyDoses.containsKey(testSubstance)) {
            val dose = weeklyDoses[testSubstance] ?: 0.0
            val testData = scienceData.find { it.compoundType == CompoundType.TESTOSTERONE }

            when {
                dose > 300 -> {
                    insights.add(CycleInsight(
                        id = "test_dose_high",
                        severity = InsightSeverity.WARNING,
                        category = InsightCategory.DOSING,
                        title = "Visoka doza testosterona",
                        message = "Doza od ${dose.toInt()}mg/ned je značajno iznad optimalne tačke (125mg). " +
                                "Androgene nuspojave (akne, retencija) i pad HDL rastu sa dozom, " +
                                "ali studija ne daje incidencu po dozi.",
                        action = "Razmotri smanjenje na 125-200mg za bolji odnos rizik/korist",
                        reference = "Bhasin S, et al. Am J Physiol Endocrinol Metab 2001;281:E1172-81 (PMID 11701431)"
                    ))
                }
                dose > 125 && dose <= 300 -> {
                    insights.add(CycleInsight(
                        id = "test_dose_moderate",
                        severity = InsightSeverity.TIP,
                        category = InsightCategory.DOSING,
                        title = "Doza iznad optimalne tačke",
                        message = "Doza od ${dose.toInt()}mg/ned daje diminishing returns. " +
                                "125mg daje ~40% max dobitaka uz minimalne nuspojave. " +
                                "${dose.toInt()}mg daje marginalno više uz značajno više nuspojava.",
                        reference = "Bhasin S, et al. Am J Physiol Endocrinol Metab 2001;281:E1172-81 (PMID 11701431)"
                    ))
                }
                dose in 100.0..150.0 -> {
                    insights.add(CycleInsight(
                        id = "test_dose_optimal",
                        severity = InsightSeverity.INFO,
                        category = InsightCategory.DOSING,
                        title = "Optimalna doza",
                        message = "Doza od ${dose.toInt()}mg/ned je blizu optimalne tačke infleksije (125mg). " +
                                "Očekuj +3-4kg mišićne mase uz minimalne neželjene efekte.",
                        reference = "Bhasin S, et al. Am J Physiol Endocrinol Metab 2001;281:E1172-81 (PMID 11701431)"
                    ))
                }
            }
        }

        // Check nandrolone dose
        val nandSubstance = substances.find {
            CompoundType.fromSubstanceId(it) == CompoundType.NANDROLONE
        }

        if (nandSubstance != null && weeklyDoses.containsKey(nandSubstance)) {
            val dose = weeklyDoses[nandSubstance] ?: 0.0

            if (dose > 100) {
                insights.add(CycleInsight(
                    id = "nand_dose_high",
                    severity = InsightSeverity.WARNING,
                    category = InsightCategory.DOSING,
                    title = "Nandrolon iznad optimalne doze",
                    message = "RCT studije pokazuju da je 100mg/ned optimalno za nandrolon. " +
                            "Iznad toga rizik/korist opada, a prolaktinske nuspojave rastu.",
                    action = "Razmotri smanjenje na 100mg/ned",
                    reference = "Sattler FR, et al. JCEM 1999"
                ))
            }
        }
    }

    private fun generateFirstCycleInsights(
        compoundTypes: List<CompoundType>,
        insights: MutableList<CycleInsight>
    ) {
        if (compoundTypes.size > 1 || compoundTypes.any { it != CompoundType.TESTOSTERONE }) {
            insights.add(CycleInsight(
                id = "first_cycle_compound",
                severity = InsightSeverity.WARNING,
                category = InsightCategory.GENERAL,
                title = "Preporuka za prvi ciklus",
                message = "Za prvi ciklus preporučuje se SAMO testosteron. " +
                        "Razlozi: 50+ RCT studija, predvidljiv odgovor, bioidentičan, lako merljiv. " +
                        "Sa više komponenti nećeš znati šta izaziva koji efekat.",
                action = "Razmotri pojednostavljenje na samo testosteron"
            ))
        }

        insights.add(CycleInsight(
            id = "first_cycle_dose",
            severity = InsightSeverity.TIP,
            category = InsightCategory.DOSING,
            title = "Preporučena doza za prvi ciklus",
            message = "Na osnovu RCT podataka, 100-150mg testosterona nedeljno je optimalno za početak. " +
                    "Očekuj +3.4kg mišićne mase sa minimalnim nuspojavama. " +
                    "'500mg prvi ciklus' nema naučnu podršku - potiče sa foruma.",
            reference = "Bhasin S, et al. Am J Physiol Endocrinol Metab 2001;281:E1172-81 (PMID 11701431)"
        ))
    }

    private fun generateEstrogenInsights(
        scienceData: List<CompoundScientificData>,
        insights: MutableList<CycleInsight>
    ) {
        val highAromatizers = scienceData.filter {
            it.aromatization in listOf(AromatizationLevel.HIGH, AromatizationLevel.VERY_HIGH)
        }

        if (highAromatizers.isNotEmpty()) {
            insights.add(CycleInsight(
                id = "estrogen_management",
                severity = InsightSeverity.WARNING,
                category = InsightCategory.ESTROGEN,
                title = "Potrebno praćenje estrogena",
                message = "Ciklus sadrži komponente sa visokom aromatizacijom. " +
                        "Ciljni E2: 20-30 pg/mL (T:E2 odnos ~10:1). " +
                        "Ako E2 > 60 pg/mL: razmotriti AI. " +
                        "Ako ginekomastija: SERM (tamoksifen/raloksifen) je prvi izbor, ne AI.",
                action = "Imaj anastrozol 0.5mg pri ruci; koristi samo ako je E2 povišen"
            ))
        }

        val noAromatizers = scienceData.filter { it.aromatization == AromatizationLevel.NONE }
        if (noAromatizers.size == scienceData.size && scienceData.isNotEmpty()) {
            insights.add(CycleInsight(
                id = "no_aromatization",
                severity = InsightSeverity.INFO,
                category = InsightCategory.ESTROGEN,
                title = "Bez aromatizacije",
                message = "Ove komponente ne aromatizuju - AI nije potreban. " +
                        "PAŽNJA: Ako je E2 prenizak (<8 pg/mL), može doći do problema sa kostima i zglobovima."
            ))
        }
    }

    private fun generateProlactinInsights(
        compoundTypes: List<CompoundType>,
        scienceData: List<CompoundScientificData>,
        insights: MutableList<CycleInsight>
    ) {
        val highProlactinRisk = scienceData.filter { it.prolactinRisk == ProlactinRisk.HIGH }

        if (highProlactinRisk.isNotEmpty()) {
            val compounds = highProlactinRisk.map { it.compoundType.displayName }.joinToString(", ")

            insights.add(CycleInsight(
                id = "prolactin_risk",
                severity = InsightSeverity.CRITICAL,
                category = InsightCategory.PROLACTIN,
                title = "Visok rizik od hiperprolaktinemije",
                message = "Komponente ($compounds) značajno povećavaju prolaktin. " +
                        "Simptomi: ginekomastija, erektilna disfunkcija, smanjen libido, laktacija. " +
                        "Kabergolin je zlatni standard (0.25-0.5mg 2x nedeljno).",
                action = "OBAVEZNO imaj Cabergoline pri ruci. Prati prolaktin u krvi."
            ))
        }
    }

    private fun generateLiverInsights(
        scienceData: List<CompoundScientificData>,
        cycleDurationWeeks: Int,
        insights: MutableList<CycleInsight>
    ) {
        val hepatotoxic = scienceData.filter {
            it.hepatotoxicity in listOf(HepatotoxicityLevel.HIGH, HepatotoxicityLevel.VERY_HIGH)
        }

        if (hepatotoxic.isNotEmpty()) {
            val severity = if (cycleDurationWeeks > 6) InsightSeverity.CRITICAL else InsightSeverity.WARNING

            insights.add(CycleInsight(
                id = "liver_toxicity",
                severity = severity,
                category = InsightCategory.LIVER,
                title = "Hepatotoksičnost - 17α-alkilovane komponente",
                message = "Oralni steroidi su hepatotoksični zbog 17α-alkilacije. " +
                        "Mehanizam: oksidativni stres, holestaza. " +
                        "Ograniči na 6-8 nedelja. Na ${cycleDurationWeeks} nedelja rizik je ${if (cycleDurationWeeks > 8) "POVIŠEN" else "umeren"}.",
                action = "Prati ALT, AST, GGT, bilirubin svake 3 nedelje"
            ))
        }
    }

    private fun generateCardiovascularInsights(
        scienceData: List<CompoundScientificData>,
        insights: MutableList<CycleInsight>
    ) {
        val highCVRisk = scienceData.filter {
            it.cardiovascularRisk in listOf(CardiovascularRisk.HIGH, CardiovascularRisk.VERY_HIGH)
        }

        if (highCVRisk.isNotEmpty()) {
            insights.add(CycleInsight(
                id = "cv_risk",
                severity = InsightSeverity.WARNING,
                category = InsightCategory.CARDIOVASCULAR,
                title = "Kardiovaskularni rizik",
                message = "Ciklus sadrži komponente sa povišenim KV rizikom. " +
                        "Studije pokazuju: 32% LVH kod dugotrajnih korisnika, pad HDL do 40%. " +
                        "11-godišnje praćenje: povećana incidencija infarkta i moždanog udara.",
                action = "Redovan kardio, prati krvni pritisak, EKG preporučen"
            ))
        }
    }

    private fun generateInteractionInsights(
        compoundTypes: List<CompoundType>,
        insights: MutableList<CycleInsight>
    ) {
        // Check for multiple 19-nors
        val nineNors = compoundTypes.filter { it in listOf(CompoundType.NANDROLONE, CompoundType.TRENBOLONE) }

        if (nineNors.size > 1) {
            insights.add(CycleInsight(
                id = "multiple_19nor",
                severity = InsightSeverity.CRITICAL,
                category = InsightCategory.COMPOUND_INTERACTION,
                title = "Kombinacija 19-nor steroida",
                message = "Kombinovanje Nandrolona i Trenbolona je IZUZETNO RIZIČNO. " +
                        "Oba su 19-nor sa dugotrajnim metabolitima. " +
                        "Oporavak HPTA može biti permanentno otežan.",
                action = "Snažno preporučeno: koristi samo jedan 19-nor po ciklusu"
            ))
        }

        // Check for too many compounds
        if (compoundTypes.size > 2) {
            insights.add(CycleInsight(
                id = "many_compounds",
                severity = InsightSeverity.WARNING,
                category = InsightCategory.COMPOUND_INTERACTION,
                title = "Mnogo komponenti",
                message = "Ciklus ima ${compoundTypes.size} različitih komponenti. " +
                        "⚠️ NEMA RCT studija koje porede stakove sa monoterapijom! " +
                        "Više komponenti = više potencijalnih interakcija i teže identifikovanje problema.",
                action = "Preporuka: ne dodavaj više od 1 nove komponente po ciklusu"
            ))
        }
    }

    private fun generateDurationInsights(
        cycleDurationWeeks: Int,
        insights: MutableList<CycleInsight>
    ) {
        when {
            cycleDurationWeeks > 16 -> {
                insights.add(CycleInsight(
                    id = "duration_long",
                    severity = InsightSeverity.WARNING,
                    category = InsightCategory.DURATION,
                    title = "Produženi ciklus",
                    message = "Ciklus od $cycleDurationWeeks nedelja prelazi optimalno trajanje. " +
                            "RCT podaci: 80%+ dobitaka dolazi u prvih 12-16 nedelja. " +
                            "Iznad 16 ned: marginalni dobici, značajno teži oporavak HPTA.",
                    reference = "Bhasin S, et al. Am J Physiol Endocrinol Metab 2001;281:E1172-81 (PMID 11701431)"
                ))
            }
            cycleDurationWeeks in 12..16 -> {
                insights.add(CycleInsight(
                    id = "duration_optimal",
                    severity = InsightSeverity.INFO,
                    category = InsightCategory.DURATION,
                    title = "Optimalno trajanje",
                    message = "Trajanje od $cycleDurationWeeks nedelja je u optimalnom opsegu (12-16 ned). " +
                            "Očekuj maksimalne mišićne mase dobitke uz standardni oporavak."
                ))
            }
            cycleDurationWeeks < 8 -> {
                insights.add(CycleInsight(
                    id = "duration_short",
                    severity = InsightSeverity.TIP,
                    category = InsightCategory.DURATION,
                    title = "Kratak ciklus",
                    message = "Ciklus od $cycleDurationWeeks nedelja je relativno kratak. " +
                            "Prednost: lakši oporavak. " +
                            "Mana: možda neće dati pune rezultate (značajni dobici od 8+ nedelja)."
                ))
            }
        }
    }

    private fun generateEvidenceInsights(
        scienceData: List<CompoundScientificData>,
        insights: MutableList<CycleInsight>
    ) {
        val animalOnly = scienceData.filter { it.evidenceLevel == EvidenceLevel.ANIMAL_ONLY }

        if (animalOnly.isNotEmpty()) {
            val compounds = animalOnly.map { it.compoundType.displayName }.joinToString(", ")

            insights.add(CycleInsight(
                id = "no_human_data",
                severity = InsightSeverity.CRITICAL,
                category = InsightCategory.GENERAL,
                title = "Komponente bez humanih studija",
                message = "Komponente ($compounds) NEMAJU humane RCT studije! " +
                        "Svi poznati efekti su iz životinjskih studija ili anegdota. " +
                        "Korišćenje znači preuzimanje potpuno nepoznatog rizika."
            ))
        }
    }
}
