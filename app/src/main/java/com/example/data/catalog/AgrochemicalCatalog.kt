package com.example.data.catalog

enum class AgroCategory(val titleAr: String) {
    ALL("الكل"),
    FUNGICIDE("مبيدات فطرية"),
    INSECTICIDE("مبيدات حشرية وعنكبوتية"),
    HERBICIDE("مبيدات الأعشاب"),
    COPPER_SULFUR("نحاسيات وكبريت وزيوت"),
    FERTILIZER("أسمدة ورقية وذوّابة")
}

enum class ChemicalCompatibilityGroup {
    NEUTRAL_FUNGICIDE,
    COPPER_ALKALINE,
    SULFUR,
    MINERAL_OIL,
    ORGANOPHOSPHATE,
    PYRETHROID,
    HERBICIDE_ACCase_GRAMINICIDE,
    HERBICIDE_HORMONE_24D,
    HERBICIDE_GLYPHOSATE,
    HERBICIDE_TRIAZINONE,
    CALCIUM_RICH,
    PHOSPHATE_SULFATE_FERTILIZER,
    BIO_HUMIC
}

data class AgrochemicalItem(
    val id: String,
    val commercialName: String,
    val activeIngredient: String,
    val category: AgroCategory,
    val targetPests: String,
    val recommendedDosage: String,
    val phiDays: String,
    val modeCode: String,
    val group: ChemicalCompatibilityGroup,
    val formulationNotes: String
)

enum class CompatibilityLevel(val labelAr: String) {
    COMPATIBLE("متوافق للخلط"),
    CAUTION("متوافق بحذر"),
    INCOMPATIBLE("غير متوافق (ممنوع الخلط)")
}

data class TankMixResult(
    val level: CompatibilityLevel,
    val explanationAr: String,
    val mixingAdviceAr: String
)

