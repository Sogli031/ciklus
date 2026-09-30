package com.injekcije.app.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

data class LearningCategoryData(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val articles: List<LearningArticleData>
)

data class LearningArticleData(
    val id: String,
    val title: String,
    val content: String
)

val learningCategories: List<LearningCategoryData> by lazy { listOf(
    // 1. DOSING INJECTABLE - Evidence-Based Optimal Dosing for Injectables
    LearningCategoryData(
        id = "dosing_injectable",
        title = "Doziranje - Injektabilni",
        icon = Icons.Default.Science,
        articles = listOf(
            LearningArticleData(
                id = "testosterone_optimal",
                title = "Testosteron E/C - detaljan vodič",
                content = """
**Testosterone Enanthate/Cypionate - Klinički vodič**

[SIMULATOR:testosterone]

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

**📊 DOZA-ODGOVOR ODNOS (Bhasin 2001)**

Izvor: Bhasin S, et al. Am J Physiol Endocrinol Metab 2001;281:E1172-81 (PMID 11701431). 61 zdrav muškarac 18–35 g, GnRH agonist za potpunu supresiju endogenog T, 20 nedelja i.m. testosteron enantata. n≈12 po grupi → velika varijansa; SD/SEM nisu javno dostupni.

📌 Prikazano je SAMO 5 stvarno testiranih doza. Serum T su NADIR vrednosti (najniža tačka, pred sledeću injekciju) — ne prosek ni pik.

| Doza (mg/ned) | Nadir serum T | FFM promena |
|------|--------------|---------|
| 25 | 253 ng/dL | bez zn. promene* |
| 50 | 306 ng/dL | bez zn. promene* |
| **125** | **542 ng/dL** | **+3.4 kg** |
| **300** | **1345 ng/dL** | **+5.2 kg** |
| **600** | **2370 ng/dL** | **+7.9 kg** |

*Na 25 i 50 mg FFM se nije značajno menjala (ns); umesto toga je rasla masna masa.

**Nuspojave — dozno-zavisan SMER (ne jedinstven %):**
• Hematokrit / hemoglobin: ↑ sa dozom (rizik eritrocitoze na višim dozama)
• HDL („dobar" holesterol): ↓ sa dozom
• PSA, raspoloženje i seksualna funkcija: bez značajne promene ni na jednoj dozi

⚠️ Populacija: mladi zdravi muškarci sa potpuno ugašenim endogenim T pre početka — ne prevodi se 1:1 na TRT kod starijih, druge estre/protokole ni na osobe sa komorbiditetima.

**Kraći protokoli:**
• 600 mg/ned + trening (10 ned): +6.1 kg FFM, +22 kg bench, +38 kg squat
• ~280 mg/ned (6 ned): značajan porast snage već u 3. nedelji

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

**🎯 ODNOS DOZA / KORIST**

Iz stvarnih FFM vrednosti studije (razlike izmerenih doza, ne interpolacija):

| Prelaz | Dodatni FFM |
|--------|--------------|
| 25→125 mg | +3.4 kg |
| 125→300 mg | +1.8 kg |
| 300→600 mg | +2.7 kg |

• 125 mg ≈ 43% FFM gaina od 600 mg; 300 mg ≈ 66% — uz znatno niži rizik.
• Veće doze daju dodatni FFM, ali nesrazmerno povećavaju hematološki (hematokrit↑) i lipidni (HDL↓) rizik.

**Zaključak:** Dupliranje doze NE duplira rezultate, ali povećava rizike.

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

**⚠️ NUSPOJAVE PO SISTEMIMA**

**1️⃣ ENDOKRINI SISTEM (HPTA supresija)**

| Doza | LH/FSH | Spermatogeneza |
|------|--------|----------------|
| 50 mg/ned | ~60% normalne | Blago smanjena |
| 100 mg/ned | ~32% normalne | ~0.8% baseline (azospermija kod 50%) |
| >100 mg/ned | Maksimalna supresija | Potpuna supresija |

**Ključno:** Čak i 100-200 mg/ned uzrokuje skoro kompletno gašenje HPT ose i infertilitet.

**Oporavak:** T i HDL se vraćaju na baseline 12-24 nedelje nakon prestanka (12-nedeljni ciklus na 200 mg).

─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─

**2️⃣ HEMATOLOŠKE PROMENE**

| Parametar | Promena | Značaj |
|-----------|---------|--------|
| Hemoglobin | +0.8 g/dL | Povećan kapacitet kiseonika |
| Hematokrit | +3.2% | ⚠️ Rizik hiperviskoznosti |
| Krvni pritisak | Blagi porast | Praćenje obavezno |

**Monitoring:** Hematokrit svake 6-12 nedelja, cilj <54%.

─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─

**3️⃣ LIPIDNI PROFIL**

| Doza | HDL pad | LDL | Trigliceridi |
|------|---------|-----|--------------|
| Niže/terapijske doze | Blag, dozno-zavisan pad HDL | Bez promene | Bez promene |
| 600 mg/ned | Najveći pad HDL | Bez značajne promene | - |

**Ključno:** HDL ("dobar holesterol") pada dozno-zavisno. LDL se obično ne menja, ali LDL/HDL odnos se pogoršava → povećan aterogeni rizik.

─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─

**4️⃣ KARDIOVASKULARNI RIZIK**

• Kraće studije: NEMA jasnog povećanja KV događaja
• FDA upozorenje: blagi porast sistolnog pritiska
• Dugoročno: HDL pad + policitemija teoretski povećavaju rizik

**Preporuka:** Kardio vežbe, redovno praćenje lipida i krvnog pritiska.

─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─

**5️⃣ PSIHOLOŠKI EFEKTI**

**Iznenađujući nalaz:** Kontrolisane studije (Bhasin 1996, 2001) NE pokazuju značajne promene u:
• Raspoloženju
• Agresivnosti
• Kogniciji
• Libidu

Čak ni na 600 mg/ned 10-20 nedelja nije bilo značajnih promena ponašanja u proseku.

─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─

**6️⃣ OSTALE NUSPOJAVE**

• Androgene nuspojave (akne, masna koža, zadržavanje tečnosti): rastu sa dozom, ali Bhasin 2001 NE daje incidencu po dozi — nema pouzdanih % (n≈12/grupa).
• Estrogene nuspojave (ginekomastija): verovatnije na višim dozama zbog veće aromatizacije; takođe nije kvantifikovano u studiji.
• PSA (prostata): bez značajne promene ni na jednoj dozi (Bhasin 2001).

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

**⏱️ VREMENSKA DINAMIKA**

**Kada počinju efekti?**

| Period | Efekti |
|--------|--------|
| 3. nedelja | Značajan porast snage (bench, sprint) |
| 8-12 nedelja | Većina mišićne mase već stečena |
| 16-20 nedelja | Marginalni dodatni gainovi |

**Kada se javljaju nuspojave?**

| Nuspojava | Početak |
|-----------|---------|
| HDL pad | Prvih nekoliko nedelja |
| Hematokrit porast | Progresivno tokom meseci |
| HPTA supresija | Odmah (reverzibilna) |

**Zaključak:** Kraći ciklus (8-12 ned) daje većinu gainova uz manje kumulativnih nuspojava.

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

**📋 MONITORING PROTOKOL**

| Test | Učestalost | Ciljna vrednost |
|------|------------|-----------------|
| Hematokrit | Svake 6-12 ned | <54% |
| HDL/LDL | Pre ciklusa, sredinom, nakon | HDL >30 mg/dL |
| Estradiol | Ako simptomi | 20-30 pg/mL |
| LH, FSH, T | Pre i posle PCT | Normalni opseg |
                """.trimIndent()
            ),
            LearningArticleData(
                id = "nandrolone_dosing",
                title = "Nandrolon (Deca) - doziranje",
                content = """
**Nandrolone Decanoate (Deca) - klinički dozno-odgovor**

Izvor: Macdonald 2007 (PMID 17522475) — RCT na hemodijaliznim (CKD) pacijentima, 24 nedelje. Mereno: apendikularna nemasna masa (ALM), ne opšta „mišićna masa". Testirane doze (muškarci): 50/100/200 mg/ned.

| Doza | Promena ALM |
|------|--------------|
| kontrola | +0.3 kg |
| 50 mg/ned | +0.8 kg |
| **100 mg/ned** | **+1.5 kg** |
| 200 mg/ned | +2.1 kg |

⚠️ Ispitanici su dijalizni pacijenti — apsolutne cifre se ne prevode 1:1 na zdrave sportiste. Studija NIJE dala nuspojave po dozi i zaključila je da doze ≥200 mg treba dalje istražiti (nije našla plato).

[IMAGE:chart_nandrolone]

**🎯 Ključni insight:**
100 mg/ned dalo je ~71% ALM dobitka od 200 mg arma (1.5 vs 2.1 kg) uz upola manju dozu — povoljan odnos doza/korist. Napomena: studija nije kvantifikovala nuspojave po dozi.

**Zašto je Nandrolon poseban:**
• Minimalne prostate nuspojave (konvertuje u DHN, ne DHT)
• Pozitivan efekat na zglobove i kolagen
• 100→200 mg: samo +0.6 kg više ALM

**⚠️ OBAVEZNO koristi sa testosteron bazom** (minimum 100-150mg Test/ned) — inače "Deca dick"!

**📚 Izvor:** Macdonald 2007 (PMID: 17522475)

**Doziranje:** Nedeljno IM (dugi estar)

**Monitoring:**
• Lipidi i hematokrit: svakih 12 nedelja
• Prolaktin: ako simptomi (erektilna disfunkcija)
                """.trimIndent()
            ),
            LearningArticleData(
                id = "trenbolone_warning",
                title = "Trenbolon - UPOZORENJE",
                content = """
**⚠️ TRENBOLON: NIJE ODOBREN ZA LJUDSKU UPOTREBU**

**NEMA kliničkih podataka za ljude.** Svi dokazi dolaze iz životinjskih modela ili retrospektivnih izveštaja korisnika.

**NUSPOJAVE (retrospektivni izveštaji korisnika, bez kliničkih studija — bez pouzdanih %):**
• Teška akna (često prijavljivana)
• Ginekomastija / progestagene nuspojave
• Hipertenzija, srčane aritmije
• Komplikacije na mestu injekcije

**PREPORUKA:**
⛔ **NE KORISTITI**
• Nema odobrene indikacije za ljude
• Dose-response nepoznat
• Ekstremno opterećenje nuspojavama
                """.trimIndent()
            ),
            LearningArticleData(
                id = "boldenone_warning",
                title = "Boldenon (EQ) - UPOZORENJE",
                content = """
**⚠️ BOLDENON: NEDOVOLJNI PODACI ZA LJUDE**

**Poremećaj imunog sistema (studija na pacovima):**
• ↑Pro-inflamatorni citokini
• Disfunkcija štitne žlezde
• Potencijalni induktor autoimunog hipotiroidizma

**PREPORUKA:**
⚠️ **NEDOVOLJNI DOKAZI**
• Nema podataka o dose-response za ljude
• NE PREPORUČUJE SE bez značajnih podataka o bezbednosti
                """.trimIndent()
            )
        )
    ),

    // 2. DOSING ORAL - Evidence-Based Optimal Dosing for Oral Steroids
    LearningCategoryData(
        id = "dosing_oral",
        title = "Doziranje - Oralni",
        icon = Icons.Default.Medication,
        articles = listOf(
            LearningArticleData(
                id = "oxandrolone_dosing",
                title = "Oxandrolon (Anavar) - doziranje",
                content = """
**Oxandrolone (Anavar) - Klinički vodič**

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

**💊 FARMAKOLOGIJA**

| Parametar | Vrednost |
|-----------|----------|
| Poluživot | 9.4-10.4h |
| Bioraspoloživost | 97% |
| Metabolizam | **BUBREŽNI** (28% nepromenjen u urinu) |
| Detekcija | Do 3 nedelje |

**KLJUČNO:** Oxandrolone je jedini oralni steroid sa pretežno bubrežnim metabolizmom - zato je NAJSIGURNIJI za jetru!

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

**📊 KLINIČKA EFIKASNOST (RCT podaci)**

| Doza | Trajanje | LBM/BCM | Populacija | Studija |
|------|----------|-----|------------|---------|
| 20 mg/dan | 12 ned | +0.9 kg BCM (~+1.8 kg tel. masa) | HIV+ | Grunfeld RCT |
| 20 mg/dan (10mg×2) | 6 ned | +2.7±1.6 kg LBM | Stariji | Schroeder RCT |
| 0.1-0.2 mg/kg | 14 dana | 2-3× brže zarastanje | Opekotine | RCT |

**Grunfeld 2006 (HIV+ RCT, JAIDS):**
- 20mg/dan × 12 nedelja
- Telesna masa +1.8 kg / BCM +0.9 kg (primarni ishodi; studija NIJE merila bilans azota)

**Schroeder 2005 (stariji muškarci):**
- Fiksno 20 mg/dan (10 mg × 2) × 6 nedelja — NIJE dozno-zavisna studija
- LBM +2.7±1.6 kg

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

**⚠️ HEPATOTOKSIČNOST (Kiracofe 2019, Burns; PMID 30545697)**

- Retrospektivna studija opekotina: transaminitis (AST/ALT >100) kod ~42% pacijenata na oksandrolonu
- Promene su uglavnom REVERZIBILNE po prestanku
- Ipak najniža hepatotoksičnost među oralnim 17-aa steroidima (delom bubrežni metabolizam)
- GGT / transaminaze monitoring obavezan

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

**🎯 PREPORUKA**

| Parametar | Vrednost |
|-----------|----------|
| Optimalna doza | 10-20 mg/dan |
| Trajanje | 8-12 nedelja |
| Rizik | **Minimalan** |
| Terapijski indeks | **Najviši** među oralnim |

**Prednosti:**
- Bubrežni metabolizam - najsigurniji za jetru
- Najviši terapijski indeks
- Ne aromatizuje
- Odličan za cutting

**📚 Ključne studije:**
- Grunfeld C, et al. J Acquir Immune Defic Syndr 2006;41:304-14 (PMID 16540931)
- Schroeder ET, et al. J Gerontol A Biol Sci Med Sci 2005 (PMID 16424293)
- Kiracofe et al. Burns 2019 (PMID 30545697) — hepatotoksičnost
                """.trimIndent()
            ),
            LearningArticleData(
                id = "stanozolol_dosing",
                title = "Stanozolol (Winstrol) - doziranje",
                content = """
**Stanozolol (Winstrol) - Klinički vodič**

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

**⛔ FATALNI SLUČAJEVI DOKUMENTOVANI**

Na 50mg EOD zabeleženi su FATALNI slučajevi peliosis hepatis i hepatocelularnog karcinoma!

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

**💊 FARMAKOLOGIJA**

| Parametar | Vrednost |
|-----------|----------|
| Poluživot | 9h (oralni) |
| Metabolizam | Jetreni (17-aa) |
| Detekcija | Do 2 meseca |

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

**📊 KARDIOVASKULARNI RIZIK**

**HDL SUPRESIJA: 49-90%**

| Doza | HDL pad | Klinički značaj |
|------|---------|-----------------|
| 0.5-2 mg/dan | Umeren | Prihvatljivo |
| 6 mg/dan | 33% | Povećan rizik |
| 20-50 mg/dan | 49-90% | **OZBILJAN RIZIK** |

**NAJGORI lipidni profil** među svim anaboličkim steroidima!

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

**⚠️ HEPATOTOKSIČNOST**

| Doza | ALT/AST | Holestaza | Rizik |
|------|---------|-----------|-------|
| 0.5-2 mg/dan | Blago ↑ | Low | Umeren |
| 20-50 mg/dan | Marked ↑ | High | **High (fatalni)** |

**Dokumentovani fatalni slučajevi:**
- Peliosis hepatis
- Hepatocelularni karcinom
- Holestaza

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

**🎯 PREPORUKA**

| Parametar | Vrednost |
|-----------|----------|
| Medicinska doza | 0.5-2 mg/dan |
| Indikacija | SAMO hereditarni angioedem (HAE) |
| Rizik | **V. High** |

**⛔ NE PREPORUČUJE SE za atletsku upotrebu!**

Stanozolol ima opravdanje SAMO za HAE management gde:
- Doza je 0.5-2 mg/dan
- Koristi se hronično pod nadzorom
- Benefiti prevazilaze rizike za tu specifičnu indikaciju

**📚 Ključne studije:**
- HAE studije (terapijske doze)
- Sloane DE, et al. J Allergy Clin Immunol 2007 (PMID 17765757) — dugoročna bezbednost stanozolola kod HAE
- Case reports fatalni ishodi
                """.trimIndent()
            ),
            LearningArticleData(
                id = "methandrostenolone_dosing",
                title = "Metandrostenolon (Dianabol) - doziranje",
                content = """
**Methandienone (Dianabol) - Klinički vodič**

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

**💊 FARMAKOLOGIJA**

| Parametar | Vrednost |
|-----------|----------|
| Poluživot | 3-6h |
| Metabolizam | Jetreni (17-aa) |
| Aromatizacija | VISOKA |

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

**📊 KLINIČKA EFIKASNOST (Hervey 1981 RCT)**

**100 mg/dan × 6 nedelja (RCT crossover, dizači tegova):**

| Parametar | Vrednost |
|-----------|----------|
| LBM | 2.3±0.4 kg |
| N retencija | 255±69 g ukupno |
| Populacija | Dizači tegova |
| Dizajn | RCT crossover |

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

**⚠️ HEPATOTOKSIČNOST**

| Parametar | Vrednost |
|-----------|----------|
| AST povišenje | 27% pacijenata |
| Holestaza | Mod-High |
| Rizik | **High** |

**Klinička doza:** 5-10 mg/dan
**Atletska doza:** do 100 mg/dan

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

**⚠️ HPTA SUPRESIJA (Holma 1976)**

Na samo 15mg/dan:
- Testosteron pada 69% (29.4→9.1 nmol/L)
- Za samo 2 meseca!

**OBAVEZNO koristi sa testosteron bazom!**

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

**🎯 PREPORUKA**

| Parametar | Vrednost |
|-----------|----------|
| Maksimalna doza | ≤25 mg/dan |
| Trajanje | 4 nedelje MAKSIMUM |
| Rizik | **High** |
| Klinička korist | **Ne preporučuje se** |

**Zašto ograničiti na 4 nedelje:**
- Značajna hepatotoksičnost
- 27% ima povišen AST
- Jako aromatizuje - problemi sa estrogenom

**📚 Ključne studije:**
- Hervey GR, et al. Clin Sci 1981 (PMID 7018798) — metandienon 100 mg/dan, crossover
- Holma 1976 (PMID: 793272)

**Monitoring:**
- Jetreni enzimi: svakih 2 nedelje
- Krvni pritisak: nedeljno
- Estradiol: ako simptomi
                """.trimIndent()
            ),
            LearningArticleData(
                id = "oxymetholone_dosing",
                title = "Oksimetolon (Anadrol) - doziranje",
                content = """
**Oxymetholone (Anadrol) - Klinički vodič**

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

**⛔ NAJHEPATOTOKSIČNIJI ORALNI STEROID!**

ALT/AST povišenje: **5× NORMALNE VREDNOSTI**

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

**💊 FARMAKOLOGIJA**

| Parametar | Vrednost |
|-----------|----------|
| Poluživot | 8-9h |
| Metabolizam | Jetreni (17-aa) |
| Detekcija | Do 2 meseca |

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

**📊 KLINIČKA EFIKASNOST**

**Twice-daily doziranje (kliničko):**

| Parametar | Vrednost |
|-----------|----------|
| Telesna masa | ~+3.5 kg (BCM ~+3.8 kg) |
| Populacija | HIV wasting (Hengge 2003) |

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

**⚠️ HEPATOTOKSIČNOST (Hengge 2003, oksimetolon HIV)**

| Doza | ALT/AST | Holestaza | Rizik |
|------|---------|-----------|-------|
| Klinička (1-2 mg/kg) | **↑↑** | V. High | V. High |
| Atletska (2-5 mg/kg) | Marked ↑ | V. High | **EKSTREMNO** |

**NAJGORI hepatotoksični profil:**
- ALT/AST/GGT prelazi 5× baseline kod ~25-43% pacijenata (Hengge)
- Visok rizik holestaze
- Dokumentovani fatalni slučajevi

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

**🎯 PREPORUKA**

| Parametar | Vrednost |
|-----------|----------|
| Maksimalna doza | 1-1.5 mg/kg/dan |
| Trajanje | 4-6 nedelja MAKSIMUM |
| Rizik | **V. High** |
| Klinička korist | **SAMO akutna upotreba** |

**Ako MORA da se koristi:**
- Striktno ograniči na 4-6 nedelja
- Intenzivan monitoring jetre svake 2 nedelje
- Prekini ODMAH ako GGT ili bilirubin porastu
- NIKAD za produžene cikluse

**📚 Ključne studije:**
- Hengge UR, et al. 2003 — oksimetolon kod HIV wasting (efikasnost i hepatotoksičnost)

**Monitoring:**
- Jetreni enzimi: svakih 2 nedelje - OBAVEZNO
- Bilirubin: baseline i svakih 2 nedelje
- Prekid pri bilo kakvom povišenju
                """.trimIndent()
            ),
            LearningArticleData(
                id = "fluoxymesterone_dosing",
                title = "Fluoksimesteron (Halotestin) - doziranje",
                content = """
**Fluoxymesterone (Halotestin) - orijentaciona tabela doza**

⚠️ Napomena: fluoksimesteron NEMA objavljenu humanu dozno-odgovor RCT studiju. Donja tabela je kvalitativna orijentaciona procena (farmakologija + klinička iskustva), NE izmereni podaci iz studije.

| Doza (mg/dan) | K+ ↓ | BP ↑ | Jetreni enzimi | Snaga ↑ | Nuspojave | Status |
|---------------|------|------|----------------|---------|-----------|--------|
| **10** | **Blago** | **Blago** | **Normalno** | **Značajno** | **Nisko** | **OPTIMALNO** |
| 20 | Umereno | Umereno | Blago ↑ | Visoko | Umereno | Dobro |
| 30+ | Visoko | Visoko | Umereno ↑ | Plateau | Visoko | Ne (>6 ned) |

**Tačka infleksije: 10-20 mg/dnevno**

**⚠️ POSEBAN RIZIK: Mineralokortikoidna aktivnost**
Fluoksimesteron inhibira 11β-HSD2 enzim, što uzrokuje:
• Retenciju natrijuma
• Gubitak kalijuma (hipokalemija)
• Hipertenziju
• Rizik od srčanih aritmija

**Trajanje ciklusa:** 6 nedelja MAKSIMUM

**Monitoring:**
• Elektroliti (Na+, K+): bazni i svake 3 nedelje
• Krvni pritisak: nedeljno
• Jetreni enzimi: svakih 4-6 nedelja
• EKG ako postoji kardio rizik
                """.trimIndent()
            ),
            LearningArticleData(
                id = "turinabol_dosing",
                title = "Turinabol (CDMT) - doziranje",
                content = """
**Turinabol (4-Chlorodehydromethyltestosterone) - Klinički vodič**

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

**💊 FARMAKOLOGIJA**

| Parametar | Vrednost |
|-----------|----------|
| Poluživot | **16h** (NAJDUŽI među oralnim) |
| Metabolizam | Jetreni (17-aa) |
| Detekcija | Do 12 meseci (metaboliti) |

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

**📊 KLINIČKA EFIKASNOST (Historijski podaci)**

| Doza | Trajanje | LBM | Populacija |
|------|----------|-----|------------|
| 10-15 mg/dan | 6-8 ned | Umereno | Historijski podaci |

**Napomena:** Podaci iz DDR atletskog programa - limitirana moderna literatura.

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

**⚠️ HEPATOTOKSIČNOST**

| Doza | ALT/AST | Holestaza | Rizik |
|------|---------|-----------|-------|
| Klinička (10-15 mg) | Mild-Mod | Moderate | Mod |
| Atletska (40-80 mg) | Mild-Mod | Moderate | **Mod-High** |

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

**⚠️ HPTA SUPRESIJA**

**Najizraženija HPTA supresija** među oralnim steroidima!

| Faktor | Značaj |
|--------|--------|
| Supresija | Ekstremna |
| Čekanje pre PCT | **2 nedelje** |
| Oporavak | Sporiji nego drugi oralni |

Zbog dugog poluživota (16h), potrebno je čekati 2 nedelje pre početka PCT-a!

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

**🎯 PREPORUKA**

| Parametar | Vrednost |
|-----------|----------|
| Optimalna doza | 30-50 mg/dan |
| Trajanje | 6 nedelja |
| Rizik | **Mod-High** |
| Klinička korist | Umeren |

**Posebna upozorenja:**
- Najduži poluživot = sporije čišćenje
- 2 nedelje čekanja pre PCT
- Najjača HPTA supresija među oralnim
- Detekcija metabolita do 12 meseci

**📚 Historijski podaci:**
- DDR atletski program dokumentacija

**Monitoring:**
- Jetreni enzimi: svakih 3 nedelje
- Ehokardiografija: bazna i post-ciklus
                """.trimIndent()
            ),
            LearningArticleData(
                id = "oral_comparison",
                title = "Oralni steroidi - uporedna analiza",
                content = """
**Oralni steroidi - Uporedna analiza kliničkih podataka**

Podaci iz peer-reviewed studija i kliničkih ispitivanja.

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

**📊 HEPATOTOKSIČNOST PO DOZAMA**

| Komponenta | Klinička doza | Atletska doza | ALT | AST | Holestaza | Rizik |
|------------|---------------|---------------|-----|-----|-----------|-------|
| Oxandrolone | 2.5-20 mg | 20-30 mg | +65.6% (rev.) | +44.5% | V. Low | Minimal |
| Oxymetholone | 1-2 mg/kg | 2-5 mg/kg | 5× ↑ | Marked ↑ | V. High | V. High |
| Stanozolol | 0.5-2 mg | 20-50 mg | Marked ↑ | Marked ↑ | High | High (fatalni) |
| Methandienone | 5-10 mg | 100 mg | 27% ↑ | Variable | Mod-High | High |
| Turinabol | 10-15 mg | 40-80 mg | Mild-Mod | Mild-Mod | Moderate | Mod-High |

**Ključno:**
- Oxandrolone ima najniži hepatotoksični rizik zbog bubrežnog metabolizma
- Oxymetholone je NAJHEPATOTOKSIČNIJI sa 5× povišenjem ALT
- Stanozolol ima dokumentovane FATALNE slučajeve

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

**📈 KLINIČKA EFIKASNOST (RCT podaci)**

| Komponenta | Doza | Trajanje | LBM gain | N retencija | Studija |
|------------|------|----------|----------|-------------|---------|
| Oxandrolone | 20 mg/dan | 12 ned | 1.3 kg | 5.5 g/dan | RCT |
| Oxandrolone | 20 mg/dan | 6 ned | 2.7 kg | + | RCT |
| Methandienone | 100 mg/dan | 6 ned | 2.3 kg | 255 g ukupno | RCT crossover |
| Oxymetholone | 100 mg/dan | - | ~3.5 kg | + | Kliničko (HIV) |
| Turinabol | 10-15 mg | 6-8 ned | Umereno | + | Historijski |
| Stanozolol | 0.5-2 mg | Hronično | N/A | N/A | HAE management |

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

**🎯 PREPORUČENE DOZE I TRAJANJE**

| Komponenta | Optimalna | Trajanje | Rizik | Klinička korist |
|------------|-----------|----------|-------|-----------------|
| Oxandrolone | 10-20 mg/dan | 8-12 ned | Minimal | Najviši TI |
| Oxymetholone | 1-1.5 mg/kg | 4-6 ned | V. High | Samo akutno |
| Methandienone | ≤25 mg/dan | 4 ned | High | Ne preporučuje se |
| Stanozolol | 0.5-2 mg/dan | - | V. High | Samo HAE |
| Turinabol | 30-50 mg/dan | 6 ned | Mod-High | Umeren |

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

**📋 RANKING PO TERAPIJSKOM INDEKSU**

1. **Oxandrolone** - Najviši TI, bubrežni metabolizam
2. **Turinabol** - Umeren TI, najduži poluživot
3. **Methandienone** - Nizak TI, visoka aromatizacija
4. **Oxymetholone** - V. nizak TI, ekstremna hepatotoksičnost
5. **Stanozolol** - Najniži TI za atletsku upotrebu

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

**⚠️ KLJUČNA UPOZORENJA**

**Oxandrolone:**
✓ Jedini oralni sa bubrežnim metabolizmom
✓ ALT promene reverzibilne do nedelje 20
✓ Najbolji izbor za produžene cikluse

**Oxymetholone:**
⛔ 5× ALT povišenje - NAJHEPATOTOKSIČNIJI
⛔ Samo za akutnu, kratkoročnu upotrebu
⛔ Dokumentovani fatalni slučajevi

**Stanozolol:**
⛔ HDL supresija 49-90% - NAJGORI lipidni profil
⛔ Fatalni slučajevi na 50mg EOD
⛔ Samo za HAE management

**Methandienone:**
⛔ 27% pacijenata ima povišen AST
⛔ Jako aromatizuje
⛔ Maksimum 4 nedelje

**Turinabol:**
⚠️ Najduži poluživot (16h)
⚠️ Najizraženija HPTA supresija
⚠️ 2 nedelje čekanja pre PCT

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

**📚 KLJUČNE STUDIJE**

- Grunfeld C, et al. J Acquir Immune Defic Syndr 2006;41:304-14 (PMID 16540931) — oksandrolon, HIV
- Schroeder ET, et al. J Gerontol A Biol Sci Med Sci 2005 (PMID 16424293) — oksandrolon
- Kiracofe et al. Burns 2019 (PMID 30545697) — oksandrolon, hepatotoksičnost
- Hervey GR, et al. Clin Sci 1981 (PMID 7018798) — metandienon
- HAE studije (Stanozolol terapijske doze)
                """.trimIndent()
            )
        )
    ),

    // 3. DOSING GENERAL - General Dosing Information
    LearningCategoryData(
        id = "dosing_general",
        title = "Doziranje - Opšte",
        icon = Icons.Default.Assessment,
        articles = listOf(
            LearningArticleData(
                id = "dosing_comparison",
                title = "Uporedna tabela doza",
                content = """
**Uporedna tabela preporučenih doza**

| Komponenta | Terapijska | Optimalna | Visoka | V. Visoka | Jedinica |
|------------|------------|-----------|--------|-----------|----------|
| Testosteron | 50-125 | **200-300** | 300-600 | 600+ | mg/ned |
| Nandrolon | 50-100 | **100-150** | 150-200 | 200+ | mg/ned |
| Oxandrolon | 5-10 | **10-20** | 20-30 | 40+ | mg/dan |
| Stanozolol | 0.5-2 | **1-3** | 4-6 | 6+ | mg/dan |
| Trenbolon | ⛔ | ⛔ | ⛔ | ⛔ | NE KORISTI |
| Boldenon | ⛔ | ⛔ | ⛔ | ⛔ | NED. POD. |

**Ključna zapažanja:**
• Terapijske doze su značajno niže od "optimalnih" doza za poboljšanje
• Svi spojevi pokazuju 2-3x raspon od optimalnog do "veoma visokog"
• Spojevi sa višim miotropnim/androgenim odnosom (oxandrolon, nandrolon) postižu efekte na nižim apsolutnim dozama
• Trenbolon i boldenon nemaju dovoljno podataka za preporuku

**Univerzalna U-kriva:**
Svi proučavani spojevi pokazuju obrazac gde:
1. Benefiti rastu do tačke infleksije
2. Zatim se usporavaju (plateau)
3. Dok nuspojave nastavljaju linearno da rastu

**Praktično pravilo:**
Udvostručenje doze NE udvostručuje rezultate. Primer: 125→300mg/ned dodaje samo +1.8 kg FFM, a 300→600mg +2.7 kg — uz nesrazmerno veći hematološki (hematokrit↑) i lipidni (HDL↓) rizik. (Bhasin 2001 ne daje jedinstven „×" faktor za nuspojave.)
                """.trimIndent()
            ),
            LearningArticleData(
                id = "monitoring_all",
                title = "Monitoring za sve AAS",
                content = """
**Strategija doziranja:**
• Nedeljne ili češće injekcije smanjuju peak nivoe vs. dvonedeljne ili mesečne
• Subkutana administracija može smanjiti peak-to-trough varijacije vs. IM
• Manji, češći bolusi = stabilniji nivoi = manji hematokrit

**Parametri za praćenje (važi za sve AAS):**

**Hematokrit:**
• Svake 12 nedelje
• Cilj: <54%
• Kritično za kardiovaskularnu bezbednost

**Lipidni panel:**
• Svakih 6-12 meseci
• Posebno važno za testosteron/nandrolon na dozama >150mg/ned
• HDL specifično: bazni i 12 nedelja

**Jetrena funkcija (AST, ALT, ALP):**
• Svakih 6-12 meseci za injektabilne
• **Svakih 3 meseca za oralne C17-alkilovane** (oxandrolon, stanozolol)

**Androgeno-responzivna tkiva:**
• PSA kod muškaraca >40
• Digitalni rektalni pregled ako porodična istorija ili povišen bazni PSA
• Testikularna zapremina ako se planira dugoročna upotreba

**Pomoćna sredstva (nisu zamena za smanjenje doze):**
• Inhibitori aromataze (anastrozol 0.5mg dnevno/EOD) za kontrolu estradiola
• SERM-ovi (tamoksifen) ako se pojavi ginekomastija
• Vitamin C — u studiji na PACOVIMA (120 mg/kg) delom koriguje imunsku disfunkciju od boldenona; nije potvrđena ljudska doza (120 mg/kg ≈ 8 g za 70 kg — ne prenosi se na ljude)

**Zlatno pravilo:**
Praćenje nikada ne zamenjuje razumno doziranje. Najbolja strategija je ostati u optimalnom opsegu gde je praćenje jednostavno jer su nuspojave retke.
                """.trimIndent()
            )
        )
    ),

    // 4. PCT - Evidence-Based (PubMed 2026)
    LearningCategoryData(
        id = "pct",
        title = "PCT - Naučno zasnovano",
        icon = Icons.Default.Refresh,
        articles = listOf(
            LearningArticleData(
                id = "pct_science",
                title = "Naučni dokazi za PCT",
                content = """
**Najnovija studija (BJU International 2026)**

King's College London i Ankara University testirali su 79 bodibildera:

| Tretman | 12-mes. normozoospermija | Testikularni volumen +20% |
|---------|--------------------------|---------------------------|
| Bez tretmana | 58.6% | 6.9% |
| Clomiphene 25mg | 69.2% | - |
| CC + hCG kombinovano | 87.5% ✓ | 70.8% ✓ |

**Ključno otkriće:**
Kombinovana terapija (Clomiphene + hCG) je superiorna za oporavak sperme (OR 6.23) i motilnost (OR 4.85).

**Clomiphene vs Tamoxifen**

Meta-analiza 1,279 pacijenata pokazala:
• Testosteron se povećava 2.60x (95% CI 1.82-3.38)
• Clomiphene je etabliran standard sa jasnim vremenskim okvirom
• Tamoxifen nema dovoljno moderne literature za PCT

**Spontani oporavak bez PCT:**
• Testosteron: 6-12 meseci
• Spermatogeneza: 1+ godina
• Sa PCT: Skraćeno na 3-6 meseci
                """.trimIndent()
            ),
            LearningArticleData(
                id = "pct_timing",
                title = "Zašto čekati 40-50 dana?",
                content = """
**Nije samo half-life!**

Testosteron Enanthate half-life je ~4.5 dana, a mean residence time (MRT) je ~8.5 dana.

**Problem 1: Pituitary desenzitizacija**
Studija Sheckter et al. pokazala:
• Tokom testosterona: LH smanjen 51%, FSH smanjen 70%
• Čak i sa normalnim GnRH signalima, pituitary NE MOŽE odgovoriti
• Potrebno 40-50 dana za potpuni oporavak

**Problem 2: Estradiol metaboliti**
• Testosteron se aromatizira u estradiol
• Estradiol ima SPORIJI clearance
• Još uvek suprimuje LH čak i kad je testosteron "očišćen"

**Problem 3: SHBG recikliranje**
• Testosteron se reciklira kroz SHBG i albumen
• Biološki efekti traju daleko duže od serumskog half-life

**Problem 4: Leydig ćelije**
• Dekompenzovane od supresije
• Potrebno vreme da se "probude"
• Čak i sa hCG, vraćaju se na samo 55-64% kapaciteta

**Ako počneš prerano:**
Clomiphene se "rasipa" - pituitary još nije reaktivan!
                """.trimIndent()
            ),
            LearningArticleData(
                id = "pct_protocol_new",
                title = "Optimalni PCT protokol",
                content = """
**Na osnovu nauke - NE bro-science!**

**KORAK 1: Izračunaj vreme čekanja**

| Ester | Poluvreme | Čekaj dana |
|-------|-----------|------------|
| Propionat | 0.8 dana | 7-10 |
| Phenylpropionat | 2.7 dana | 25-35 |
| Enanthate | 4.5 dana | 40-50 |
| Cypionate | 8 dana | 50-60 |
| Decanoate | 7.5 dana | 50-60 |
| Undecanoate | 20.9 dana | 70-90 |

**KORAK 2: Izaberi protokol**

**Prvi izbor - Clomiphene 25mg dnevno:**
• Jeftin, jednostavan, dokazan
• 69.2% normozoospermija sa CC monoterapijom u 12. mesecu (İbis 2025)

**Optimalni izbor - CC + hCG:**
• Clomiphene 25mg dnevno
• hCG 1500 IU subkutano 3x nedeljno
• 87.5% normozoospermija

**KORAK 3: Trajanje**
• Minimum: 3 meseca
• Idealno: 6 meseci za potpun oporavak sperme
• Kontrola testosterona na 6 nedelja, pa svake 4 nedelje

**Ako nema poboljšanja posle 3 meseca:**
Dodaj hCG ili rFSH (za FSH <1.5 IU/L)
                """.trimIndent()
            ),
            LearningArticleData(
                id = "pct_trajanje",
                title = "Koliko traje PCT? (naučni dokaz)",
                content = """
**Zašto 2-4 nedelje NIJE dovoljno**

Popularne bro-science šeme (npr. "Clomiphene 4 nedelje i gotovo") nemaju naučnu podlogu. Sve zvanične studije koriste PCT **mesecima**, ne nedeljama.

---

**STUDIJA 1: BJU International 2026**
(79 bodibildera, AAS ≤ 6 meseci)

• Doza: Clomiphene **25mg dnevno** ± hCG 1500 IU 3x/ned
• Praćenje: 12 meseci
• Testosteron se normalizovao kod SVIH do 6. meseca (čak i bez terapije)
• Ali sperma i testisi su se značajno bolje oporavili sa PCT-om
• **Tačno trajanje davanja leka nije navedeno u apstraktu** — ali praćenje je bilo 12 meseci

---

**STUDIJA 2: CloTASH protokol (Norveška, 2024)**
(10 muškaraca, prosečno 11 godina AAS upotrebe)

• Doza: Clomiphene **25mg svaki drugi dan**
• Trajanje: **16 nedelja (4 meseca)**
• Dodatno: Transdermalni testosteron prvih 4 nedelje, hCG po potrebi od 4. nedelje
• Rezultat: Porast LH, FSH i testosterona; oko polovine normalizovalo T

**Ovo je jedini javno objavljeni zvanični PCT protokol specifično za AAS korisnike.**

---

**STUDIJA 3: Fertilitetni protokol (PMC 2020)**
(Za muškarce koji žele trudnoću u narednih <6 meseci)

• Doza: Clomiphene **25mg dnevno** + hCG 3000 IU svaki drugi dan
• Trajanje: **Mesecima — dok se hormoni i spermiogram ne oporave**
• Kontrola: Spermiogram i hormoni svaka 2 meseca
• Ako FSH ne raste: umesto CC dodaje se rFSH 75 IU svaki drugi dan

---

**EKSPERTSKE SMERNICE**

**Anawalt 2019 (JCEM):**
• Kratkotrajno lečenje CC ili hCG (≤1 godina) verovatno pomaže
• Sve je off-label, bez čvrstih RCT-ova
• Terapija se prilagođava po nalazima i simptomima

**Rahnema 2014:**
• CC, hCG i po potrebi FSH
• Terapija mora biti individualizovana uz laboratorijsko praćenje

---

**SISTEMSKI PREGLED: Doze i trajanja kod muškaraca**

Meta-analiza 19 studija (1.279 pacijenata):
• Terapija trajala od **1,5 do 52 meseca**
• Najčešće doze: 25-50mg dnevno ili svaki drugi dan
• Značajan porast testosterona, LH i FSH
• Malo nuspojava

Konkretni protokoli iz literature:
| Protokol | Doza | Trajanje |
|----------|------|----------|
| Shabsigh et al. | 25mg/dan | 4-6 nedelja |
| Da Ros & Averbeck | 25mg/dan | 3 meseca |
| Guay et al. | 50mg 3x/ned | 4 meseca |
| CloTASH | 25mg EOD | 16 nedelja |
| Duže serije | do 100mg/dan | 1,5-2 godine |

---

**ZAKLJUČAK — Koliko realno traje PCT?**

Na osnovu SVIH zvaničnih podataka:

⚠️ **Minimum: 8-16 nedelja (2-4 meseca)**
✅ **Optimalno: 3-6 meseci** uz kontrole hormona
🔬 **Za fertilitet: mesecima, dok se spermiogram ne oporavi**

**Kad prestati?**
Kada su TT, LH i FSH stabilno u referentnom opsegu na **dve uzastopne kontrole** bez povećanja doze.

**NIKAD ne prekidaj na osnovu fiksnog broja nedelja** — uvek na osnovu laboratorijskih nalaza!

---

**VAŽNO:**
Bez nadzora endokrinologa/urologa nema sigurnog načina da sam znaš kad da staneš. Ovi podaci su informativni, ne zamena za lekarsku kontrolu.
                """.trimIndent()
            ),
            LearningArticleData(
                id = "pct_timeline",
                title = "Vremenski okvir oporavka",
                content = """
**Šta se dešava po danima (Enanthate/Cypionate)**

| Dani | Testosteron | Pituitary | Clomiphene? |
|------|-------------|-----------|-------------|
| 0-10 | 1000→400 ng/dL | SUPRIMOVAN | ❌ Ne |
| 10-20 | 400→200 ng/dL | SUPRIMOVAN | ❌ Ne |
| 20-35 | 200→100 ng/dL | Buđenje | ⚠️ Slabo |
| 35-50 | 100→30 ng/dL | REAKTIVAN | ✅ POČNI |

**Sa Clomiphenom - vremenski okvir:**

• **Nedelja 2-4:** LH i FSH počinju da se vraćaju
• **~6 meseci:** Kod većine hormonska normalizacija (İbis 2025: normalizacija u svim grupama do 6. meseca)
• **9-12 meseci:** Nastavak oporavka spermatogeneze i testikularnog volumena

**Očekivani rezultati (İbis 2025, rekreativni AAS korisnici):**
• Normozoospermija u 12. mesecu: 69.2% (CC), 87.5% (CC+hCG), 58.6% bez terapije
• CC+hCG nezavisno predviđa normozoospermiju (OR 6.23)

**VAŽNO:**
Najveći hormonski oporavak je u prvih 3 meseca, ali oporavak spermatogeneze i testikularnog volumena nastavlja da se poboljšava do 12 meseci sa terapijom. Trajanje PCT-a treba da se zasniva na laboratorijskim nalazima, ne na fiksnom broju nedelja.
                """.trimIndent()
            ),
            LearningArticleData(
                id = "hcg_new",
                title = "hCG - Naučno objašnjenje",
                content = """
**Zašto hCG u kombinaciji sa Clomiphenom?**

hCG direktno stimuliše Leydig ćelije (zaobilazi pituitary).
Clomiphene stimuliše pituitary da proizvodi LH/FSH.

**Zajedno:**
• hCG omogućava brzu androgen repleniju
• Clomiphene obnavlja prirodnu HPTA os
• Sinergijski efekat: 87.5% vs 69.2% oporavak

**Doziranje (na osnovu studija):**
• hCG: 1500 IU subkutano 3x nedeljno
• Ne koristiti hCG SAM - uvek sa SERM-om

**Mešanje hCG:**
5000 IU + 2mL bakteriostatične vode = 250 IU po 0.1mL

**Čuvanje:**
• Frižider nakon mešanja (u roku od 72h)
• Traje ~60 dana
• Posle toga gubi 10% potencije nedeljno

**RAZLIKA OD STAROG PROTOKOLA:**
Stari protokol: hCG PRE PCT-a, pa prekid
Novi protokol: hCG TOKOM PCT-a zajedno sa Clomiphenom

Nauka je pokazala da kombinovana upotreba daje bolje rezultate nego sekvencijalna!
                """.trimIndent()
            ),
            LearningArticleData(
                id = "pct_monitoring",
                title = "Praćenje oporavka",
                content = """
**Krvne analize tokom PCT-a**

**Šta testirati:**
• Testosteron (ukupni i slobodni)
• LH i FSH
• Estradiol (E2)
• SHBG
• Inhibin B (marker funkcije testisa)

**Kada testirati:**
• Baseline: Pre početka PCT-a
• 6 nedelja: Prva kontrola
• Zatim svake 4 nedelje

**Ciljne vrednosti:**
• Testosteron: >300 ng/dL (idealno >450)
• LH: 2-9 IU/L
• FSH: 1.5-12 IU/L
• Estradiol: 20-40 pg/mL

**Crvene zastavice:**
• FSH <1.5 IU/L → Dodaj rFSH 75 IU 3x nedeljno
• Testosteron ne raste posle 6 nedelja → Dodaj hCG
• Estradiol previsok → Razmotriti Anastrozole

**Spermatogram:**
• Uraditi na 6 i 12 meseci
• Normozoospermija: >15 miliona/mL, >40% pokretljivost

**Simptomi niskog testosterona tokom PCT-a:**
• Umor, depresija
• Gubitak libida
• Gubitak mišićne mase
→ Ovi simptomi su OČEKIVANI prvih 4-6 nedelja!
                """.trimIndent()
            ),
            LearningArticleData(
                id = "pct_testosterone",
                title = "PCT za Testosteron estere",
                content = """
**TESTOSTERON PROPIONAT (Test Prop)**

Half-Life: ~19 sati (0.8 dana)
Čist iz tela: 10-14 dana

| Dan | Akcija |
|-----|--------|
| 0 | Poslednja injekcija |
| 10-15 | POČNI Clomiphene 25mg |
| Ned. 2-3 | LH/FSH se vraćaju |
| Ned. 3-4 | Testosteron raste |
| Mesec 1-2 | Oporavak |

**TESTOSTERON ENANTHATE / CYPIONATE**

Half-Life: 4.5-5 dana
Mean Residence Time: ~8.5 dana
Čist iz tela: 40-50 dana

| Dan | Akcija |
|-----|--------|
| 0 | Poslednja injekcija |
| 20-40 | Pituitary počinje buđenje |
| 40-50 | POČNI Clomiphene 25mg |
| Ned. 2-4 | LH/FSH se vraćaju |
| Ned. 4-8 | Testosteron normalan |

**Zašto 40-50 dana?**
• SHBG recikliranje produžava efekat
• Pituitary desenzitizacija traje 40-50 dana
• Estradiol se čisti sporije

**TESTOSTERON UNDECANOAT (TU)**

Half-Life: 18-24 dana
Čist iz tela: 50-60 dana

| Dan | Akcija |
|-----|--------|
| 0 | Poslednja injekcija |
| 50-60 | POČNI Clomiphene 25mg |
| Mes. 2-4 | Testosteron normalan |
| Mes. 6-12 | Full recovery |

⚠️ NAJSPORIJI oporavak - može trajati 12-15 meseci!
                """.trimIndent()
            ),
            LearningArticleData(
                id = "pct_nandrolone",
                title = "PCT za Nandrolon (Deca/NPP)",
                content = """
**NANDROLON DECANOAT (Deca-Durabolin)**

Half-Life: 7-12 dana (ester), 4.3h (nandrolon)
Detekcija u serumu: 19+ nedelja!
Testikularni problem: Oštećuje TJP1 protein

| Dan | Akcija |
|-----|--------|
| 0 | Poslednja injekcija |
| 40-50 | POČNI CC 25mg + hCG 1500IU 3x/ned |
| Ned. 2-6 | LH/FSH se vraćaju |
| Mes. 1-3 | Testosteron vraćen, inhibin B još nizak |
| Mes. 6-12 | Full recovery |

⚠️ **Zašto je hCG preporučen sa Deca-om:**
• 19-nor metaboliti dugo perzistiraju
• hCG može ubrzati Leydig cell oporavak
• Sa CC+hCG normozoospermija 87.5% vs 69.2% samo CC (u 12. mesecu, İbis 2025)
• Dodavanje hCG poboljšava oporavak sperme i testikularni volumen

**NANDROLON PHENYLPROPIONAT (NPP)**

Half-Life: 2.7 dana (ester)
Detekcija: 8 nedelja (brže nego Deca)

| Dan | Akcija |
|-----|--------|
| 0 | Poslednja injekcija |
| 25-35 | POČNI CC 25mg + hCG 1500IU 3x/ned |
| Ned. 2-4 | LH/FSH se vraćaju |
| Mes. 1-3 | Testosteron vraćen |
| Mes. 3-6 | Sperma se vraća |

✓ Brži oporavak nego Deca, ali hCG i dalje kritičan!

**~12.5% ne dostigne normozoospermiju sa CC+hCG u 12. mesecu (İbis 2025)**
→ Kod FSH <1.5 IU/L može trebati rFSH 75IU 3x nedeljno
                """.trimIndent()
            ),
            LearningArticleData(
                id = "pct_trenbolone",
                title = "PCT za Trenbolon",
                content = """
**⚠️ TRENBOLON = EKSTREMNA SUPRESIJA**

Potencija: anabolički/androgeni rejting ~5× viši od testosterona (životinjski bioesej, ne klinička potentnost)
Ne aromatizira (nema estradiol)
Antiglucocorticoid: Smanjuje cortisol

**TRENBOLON ACETATE (Tren A)**

Half-Life: <3 dana
Čist iz tela: 15-20 dana

| Dan | Akcija |
|-----|--------|
| 0 | Poslednja injekcija |
| 10-20 | POČNI CC 25-37.5mg + hCG 1500IU 3x/ned |
| Ned. 2-4 | LH/FSH se vraćaju (SPORO) |
| Mes. 1-2 | Testosteron donje-normalno |
| Mes. 2-4 | Testosteron normalno |
| Mes. 4-12 | Sperma se vraća |

**TRENBOLON ENANTHATE (Tren E)**

Half-Life: 4-5 dana
Čist iz tela: 30-40 dana

| Dan | Akcija |
|-----|--------|
| 0 | Poslednja injekcija |
| 35-45 | POČNI CC 25-37.5mg + hCG 1500IU 3x/ned |
| Ned. 3-6 | LH/FSH se vraćaju |
| Mes. 2-4 | Testosteron normalno |
| Mes. 4-12 | Sperma se vraća |

**Zašto viša doza Clomiphena (37.5mg)?**
• Trenbolon je 5x potentniji
• Ekstremna pituitary supresija
• Trebaj jača stimulacija

**⚠️ Deo korisnika sporo/nepotpuno oporavi HPTA (nema pouzdanog % za „trajnu" supresiju iz studija)**
→ hCG i rFSH mogu biti potrebni; oslanjaj se na laboratorijske nalaze
                """.trimIndent()
            ),
            LearningArticleData(
                id = "pct_other_aas",
                title = "PCT za ostale AAS",
                content = """
**BOLDENONE UNDECYLENATE (EQ / Equipoise)**

Half-Life: ~14 dana
Čist iz tela: 40-50 dana
Aromatizira: DA

| Dan | Akcija |
|-----|--------|
| 0 | Poslednja injekcija |
| 40-50 | POČNI CC 25mg ± Anastrozole 0.5mg |
| Ned. 2-4 | LH/FSH se vraćaju |
| Mes. 1-3 | Testosteron vraćen |

⚠️ Anastrozole ako je estradiol previsok

---

**MASTERON PROPIONAT**

Half-Life: 2 dana
Ne aromatizira, blaga supresija

| Dan | Akcija |
|-----|--------|
| 0 | Poslednja injekcija |
| 10-15 | POČNI CC 25mg |
| Ned. 1-2 | LH/FSH se vraćaju |
| Ned. 2-4 | Testosteron vraćen |

**MASTERON ENANTHATE**

| Dan | Akcija |
|-----|--------|
| 0 | Poslednja injekcija |
| 35-45 | POČNI CC 25mg |
| Ned. 2-4 | LH/FSH se vraćaju |

---

**ORALNI: ANAVAR & WINSTROL**

Half-Life: ~9 sati
Brz metabolizam, blaga supresija

| Dan | Akcija |
|-----|--------|
| 0 | Poslednja doza |
| 5-10 | POČNI CC 25mg |
| Ned. 1-2 | LH/FSH se vraćaju |
| Ned. 2-3 | Testosteron vraćen |

✓ NAJBRŽI oporavak od svih AAS
                """.trimIndent()
            ),
            LearningArticleData(
                id = "pct_combinations",
                title = "PCT za kombinovane cikluse",
                content = """
**PRAVILO: Koristi NAJDUŽI ester kao referencu**

---

**Test E 500mg + Deca 300mg (Bulk)**

| Komponenta | PCT Faktor |
|------------|------------|
| Test E | 40-50 dana |
| Deca | Testikularni problem |

**PCT Plan:**
• Dani 40-50: POČNI CC 25mg + hCG 1500IU 3x/ned
• Mes. 1-3: Hormonski oporavak
• Mes. 6-12: Kompletan oporavak

---

**Test E 300mg + Tren E 300mg (Recomp)**

| Komponenta | PCT Faktor |
|------------|------------|
| Test E | 40-50 dana |
| Tren E | EKSTREMNA supresija |

**PCT Plan:**
• Dani 40-50: POČNI CC 25-37.5mg + hCG 1500IU 3x/ned
• Mes. 1-4: Sporiji oporavak
• Mes. 4-12: Spermatogeneza

---

**Test Prop + Mast Prop + Tren A (Cut)**

Svi brzi esteri!

**PCT Plan:**
• Dani 10-15: POČNI CC 25mg + hCG 1500IU 3x/ned
• Ned. 2-4: LH/FSH se vraćaju
• Mes. 1-2: Testosteron vraćen

---

**Test U + Deca (Sporiji Bulk)**

NAJDUŽI PCT!

**PCT Plan:**
• Dani 50-60: POČNI CC 25mg + hCG 1500IU 3x/ned
• Mes. 2-4: Hormonski oporavak
• Mes. 9-15: Kompletan recovery

⚠️ Recovery može trajati do 15 meseci!
                """.trimIndent()
            ),
            LearningArticleData(
                id = "pct_summary_table",
                title = "PCT tabela - svi AAS",
                content = """
**KOMPLETNA PCT TABELA**

| AAS | PCT Početak | Dužina | Dodatno |
|-----|-------------|--------|---------|
| Test Prop | 10-15 dana | 3-4 mes. | / |
| Test E/C | 40-50 dana | 4-6 mes. | / |
| Test U | 50-60 dana | 6-12 mes. | / |
| Deca | 40-50 dana | 6-9 mes. | +hCG! |
| NPP | 20-30 dana | 4-6 mes. | +hCG! |
| Tren A | 10-20 dana | 6-12 mes. | +hCG, ↑CC |
| Tren E | 35-45 dana | 6-12 mes. | +hCG |
| EQ | 40-50 dana | 4-6 mes. | ±AI |
| Mast P | 10-15 dana | 2-4 mes. | / |
| Mast E | 35-45 dana | 3-5 mes. | / |
| Anavar | 5-10 dana | 2-4 mes. | / |
| Winstrol | 5-10 dana | 2-4 mes. | / |

---

**BRZINA OPORAVKA:**

🟢 **BRZA:** Test Prop, Mast Prop, Oralni
🟡 **SREDNJA:** Test E/C, NPP, EQ, Mast E
🔴 **SPORA:** Test U, Deca, Tren

---

**KLJUČNE NAPOMENE:**

1. **Duži ester = Duži PCT**
2. **Trenbolon = Ekstremna supresija** (hCG obavezan)
3. **Nandrolon = Testikularni problem** (hCG obavezan)
4. **Kombinovani ciklusi = NAJDUŽI ester**
5. **~12.5% ne dostigne normozoospermiju** sa CC+hCG u 12. mesecu (İbis 2025) → rFSH kod FSH <1.5

---

**OBAVEZNO CLOMIPHENE 25mg!**
Viša doza NEMA dodatnog benefita
(osim za Tren - 37.5mg)
                """.trimIndent()
            )
        )
    ),

    // 5. FERTILITET - Evidence-Based (PubMed, WHO, Lancet)
    LearningCategoryData(
        id = "fertility",
        title = "Fertilitet (naučno)",
        icon = Icons.Default.ChildCare,
        articles = listOf(
            LearningArticleData(
                id = "fertility_decline",
                title = "Opadanje plodnosti tokom ciklusa",
                content = """
**Šta se dešava sa spermom od prvog dana ciklusa?**

Podaci dolaze iz WHO kontraceptivnih RCT-ova sa testosteron enantatom (200mg nedeljno) i modelovanja ~14.000 uzoraka sperme (Ly et al. 2005).

---

**VREMENSKI TOK OPADANJA SPERME**

Poluživot supresije spermatogeneze: **5,5 nedelja**

| Nedelja | Koncentracija sperme | Status |
|---------|---------------------|--------|
| 0 | ≥20-40 mil/mL | Normalan |
| 6-8 | <15 mil/mL | Oligozoospermija |
| 8-9 | <5 mil/mL | Teška oligozoospermija |
| 10 | <3 mil/mL | Teška oligozoospermija |
| 13 | <1 mil/mL | Gotovo azoospermija |
| 17 (prosek) | 0 | Azoospermija |

📚 *Ly LP, Liu PY, Handelsman DJ. Hum Reprod 2005; WHO Task Force, Lancet 1990*

---

**KOLIKO BRZO PO TIPU PROTOKOLA?**

| Protokol | Azoospermija | Vreme |
|----------|-------------|-------|
| TE 200mg/ned (WHO) | 65% u 6 mes. | Prosek 120±40 dana |
| TU 500mg/mes (kineski RCT) | 93% u 6 mes. | Medijana 108 dana |
| T + progestin (kombinovano) | >95% | Brže i dublje |

📚 *WHO Lancet 1990; Gu et al. JCEM 2009; Liu et al. JCEM 2008*

---

**KOLIKO NJIH NIKAD NE POSTANE AZOOSPERMIČNO?**

| Studija | Procenat koji NE dostiže azoospermiju |
|---------|--------------------------------------|
| WHO TE 200mg/ned | 35% (ali većina <3 mil/mL) |
| WHO oba ispitivanja | ~2% ne postigne ni ≤3 mil/mL |
| TU 500mg/mes (Kina) | 4,8% ne postigne ≤1 mil/mL |

Dakle, **5-35% nikad ne postigne potpunu azoospermiju**, ali skoro svi padnu u tešku oligozoospermiju.

📚 *WHO Task Force, Fertil Steril 1996; Liu JCEM 2008; Gu JCEM 2009*

---

**A KOD BODIBILDERA (observacione studije)?**

Cross-sectional studije na aktivnim AAS korisnicima:

• **Knuth 1989:** 41 bodibilder — 59% sa <20 mil/mL
• **Torres-Calleja 2001:** 15 korisnika — 3 azoospermije, 8 oligozoospermija, samo 2 normalna
• **HAARLEM studija:** Srednji broj tokom ciklusa: svega **4 mil** u ejakulatu

Tačna dinamika po nedeljama za realne AAS cikluse (visoke doze, stacking) **nije izmerena** u kontrolisanim uslovima.

📚 *Knuth, Andrologia 1989; Torres-Calleja, Life Sciences 2001; Shankara-Narayana 2020*
                """.trimIndent()
            ),
            LearningArticleData(
                id = "fertility_recovery",
                title = "Oporavak plodnosti posle ciklusa",
                content = """
**Koliko treba spermi da se vrati?**

Zavisi od toga koliko dugo si koristio i šta si koristio.

---

**KRATKOTRAJNA UPOTREBA (≤18 meseci, RCT podaci)**

Meta-analiza 30 studija, 1549 muškaraca (Liu et al. Lancet 2006):

| Mesec posle prestanka | % koji dostiže ≥20 mil/mL |
|----------------------|---------------------------|
| 3 | Medijana oporavka (3,4 mes.) |
| 6 | 67% |
| 12 | 90% |
| 16 | 96% |
| 24 | ~100% |

**Nijedan muškarac** nije ostao trajno neplodan u kratkoročnim studijama!

Medijane oporavka do specifičnih pragova:
• Do 3 mil/mL: **2,5 meseca**
• Do 10 mil/mL: **3,0 meseca**
• Do 20 mil/mL: **3,4 meseca**

📚 *Liu et al. Lancet 2006*

---

**WHO TE 200mg/ned — konkretno:**
• Od azoospermije do ≥20 mil/mL: **3,7 meseca**
• Do sopstvene bazalne vrednosti: **6,7 meseci**

📚 *WHO Task Force, Lancet 1990*

---

**DUGOGODIŠNJA AAS UPOTREBA (observaciono)**

Podaci iz kliničkih serija i HAARLEM studije:

| Parametar | Vrednost |
|-----------|---------|
| Oporavak AMH | ~7,3 meseca |
| Oporavak LH | ~10,7 meseci |
| Oporavak izlučivanja sperme (sperm output) | ~14,1 mesec |

📚 *Shankara-Narayana 2020 (JCEM)*

---

**KLJUČNA RAZLIKA:**

🟢 **Kratak ciklus (≤18 mes.):** Skoro svi se oporave za 1-2 godine
🔴 **Dugogodišnja upotreba:** Sporiji i ponekad nepotpun oporavak (bez pouzdanog % za „trajnu" subfertilnost)

📚 *Liu Lancet 2006; McBride/Samplaski 2016; Clinician's Guide 2025*
                """.trimIndent()
            ),
            LearningArticleData(
                id = "fertility_pct_impact",
                title = "Uticaj PCT-a na fertilitet",
                content = """
**Da li PCT ubrzava oporavak sperme?**

Kratko: DA, ali ne garantuje potpun oporavak.

---

**SA hCG + SERM TERAPIJOM**

**Wenker 2015 (49 muškaraca, azo/teška oligo tokom TRT/AAS):**
• Terapija: hCG 3000 IU EOD + CC/tamoxifen/anastrozol
• **95,9%** imalo povratak sperme
• Prosečno vreme do prvog pojavljivanja: **4,6 meseca**
• Prosečna prva koncentracija: **22,6 mil/mL**

📚 *Wenker et al. J Sex Med 2015*

---

**Kohn 2017 (66 muškaraca posle TRT):**
• Terapija: hCG + SERM
• **70%** dostiglo TMC >5 miliona
• Starost i trajanje TRT-a **značajno produžavaju** oporavak

📚 *Kohn et al. Fertil Steril 2017*

---

**Fertility Outcomes 2023 (38 muškaraca posle AAS):**
• Terapija: CC 50mg EOD + hCG 2000 IU 3x/ned, medijana 5 meseci
• Od 18 inicijalno azoospermičnih:
  - 33,3% → teška oligozoospermija (<5 mil/mL)
  - 33,3% → oligozoospermija (<15 mil/mL)
  - 5,6% → normozoospermija (>15 mil/mL)
  - **27,8% ostaje azoospermično** posle 6 meseci
• ~38% parova ostvarilo trudnoću (2/3 prirodno, 1/3 IUI/IVF)

📚 *Fertility and Sterility 2023*

---

**hCG + rFSH vs hCG + Clomiphene (Campbell 2021):**

| Kombinacija | Prosečno vreme do oporavka |
|-------------|---------------------------|
| hCG + rFSH | 6,4 meseca |
| hCG + Clomiphene | 14,8 meseci |

rFSH je skuplji ali potencijalno brži.

📚 *Campbell et al. 2021*

---

**SUMARNO:**

• PCT (hCG ± CC) vraća spermu kod **70-96%** u 4-6 meseci
• Ali **potpuna normozoospermija** samo kod manjeg dela dugogodišnjih korisnika
• **27-32% dugogodišnjih korisnika** može ostati azoospermično i sa terapijom
• PCT ne zamenjuje vreme — ali ga skraćuje
                """.trimIndent()
            ),
            LearningArticleData(
                id = "fertility_risk_factors",
                title = "Faktori rizika za neplodnost",
                content = """
**Ko je u većem riziku za spor ili nepotpun oporavak?**

---

**1. TRAJANJE UPOTREBE**

Najvažniji faktor. Podaci:
• ≤18 meseci (RCT): **100%** oporavak do 24 meseca
• >1 godina AAS: Značajno raste rizik za spor oporavak
• >2,5 godine: ~27% bivših korisnika i dalje ima hipogonadizam i smanjene testise
• Svaki dodatni **mesec** TRT/AAS produžava vreme do oporavka

📚 *Liu Lancet 2006; Kohn F&S 2017; Rasmussen & Kanayama*

---

**2. TIP AAS**

| Kategorija | Rizik | Objašnjenje |
|------------|-------|-------------|
| Sam testosteron | Umeren | Najbolje proučen, najpredvidljiviji oporavak |
| Testosteron + progestin | Viši | Brža/dublja supresija, nešto sporiji oporavak |
| 19-nor (nandrolon, trenbolon) | Visok | Snažna progestinska aktivnost, teoretski jače potiskivanje |

⚠️ **Nema direktnih RCT-ova** koji porede oporavak po pojedinačnom AAS-u kod ljudi!

📚 *Liu JCEM 2008; Al Hashimi 2025*

---

**3. FARMAKOKINETIKA ESTRA**

• Kraće-delujući esteri → **brži oporavak**
• Dugodelujući esteri (undecanoat, decanoat) → sporiji oporavak
• Razlog: duže zadržavanje u telu = duža supresija

📚 *Liu Lancet 2006*

---

**4. STAROST**

• Svaka dodatna godina života **produžava** vreme do oporavka
• Mlađi muškarci se generalno brže supresuju ALI i brže oporave
• Efekat starosti je stabilniji prediktor nego trajanje upotrebe

📚 *Kohn F&S 2017; Liu JCEM 2008*

---

**5. BAZALNA FUNKCIJA TESTISA**

| Faktor | Uticaj |
|--------|--------|
| Viši početni broj spermatozoida | Brži oporavak |
| Veći testisi pre ciklusa | Brži oporavak |
| Prethodna subfertilnost | Sporiji oporavak |
| Manji bazalni volumen testisa | Lošija prognoza |

📚 *Liu JCEM 2008; Clinician's Guide 2025*

---

**6. BROJ PRETHODNIH CIKLUSA**

Nema direktnih kvantitativnih studija, ali klinički vodiči navode da višestruki ciklusi nose kumulativan rizik za trajno oštećenje Leydig i Sertoli ćelija.

📚 *McBride/Samplaski 2016; Al Hashimi 2025*

---

**KO JE NAJUGROŽENIJI?**

🔴 Dugogodišnja upotreba + 19-nor AAS + stariji + mali testisi = **najveći rizik za trajnu neplodnost**

🟢 Kratak ciklus + sam testosteron + mlad + normalan spermiogram pre = **odlična prognoza**
                """.trimIndent()
            ),
            LearningArticleData(
                id = "fertility_visual_timeline",
                title = "Vizuelni tok fertiliteta",
                content = """
**OD POČETKA CIKLUSA DO POTPUNOG OPORAVKA**

Podaci za tipičan scenario: testosteron enantat ~200mg/ned, normalan početni spermiogram.

---

**▼ TOKOM CIKLUSA (opadanje)**

| Period | Sperma (mil/mL) | Status |
|--------|-----------------|--------|
| Nedelja 0 | 40-80 | ✅ Normalan |
| Nedelja 4 | ~20-30 | ⚠️ Pad počeo |
| Nedelja 6-8 | <15 | 🟡 Oligozoospermija |
| Nedelja 10 | <3 | 🟠 Teška oligozoospermija |
| Nedelja 13 | <1 | 🔴 Skoro azoospermija |
| Nedelja 17+ | 0 | ⛔ Azoospermija (65-93%) |

---

**▲ POSLE PRESTANKA — KRATAK CIKLUS (≤18 mes.)**

Bez PCT, na osnovu meta-analize 1549 muškaraca:

| Mesec | Sperma (mil/mL) | Status | % oporavljenih |
|-------|-----------------|--------|----------------|
| 0 | 0 | ⛔ Azoospermija | 0% |
| 2,5 | >3 | 🔴 Vraća se | - |
| 3 | >10 | 🟠 Raste | - |
| 3,4 | >20 | 🟡 Medijana oporavka | ~50% |
| 6 | >20 | ✅ Normalan | 67% |
| 12 | >20 | ✅ Normalan | 90% |
| 16 | >20 | ✅ Normalan | 96% |
| 24 | >20 | ✅ Potpun | ~100% |

📚 *Liu et al. Lancet 2006*

---

**▲ POSLE PRESTANKA — DUGOGODIŠNJA UPOTREBA**

| Mesec | Šta se dešava |
|-------|---------------|
| 0 | Azoospermija / teška oligozoospermija |
| ~4-5 | Kod većine prvo pojavljivanje sperme (Wenker 2015) |
| ~7-11 | Oporavak AMH/LH (Shankara-Narayana) |
| ~14 | Oporavak izlučivanja sperme (Shankara-Narayana) |
| 24-36 | Kod dugogodišnjih korisnika sporiji oporavak |

📚 *Shankara-Narayana 2020; Wenker 2015*

---

**▲ SA PCT-om (hCG + CC)**

| Mesec | Šta se dešava |
|-------|---------------|
| 0 | Početak PCT-a |
| 2-3 | Sperma počinje da se pojavljuje |
| 4-5 | Prosečno prvi broj sperme (~22,6 mil/mL) |
| 6 | 70-96% ima spermu u ejakulatu |
| 12 | 87,5% normozoospermija (CC+hCG, kratak ciklus) |

📚 *Wenker 2015; BJU International 2026*

---

**DOZNA ZAVISNOST — PARADOKS**

• Niže doze T u kombinovanim režimima → **potpunija** supresija
• Više doze ponekad „probijaju" negativnu povratnu spregu
• Za mega-doze AAS (višestruko iznad kontraceptivnih) **nema kvantitativnih podataka**

📚 *Liu JCEM 2008*

---

**ŠTA NEDOSTAJE U NAUCI (do 02/2026):**

❌ Precizna dinamika za nandrolon/trenbolon po nedeljama — nema humanih studija
❌ Direktno poređenje brzine oporavka po pojedinačnom AAS-u — nema
❌ RCT koji poredi PCT protokole kod AAS korisnika — nema (samo retrospektivne serije)
                """.trimIndent()
            )
        )
    ),

    // 6. CYCLES - Evidence-Based (PubMed)
    LearningCategoryData(
        id = "cycles",
        title = "Ciklusi (naučno)",
        icon = Icons.Default.Loop,
        articles = listOf(
            LearningArticleData(
                id = "optimal_testosterone_dose",
                title = "Optimalna doza testosterona",
                content = """
**Ključni nalaz: 125mg/nedeljno kao tačka preokreta**

Bhasin et al. (2001, Am J Physiol Endocrinol Metab) i kasnija istraživanja pokazuju da **125mg testosteron enantata nedeljno** predstavlja optimalnu tačku gde:
• Dobici u mišićnoj masi još uvek rastu linearno
• Neželjeni efekti ostaju na bazalnom nivou
• Odnos rizik/korist je najpovoljniji

**Dozno-zavisni odgovor (20 nedelja, Bhasin 2001):**

| Doza | FFM promena |
|------|-------------|
| 25mg | ns (bez zn. promene) |
| 50mg | ns (bez zn. promene) |
| 125mg | +3.4 kg |
| 300mg | +5.2 kg |
| 600mg | +7.9 kg |

Snaga na leg press-u raste dozno-zavisno; studija ne daje pouzdane kg vrednosti po dozi (i meri leg press, ne bench).

**Zašto ne viša doza?**

Iznad 125mg, kriva dobiti postaje sve ravnija dok neželjeni efekti rastu eksponencijalno:
• Androgene (akne) i estrogene (ginekomastija) nuspojave: rastu sa dozom — Bhasin 2001 NE daje incidencu po dozi
• Supresija HDL: linearno pada sa dozom
• Eritrocitoza: rizik raste sa dozom (izraženo na 300-600mg)

**Zaključak:**
125mg/nedeljno daje ~40% maksimalnih dobitaka uz <10% neželjenih efekata. Udvostručavanje na 250mg ne udvostručava dobitke, ali udvostručava rizike.

📚 *Bhasin S, et al. Am J Physiol Endocrinol Metab 2001;281:E1172-81 (PMID 11701431)*
                """.trimIndent()
            ),
            LearningArticleData(
                id = "dose_response_science",
                title = "Nauka o dozno-zavisnom odgovoru",
                content = """
**Logaritamski, ne linearni odgovor**

Odnos doza-efekat prati **logaritamsku krivu**:
• Prve doze daju najveće efekte
• Svako sledeće povećanje daje manje
• Plato se dostiže na ~600mg

**Klinički dokazi (Bhasin 2001):**

Promena u % od maksimuma (FFM, od 600mg):
• 125mg = 43% (3.4/7.9)
• 300mg = 66% (5.2/7.9)
• 600mg = 100%

**Nuspojave (dozno-zavisan smer — Bhasin 2001 ne daje % po dozi):**

• Akne / androgene nuspojave: rastu sa dozom
• Ginekomastija: verovatnija na višim dozama (više aromatizacije)
• HDL: pada dozno-zavisno

**Praktična primena:**

"Više = bolje" NE važi za AAS:
• 2x doza ≠ 2x rezultati
• 2x doza → nesrazmerno veći rizik (nema jedinstvenog „×" faktora iz studije)
• Optimum za većinu: 100-200mg/nedeljno

📚 *Bhasin S, et al. Am J Physiol Endocrinol Metab 2001;281:E1172-81 (PMID 11701431)*
                """.trimIndent()
            ),
            LearningArticleData(
                id = "first_cycle_science",
                title = "Prvi ciklus - naučni pristup",
                content = """
**Zašto testosteron kao jedino jedinjenje?**

1. **Najbolje proučen** - 50+ RCT studija
2. **Predvidljiv odgovor** - jasna dozno-zavisna kriva
3. **Bioidentičan** - telo već zna kako da ga metaboliše
4. **Merljiv** - jednostavno praćenje krvnom slikom

**Preporučeni protokol (na osnovu RCT podataka):**

• **Jedinjenje**: Testosteron Enanthate
• **Doza**: 100-150mg nedeljno
• **Trajanje**: 12-16 nedelja (studije koriste 10-20)
• **Frekvencija**: 2x nedeljno za stabilne nivoe

**Očekivani rezultati (RCT podaci, 125mg):**
• FFM: +3.4 kg
• Snaga na leg press-u raste (studija meri leg press, ne bench)
• Neželjeni efekti: minimalni na ovoj dozi

**Zašto NE 500mg?**

Tradicionalna "500mg prvi ciklus" preporuka:
• Nema RCT podršku
• Potiče iz bodibilding foruma
• Više doze = više nuspojava (akne, estrogene), bez srazmerne dodatne koristi
• Oporavak HPTA teži sa višim dozama

📚 *Bhasin S, et al. Am J Physiol Endocrinol Metab 2001 (PMID 11701431)*
                """.trimIndent()
            ),
            LearningArticleData(
                id = "esters_science",
                title = "Farmakokinetika estera",
                content = """
**Poluvremena eliminacije (klinički podaci)**

| Ester | Poluvreme | Cmax (dani) | Frekvencija |
|-------|-----------|-------------|-------------|
| Propionat | 0.8 dana | 1 | Dnevno/EOD |
| Phenylpropionat | 2.7 dana | 1-2 | EOD |
| Enanthate | 4.5 dana | 2-3 | 2x nedeljno |
| Cypionate | 8.0 dana | 3-4 | Nedeljno |
| Decanoate | 7.5 dana | 3-4 | Nedeljno |
| Undecanoate (IM) | 20.9 dana | 7-10 | Na 10-14 dana |

**Zašto enanthate/cypionate za cikluse?**

• Optimalan balans stabilnosti i fleksibilnosti
• 2x nedeljno održava varijaciju <20%
• PCT nakon 5-7 nedelja (clearance + pituitary oporavak)
• Najbolje proučeni u RCT studijama

**Matematika doziranja:**

Za 125mg nedeljno sa enanthatom:
• 62.5mg ponedjeljak + 62.5mg četvrtak
• Ili: 42mg pon/sre/pet za još stabilnije nivoe

**Čekanje za PCT:**
• Enanthate: 40-50 dana (clearance ~3 nedelje, ali pituitary oporavak zahteva duže)
• Undecanoate: 6-8 nedelja

📚 *Behre HM, et al. J Clin Endocrinol Metab 2004*
                """.trimIndent()
            ),
            LearningArticleData(
                id = "other_compounds_rct",
                title = "Druga jedinjenja - RCT podaci",
                content = """
**Nandrolone Decanoate (Deca)**

Studije na HIV pacijentima i starijima:
• **100mg/nedeljno**: +3.2 kg telesne mase (DXA čista masa ~3.9 kg), 12 nedelja (Sattler)
• Optimum: 100mg - iznad toga rizik/korist opada

Neželjeni efekti:
• Prolaktin ↑ pri >100mg
• Snažnija HPTA supresija od testosterona
• Dugo poluvreme 19-nor metabolita (~9-12 meseci detekcije)

**Oxandrolone (Anavar)**

• **20mg/dnevno**: +1.8 kg telesne mase / BCM +0.9 kg (12 nedelja, HIV — Grunfeld)
• Minimalna hepatotoksičnost do 20mg
• Iznad 40mg: ALT raste >3x
• Jedini oral sa RCT podacima o bezbednosti

**Stanozolol (Winstrol)**

• **6mg/dnevno**: Dokazana efikasnost (hereditarni angioedem)
• SHBG ↓↓ (dramatično sniženje)
• Hepatotoksičnost: dozno-zavisna

**Trenbolone i Boldenone**

⚠️ **NEMA humanih RCT studija**
• Svi podaci iz životinjskih studija ili anegdota
• Trenbolon: 5x androgena potentnost (in vitro)
• Boldenone: dugo poluvreme, minimalni podaci
• Korišćenje = preuzimanje nepoznatog rizika

📚 *Sattler FR, et al. JCEM 1999 (PMID 10199766); Grunfeld C, et al. J Acquir Immune Defic Syndr 2006 (PMID 16540931)*
                """.trimIndent()
            ),
            LearningArticleData(
                id = "cycle_length_science",
                title = "Dužina ciklusa - naučni dokazi",
                content = """
**Optimalna dužina ciklusa**

RCT studije koriste različite dužine:
• **10 nedelja**: Značajni dobici već vidljivi
• **16 nedelja**: Maksimalni FFM dobici u većini studija
• **20 nedelja**: Minimalno dodatno poboljšanje

**Bhasin studija (testosteron, 600mg):**

Studija je merila sastav tela na početku i na 20. nedelji: FFM +7.9 kg. NEMA objavljene FFM tačke za 8. i 16. nedelju u toj studiji — vremenski tok je opšta klinička procena, ne izmereni podaci.

Zaključak: većina dobitaka verovatno dolazi u prvih 12-16 nedelja (procena).

**HPTA oporavak vs dužina ciklusa:**

• 8-10 nedelja: Brži oporavak (4-8 nedelja PCT)
• 12-16 nedelja: Standardni oporavak (6-12 nedelja)
• 20+ nedelja: Prolongirani oporavak, veći rizik od trajne supresije

**Preporuka zasnovana na dokazima:**

• **Prvi ciklus**: 12 nedelja (dovoljna dužina za procenu odgovora)
• **Kasniji ciklusi**: Do 16 nedelja
• **Iznad 16 nedelja**: Marginalni dobici, značajno veći rizik

📚 *Bhasin S, et al. Am J Physiol Endocrinol Metab 2001 (PMID 11701431)*
                """.trimIndent()
            ),
            LearningArticleData(
                id = "stacking_evidence",
                title = "Kombinovanje jedinjenja - dokazi",
                content = """
**Da li kombinovanje daje bolje rezultate?**

⚠️ **Nema RCT studija koje porede stakove sa monoterapijom!**

Teoretski argumenti ZA:
• Različiti mehanizmi delovanja
• Sinergija (npr. androgeno + anabolično)
• Niže doze svakog = manje specifičnih NE

Teoretski argumenti PROTIV:
• Neželjeni efekti se sabiraju ili množe
• Nemoguće identifikovati izvor problema
• Veća opterećenje jetre (kod orala)
• Komplikovaniji PCT

**Šta znamo iz studija:**

Testosteron + Nandrolone (HIV studije):
• Pokazuje sinergetski efekat na FFM
• ALI: značajno veća supresija HPTA
• ALI: veći rizik od prolaktinskih NE

**Preporuka:**

1. Monoterapija testosteronom za prve 2-3 ciklusa
2. Ako dodajete jedinjenje, samo JEDNO novo po ciklusu
3. Dozvolite 2+ ciklusa između dodavanja novih
4. Vodite detaljne beleške o odgovoru

📚 *Sattler FR, et al. Clin Infect Dis 2002*
                """.trimIndent()
            )
        )
    ),

    // 6. ESTROGEN - Evidence-Based (PubMed)
    LearningCategoryData(
        id = "estrogen",
        title = "Estrogen (naučno)",
        icon = Icons.Default.Balance,
        articles = listOf(
            LearningArticleData(
                id = "estrogen_physiology",
                title = "Fiziologija estrogena kod muškaraca",
                content = """
**Normalni nivoi estradiola (E2)**

• Zdravi muškarci: **10-60 pg/mL**
• Fertilni muškarci: 10-82 pg/mL
• Optimalno na TRT: sredina 20-ih pg/mL

**T:E2 odnos**

Normalan odnos testosteron:estradiol je **~10:1**
• Studija fertilnih muškaraca: 20-ti percentil T:E2 = 9.8
• Odnos <10 može ukazivati na relativno visok estrogen

**Efekti visokog estrogena:**
• Proliferacija tkiva dojke (ginekomastija)
• Retencija tečnosti (vazopresin, RAA sistem)
• Pro-trombotičke promene (↑koagulacioni faktori)
• Nepovoljno sniženje HDL holesterola

**Efekti niskog estrogena:**
• Smanjena mineralna gustina kostiju
  - Muškarci u najvišem E2 kvartilu imaju značajno višu BMD
• Lošiji lipidni profil
• Mogući uticaj na raspoloženje i zdravlje zglobova
• AI inhibicija povećava markere koštane resorpcije

⚠️ **Cilj**: Održati E2 u optimalnom opsegu, ne prenisko!

📚 *J Clin Endocrinol Metab; Fertil Steril*
                """.trimIndent()
            ),
            LearningArticleData(
                id = "aromatization_kinetics",
                title = "Kinetika aromatizacije",
                content = """
**Konverzija T→E2**

Testosteron se konvertuje u estradiol enzimom aromatazom u masnom i drugim tkivima.

**Michaelis-Menten kinetika:**
• Km aromataze je u opsegu desetina nM (substrat-zavisno)
• Konverzija je dozno-zavisna ali **saturabilna**

**Šta to znači u praksi:**

Kako doza testosterona raste:
• Apsolutni E2 raste (više supstrata = više E2)
• ALI relativna frakcija aromatizacije OPADA
• E2/T odnos se SMANJUJE na visokim dozama T

**Primer:**
| Doza T | E2 (apsolutno) | E2/T odnos |
|--------|----------------|------------|
| Niska | Nizak | Viši |
| Srednja | Srednji | Srednji |
| Visoka | Visok | Niži |

**Klinička implikacija:**

Na nižim dozama testosterona, veći procenat se aromatizuje → AI može biti potrebniji nego što se očekuje.

Na visokim dozama, manje se proporcionalno aromatizuje, ali apsolutna količina E2 može i dalje biti visoka.

📚 *J Clin Endocrinol Metab; Steroids*
                """.trimIndent()
            ),
            LearningArticleData(
                id = "ai_clinical_evidence",
                title = "Aromataza inhibitori - klinički dokazi",
                content = """
**Pregled AI lekova (treća generacija)**

| AI | Tip | Doza (muškarci) | E2 promena | T promena |
|----|-----|-----------------|------------|-----------|
| Anastrozol | Nesteroidni | 0.5-1mg na 2-3 dana | ~50%↓ | ~25%↑ |
| Exemestan | Steroidni | 12.5-25mg dnevno | ~33%↓ | ~56%↑ |
| Letrozol | Nesteroidni | 2.5mg dnevno | >80%↓ | ↑↑ |

**Anastrozol (Arimidex)**

Studija na gojaznim hipogonadnim muškarcima (BMI ~30):
• 1mg dnevno tokom 5 meseci
• E2: 32 → 16 pg/mL (prepolovljen)
• T: 271 → 412 ng/dL
• Bioraspoloživi T udvostručen kod starijih muškaraca

Efekti: ↑LH/FSH, skroman porast markera koštane resorpcije
12 nedelja: normalizovao T bez promene lipida ili inflamatornih markera

**Exemestan (Aromasin)**

Steroidni, ireverzibilni ("suicidalni") inhibitor
• 25-50mg dnevno tokom 10 dana:
  - E2 snižen 33-38%
  - T povećan 56-60%
• Lipidi i IGF-1 nepromenjeni (parcijalna androgenost štiti lipide)
• Bioraspoloživost niska (~5%), ali ↑40% sa hranom

**Letrozol**

Najpotentniji AI - može sniziti E2 praktično na nulu (<3 pg/mL)
⚠️ Rizik od prekomernog sniženja → gubitak kosti, bol u zglobovima
Koristi se samo za teške slučajeve ili kratke kurseve

📚 *J Clin Endocrinol Metab; Cancer; Clin Ther*
                """.trimIndent()
            ),
            LearningArticleData(
                id = "serms_clinical_evidence",
                title = "SERM - klinički dokazi",
                content = """
**Mehanizam delovanja**

SERM-ovi se vezuju za estrogenske receptore:
• **Antagonisti** u nekim tkivima (dojka)
• **Agonisti** u drugim (kost)
• NE snižavaju serumski E2, ali blokiraju efekat

**Tamoxifen (Nolvadex)**

Antagonist u dojci, agonist u kosti

Ukrštena studija kod muškaraca:
• Značajno povećava LH, FSH i testosteron
• T↑40% (vs 25% sa raloksifenom)
• IGF-1 ↓25%
• SHBG ↑20%

Doza: 10-20mg dnevno
Dugoročna bezbednost prihvatljiva; rizik od tromboembolije <1-2%

**Clomiphene Citrate (Clomid)**

Sadrži dva izomera:
• **Enclomifen**: čist antagonist
• **Zuclomifen**: slab agonist

Muškarci sa niskim T na klomifenu:
• Prosečan porast T: **~160%**
• Nuspojave retke (<10%)
• Čuva fertilitet (za razliku od egzogenog T)

FDA odobren za žensku neplodnost, off-label za muški hipogonadizam

**Raloxifen**

Antagonist u dojci i uterusu, agonist u kosti
• Povećava LH/T, ali manje nego tamoksifen
• NE smanjuje IGF-1 (za razliku od tamoksifena)
• Može bolje štititi kost
• Nije FDA odobren za muškarce

📚 *J Clin Endocrinol Metab; Fertil Steril*
                """.trimIndent()
            ),
            LearningArticleData(
                id = "gynecomastia_science",
                title = "Ginekomastija - patofiziologija i lečenje",
                content = """
**Patofiziologija**

Proliferacija duktnog tkiva dojke zbog estrogenske stimulacije koja prevazilazi androgensku inhibiciju.

Uzroci:
• Visoka periferna aromatizacija (gojaznost, egzogeni androgeni)
• Nizak testosteron (hipogonadizam)
• 95% pubertetske ginekomastije je fiziološka (prolazno ↑E2/T)

**Prirodni tok**

• Pubertetska ginekomastija često se povlači za ~1-2 godine
• Ako traje >12 meseci → stromalna fibroza
• Hronična ginekomastija (>1 god): medicinski tretman obično neefikasan → hirurgija

⏰ **Prozor za lečenje: prvih 6-12 meseci od početka!**

**Medicinska terapija - efikasnost:**

| Tretman | Doza | Poboljšanje | >50% redukcija |
|---------|------|-------------|----------------|
| Tamoksifen | 10-20mg/dan | 86% | 41% |
| Raloksifen | 60mg/dan | 91% | 86% |
| Anastrozol | 1mg/dan | Ograničeno | Inferioran |
| Letrozol | 2.5mg/dan | Anegdotalno | - |

**Ključni nalaz:**

Meta-analiza kod pacijenata sa karcinomom prostate:
• Tamoksifen 20mg: značajno smanjio ginekomastiju (RR ~0.07 vs placebo)
• Anastrozol: NIJE značajno sprečio niti reverzovaov ginekomastiju

**Zaključak**: SERM-ovi (tamoksifen/raloksifen) su prvi izbor za ginekomastiju. AI su inferiorni.

📚 *J Clin Oncol; Breast Cancer Res Treat*
                """.trimIndent()
            ),
            LearningArticleData(
                id = "ai_serm_interactions",
                title = "AI + SERM interakcije",
                content = """
**Farmakokinetička interakcija**

ATAC studija (karcinom dojke, žene):
• Tamoksifen 20mg + Anastrozol 1mg zajedno
• Rezultat: **27% niži** plazma anastrozol nego sam
• Uprkos tome, supresija estrogena bila ekvivalentna

Slične interakcije moguće sa drugim kombinacijama, ali podaci kod muškaraca su retki.

**Klinička upotreba - kada kombinovati?**

Generalno: **izabrati jednu modalnost**

• **SERM** (tamoksifen/raloksifen): za ginekomastiju ili bol u grudima
• **AI**: kada estrogen ostaje visok uprkos SERM-u, ili profilaktički pri ekstremnoj aromatizaciji

**Kombinovana terapija (SERM + AI):**

Teorijski:
• SERM blokira rezidualni efekat estrogena
• AI suprimira novu proizvodnju estrogena
• = Maksimalna blokada

⚠️ Rizik: sniženje estrogena ISPOD zdravih nivoa

**Praktične preporuke:**

1. Ako E2 raste iznad ciljnog opsega → dodati AI
2. Ako se ginekomastija/bol razvije → SERM kao prvi izbor
3. Kombinacija samo za refraktorne slučajeve, uz praćenje

**Prilagođavanje doze:**
Tamoksifen smanjuje nivo anastrozola za ~27%, pa može biti potrebno povećati dozu AI ako se koriste zajedno.

📚 *Lancet Oncol; J Clin Oncol*
                """.trimIndent()
            ),
            LearningArticleData(
                id = "estrogen_practical_guide",
                title = "Praktični vodič za kontrolu estrogena",
                content = """
**Ciljne vrednosti**

• E2: **20-30 pg/mL** (sredina normalnog opsega)
• T:E2 odnos: ~10:1 ili viši
• E2 <8 pg/mL = prenisko (rizik za kosti)
• E2 >60 pg/mL = previsoko (rizik od ginekomastije)

**Algoritam odlučivanja**

```
E2 visok + Bez simptoma → Praćenje
E2 visok + Retencija vode → AI (anastrozol 0.5mg 2x ned)
E2 visok + Ginekomastija → SERM (tamoksifen 20mg)
E2 nizak → Smanjiti/ukinuti AI
```

**Doziranje na osnovu dokaza:**

| Situacija | Lek | Doza |
|-----------|-----|------|
| Prevencija (visok E2) | Anastrozol | 0.5-1mg na 2-3 dana |
| Prevencija (visok E2) | Exemestan | 12.5mg EOD sa hranom |
| Aktivna ginekomastija | Tamoksifen | 10-20mg dnevno |
| Aktivna ginekomastija | Raloksifen | 60mg dnevno |
| Refraktorna ginekomastija | Letrozol | 2.5mg kratkoročno |

**Trajanje terapije:**
• AI: dok traje ciklus + praćenje E2
• SERM za ginekomastiju: 3-6 meseci
• Ginekomastija >12 meseci: hirurška konzultacija

**Greške koje treba izbegavati:**
❌ Srušiti E2 na nulu (kosti, zglobovi, raspoloženje)
❌ Koristiti AI bez merenja E2
❌ Čekati predugo sa ginekomastijom (fibroza)
❌ Letrozol kao prva linija (prepotentan)

📚 *Bazirano na kliničkim studijama navedenim u prethodnim člancima*
                """.trimIndent()
            )
        )
    ),

    // 7. SIDE EFFECTS - Evidence-Based (PubMed)
    LearningCategoryData(
        id = "side_effects",
        title = "Nuspojave (naučno)",
        icon = Icons.Default.Warning,
        articles = listOf(
            LearningArticleData(
                id = "acne_science",
                title = "Akne - Naučni pristup",
                content = """
**INCIDENCA: 50% korisnika AAS**

**Patofiziologija:**
DHT (dihidrotestosteron) indukuje diferencijaciju sebocita kroz AR-zavisne puteve. Studije pokazuju:
• ↑Triglicerida u lojnim žlezdama
• ↑Propionibacteria acnes CFU/cm²
• ↑Holesterola i slobodnih masnih kiselina

Histološki: Dramatična hipertrofija lojnih žlezda kod korisnika AAS.

**LEČENJE PO TEŽINI:**

**Blage-umerene akne:**
Topikalni retinoid + Benzoyl Peroxide

12-nedeljne studije pokazuju:
• Značajno smanjenje ukupnog broja akni (p < 0.05)
• IGA poboljšanje 26-34% vs 7-12% placebo
• Adapalene ima bolju podnošljivost od tretinoina

Protokol: BP 2.5% ujutru + Adapalene 0.1% uveče

**Teške/rezistentne akne - Isotretinoin:**

Doziranje: 0.5-1.0 mg/kg/dan
Trajanje: 4-10 meseci
Kumulativna doza: 120-150 mg/kg

Efikasnost:
• 61% potpuno izlečenje posle 1 ciklusa
• 85-90%+ sa kumulativnom dozom ≥120 mg/kg
• 39% zahteva dodatni tretman

Nuspojave:
• Suva koža/sluznice (>90%)
• ↑Transaminaze (reverzibilno)
• ↑Trigliceridi, holesterol
• Teratogenost (apsolutna kontraindikacija u trudnoći)

**Održavanje posle Isotretinoina:**
Adapalene 0.1% + BP 2.5% = samo 2.94% relapsa (2/68) u 12-mesečnoj studiji
                """.trimIndent()
            ),
            LearningArticleData(
                id = "cardiovascular_science",
                title = "Kardiovaskularni efekti",
                content = """
**HIPERTENZIJA - Mehanizam:**

Nije prosta retencija tečnosti! Studije pokazuju:
• ↓Baroreflex osetljivost (p < 0.05)
• ↑Simpatovagalni balans (LF/HF ratio, p < 0.05)
• ↑Arterijska krutost (pulse wave velocity, p < 0.05)

**HIPERTROFIJA LEVE KOMORE (LVH):**

Prevalenca: **32% dugoročnih korisnika** (32/100)

Među onima sa LVH:
• 23% ima LVEF ≤40% vs 6% bez LVH (p = 0.02)
• GLS: -14.7% vs -16.5% (p = 0.002)

**Dose-Response:**
• Svako povećanje od 10 godina kumulativne upotrebe = +0.60 SD koronarnog plaka (p = 0.008)
• Doza AAS objašnjava 29% varijanse u LV masi

⚠️ EKG nije dovoljan! Osetljivost samo 31-34%. Ehokardiografija je OBAVEZNA.

**HEMATOKRIT - raste sa dozom i pikom:**

• Poređenje režima ISTE nedeljne doze: 100mg/ned (češće) → ~1% prelazi 54%; 200mg na 2 nedelje (viši pik) → ~8% prelazi 54%
• Napomena: ovo poredi učestalost/pik pri istoj nedeljnoj dozi, NE dvostruku dozu

Mehanizam: ↑EPO + ↓Hepcidin

**LEČENJE HIPERTENZIJE:**

**Prva linija: ACE-I ili ARB** (NE beta-blokatori!)
• Enalapril 5-10mg dnevno
• Lisinopril 10mg dnevno
• Losartan - kardioprotektivni efekti u životinjskim modelima

Beta-blokatori smanjuju kapacitet za vežbanje (p < 0.05) i nisu preporučeni za sportiste.

**DUGOROČNI RIZIK (11-godišnje praćenje):**

| Ishod | aHR (95% CI) |
|-------|--------------|
| Akutni MI | 3.00 (1.67-5.39) |
| Kardiomiopatija | **8.90** (4.99-15.88) |
| Srčana insuficijencija | 3.63 (2.01-6.55) |
| VTE | 2.42 (1.54-3.80) |
| Aritmije | 2.26 (1.53-3.32) |
                """.trimIndent()
            ),
            LearningArticleData(
                id = "prolactin_science",
                title = "Hiperprolaktinemija",
                content = """
**KOJI SPOJEVI POVEĆAVAJU PROLAKTIN?**

**19-Nor jedinjenja (Nandrolon, Trenbolon):**
Kompleksni endokrini mehanizam, nije samo aromatizacija.

**Testosteron:**
Povećava prolaktin kroz aromatizaciju u estradiol.
Uočena je pozitivna korelacija testosterona i prolaktina (preko aromatizacije u estradiol).

**LEČENJE:**

**Cabergoline (Dostinex) - ZLATNI STANDARD**

Doziranje: 0.5-1.0 mg 2x nedeljno

Efikasnost (Verhelst 1999, 455 pacijenata):
• 86% normalizuje prolaktin
• 92% kod idiopatske/mikroprolaktinoma
• 77% kod makroprolaktinoma
• 70% kod bromokriptin-rezistentnih

Cabergoline vs Bromocriptine:
• Cabergoline je efikasniji u normalizaciji prolaktina i znatno se bolje podnosi (manje GI nuspojava) — Webster 1994

**Bromocriptine:**
2.5-5mg dnevno. Rezervisati za one koji ne tolerišu cabergoline.

**Vitamin B6 - OGRANIČENI DOKAZI:**

Životinjski modeli: Djelimična supresija, značajno slabija od bromocriptina (p < 0.01)

Ljudska studija (šizofrenija):
• 600mg/dan, 16 nedelja
• 68% redukcija prolaktina
• ALI: Adjuvantna terapija, ne monoterapija

⚠️ **ZAKLJUČAK:**
Vitamin B6 je "visoko varijabilan i nepredvidiv" - ne može se preporučiti kao monoterapija. Cabergoline ostaje zlatni standard.
                """.trimIndent()
            ),
            LearningArticleData(
                id = "liver_science",
                title = "Hepatotoksičnost",
                content = """
**MEHANIZAM: 17-Alpha-Alkilacija**

Strukturna modifikacija koja omogućava oralnu bioraspoloživost ALI uzrokuje hepatotoksičnost.

Studija na hepatocitima pacova:
• 17α-alkilovani (metiltestosteron, oksimetolon, stanozolol): Značajno oštećenje na 10⁻⁴ M
• Ne-alkilovani (testosteron cypionate, nandrolon): BEZ hepatotoksičnih efekata

**Mehanizam oštećenja:**
• Oksidativni stres
• ↑Sinteze žučnih kiselina
• Hiperplazija hepatocita
• Mitohondrijalna disfunkcija

**KLINIČKE MANIFESTACIJE:**

**Holestaza sa žuticom:**
Najčešća manifestacija. Razvija se nakon 2-5 meseci 17α-alkilovane terapije. Većina se vraća po prestanku.

**Peliosis Hepatis (krvarenje u jetri):**
• **37% korisnika steroida** vs 3.6% ne-korisnika (autopsijska serija)
• Dva histološka obrasca: Flebektaktični (71%) i Parenhimski (29%)

**INTERPRETACIJA ENZIMA - KRITIČNO:**

⚠️ Povišeni AST/ALT kod bodibildera ČESTO nisu od jetre!

Kod PRAVE hepatotoksičnosti:
• ALT, AST, I GGT svi povišeni

Kod oštećenja mišića (trening):
• ALT i AST povišeni
• GGT NORMALAN
• CK veoma povišen

Studija lekara: 63% pogrešno dijagnostikuje "bolest jetre" kod bodibildera sa normalnim GGT!

**Stvarna incidenca povišenih enzima:**
• Povišen ALT kod ~1/3 pacijenata na stanozololu; AST kod ~29%
• Obično se normalizuju po prestanku

**HEPATOPROTEKTIVNI AGENSI:**

**Silimarin (Milk Thistle):**
24-nedeljni RCT, 154 pacijenta sa hepatitisom C:
• 420mg i 700mg dnevno
• NEMA značajne razlike vs placebo u redukciji ALT

⚠️ Teoretski koristan, ali NEDOKAZANA efikasnost kod ljudi.

**NAC i TUDCA:**
Ograničeni podaci u literaturi za AAS-indukovanu hepatotoksičnost.

**NAJVAŽNIJA INTERVENCIJA:**
Prekid upotrebe 17α-alkilovanog steroida!
                """.trimIndent()
            ),
            LearningArticleData(
                id = "tendon_science",
                title = "Tetive i zglobovi",
                content = """
**MEHANIZAM RUPTURE TETIVA:**

Kritični nesklad: Mišićna snaga raste BRŽE nego prilagođavanje tetiva.

**Abnormalni kolagen:**

Zdrave tetive: 85-90% Tip I kolagen
Oštećene tetive: ↑Tip III kolagen (nezrelo tkivo)

Studije pokazuju:
• ↑Ekspresija Tip III kolagena na mestima rupture (p = 0.008)
• ↑Markeri sinteze Tip III (p = 0.016)
• Tenociti iz rupturiranih Ahilovih tetiva proizvode VIŠE Tip III kolagena

AAS pogoršavaju ovaj patološki obrazac - preferencijelna sinteza Tip III kolagena kao odgovor na stres.

**NAJČEŠĆA MESTA RUPTURE:**
• Triceps
• Pectoralis major (posebno kod bench pressa)
• Quadriceps
• Ahilova tetiva

⚠️ Rupture obično zahtevaju hiruršku intervenciju.

**"BOLNI PUMPOVI" - Mehanizam:**

**Uloga Taurina:**
Taurin je ključan za funkciju jonskih kanala i homeostazu kalcijuma u mišićima.

Studija suplementacije taurinom:
• ↓LDH nivo
• ↓Kreatin kinaza aktivnost
• ↓Markeri oksidativnog oštećenja
• Poboljšana snaga i smanjeni DOMS

ALI: 8-nedeljno istraživanje na triatloncima nije pokazalo benefit - sugeriše da taurin pomaže SAMO kada je deplecija (npr. od AAS).

**BOL U ZGLOBOVIMA - Mehanizmi:**

1. Ubrzani promet vezivnog tkiva sa akumulacijom Tip III kolagena
2. Nagli dobici snage koji prevazilaze adaptaciju zglobova
3. Izmenjeni sastav sinovijalne tečnosti (nedovoljno istraženo)

Specifične farmakološke intervencije za AAS-indukovani bol u zglobovima nisu dobro proučene osim standardnih NSAID-a.
                """.trimIndent()
            ),
            LearningArticleData(
                id = "injection_reactions_science",
                title = "Reakcije na injekcije",
                content = """
**POST-INJEKCIONI BOL (PIP):**

**Incidenca: 80%** (prospektivna studija, 125 muškaraca, 168 injekcija)

Karakteristike:
• Vrhunac odmah nakon injekcije
• Umerena jačina
• Traje 1-2 dana
• Vraća se na bazalnu liniju do 4. dana
• VISOKO reproduktivno između uzastopnih injekcija

Faktori koji utiču:
• Bolnije kod prethodnog bolnog iskustva (p < 0.05)
• Manje bolno kod starijih i gojaznih (p < 0.05)

**POREĐENJE MESTA INJEKCIJE** (551 injekcija):

| Mesto | Karakteristike |
|-------|----------------|
| Gluteus | Manje komplikacija, više boli |
| Deltoid | Manje boli, više krvarenja |
| Butina | Manje boli, više krvarenja |

⚠️ Deltoid može uzrokovati lokalizovanu rabdomiolizu kod velikih volumena!

**INFEKCIJE:**

Do 13% korisnika prijavljuje nesigurne prakse:
• Ponovno korišćenje igala
• Deljenje igala
• Deljenje višedoznih bočica

Dokumentovani organizmi:
• Mycobacterium smegmatis
• Staphylococcus vrste
• Streptococcus vrste
• Pseudomonas aeruginosa
• Gljivice (kontaminirani preparati)

**PLUĆNA ULJNA MIKROEMBOLIJA (POME):**

Incidenca: **<0.1% po injekciji** (28/90,092 doza)

Simptomi:
• Nagla pojava neproduktivnog kašlja
• Nesvestica

Ishod:
• 21/22 slučaja se oporavilo
• 13/17 se oporavilo za ≤30 minuta
• >60% nije zahtevalo medicinsku intervenciju
• >60% ozbiljnih slučajeva imalo greške u tehnici injekcije

Prevencija:
• Pravilna IM (ne IV) tehnika
• Aspiracija igle
• Odgovarajuća mesta (gluteus > butina > deltoid)

**"TEST GRIP":**

Razlikuje se od POME. Mehanizam je verovatno lokalna inflamacija depoa koja pokreće sistemski inflamatorni odgovor, a ne prava imunska reakcija na ulje.
                """.trimIndent()
            ),
            LearningArticleData(
                id = "side_effects_summary",
                title = "Zbirna tabela nuspojava",
                content = """
**ZBIRNA TABELA: Incidenca, mehanizmi, lečenje**

| Nuspojava | Incidenca | Mehanizam | Prva linija lečenja |
|-----------|-----------|-----------|---------------------|
| Akne | 50% | Sebocejna hipertrofija, DHT | Retinoid + BP |
| Teške akne | 5-10% | P. acnes proliferacija | Isotretinoin 0.5-1 mg/kg |
| Hipertenzija | Blago povišenje | Simpatička disregulacija | ACE-I ili ARB |
| LVH | 32% dugoročnih | Dozno-zavisna hipertrofija | ACE-I/ARB, eho monitoring |
| ↑Hematokrit | Linearno, dozno | ↑EPO, ↓Hepcidin | Redukcija doze, flebotomija |
| Hiperprolaktinemija | Slabo kvantifikovano | Aromatizacija, 19-nor | Cabergoline 0.5-1mg 2x/ned |
| Hepatotoksičnost | 2-3% enzimi | Oksidativni stres | Prekid leka, monitor GGT |
| Peliosis hepatis | 37% vs 3.6% | Sinusoidna dilatacija | Hitno prekinuti lek |
| Ruptura tetiva | ↑Rizik | Tip III kolagen | Konzervativno/hirurški |
| PIP | 80% | Lokalna inflamacija | Manja doza, rotacija mesta |
| POME | <0.1% | IV mikroembolija ulja | Pravilna IM tehnika |

**KLJUČNE PORUKE:**

1. Većina efekata je REVERZIBILNA po prestanku
2. ALI: Neke kardiovaskularne promene (ateroskleroza, fibroza) mogu perzistirati
3. Doza i trajanje su KRITIČNI faktori rizika
4. Ehokardiografija > EKG za detekciju LVH
5. GGT je ključan za razlikovanje prave hepatotoksičnosti od oštećenja mišića
                """.trimIndent()
            )
        )
    ),

    // 8. INJECTIONS
    LearningCategoryData(
        id = "injections",
        title = "Tehnike injekcija",
        icon = Icons.Default.Vaccines,
        articles = listOf(
            LearningArticleData(
                id = "injection_basics",
                title = "Osnove injekcija",
                content = """
**Tipovi injekcija**

**Intramuskularna (IM):**
• Direktno u misic
• Za uljane rastvore
• Igla: 1-1.5" (25-38mm)
• Gauge: 22-25G

**Subkutana (SubQ):**
• Pod kozu
• Za vodene rastvore (hCG, HGH, peptidi)
• Igla: 0.5" (12mm)
• Gauge: 27-31G (insulin igle)

**Priprema:**
1. Operite ruke
2. Ocistite vrh vial-a alkoholom
3. Napunite spric
4. Promenite iglu za ubrizgavanje
5. Ocistite mesto injekcije alkoholom
6. Ubrizgajte
7. Sacekajte par sekundi pre izvlacenja
                """.trimIndent()
            ),
            LearningArticleData(
                id = "pip",
                title = "PIP - Post Injection Pain",
                content = """
**Post Injection Pain (PIP)**

**Uzroci:**
• Visoka koncentracija (npr. Test 400)
• Kratki esteri (propionat)
• Losa tehnika
• "Virgin" misic
• Baktierije (ozbiljno)

**Normalan PIP:**
• Bol i oticanje 1-3 dana
• Postupno se smanjuje
• Nema crvenila, topline, groznicie

**Ozbiljni znaci (idite kod lekara):**
• Crvenilo koje se siri
• Toplina
• Groznica
• Bol koji se pogorsava nakon 3 dana
• Gnoj

**Kako smanjiti PIP:**
• Zagrejte ulje pre injekcije
• Masazirajte mesto posle
• Ubrizgavajte sporije (30 sek/mL)
• Koristite Z-track tehniku
• Rotirajte mesta
                """.trimIndent()
            )
        )
    ),

    // 9. BASICS - Evidence-Based (PubMed)
    LearningCategoryData(
        id = "basics",
        title = "Osnove (naučno)",
        icon = Icons.Default.School,
        articles = listOf(
            LearningArticleData(
                id = "ar_mechanism",
                title = "Mehanizam delovanja - Androgeni receptor",
                content = """
**Androgeni receptor (AR) - molekularna osnova**

AR je ligand-aktivirani transkripcioni faktor sa tri domena:
• **N-terminalni domen** - regulacija transkripcije
• **DNK-vezujući domen** - vezivanje za gene
• **Ligand-vezujući domen** - vezivanje testosterona/DHT

**Genomski mehanizam (klasični):**

1. Testosteron/DHT se vezuje za AR u citoplazmi
2. Konformaciona promena → translokacija u nukleus
3. AR dimerizacija i vezivanje za ARE (androgen response elementi)
4. ARE = 15bp palindromske sekvence u promotorima ciljnih gena
5. Koregulatorski proteini (ko-aktivatori/ko-represori) modulišu transkripciju

**Negenomski mehanizam (brzi):**

Deluje za sekunde do minuta, nezavisno od transkripcije:
• AR direktno aktivira PI3K (fosfatidilinozitol 3-kinazu)
• Interakcija sa p85α regulatornom subjedinicom
• Aktivacija PI3K/Akt signalnog puta
• Fosforilacija FKHR-L1 i proapoptotskog proteina Bad
• Rezultat: povećano preživljavanje ćelija i anabolički procesi

**Auto-regulacija:**
Androgeni indukuju smanjenje AR mRNA kroz negativnu povratnu spregu, što može objasniti toleranciju kod dugotrajnih korisnika.

📚 *Endocrine Reviews; J Biol Chem; Mol Endocrinol*
                """.trimIndent()
            ),
            LearningArticleData(
                id = "protein_synthesis_mtor",
                title = "Sinteza proteina i mTOR put",
                content = """
**mTORC1 aktivacija - centralni mehanizam hipertrofije**

Testosteron aktivira mTORC1 (mechanistic target of rapamycin complex 1) preko IGF-1/Akt signalizacije:

**Nizvodni ciljevi:**
• **p70S6K1** - fosforilacija → elongacija peptidnog lanca
• **4E-BP1** - fosforilacija → inicijacija translacije mRNA

**Sinergija sa treningom:**
Kada se kombinuju testosteron + trening otpora, mTORC1 aktivacija je pojačana - sinergistički efekat superiorniji od oba stimulusa pojedinačno.

**Satelitske ćelije i mionukleusi:**

Satelitske ćelije (mišićne progenitorske ćelije) eksprimiraju AR:
• Testosteron indukuje proliferaciju ~32-36h nakon tretmana
• ~30% populacije satelitskih ćelija ulazi u DNK sintezu
• Proliferirane ćelije se fuzionišu sa postojećim mišićnim vlaknima
• Rezultat: više mionukleusa = veći kapacitet za protein

**"Mišićna memorija":**
Neke ćerke ćelije izbegavaju diferencijaciju i vraćaju se u mirovanje, popunjavajući rezervu satelitskih ćelija. Ovo omogućava održane anaboličke odgovore i objašnjava brži povratak forme kod bivših korisnika.

**Poliaminska biosinteza:**
Testosteron povećava ekspresiju ODC1 i AMD1 gena → proizvodnja poliamina (putrescin, spermidin, spermin) esencijalnih za ćelijski ciklus.

📚 *Am J Physiol Endocrinol Metab; FASEB J*
                """.trimIndent()
            ),
            LearningArticleData(
                id = "endogenous_testosterone",
                title = "Endogena proizvodnja testosterona",
                content = """
**Lejdigove ćelije i cirkadijalni ritam**

Testosteron se proizvodi u Lejdigovim ćelijama testisa pod kontrolom LH.

**Dnevni ritam:**
• Vrhunac: rano jutro (ZT 7-11)
• Regulisan cirkadijalnim clock genima u Lejdigovim ćelijama
• LH-cAMP signalizacija sinhronizuje clock gene (PER1, Dec1/2, Rorb, Bmal1)

**REVERBA protein:**
• Ključan za povezivanje cirkadijalnog oscilatora sa steroidogenezom
• Kontroliše ekspresiju StAR, CYP11A1, CYP17A1, HSD3B2
• BMAL1 knockout → teško oštećena proizvodnja testosterona uprkos povišenom LH

**HPG osa - negativna povratna sprega:**

```
Hipotalamus → GnRH (pulsatilno)
      ↓
Hipofiza → LH + FSH
      ↓
Testisi → Testosteron
      ↓
Negativna povratna sprega na oba nivoa
```

**Supresija na nivou hipofize:**
• Testosteron suprimira srednji LH na ~50%
• FSH suprimira na ~30%
• Ovaj efekat je GnRH-nezavisan

**Supresija na nivou hipotalamusa:**
• Testosteron usporava frekvenciju GnRH pulseva

📚 *J Clin Endocrinol Metab; Endocrinology*
                """.trimIndent()
            ),
            LearningArticleData(
                id = "reference_ranges",
                title = "Referentni opsezi testosterona",
                content = """
**Normalne vrednosti (muškarci)**

| Parametar | Opseg | Napomena |
|-----------|-------|----------|
| Ukupni T | 200-700 ng/dL | Zavisi od laboratorije |
| Slobodni T (19-39g) | 120-368 pg/mL | Najviši u mladosti |
| Slobodni T (>70g) | 45-138 pg/mL | Značajan pad |
| SHBG | Relativno stabilan | Manje varira sa godinama |

**Pad sa godinama:**

Slobodni testosteron opada značajno (⚠️ napomena: ove vrednosti su iz japanske analog-RIA metode i NISU direktno uporedive sa opsegom 120-368 pg/mL iz tabele iznad — različite metode merenja daju različite skale):
• 20-39 godina: 8.5-27.9 pg/mL (japanska populacija)
• >70 godina: 4.5-13.8 pg/mL
• Ukupni T takođe opada, ali manje dramatično

**SHBG (Sex Hormone Binding Globulin):**
• Vezuje testosteron u krvi
• Samo slobodna frakcija je biološki aktivna
• Izračunati slobodni T, bioraspoloživi T i FAI (free androgen index) pokazuju visoku podudarnost u identifikaciji hipogonadizma

**Klinička dijagnoza:**
Kombinacija kliničke slike + laboratorijskih nalaza je ključna. Sam broj nije dovoljan za dijagnozu.

📚 *J Clin Endocrinol Metab; Eur J Endocrinol*
                """.trimIndent()
            ),
            LearningArticleData(
                id = "aas_pharmacology",
                title = "AAS farmakologija - strukture",
                content = """
**Anabolički:Androgeni (A:A) odnos**

Određuje se životinjskim bioesejima:
• **Anabolička aktivnost**: težina levator ani mišića
• **Androgena aktivnost**: težina prostate + semenih vezikula

A:A odnos je svojstvo MATIČNOG steroida, ne estera:
• Ester (propionat, enantat, dekanoat…) menja SAMO brzinu oslobađanja/poluživot, NE A:A odnos
• Primer: nandrolon ima ~10:1 A:A bez obzira na to koji je ester vezan

⚠️ Ovi odnosi iz životinjskih modela ne predviđaju nužno kliničku separaciju kod ljudi.

**17α-Alkilacija - oralna bioraspoloživost**

Modifikacija za preživljavanje prvog prolaza kroz jetru:
• Primeri: metiltestosteron, oksandrolon, oksimetolon, stanozolol

**Posledica - hepatotoksičnost:**
• Holestazna žutica (najvažniji hepatični NE)
• Peliosis hepatis (šupljine ispunjene krvlju)
• Hepatični adenomi i karcinomi
• Mehanizam: oksidativni stres i ROS

Nealkilovani steroidi (testosteron esteri, nandrolon) imaju minimalni hepatotoksični potencijal.

**19-Nortestosteron derivati (Nandrolon)**

Nedostaje metil grupa na C19:
• Povećana anabolička potentnost
• Veća progestinska aktivnost
• Drugačiji obrazac aromatizacije
• 11Î²-alkil derivati: IC50 0.8-6.6 nM za AR

📚 *Steroids; J Steroid Biochem Mol Biol*
                """.trimIndent()
            ),
            LearningArticleData(
                id = "hpta_suppression",
                title = "HPTA supresija - kinetika",
                content = """
**Brzina supresije - dozno zavisna**

| Doza | Vreme do nedetektabilnog LH/FSH |
|------|----------------------------------|
| 100mg/ned | 5-6 nedelja |
| 250-500mg/ned | 2 nedelje |

**Redosled supresije:**
1. FSH supresija brža i dublja od LH
2. FSH LHRH odgovori nestaju 1-3 nedelje nakon bazalnog FSH pada
3. LH odgovori ostaju detektabilni 4-6 nedelja nakon FSH gubitka

**Kompletna supresija (8 nedelja suprafiziološke upotrebe):**
• LH: <0.1 mIU/mL (nedetektabilan)
• FSH: <1.47 mIU/mL
• Endogeni T: izrazito snižen (pri potpunoj supresiji blizu kastracijskog nivoa)

**Faktori koji utiču na oporavak:**

| Faktor | Uticaj na (sporiji) oporavak |
|--------|------------------------------|
| Trajanje upotrebe | jak negativan (najvažniji prediktor) |
| Individualne doze | jak negativan |
| Tip AAS | umeren negativan |
| Kumulativna doza | slabiji negativan |

(Smer/rangiranje su iz literature; tačne r-vrednosti nisu pouzdano izvorno potvrđene.)

**Vreme oporavka:**
• <1 godina upotrebe: obično oporavak unutar 1 godine
• Produžena upotreba: varijabilan i potencijalno nekompletan oporavak
• Spermatogeneza: ~1 godina (duže od T normalizacije)

📚 *Fertil Steril; J Clin Endocrinol Metab*
                """.trimIndent()
            ),
            LearningArticleData(
                id = "ffmi_natural_limits",
                title = "FFMI i prirodne granice",
                content = """
**Fat-Free Mass Index (FFMI)**

Formula: FFMI = (bezmasna masa u kg) / (visina u m)²

**Studija 157 sportista (Kouri et al.):**
• 83 AAS korisnika vs 74 nekorisnika
• Gornja granica FFMI kod nekorisnika: **25.0 kg/m²**

**Mr. America pobednici (pre-steroidna era, 1939-1959):**
• Prosečan FFMI: **25.4 kg/m²**
• Ovo predstavlja genetski plafon za prirodne sportiste

**Savremeni kolegijalani sportisti (97.5-ti percentil):**

| Sport | FFMI (muškarci) |
|-------|-----------------|
| Fudbal (linemen) | 24.28 ± 2.39 |
| Vaterpolo | 20.68 ± 3.56 |
| Žene (svi sportovi) | 23.90 |

**Miostatin - genetski plafon**

Miostatin (TGF-Î² superfamilija) negativno reguliše rast mišića:
• Knockout u odraslih miševa: **+25% mišićne mase** za 3 meseca
• Polimorfizam rs1805086 (K153R):
  - R alel češći kod snažnih sportista (OR=2.02, p=0.05)
  - Žene sa manje čestim alelom: **68% veći porast mišićnog volumena** od treninga

**AR gustina i individualni odgovor:**
Gustina androgenih receptora varira među individuama i određuje efikasnost liganda. Ista doza može proizvesti potpuno različite efekte kod različitih osoba.

📚 *Clin J Sport Med; Proc Natl Acad Sci USA*
                """.trimIndent()
            ),
            LearningArticleData(
                id = "risk_benefit_framework",
                title = "Okvir rizik/korist",
                content = """
**Kratkoročni vs dugoročni rizici**

**Kratkoročni (nedelje-meseci) - uglavnom reverzibilni:**
• Hipertenzija
• Dislipidemija (↑LDL, ↓HDL)
• HPTA supresija
• Ginekomastija
• Povišeni jetreni enzimi
• Akne

**Dugoročni (meseci-godine) - delimično/ireverzibilni:**
• Ubrzana ateroskleroza
• Hipertrofija leve komore
• Kardiomiopatija
• Hepatični maligniteti

**Kohortna studija (11 godina praćenja):**
AAS korisnici imaju značajno povećanu incidenciju:
• Infarkta miokarda
• Moždanog udara
• Aritmija
• Kardiovaskularne smrti

**Reverzibilnost efekata:**

| Efekat | Reverzibilnost | Vreme |
|--------|----------------|-------|
| HPTA supresija | Da | 3-12 mes |
| ↓HDL | Da | Meseci |
| Hipertenzija | Često | Meseci |
| Atrofija testisa | Da | Mes-godine |
| Spermatogeneza | Da | ~1 godina |
| LV hipertrofija | Delimično | Mes-godine |
| Ateroskleroza | Ne | Trajna |
| Hepatični tumori | Ne | Trajni |

**Princip smanjenja štete:**
• Medicinski nadzor i monitoring
• Rano otkrivanje KV faktora rizika (pritisak, lipidi, EKG, eho)
• Strukturirano smanjivanje doze
• PCT protokoli (hCG + CC > monoterapija)

📚 *Heart; Circulation; Eur Heart J*
                """.trimIndent()
            ),
            LearningArticleData(
                id = "psychiatric_effects",
                title = "Psihijatrijski efekti",
                content = """
**Kontrolisana studija (88 AAS korisnika vs 68 nekorisnika)**

**23% AAS korisnika** prijavilo velike poremećaje raspoloženja:
• Manija
• Hipomanija
• Velika depresija

Poremećaji raspoloženja su se javljali **značajno češće tokom AAS upotrebe** nego van upotrebe (p<0.001) i češće kod korisnika nego nekorisnika (p<0.01).

**Agresija - dozno i kontekstualno zavisna:**
• Nivoi testosterona pozitivno povezani sa agresivnim odgovorima
• Naročito na provokaciju

**Korisnici sa poremećajima ličnosti:**

| Ishod | OR | p vrednost |
|-------|-----|------------|
| Agresivna osećanja/ponašanja | 4.9 | 0.04 |
| Suicidalne ideje/pokušaji | 4.6 | 0.04 |
| Kriminalitet | 6.5 | 0.03 |

**Anksioznost:**
• Poboljšava se tokom smanjivanja doze
• Pad sa medijane 15 na 10 nakon 6 meseci praćenja (p=0.01)

**Sindrom ustezanja:**
Ustezanje od AAS proizvodi depresiju i druge psihijatrijske simptome, što ukazuje na potencijal za psihološku zavisnost.

**Mehanizam:**
• Direktno AR-posredovano delovanje na moždane krugove
• Indirektni efekti kroz poremećaj HPG ose
• Sekundarni hipogonadizam tokom i nakon upotrebe

📚 *Arch Gen Psychiatry; Psychol Med*
                """.trimIndent()
            ),
            LearningArticleData(
                id = "polycythemia_risk",
                title = "Policitemija i tromboembolijski rizik",
                content = """
**Mehanizam - eritropoetin (EPO)**

AAS povećavaju serumski EPO → proliferacija eritrocita → policitemija

**Prospektivna studija AAS korisnika:**

| Grupa | EPO (mIU/mL) | p |
|-------|--------------|---|
| T-pozitivni | 11.83 ± 4.19 | 0.03 |
| T-negativni kontrole | 6.60 ± 2.70 | - |

**Korelacije:**
• Serumski EPO ↔ serumski testosteron: rs=0.46, p=0.01
• Serumski EPO ↔ % retikulocita: rs=0.43, p=0.02

**Paradoksalni nalaz - ASIH (AAS-indukovani hipogonadizam):**
• LH <1.4 IU/L (nedetektabilan)
• ~75% viši EPO
• ~140% viša frakcija fluorescencije retikulocita
• Uprkos nižem testosteronu!

Ovo sugeriše da sam ASIH status može stimulisati EPO proizvodnju kroz alternativnu endokrinu signalizaciju.

**Rizik prema formulaciji:**

Kratkodelujući injekcioni testosteron >> transdermalni ili dugodejstvujući depo

Razlog: viši vrhovi testosterona sa čestim injekcijama

**Posledice policitemije:**
• Povećana viskoznost krvi
• Rizik od arterijske tromboze
• Rizik od venske tromboembolije

**Klinički prag:** Hematokrit >54% zahteva intervenciju (flebotomija, smanjenje doze)

📚 *Heiland et al., Drug Testing and Analysis 2023 (PMID 36730044)*
                """.trimIndent()
            )
        )
    ),

    // 10. BLOODWORK
    LearningCategoryData(
        id = "bloodwork",
        title = "Krvne analize",
        icon = Icons.Default.Bloodtype,
        articles = listOf(
            LearningArticleData(
                id = "bloodwork_importance",
                title = "Zasto su krvne analize vazne",
                content = """
**Vaznost krvnih analiza**

Radite krvnu sliku minimum 3-4 puta godisnje.

**Kada raditi:**
• Pre ciklusa - bazni nivoi
• Sredinom ciklusa - najgori zdravstveni parametri
• Nakon PCT-a - provera oporavka
• Za B&C: 3-4 puta godisnje

**Primer efekata na krvnu sliku:**
• Testosteron, Dianabol, Nandrolone - povecavaju estrogen
• Trenbolone - utice na lipide i kortizol
• Equipoise - povecava crvena krvna zrnca (RBC)
                """.trimIndent()
            ),
            LearningArticleData(
                id = "what_to_test",
                title = "Sta testirati",
                content = """
**Standardni hormonski panel**

**Androgeni:**
• LH/FSH - gonadotropini
• Testosteron (ukupni i slobodni)
• SHBG
• Estradiol (E2)

**Osnovni zdravstveni markeri:**
• CBC (kompletna krvna slika)
• Lipidi (LDL/HDL)
• Funkcija jetre (AST/ALT/GGT/Bilirubin)
• PSA (prostata-specifican antigen)
• Insulin, Glukoza

**Za Trenbolon:**
Koristite LC/MS metodu za estradiol - ECLIA/RIA ce dati lazno visok rezultat jer racunaju tren kao estrogen.
                """.trimIndent()
            ),
            LearningArticleData(
                id = "health_markers",
                title = "Zdravstveni markeri objasnjeni",
                content = """
**Jetra:**
• ALT (SGPT) - primarni marker stresa jetre
• AST (SGOT) - takodje marker jetre, ali manje specifican
• GGT - marker zucnih kanala
• Bilirubin - otpadni produkt iz razgradnje crvenih krvnih zrnaca

**Holesterol:**
• HDL - "dobar" holesterol (visi je bolji)
• LDL - "los" holesterol (nizi je bolji)
• VLDL - "vrlo los" holesterol
• LDL/HDL odnos - najvazniji pokazatelj rizika

**Krv:**
• Hematokrit - procenat crvenih krvnih zrnaca
• Hemoglobin - prenosnik kiseonika
• RBC - broj crvenih krvnih zrnaca

**Hormoni:**
• Testosteron - ukupni i slobodni
• Estradiol - glavni estrogen
• LH/FSH - hormoni hipofize
• Prolaktin - moze biti povisen na 19-nor steroidima

**Stitna zlezda:**
• TSH - hormon koji stimulise stitnu zlezdu
• T3/T4 - hormoni stitne zlezde
                """.trimIndent()
            )
        )
    )
) }

// Helper function to get category by ID
fun getLearningCategory(categoryId: String): LearningCategoryData? {
    return learningCategories.find { it.id == categoryId }
}

// Helper function to get article by ID
fun getLearningArticle(categoryId: String, articleId: String): LearningArticleData? {
    return getLearningCategory(categoryId)?.articles?.find { it.id == articleId }
}
