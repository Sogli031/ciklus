package com.injekcije.app.data

object SampleData {

    private val cachedSubstances: List<Substance> by lazy { listOf(
        // =========================================================================
        // TESTOSTERONE
        // =========================================================================
        Substance(
            id = "test_e",
            name = "Testosterone Enanthate",
            shortName = "Test E",
            defaultConcentration = 250.0,
            category = "Testosterone",
            recommendedFrequency = InjectionFrequency.E3_5D,
            recommendedDose = 500.0,  // mg/week
            recommendedMaxWeeks = 16,  // 12-16 nedelja optimalno
            esterType = EsterType.ENANTHATE
        ),
        Substance(
            id = "test_c",
            name = "Testosterone Cypionate",
            shortName = "Test C",
            defaultConcentration = 250.0,
            category = "Testosterone",
            recommendedFrequency = InjectionFrequency.E3_5D,
            recommendedDose = 500.0,  // mg/week
            recommendedMaxWeeks = 16,  // 12-16 nedelja optimalno
            esterType = EsterType.CYPIONATE
        ),
        Substance(
            id = "test_p",
            name = "Testosterone Propionate",
            shortName = "Test P",
            defaultConcentration = 100.0,
            category = "Testosterone",
            recommendedFrequency = InjectionFrequency.EOD,
            recommendedDose = 350.0,  // mg/week (50mg EOD)
            recommendedMaxWeeks = 12,  // kraći ester, kraći ciklusi
            esterType = EsterType.PROPIONATE
        ),
        Substance(
            id = "test_s",
            name = "Testosterone Suspension",
            shortName = "Test Susp",
            defaultConcentration = 100.0,
            category = "Testosterone",
            recommendedFrequency = InjectionFrequency.ED,
            recommendedDose = 350.0,  // mg/week
            recommendedMaxWeeks = 8,   // bez estera, kraći ciklusi
            esterType = EsterType.PROPIONATE  // suspenzija bez estera, t½ ~1 dan - kao propionat
        ),
        Substance(
            id = "sustanon",
            name = "Sustanon 250",
            shortName = "Sust",
            defaultConcentration = 250.0,
            category = "Testosterone",
            recommendedFrequency = InjectionFrequency.E3_5D,
            recommendedDose = 500.0,  // mg/week
            recommendedMaxWeeks = 16,  // mešavina estera
            esterType = EsterType.DECANOATE  // najduža komponenta blenda je testosteron dekanoat
        ),

        // =========================================================================
        // TRENBOLONE
        // =========================================================================
        Substance(
            id = "tren_e",
            name = "Trenbolone Enanthate",
            shortName = "Tren E",
            defaultConcentration = 200.0,
            category = "Trenbolone",
            recommendedFrequency = InjectionFrequency.E3_5D,
            recommendedDose = 300.0,  // mg/week
            recommendedMaxWeeks = 10,  // 8-10 nedelja maks zbog toksičnosti
            esterType = EsterType.ENANTHATE
        ),
        Substance(
            id = "tren_a",
            name = "Trenbolone Acetate",
            shortName = "Tren A",
            defaultConcentration = 100.0,
            category = "Trenbolone",
            recommendedFrequency = InjectionFrequency.EOD,
            recommendedDose = 300.0,  // mg/week
            recommendedMaxWeeks = 8,   // kraći ester, kraći ciklusi
            esterType = EsterType.ACETATE
        ),
        Substance(
            id = "tren_hex",
            name = "Trenbolone Hexahydrobenzylcarbonate",
            shortName = "Tren Hex",
            defaultConcentration = 76.5,
            category = "Trenbolone",
            recommendedFrequency = InjectionFrequency.WEEKLY,
            recommendedDose = 300.0,  // mg/week
            recommendedMaxWeeks = 10,  // duži ester
            esterType = EsterType.ENANTHATE  // hexahydrobenzylcarbonate, t½ ~5 dana - kao enanthate
        ),

        // =========================================================================
        // NANDROLONE
        // =========================================================================
        Substance(
            id = "deca",
            name = "Nandrolone Decanoate",
            shortName = "Deca",
            defaultConcentration = 250.0,
            category = "Nandrolone",
            recommendedFrequency = InjectionFrequency.WEEKLY,
            recommendedDose = 400.0,  // mg/week
            recommendedMaxWeeks = 16,  // 12-16 nedelja, dug ester
            esterType = EsterType.DECANOATE
        ),
        Substance(
            id = "npp",
            name = "Nandrolone Phenylpropionate",
            shortName = "NPP",
            defaultConcentration = 100.0,
            category = "Nandrolone",
            recommendedFrequency = InjectionFrequency.EOD,
            recommendedDose = 350.0,  // mg/week
            recommendedMaxWeeks = 10,  // kraći ester od dece
            esterType = EsterType.PHENYLPROPIONATE
        ),

        // =========================================================================
        // MASTERON
        // =========================================================================
        Substance(
            id = "mast_e",
            name = "Masteron Enanthate",
            shortName = "Mast E",
            defaultConcentration = 200.0,
            category = "Masteron",
            recommendedFrequency = InjectionFrequency.E3_5D,
            recommendedDose = 400.0,  // mg/week
            recommendedMaxWeeks = 12,  // 8-12 nedelja
            esterType = EsterType.ENANTHATE
        ),
        Substance(
            id = "mast_p",
            name = "Masteron Propionate",
            shortName = "Mast P",
            defaultConcentration = 100.0,
            category = "Masteron",
            recommendedFrequency = InjectionFrequency.EOD,
            recommendedDose = 350.0,  // mg/week
            recommendedMaxWeeks = 10,  // kraći ester
            esterType = EsterType.PROPIONATE
        ),

        // =========================================================================
        // BOLDENONE
        // =========================================================================
        Substance(
            id = "eq",
            name = "Boldenone Undecylenate (Equipoise)",
            shortName = "EQ",
            defaultConcentration = 300.0,
            category = "Boldenone",
            recommendedFrequency = InjectionFrequency.WEEKLY,
            recommendedDose = 500.0,  // mg/week
            recommendedMaxWeeks = 20,  // veoma dug ester, 16-20 nedelja
            esterType = EsterType.UNDECYLENATE
        ),
        Substance(
            id = "bold_c",
            name = "Boldenone Cypionate",
            shortName = "Bold C",
            defaultConcentration = 200.0,
            category = "Boldenone",
            recommendedFrequency = InjectionFrequency.E3_5D,
            recommendedDose = 400.0,  // mg/week
            recommendedMaxWeeks = 16,  // kraći ester od EQ
            esterType = EsterType.CYPIONATE
        ),

        // =========================================================================
        // PRIMOBOLAN
        // =========================================================================
        Substance(
            id = "primo_e",
            name = "Primobolan (Metenolone Enanthate)",
            shortName = "Primo",
            defaultConcentration = 100.0,
            category = "Primobolan",
            recommendedFrequency = InjectionFrequency.E3_5D,
            recommendedDose = 500.0,  // mg/week (needs higher dose)
            recommendedMaxWeeks = 12,  // 8-12 nedelja, blag steroid
            esterType = EsterType.ENANTHATE
        ),

        // =========================================================================
        // DHT DERIVATIVES
        // =========================================================================
        Substance(
            id = "dhb",
            name = "Dihydroboldenone (1-Test Cyp)",
            shortName = "DHB",
            defaultConcentration = 100.0,
            category = "DHT",
            recommendedFrequency = InjectionFrequency.EOD,
            recommendedDose = 300.0,  // mg/week
            recommendedMaxWeeks = 12,  // 8-12 nedelja
            esterType = EsterType.CYPIONATE  // DHB = 1-Testosterone Cypionate
        ),
        Substance(
            id = "winstrol",
            name = "Winstrol Depot (Stanozolol)",
            shortName = "Winstrol",
            defaultConcentration = 50.0,
            category = "DHT",
            recommendedFrequency = InjectionFrequency.EOD,
            recommendedDose = 50.0,  // mg/week - OPREZ: fatalni slučajevi na 175mg+/ned
            recommendedMaxWeeks = 4,   // MAKSIMUM 4 nedelje - hepatotoksičan!
            esterType = EsterType.PROPIONATE  // vodena suspenzija bez estera, t½ ~24h - kao propionat
        ),

        // =========================================================================
        // PCT / SUPPORT
        // =========================================================================
        Substance(
            id = "hcg",
            name = "Human Chorionic Gonadotropin",
            shortName = "HCG",
            defaultConcentration = 2500.0, // IU/mL (5000 IU rekonstituisano u 2 mL)
            category = "PCT",
            recommendedFrequency = InjectionFrequency.E3D,
            recommendedDose = 1500.0,  // IU/week (500IU x3)
            recommendedMaxWeeks = 8,   // PCT: 6-8 nedelja
            esterType = EsterType.ORAL  // peptid bez estera - ne utiče na čekanje pre PCT-a
        ),
        Substance(
            id = "hmg",
            name = "Human Menopausal Gonadotropin",
            shortName = "HMG",
            defaultConcentration = 75.0, // IU
            category = "PCT",
            recommendedFrequency = InjectionFrequency.E3D,
            recommendedDose = 225.0,  // IU/week (75IU x3)
            recommendedMaxWeeks = 8,   // PCT: 6-8 nedelja
            esterType = EsterType.ORAL  // peptid bez estera - ne utiče na čekanje pre PCT-a
        ),

        // =========================================================================
        // GROWTH HORMONE
        // =========================================================================
        Substance(
            id = "hgh",
            name = "Human Growth Hormone",
            shortName = "HGH",
            defaultConcentration = 10.0, // IU per vial
            category = "Peptidi",
            recommendedFrequency = InjectionFrequency.ED,
            recommendedDose = 28.0,  // IU/week (4IU ED)
            recommendedMaxWeeks = 24,  // dugoročna upotreba, min 3-6 meseci
            esterType = EsterType.ORAL  // peptid bez estera - ne utiče na čekanje pre PCT-a
        ),

        // =========================================================================
        // ORALNI STEROIDI (recommendedDose = DNEVNA doza)
        // =========================================================================
        Substance(
            id = "oxandrolone",
            name = "Oxandrolone (Anavar)",
            shortName = "Anavar",
            defaultConcentration = 10.0,  // mg po tableti
            category = "Oralni",
            recommendedFrequency = InjectionFrequency.ED,
            recommendedDose = 20.0,  // mg/day - optimalno 10-20mg, iznad 40mg ALT↑↑↑
            recommendedMaxWeeks = 8,   // 8-12 nedelja, najbezbedniji oralni
            esterType = EsterType.ORAL
        ),
        Substance(
            id = "oxymetholone",
            name = "Oxymetholone (Anadrol)",
            shortName = "Anadrol",
            defaultConcentration = 50.0,
            category = "Oralni",
            recommendedFrequency = InjectionFrequency.ED,
            recommendedDose = 50.0,  // mg/day
            recommendedMaxWeeks = 4,   // MAKSIMUM 4-6 nedelja - najhepatotoksičniji!
            esterType = EsterType.ORAL
        ),
        Substance(
            id = "stanozolol_oral",
            name = "Stanozolol oral (Winstrol)",
            shortName = "Winstrol",
            defaultConcentration = 10.0,
            category = "Oralni",
            recommendedFrequency = InjectionFrequency.ED,
            recommendedDose = 6.0,  // mg/day - klinički: 0.5-2mg, max 6mg (fatalni sl. na 50mg EOD)
            recommendedMaxWeeks = 4,   // MAKSIMUM 4 nedelje - hepatotoksičan!
            esterType = EsterType.ORAL
        ),
        Substance(
            id = "methandienone",
            name = "Methandienone (Dianabol)",
            shortName = "Dbol",
            defaultConcentration = 10.0,
            category = "Oralni",
            recommendedFrequency = InjectionFrequency.ED,
            recommendedDose = 25.0,  // mg/day - max preporučeno ≤25mg, 4 ned
            recommendedMaxWeeks = 4,   // MAKSIMUM 4 nedelje
            esterType = EsterType.ORAL
        ),
        Substance(
            id = "turinabol",
            name = "Turinabol (4-Chloro-DHMT)",
            shortName = "Tbol",
            defaultConcentration = 10.0,
            category = "Oralni",
            recommendedFrequency = InjectionFrequency.ED,
            recommendedDose = 40.0,  // mg/day
            recommendedMaxWeeks = 6,   // 6 nedelja - 2 nedelje čekanja pre PCT
            esterType = EsterType.ORAL
        ),

        // =========================================================================
        // OTHER
        // =========================================================================
        Substance(
            id = "custom",
            name = "Custom Supstanca",
            shortName = "Custom",
            defaultConcentration = 100.0,
            category = "Ostalo",
            recommendedFrequency = InjectionFrequency.E3_5D,
            recommendedDose = 0.0,  // user must enter
            recommendedMaxWeeks = 12  // podrazumevano
        )
    )}

    fun getDefaultSubstances(): List<Substance> = cachedSubstances

    fun getSubstanceById(id: String): Substance? =
        cachedSubstances.firstOrNull { it.id == id }

    fun getCategories(): List<String> =
        cachedSubstances.map { it.category }.distinct()

    fun getSubstancesByCategory(category: String): List<Substance> =
        cachedSubstances.filter { it.category == category }
}