object AgrochemicalCatalog {
    val items = listOf(
        // ==================== FUNGICIDES (8) ====================
        AgrochemicalItem(
            id = "amistar_top",
            commercialName = "أميستار توب (Amistar Top 325 SC)",
            activeIngredient = "أزوكسيستروبين 200 غ/ل + ديفينوكونازول 125 غ/ل",
            category = AgroCategory.FUNGICIDE,
            targetPests = "عين الطاووس في الزيتون، الندوة المبكرة في الطماطم والبطاطا، أصداء القمح",
            recommendedDosage = "75 - 100 مل / 100 لتر ماء (0.75 - 1 لتر/هكتار)",
            phiDays = "7 أيام (طماطم/بطاطا) / 21 يوماً (زيتون) / 35 يوماً (حبوب)",
            modeCode = "FRAC 11 + 3",
            group = ChemicalCompatibilityGroup.NEUTRAL_FUNGICIDE,
            formulationNotes = "معلق مركز (SC) مزدوج الفعل وقائي وعلاجي؛ واسع الانتشار في بساتين الزيتون ومزارع الطماطم بالشمال الإفريقي."
        ),
        AgrochemicalItem(
            id = "ridomil_gold",
            commercialName = "ريدوميل جولد إم زد (Ridomil Gold MZ 68 WG)",
            activeIngredient = "ميتالاكسيل-M 4% + مانكوزيب 64%",
            category = AgroCategory.FUNGICIDE,
            targetPests = "الندوة المتأخرة (الميلديو) في البطاطا والطماطم، البياض الزغبي",
            recommendedDosage = "250 غ / 100 لتر ماء (2.5 كغ / هكتار)",
            phiDays = "14 يوماً",
            modeCode = "FRAC 4 + M03",
            group = ChemicalCompatibilityGroup.NEUTRAL_FUNGICIDE,
            formulationNotes = "جهازي + تلامسي (WG) لحماية النموات الحديثة؛ يفضل تطبيقه في بداية الطور الخضري وقبل الأمطار."
        ),
        AgrochemicalItem(
            id = "acrobat_mz",
            commercialName = "أكروبات إم زد (Acrobat MZ 69 WG)",
            activeIngredient = "ديميثومورف 9% + مانكوزيب 60%",
            category = AgroCategory.FUNGICIDE,
            targetPests = "الميلديو (الندوة المتأخرة) في البطاطا الموسمية وآخر فصل والطماطم",
            recommendedDosage = "200 - 250 غ / 100 لتر ماء (2 كغ / هكتار)",
            phiDays = "14 يوماً",
            modeCode = "FRAC 40 + M03",
            group = ChemicalCompatibilityGroup.NEUTRAL_FUNGICIDE,
            formulationNotes = "مضاد لتبوّغ الفطريات البيضية (Oomycetes)؛ فعال في مناطق وادي سوف وعين الدفلى عند اشتداد الرطوبة."
        ),
        AgrochemicalItem(
            id = "equation_pro",
            commercialName = "إيكويشن برو (Equation Pro WG)",
            activeIngredient = "فاموكسادون 22.5% + سيموكسانيل 30%",
            category = AgroCategory.FUNGICIDE,
            targetPests = "الندوة المتأخرة والندوة المبكرة في البطاطا والطماطم",
            recommendedDosage = "40 غ / 100 لتر ماء (400 غ / هكتار)",
            phiDays = "7 - 14 يوماً",
            modeCode = "FRAC 11 + 27",
            group = ChemicalCompatibilityGroup.NEUTRAL_FUNGICIDE,
            formulationNotes = "سريع الاختراق ومقاوم للغسيل بالأمطار بعد ساعتين؛ يستخدم وقائياً أو في بداية ظهور البقع."
        ),
        AgrochemicalItem(
            id = "score_250",
            commercialName = "سكور 250 (Score 250 EC)",
            activeIngredient = "ديفينوكونازول 250 غ/ل",
            category = AgroCategory.FUNGICIDE,
            targetPests = "التبقع الألترناري في الطماطم والبطاطا، البياض الدقيقي، جرب الأشجار المثمرة",
            recommendedDosage = "50 مل / 100 لتر ماء (0.5 لتر / هكتار)",
            phiDays = "7 - 14 يوماً",
            modeCode = "FRAC 3",
            group = ChemicalCompatibilityGroup.NEUTRAL_FUNGICIDE,
            formulationNotes = "تريازول جهازي علاجي؛ يُتناوب مع مبيدات غير تريازولية لتفادي المقاومة."
        ),
        AgrochemicalItem(
            id = "prosaro_250",
            commercialName = "بروسارو 250 (Prosaro 250 EC)",
            activeIngredient = "بروثيوكونازول 125 غ/ل + تيبوكونازول 125 غ/ل",
            category = AgroCategory.FUNGICIDE,
            targetPests = "الصدأ الأصفر والبني، تبقع السبطوريا، وفيوزاريوم السنابل في القمح والشعير",
            recommendedDosage = "0.8 - 1 لتر / هكتار (في 200-300 لتر ماء)",
            phiDays = "35 يوماً",
            modeCode = "FRAC 3",
            group = ChemicalCompatibilityGroup.NEUTRAL_FUNGICIDE,
            formulationNotes = "معيار الحماية الفطرية في سهول القمح (سطيف، قسنطينة، تيارت) عند ظهور الورقة العلم أو بداية الإزهار."
        ),
        AgrochemicalItem(
            id = "switch_62",
            commercialName = "سويتش 62.5 (Switch 62.5 WG)",
            activeIngredient = "سيبرودينيل 37.5% + فلوديوكسونيل 25%",
            category = AgroCategory.FUNGICIDE,
            targetPests = "العفن الرمادي (Botrytis cinerea) والعفن الأبيض في الطماطم المحمية",
            recommendedDosage = "80 - 100 غ / 100 لتر ماء (1 كغ / هكتار)",
            phiDays = "7 أيام",
            modeCode = "FRAC 9 + 12",
            group = ChemicalCompatibilityGroup.NEUTRAL_FUNGICIDE,
            formulationNotes = "ممتاز لمكافحة العفن الرمادي داخل البيوت البلاستيكية (بسكرة وتيبازة) مع تهوية الصوبة."
        ),
        AgrochemicalItem(
            id = "aliye_fosetyl",
            commercialName = "ألييت فلاش (Aliette Flash 80 WG)",
            activeIngredient = "فوسيتيل الألمنيوم 80%",
            category = AgroCategory.FUNGICIDE,
            targetPests = "تصمغ الحمضيات (Phytophthora) وعفن الجذور والتاج في المشاتل",
            recommendedDosage = "250 - 300 غ / 100 لتر ماء (رش ورقي أو سقي جذري)",
            phiDays = "14 يوماً",
            modeCode = "FRAC P07",
            group = ChemicalCompatibilityGroup.PHOSPHATE_SULFATE_FERTILIZER,
            formulationNotes = "جهازي صاعد ونازل ينشط مناعة الشجرة؛ يُمنع خلطه مع مركبات النحاس والأسمدة الكالسيومية أو الآزوتية الورقية."
        ),

        // ==================== COPPER, SULFUR & OILS (4) ====================
        AgrochemicalItem(
            id = "kocide_2000",
            commercialName = "كوسايد 2000 (Kocide 2000 WG)",
            activeIngredient = "هيدروكسيد النحاس 53.8% (نحاس معدني 35%)",
            category = AgroCategory.COPPER_SULFUR,
            targetPests = "عين الطاووس وسل الزيتون البكتيري، التبقع البكتيري في الطماطم، تصمغ الحمضيات",
            recommendedDosage = "150 - 200 غ / 100 لتر ماء",
            phiDays = "14 يوماً",
            modeCode = "FRAC M01",
            group = ChemicalCompatibilityGroup.COPPER_ALKALINE,
            formulationNotes = "مبيد وقائي تلامسي قلوي التفاعل؛ يرش بعد التقليم وجني الزيتون أو قبل أمطار الخريف."
        ),
        AgrochemicalItem(
            id = "bordeaux_mix",
            commercialName = "محلول بوردو (Bouillie Bordelaise 20%)",
            activeIngredient = "كبريتات النحاس المعادلة بالجير الحي 20%",
            category = AgroCategory.COPPER_SULFUR,
            targetPests = "الخدمة الشتوية للزيتون والحمضيات ضد عين الطاووس وتصمغ القلف",
            recommendedDosage = "500 - 800 غ / 100 لتر ماء",
            phiDays = "21 يوماً",
            modeCode = "FRAC M01",
            group = ChemicalCompatibilityGroup.COPPER_ALKALINE,
            formulationNotes = "شديد القلوية، يرسب معظم المبيدات العضوية والفوسفورية والغليفوسات إذا خُلط معها."
        ),
        AgrochemicalItem(
            id = "thiovit_jet",
            commercialName = "ثيوفيت جيت (Thiovit Jet 80 WG)",
            activeIngredient = "كبريت ميكروني 80%",
            category = AgroCategory.COPPER_SULFUR,
            targetPests = "البياض الدقيقي في الطماطم والقمح، الأكاروسات (العنكبوت الأحمر وعنكبوت الحمضيات)",
            recommendedDosage = "300 - 400 غ / 100 لتر ماء",
            phiDays = "3 أيام",
            modeCode = "FRAC M02",
            group = ChemicalCompatibilityGroup.SULFUR,
            formulationNotes = "لا يرش عند حرارة فوق 30°م، ويجب الفصل بـ 14 إلى 21 يوماً عن الزيوت المعدنية."
        ),
        AgrochemicalItem(
            id = "summer_oil",
            commercialName = "زيت معدني صيفي نقي (Citrole / Paraffinic Oil)",
            activeIngredient = "زيت بارافيني مكرر 80% EC",
            category = AgroCategory.COPPER_SULFUR,
            targetPests = "الحشرة القشرية الحمراء والسوداء على الحمضيات، بسيل الزيتون، بيوض العناكب",
            recommendedDosage = "1 - 1.5 لتر / 100 لتر ماء",
            phiDays = "7 أيام",
            modeCode = "مكافحة فيزيائية",
            group = ChemicalCompatibilityGroup.MINERAL_OIL,
            formulationNotes = "شائع جداً في بساتين متيجة والشلف؛ يراعى ري الأشجار قبل الرش وتجنب الرش في الذروة الحرارية."
        ),

        // ==================== INSECTICIDES & ACARICIDES (7) ====================
        AgrochemicalItem(
            id = "vertimec",
            commercialName = "فيرتيميك (Vertimec 1.8% EC)",
            activeIngredient = "أبامكتين 18 غ/ل",
            category = AgroCategory.INSECTICIDE,
            targetPests = "صانعة أنفاق أوراق الحمضيات، العنكبوت الأحمر (Tetranychus urticae)، التوتا أبسولوتا",
            recommendedDosage = "50 - 75 مل / 100 لتر ماء",
            phiDays = "7 أيام",
            modeCode = "IRAC 6",
            group = ChemicalCompatibilityGroup.NEUTRAL_FUNGICIDE,
            formulationNotes = "يرش مساءً لتفادي تحلله بالأشعة فوق البنفسجية، مع إضافة زيت معدني خفيف أو مادة ناشرة."
        ),
        AgrochemicalItem(
            id = "coragen_200",
            commercialName = "كوراجين 200 (Coragen 200 SC)",
            activeIngredient = "كلورانترانيليبرول 200 غ/ل",
            category = AgroCategory.INSECTICIDE,
            targetPests = "حافرة أنفاق الطماطم (Tuta absoluta)، عثة درنات البطاطا، الديدان القارضة",
            recommendedDosage = "15 - 20 مل / 100 لتر ماء (150 - 200 مل / هكتار)",
            phiDays = "3 أيام",
            modeCode = "IRAC 28",
            group = ChemicalCompatibilityGroup.NEUTRAL_FUNGICIDE,
            formulationNotes = "مبيد انتقائي آمن نسبياً على الحشرات النافعة، أساسي في برامج مكافحة Tuta absoluta."
        ),
        AgrochemicalItem(
            id = "tracer_spinosad",
            commercialName = "تريسر 480 (Tracer 480 SC)",
            activeIngredient = "سبينوساد 480 غ/ل (مشتق حيوي)",
            category = AgroCategory.INSECTICIDE,
            targetPests = "التوتا أبسولوتا، التربس، ذبابة ثمار الزيتون، عثة الزيتون (Prays oleae)",
            recommendedDosage = "20 - 30 مل / 100 لتر ماء",
            phiDays = "3 أيام",
            modeCode = "IRAC 5",
            group = ChemicalCompatibilityGroup.BIO_HUMIC,
            formulationNotes = "مبيد حيوي فعال يدمج في الزراعة المستدامة والمكافحة المتكاملة."
        ),
        AgrochemicalItem(
            id = "confidor",
            commercialName = "كونفيدور 200 (Confidor 200 SL)",
            activeIngredient = "إيميداكلوبريد 200 غ/ل",
            category = AgroCategory.INSECTICIDE,
            targetPests = "المن الأخضر والأسود في الحمضيات والبطاطا، الذبابة البيضاء (Bemisia tabaci)، بسيل الزيتون",
            recommendedDosage = "50 - 75 مل / 100 لتر ماء (أو 500 مل / هكتار في الشبكة)",
            phiDays = "21 يوماً",
            modeCode = "IRAC 4A",
            group = ChemicalCompatibilityGroup.NEUTRAL_FUNGICIDE,
            formulationNotes = "نيونيكوتينويد جهازي شديد السمية للنحل؛ يُمنع رشه وقت الإزهار نهائياً."
        ),
        AgrochemicalItem(
            id = "actara_25",
            commercialName = "أكتارا 25 (Actara 25 WG)",
            activeIngredient = "ثياميثوكسام 25%",
            category = AgroCategory.INSECTICIDE,
            targetPests = "منّ البطاطا الناقل للفيروسات، الذبابة البيضاء في الطماطم، منّ الحمضيات",
            recommendedDosage = "20 - 30 غ / 100 لتر ماء (200 - 300 غ / هكتار)",
            phiDays = "14 يوماً",
            modeCode = "IRAC 4A",
            group = ChemicalCompatibilityGroup.NEUTRAL_FUNGICIDE,
            formulationNotes = "سريع الامتصاص ورقياً وجذرياً؛ يحمي الشتلات من نقل الفيروسات (TYLCV / PVY)."
        ),
        AgrochemicalItem(
            id = "decis_ec",
            commercialName = "ديسيس 25 (Decis 25 EC)",
            activeIngredient = "دلتاميثرين 25 غ/ل",
            category = AgroCategory.INSECTICIDE,
            targetPests = "بسيل الزيتون، ذبابة ثمار الزيتون، عثة درنات البطاطا، بق الحبوب",
            recommendedDosage = "50 مل / 100 لتر ماء (0.5 لتر / هكتار)",
            phiDays = "7 - 14 يوماً",
            modeCode = "IRAC 3A",
            group = ChemicalCompatibilityGroup.PYRETHROID,
            formulationNotes = "بيروثرويد صاعق؛ يتحلل في المياه القلوية لذا يجب خفض pH الخزان إلى 6.0 قبل الإضافة."
        ),
        AgrochemicalItem(
            id = "dimethoate",
            commercialName = "دايمثويت 40% (Dimethoate 40 EC)",
            activeIngredient = "دايمثويت 400 غ/ل",
            category = AgroCategory.INSECTICIDE,
            targetPests = "ذبابة ثمار الزيتون (Bactrocera oleae)، ذبابة الفاكهة المتوسطية على الحمضيات",
            recommendedDosage = "125 - 150 مل / 100 لتر ماء",
            phiDays = "28 يوماً",
            modeCode = "IRAC 1B",
            group = ChemicalCompatibilityGroup.ORGANOPHOSPHATE,
            formulationNotes = "فوسفور عضوي يتفكك بسرعة مع النحاس والقلويات، ويلتزم بفترة تحريم طويلة قبل عصر الزيتون."
        ),

        // ==================== HERBICIDES (5) ====================
        AgrochemicalItem(
            id = "sencor_70",
            commercialName = "سنكور 70 (Sencor 70 WG)",
            activeIngredient = "ميتريبوزين 70%",
            category = AgroCategory.HERBICIDE,
            targetPests = "الأعشاب عريضة الأوراق وبعض النجيلية (القطيفة، الرمرام، عنب الديب) في البطاطا والطماطم",
            recommendedDosage = "500 - 750 غ / هكتار (قبل الإنبات أو بعد الإنبات على بطاطا < 10 سم)",
            phiDays = "45 يوماً",
            modeCode = "HRAC 5",
            group = ChemicalCompatibilityGroup.HERBICIDE_TRIAZINONE,
            formulationNotes = "مبيد أعشاب أساسي في مزارع البطاطا بالجزائر؛ يُمنع رشه مع الزيوت أو في الترب الرملية الخفيفة جداً بجرعة عالية."
        ),
        AgrochemicalItem(
            id = "mustang_306",
            commercialName = "موستانج 306 (Mustang 306 SE)",
            activeIngredient = "فلوراسولام 6.25 غ/ل + 2,4-D إستر 300 غ/ل",
            category = AgroCategory.HERBICIDE,
            targetPests = "الأعشاب عريضة الأوراق في القمح الصلب واللين والشعير (الخردل البري، الخشخاش، الحميض)",
            recommendedDosage = "0.5 - 0.6 لتر / هكتار (من مرحلة 3 أوراق إلى نهاية التفريع)",
            phiDays = "60 يوماً (مرحلة التفريع)",
            modeCode = "HRAC 2 + 4",
            group = ChemicalCompatibilityGroup.HERBICIDE_HORMONE_24D,
            formulationNotes = "لا يُخلط مطلقاً مع مبيدات النجيليات الاختيارية (مثل Topik) لتجنب التضاد الفيزيولوجي وفقدان الفعالية."
        ),
        AgrochemicalItem(
            id = "topik_240",
            commercialName = "توبيك 240 (Topik 240 EC)",
            activeIngredient = "كلودينافوب-بروبارجيل 240 غ/ل + كلوكينتوسيت-ميكسيل",
            category = AgroCategory.HERBICIDE,
            targetPests = "الأعشاب النجيلية في القمح (الشوفان البري، الزوان Lolium، الفالاريس)",
            recommendedDosage = "0.2 - 0.25 لتر / هكتار",
            phiDays = "60 يوماً",
            modeCode = "HRAC 1 (ACCase)",
            group = ChemicalCompatibilityGroup.HERBICIDE_ACCase_GRAMINICIDE,
            formulationNotes = "اختياري للقمح فقط (يُمنع على الشعير)؛ يفقد فعاليته إذا خُلط مع مبيدات 2,4-D أو الأسمدة القلوية."
        ),
        AgrochemicalItem(
            id = "roundup_360",
            commercialName = "راوندأب / غليفوسات 360 (Glyphosate 360 SL)",
            activeIngredient = "غليفوسات 360 غ/ل (ملح إيزوبروبيل أمين)",
            category = AgroCategory.HERBICIDE,
            targetPests = "الأعشاب الحولية والمعمرة (النجيل، السعد، العليّق) بين صفوف الزيتون والحمضيات وقبل الزرع",
            recommendedDosage = "3 - 5 لتر / هكتار (1.5 - 2 لتر / 100 لتر ماء)",
            phiDays = "14 - 21 يوماً (رش موجّه بعيداً عن الأوراق الخضراء)",
            modeCode = "HRAC 9",
            group = ChemicalCompatibilityGroup.HERBICIDE_GLYPHOSATE,
            formulationNotes = "يتعطل بالكالسيوم والنحاس والمياه العسرة؛ استخدم ماءً نقياً ويفضل إضافة كبريتات الأمونيوم، وتجنب ملامسة سرطانات الزيتون."
        ),
        AgrochemicalItem(
            id = "fusilade_forte",
            commercialName = "فيوزيليد فورتي (Fusilade Forte 150 EC)",
            activeIngredient = "فلوازيفوب-P-بوتيل 150 غ/ل",
            category = AgroCategory.HERBICIDE,
            targetPests = "الأعشاب النجيلية (النجيل، ذيل الثعلب، السورغوم) داخل حقول البطاطا والطماطم",
            recommendedDosage = "0.8 - 1.5 لتر / هكتار",
            phiDays = "28 - 42 يوماً",
            modeCode = "HRAC 1 (ACCase)",
            group = ChemicalCompatibilityGroup.HERBICIDE_ACCase_GRAMINICIDE,
            formulationNotes = "آمن تماماً على المحاصيل عريضة الأوراق؛ يُراعى عدم خلطه مع مبيدات عريضة الأوراق أو النحاسيات."
        ),

        // ==================== SOLUBLE FERTILIZERS (3) ====================
        AgrochemicalItem(
            id = "calcium_nitrate",
            commercialName = "نترات الكالسيوم الذوابة (15.5-0-0 + 26% CaO)",
            activeIngredient = "نيتروجين نتراتي 15.5% + كالسيوم ميسّر 26%",
            category = AgroCategory.FERTILIZER,
            targetPests = "علاج عفن الطرف الزهري في الطماطم والفلفل، تشقق ثمار الحمضيات، وتقوية درنات البطاطا",
            recommendedDosage = "300 غ / 100 لتر ماء (ورقي) أو 20 كغ / هكتار في شبكة التنقيط",
            phiDays = "0 يوم",
            modeCode = "سماد كالسيومي",
            group = ChemicalCompatibilityGroup.CALCIUM_RICH,
            formulationNotes = "يُوضع في خزان التسميد A مستقلاً تماماً عن الفوسفات والكبريتات والهيوميك."
        ),
        AgrochemicalItem(
            id = "map_soluble",
            commercialName = "ماب ذوّاب (MAP 12-61-0)",
            activeIngredient = "فوسفات أحادي الأمونيوم (12% N + 61% P₂O₅)",
            category = AgroCategory.FERTILIZER,
            targetPests = "دفع التجذير في البطاطا والطماطم، تنشيط الإزهار، وتخفيض قلوية مياه الري",
            recommendedDosage = "250 غ / 100 لتر ماء (ورقي) أو 15 - 25 كغ / هكتار",
            phiDays = "0 يوم",
            modeCode = "سماد فوسفاتي حامضي",
            group = ChemicalCompatibilityGroup.PHOSPHATE_SULFATE_FERTILIZER,
            formulationNotes = "لا يُخلط أبداً مع نترات الكالسيوم أو مركبات النحاس."
        ),
        AgrochemicalItem(
            id = "humic_potassium",
            commercialName = "هيومات البوتاسيوم + فولفيك (Humic 70%)",
            activeIngredient = "حمض الهيوميك 70% + فولفيك 15% + K₂O 10%",
            category = AgroCategory.FERTILIZER,
            targetPests = "تحسين الترب الكلسية والمالحة، تنشيط المجموع الجذري وتيسير العناصر الصغرى",
            recommendedDosage = "2 - 4 كغ / هكتار مع مياه التنقيط",
            phiDays = "0 يوم",
            modeCode = "محسن تربة عضوي",
            group = ChemicalCompatibilityGroup.BIO_HUMIC,
            formulationNotes = "تفاعل قلوي؛ قد يترسب عند خلطه بتركيز عالٍ مع الأحماض القوية (MAP) أو النحاس أو الكالسيوم."
        )
    )

    fun checkCompatibility(a: AgrochemicalItem, b: AgrochemicalItem): TankMixResult {
        if (a.id == b.id) {
            return TankMixResult(
                level = CompatibilityLevel.CAUTION,
                explanationAr = "تم اختيار نفس المنتج مرتين. اختر منتجين مختلفين لفحص قابلية الخلط بينهما.",
                mixingAdviceAr = "تأكد من عدم تجاوز الجرعة القصوى للمادة الفعالة لتفادي السمية النباتية."
            )
        }

        val groups = setOf(a.group, b.group)

        // 1. Hormone 2,4-D Herbicide + ACCase Graminicide (Classic Cereal Antagonism in North Africa!)
        if (groups.contains(ChemicalCompatibilityGroup.HERBICIDE_HORMONE_24D) &&
            groups.contains(ChemicalCompatibilityGroup.HERBICIDE_ACCase_GRAMINICIDE)
        ) {
            return TankMixResult(
                level = CompatibilityLevel.INCOMPATIBLE,
                explanationAr = "تضاد كيميائي وفيزيولوجي خطير (Antagonism)! خلط مبيدات 2,4-D الهرمونية (عريضة الأوراق) مع مبيدات النجيليات (ACCase مثل توبيك أو فيوزيليد) يُضعف فعالية مكافحة الشوفان البري والزوان بنسبة تتجاوز 40-50%.",
                mixingAdviceAr = "يُمنع الخلط في خزان واحد؛ يُرش مبيد النجيليات أولاً ثم يُنتظر 7 إلى 10 أيام قبل رش مبيد الأعشاب عريضة الأوراق."
            )
        }

        // 2. Glyphosate + Copper / Calcium / Phosphate / Soil-acting Triazinone / Mineral Oil
        if (groups.contains(ChemicalCompatibilityGroup.HERBICIDE_GLYPHOSATE) &&
            (groups.contains(ChemicalCompatibilityGroup.COPPER_ALKALINE) ||
                groups.contains(ChemicalCompatibilityGroup.CALCIUM_RICH) ||
                groups.contains(ChemicalCompatibilityGroup.PHOSPHATE_SULFATE_FERTILIZER) ||
                groups.contains(ChemicalCompatibilityGroup.HERBICIDE_TRIAZINONE))
        ) {
            return TankMixResult(
                level = CompatibilityLevel.INCOMPATIBLE,
                explanationAr = "تعطيل لفعالية الغليفوسات! ترتبط أيونات الكالسيوم والنحاس والحديد بمركب الغليفوسات (Chelation) وتمنع امتصاصه عبر أوراق الأعشاب، كما يؤدي خلطه مع الميتريبوزين لتضاد تلامسي.",
                mixingAdviceAr = "استخدم الغليفوسات منفرداً في مياه عذبة قليلة الكالسيوم (EC منخفضة)، مع إمكانية إضافة كبريتات الأمونيوم لرفع الفعالية."
            )
        }

        // 3. Metribuzin (Sencor) + Mineral Oil / Foliar Fertilizers (Phytotoxicity on Potato!)
        if (groups.contains(ChemicalCompatibilityGroup.HERBICIDE_TRIAZINONE) &&
            (groups.contains(ChemicalCompatibilityGroup.MINERAL_OIL) ||
                groups.contains(ChemicalCompatibilityGroup.CALCIUM_RICH) ||
                groups.contains(ChemicalCompatibilityGroup.PHOSPHATE_SULFATE_FERTILIZER))
        ) {
            return TankMixResult(
                level = CompatibilityLevel.INCOMPATIBLE,
                explanationAr = "خطر احتراق أوراق البطاطا والطماطم! إضافة الزيوت المعدنية أو الأسمدة الورقية مع الميتريبوزين (سنكور) تزيد الاختراق الورقي وتسبب اصفراراً وتلفاً للمحصول.",
                mixingAdviceAr = "طبّق مبيد الأعشاب منفرداً في تربة رطبة دون خلطه بالزيوت أو الأسمدة الورقية."
            )
        }

        // 4. Oil + Sulfur (Severe phytotoxicity!)
        if (groups.contains(ChemicalCompatibilityGroup.MINERAL_OIL) &&
            groups.contains(ChemicalCompatibilityGroup.SULFUR)
        ) {
            return TankMixResult(
                level = CompatibilityLevel.INCOMPATIBLE,
                explanationAr = "خطر احتراق الأوراق والثمار (Phytotoxicity)! يتفاعل الكبريت مع الزيوت المعدنية مسبباً تلفاً حاداً للأنسجة النباتية وسقوط أوراق الحمضيات.",
                mixingAdviceAr = "يجب الفصل بين الرشتين بمدة لا تقل عن 14 إلى 21 يوماً."
            )
        }

        // 5. Calcium + Phosphate/Sulfate/Aliette (Precipitation clogging emitters)
        if (groups.contains(ChemicalCompatibilityGroup.CALCIUM_RICH) &&
            (groups.contains(ChemicalCompatibilityGroup.PHOSPHATE_SULFATE_FERTILIZER) ||
                groups.contains(ChemicalCompatibilityGroup.SULFUR) ||
                groups.contains(ChemicalCompatibilityGroup.BIO_HUMIC))
        ) {
            return TankMixResult(
                level = CompatibilityLevel.INCOMPATIBLE,
                explanationAr = "ترسيب وتخثر غير متوافق! يتفاعل الكالسيوم مع الفوسفات أو الكبريتات أو الفوسيتيل أو الهيوميك مكوناً رواسب صلبة تسد الفوهات ونقاطات الري.",
                mixingAdviceAr = "استخدم خزانين منفصلين (خزان A للكالسيوم وخزان B للفوسفور والكبريتات والهيوميك)."
            )
        }

        // 6. Copper Alkaline + Organophosphate / Pyrethroid / Mineral Oil / Phosphate-Aliette
        if (groups.contains(ChemicalCompatibilityGroup.COPPER_ALKALINE) &&
            (groups.contains(ChemicalCompatibilityGroup.ORGANOPHOSPHATE) ||
                groups.contains(ChemicalCompatibilityGroup.PYRETHROID) ||
                groups.contains(ChemicalCompatibilityGroup.MINERAL_OIL) ||
                groups.contains(ChemicalCompatibilityGroup.PHOSPHATE_SULFATE_FERTILIZER))
        ) {
            return TankMixResult(
                level = CompatibilityLevel.INCOMPATIBLE,
                explanationAr = "تفاعل قلوي غير متوافق! مركبات النحاس القلوية تحلل المبيدات الفوسفورية والبيروثرويدية وتفقدها فعاليتها، كما تتفاعل مع الفوسيتيل (Aliette) والزيوت مسببة سمية ورقية.",
                mixingAdviceAr = "يُرش النحاس منفرداً، مع ترك فاصل 7 إلى 10 أيام قبل أو بعد هذه المركبات."
            )
        }

        // 7. ACCase Graminicide + Neutral/Bio/Alkaline caution
        if (groups.contains(ChemicalCompatibilityGroup.HERBICIDE_ACCase_GRAMINICIDE) ||
            groups.contains(ChemicalCompatibilityGroup.HERBICIDE_HORMONE_24D) ||
            groups.contains(ChemicalCompatibilityGroup.HERBICIDE_GLYPHOSATE) ||
            groups.contains(ChemicalCompatibilityGroup.HERBICIDE_TRIAZINONE)
        ) {
            return TankMixResult(
                level = CompatibilityLevel.CAUTION,
                explanationAr = "حذر عند خلط مبيدات الأعشاب: يختلف توقيت وضغط فوهة رش مبيد الأعشاب (قطرات خشنة بضغط منخفض) عن مبيدات الفطريات والحشرات (قطرات ناعمة).",
                mixingAdviceAr = "يفضل رش مبيدات الأعشاب منفردة، وإن لزم الخلط مع مبيد فطري للحبوب أجرِ اختبار الجرة أولاً واضبط pH الماء."
            )
        }

        // 8. Bio/Humic + Copper or Acidic MAP
        if (groups.contains(ChemicalCompatibilityGroup.BIO_HUMIC) &&
            (groups.contains(ChemicalCompatibilityGroup.COPPER_ALKALINE) ||
                groups.contains(ChemicalCompatibilityGroup.PHOSPHATE_SULFATE_FERTILIZER))
        ) {
            return TankMixResult(
                level = CompatibilityLevel.CAUTION,
                explanationAr = "قابلية ترسيب وتخثر: أحماض الهيوميك القلوية قد تتخثر مع الأسمدة شديدة الحموضة (MAP) أو أيونات النحاس الحرة.",
                mixingAdviceAr = "أجرِ اختبار الجرة (Jar Test) في كوب ماء لمدة 15 دقيقة قبل التعبئة في الخزان الكبير."
            )
        }

        // 9. Pyrethroids / Organophosphates + pH caution
        if (groups.contains(ChemicalCompatibilityGroup.PYRETHROID) ||
            groups.contains(ChemicalCompatibilityGroup.ORGANOPHOSPHATE)
        ) {
            return TankMixResult(
                level = CompatibilityLevel.CAUTION,
                explanationAr = "متوافق مبدئياً ولكن بشروط الحموضة: هذه المجموعة الحشرية تتأثر بالتحلل القلوي إذا ارتفع pH المياه في الجزائر/شمال إفريقيا فوق 7.2.",
                mixingAdviceAr = "اضبط pH الماء عند 6.0 قبل الخلط واتبع ترتيب WALES (المساحيق WG/WP أولاً ثم المعلقات SC ثم المستحلبات EC)."
            )
        }

        return TankMixResult(
            level = CompatibilityLevel.COMPATIBLE,
            explanationAr = "المنتجان متوافقان فيزيائياً وكيميائياً ضمن محلول الرش عند الالتزام بالتخفيف الموصى به.",
            mixingAdviceAr = "املأ نصف الخزان بالماء، أضف المنتج الأول مع التقليب المستمر (حسب ترتيب WALES)، ثم المنتج الثاني، وأكمل الحجم بالماء مع الرش الفوري."
        )
    }
}
