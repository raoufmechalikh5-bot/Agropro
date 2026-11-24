package com.example.data.catalog

import androidx.annotation.DrawableRes
import com.example.R

data class DiseaseFlashcard(
    val id: String,
    val cropAr: String,
    val diseaseNameAr: String,
    val scientificName: String,
    val pathogenType: String,
    val severityTypical: String,
    val keySymptoms: String,
    val diagnosticClue: String,
    val activeIngredient: String,
    val phiDays: String,
    @DrawableRes val drawableRes: Int? = null
)

object FlashcardCatalog {
    val crops = listOf("الكل", "الزيتون", "الطماطم", "البطاطا", "الحمضيات", "القمح")

    val cards = listOf(
        // ==================== OLIVE (4) ====================
        DiseaseFlashcard(
            id = "olive_peacock",
            cropAr = "الزيتون",
            diseaseNameAr = "مرض عين الطاووس",
            scientificName = "Spilocaea oleagina (Cycloconium oleaginum)",
            pathogenType = "فطري",
            severityTypical = "مرتفعة في مناطق القبائل والغرب الساحلي الرطب",
            keySymptoms = "بقع دائرية داكنة على السطح العلوي للورقة محاطة بهالة صفراء تشبه عين الطاووس، يتبعها تساقط مبكر للأوراق وضعف الإزهار.",
            diagnosticClue = "اختبار هيدروكسيد الصوديوم NaOH 5%: تظهر البقع الكامنة باللون الأسود خلال 15-20 دقيقة.",
            activeIngredient = "هيدروكسيد النحاس / أزوكسيستروبين + ديفينوكونازول",
            phiDays = "14 - 21 يوماً",
            drawableRes = R.drawable.img_crop_olive
        ),
        DiseaseFlashcard(
            id = "olive_verticillium",
            cropAr = "الزيتون",
            diseaseNameAr = "الذبول الفرتسيليومي في الزيتون",
            scientificName = "Verticillium dahliae",
            pathogenType = "فطري وعائي (تربة)",
            severityTypical = "حرجة (يؤدي لموت الأشجار الفتية)",
            keySymptoms = "ذبول وجفاف مفاجئ لأغصان من جانب واحد من الشجرة مع بقاء الأوراق البنية عالقة وملفوفة حول العرق الوسطي.",
            diagnosticClue = "تلون بني داكن في الحزم الوعائية عند تقشير الخشب الغض؛ ينتشر بالزراعة البينية مع البطاطا والطماطم.",
            activeIngredient = "مكافحة وقائية: تقليم وحرق الأغصان المصابة، تطبيق فطر Trichoderma harzianum، ومنع الباذنجانيات.",
            phiDays = "إدارة حيوية دون تحريم",
            drawableRes = R.drawable.img_crop_olive
        ),
        DiseaseFlashcard(
            id = "olive_knot",
            cropAr = "الزيتون",
            diseaseNameAr = "سل الزيتون البكتيري (تدرن الأغصان)",
            scientificName = "Pseudomonas savastanoi pv. savastanoi",
            pathogenType = "بكتيري",
            severityTypical = "مرتفعة بعد تساقط البَرَد أو الضرب بالعصي عند الجني",
            keySymptoms = "تورمات وأورام خشبية خشنة غير منتظمة على الأغصان والفروع الصغيرة تضعف سريان العصارة.",
            diagnosticClue = "تخرج إفرازات بكتيرية لزجة من الشقوق خلال الأمطار، وتنمو الأورام في مواقع جروح التقليم والجني.",
            activeIngredient = "الرش الوقائي بهيدروكسيد النحاس أو محلول بوردو فور الانتهاء من التقليم أو بعد البَرَد",
            phiDays = "14 - 21 يوماً",
            drawableRes = R.drawable.img_crop_olive
        ),
        DiseaseFlashcard(
            id = "olive_fly",
            cropAr = "الزيتون",
            diseaseNameAr = "ذبابة ثمار الزيتون",
            scientificName = "Bactrocera oleae",
            pathogenType = "حشري (ثنائية الأجنحة)",
            severityTypical = "حرجة (ترفع حموضة زيت الزيتون وتسبب تساقط الثمار)",
            keySymptoms = "وخزة مثلثة صغيرة على حبة الزيتون تتحول لبقة داكنة مع نفق داخلي لليرقة في اللب وتدهور جودة الزيت.",
            diagnosticClue = "تُراقب بالمصائد الفرمونية والغذائية (فوسفات ثنائي الأمونيوم) وعند تجاوز 5% إصابات حية يبدأ التدخل.",
            activeIngredient = "سبينوساد (طعوم جاذبة) / دلتاميثرين / دايمثويت",
            phiDays = "3 أيام (سبينوساد) / 28 يوماً (دايمثويت)",
            drawableRes = R.drawable.img_crop_olive
        ),

        // ==================== TOMATO (5) ====================
        DiseaseFlashcard(
            id = "tomato_early_blight",
            cropAr = "الطماطم",
            diseaseNameAr = "الندوة المبكرة (التبقع الألترناري)",
            scientificName = "Alternaria solani",
            pathogenType = "فطري",
            severityTypical = "مرتفعة",
            keySymptoms = "بقع بنية داكنة ذات حلقات دائرية متحدة المركز (لوحة الرماية) محاطة بهالة صفراء على الأوراق السفلية وتصيب الثمار عند العنق.",
            diagnosticClue = "الجراثيم الكونيدية صولجانية الشكل مقسمة طولياً وعرضياً (Dictyospores) تحت المجهر.",
            activeIngredient = "أزوكسيستروبين + ديفينوكونازول / ديفينوكونازول / مانكوزيب",
            phiDays = "3 - 7 أيام",
            drawableRes = R.drawable.img_crop_tomato
        ),
        DiseaseFlashcard(
            id = "tomato_tuta",
            cropAr = "الطماطم",
            diseaseNameAr = "حافرة أنفاق الطماطم (توتا أبسولوتا)",
            scientificName = "Tuta absoluta",
            pathogenType = "حشري (حرشفية الأجنحة)",
            severityTypical = "حرجة جداً في البيوت المحمية ببسكرة والساحل",
            keySymptoms = "أنفاق شفافة عريضة غير منتظمة داخل نصل الورقة تحوي برازاً أسود، وثقوب دقيقة عند كأس الثمرة تسبب تعفنها.",
            diagnosticClue = "تتركز اليرقة بين بشرتي الورقة؛ تختلف عن صانعة أنفاق الليريوميزا بأنفاقها العريضة البقعية وليست خيطية.",
            activeIngredient = "كلورانترانيليبرول (كوراجين) / سبينوساد / أبامكتين بالتناوب",
            phiDays = "3 - 7 أيام",
            drawableRes = R.drawable.img_crop_tomato
        ),
        DiseaseFlashcard(
            id = "tomato_botrytis",
            cropAr = "الطماطم",
            diseaseNameAr = "العفن الرمادي في البيوت المحمية",
            scientificName = "Botrytis cinerea",
            pathogenType = "فطري",
            severityTypical = "مرتفعة شتاءً داخل الصوبات البلاستيكية",
            keySymptoms = "تعفن مائي طري على السيقان والبتلات والثمار يغطيه عفن زغبي رمادي كثيف (بقع شبحية Ghost spots على الثمار).",
            diagnosticClue = "حوامل كونيدية متفرعة تشبه عناقيد العنب عند التكبير؛ ينشط في الرطوبة > 85% وقلة التهوية.",
            activeIngredient = "سيبرودينيل + فلوديوكسونيل (سويتش) / بوسكاليد + بيراكلوستروبين",
            phiDays = "7 أيام",
            drawableRes = R.drawable.img_crop_tomato
        ),
        DiseaseFlashcard(
            id = "tomato_tylcv",
            cropAr = "الطماطم",
            diseaseNameAr = "فيروس تجعد واصفرار أوراق الطماطم",
            scientificName = "Tomato yellow leaf curl virus (TYLCV)",
            pathogenType = "فيروسي (Geminivirus)",
            severityTypical = "حرجة (يوقف عقد الثمار تماماً إذا أصاب الشتلة مبكراً)",
            keySymptoms = "التفاف حواف الأوراق القمية للأعلى كالملعقة، اصفرار ما بين العروق، تقزم شديد للنبات وقصر السلاميات.",
            diagnosticClue = "ينتقل حصراً عبر الذبابة البيضاء (Bemisia tabaci) وليس بالملامسة أو البذور.",
            activeIngredient = "وقائي: شبك مانع للحشرات، أصناف مقاومة (Ty)، ومكافحة الذبابة البيضاء بثياميثوكسام أو إيميداكلوبريد",
            phiDays = "14 - 21 يوماً لمكافحة الناقل",
            drawableRes = R.drawable.img_crop_tomato
        ),
        DiseaseFlashcard(
            id = "tomato_ber",
            cropAr = "الطماطم",
            diseaseNameAr = "عفن الطرف الزهري للثمار",
            scientificName = "Physiological Calcium Disorder (Blossom End Rot)",
            pathogenType = "اضطراب فيزيولوجي / نقص كالسيوم",
            severityTypical = "متوسطة إلى مرتفعة في الترب المالحة أو الري غير المنتظم",
            keySymptoms = "بقعة جلدية غائرة بنية إلى سوداء جافة في الطرف السفلي (الزهري) للثمرة الخضراء أو الناضجة.",
            diagnosticClue = "غياب الجراثيم الفطرية المبدئي؛ سببه عدم انتظام الري أو ارتفاع ملوحة التربة (EC) الذي يعيق انتقال Ca++.",
            activeIngredient = "تنظيم فترات الري + حقن أو رش نترات الكالسيوم الذوابة (15.5-0-0 + 26% CaO)",
            phiDays = "0 يوم",
            drawableRes = R.drawable.img_crop_tomato
        ),

        // ==================== POTATO (5) ====================
        DiseaseFlashcard(
            id = "potato_late_blight",
            cropAr = "البطاطا",
            diseaseNameAr = "الندوة المتأخرة (الميلديو) في البطاطا",
            scientificName = "Phytophthora infestans",
            pathogenType = "فطري (Oomycete)",
            severityTypical = "حرجة جداً (أخطر مرض وبائي على البطاطا بالجزائر)",
            keySymptoms = "بقع مائية داكنة على حواف الأوراق محاطة بزغب أبيض رمادي على السطح السفلي صباحاً، واصفرار ثم احتراق الحقل خلال أيام.",
            diagnosticClue = "تصيب الدرنات بعفن بني صدئ صلب يمتد تحت القشرة؛ تنشط عند حرارة 15-22°م ورطوبة > 90%.",
            activeIngredient = "ميتالاكسيل-M + مانكوزيب / ديميثومورف + مانكوزيب / سيموكسانيل + فاموكسادون",
            phiDays = "7 - 14 يوماً"
        ),
        DiseaseFlashcard(
            id = "potato_scab",
            cropAr = "البطاطا",
            diseaseNameAr = "الجرب العادي في البطاطا",
            scientificName = "Streptomyces scabies",
            pathogenType = "بكتيري خيطي (Actinobacteria)",
            severityTypical = "متوسطة (يخفض الجودة التسويقية للدرنات)",
            keySymptoms = "بقع فلينية خشنة بارزة أو غائرة دائرية على قشرة الدرنة دون إتلاف اللب الأبيض.",
            diagnosticClue = "يزداد بشدة في الترب الكلسية القاعدية (pH > 7.2) عند جفاف التربة في مرحلة تشكل الدرنات.",
            activeIngredient = "انتظام الري أثناء بدء التدرن + إضافة الكبريت الزراعي والأسمدة الحامضية لتخفيض pH",
            phiDays = "إدارة زراعية"
        ),
        DiseaseFlashcard(
            id = "potato_rhizoctonia",
            cropAr = "البطاطا",
            diseaseNameAr = "مرض القشرة السوداء وعفن الساق (الريزوكتونيا)",
            scientificName = "Rhizoctonia solani",
            pathogenType = "فطري (Basidiomycete)",
            severityTypical = "مرتفعة في الزراعات المبكرة والباردة",
            keySymptoms = "أجسام حجرية سوداء تشبه التراب الملتصق على قشرة الدرنة (لا تزول بالغسل)، وتقرحات بنية غائرة على السيقان تحت الأرض.",
            diagnosticClue = "تفرعات الميسيليوم قائمة الزاوية (90°) مع تخصر عند الحاجز تحت المجهر، وتكوّن درنات هوائية صغيرة على الساق.",
            activeIngredient = "معاملة الدرنات البذارية بفلوديوكسونيل أو بنسيكورون أو أزوكسيستروبين في خط الزراعة",
            phiDays = "معاملة بذار عند الغرس"
        ),
        DiseaseFlashcard(
            id = "potato_pvy",
            cropAr = "البطاطا",
            diseaseNameAr = "موزاييك البطاطا (فيروس Y)",
            scientificName = "Potato virus Y (PVY)",
            pathogenType = "فيروسي (Potyvirus)",
            severityTypical = "مرتفعة (تدهور البذار وانخفاض الغلة)",
            keySymptoms = "تبرقش أوراق أخضر فاتح وداكن، تجعد وتقزم الأوراق، وتنخر العروق السفلية (سقوط الأوراق) مع إمكانية تنخر حلقي بالدرنات.",
            diagnosticClue = "ينتقل بأسلوب غير مثابر عبر حشرة منّ الخوخ الأخضر (Myzus persicae) وبإعادة استخدام درنات مصابة.",
            activeIngredient = "استخدام بذار معتمد خالٍ من الفيروسات + مكافحة حشرات المنّ بثياميثوكسام أو إيميداكلوبريد",
            phiDays = "14 - 21 يوماً"
        ),
        DiseaseFlashcard(
            id = "potato_tuber_moth",
            cropAr = "البطاطا",
            diseaseNameAr = "عثة درنات البطاطا (Teigne de la pomme de terre)",
            scientificName = "Phthorimaea operculella",
            pathogenType = "حشري (حرشفية الأجنحة)",
            severityTypical = "حرجة في البطاطا آخر فصل والمخزنة صيفاً",
            keySymptoms = "أنفاق متعرجة داخل الدرنات المكشوفة مملوءة بفضلات داكنة تفتح الباب للتعفن البكتيري، وأنفاق في الأوراق.",
            diagnosticClue = "تشتد مع تشقق التربة الجافة قبل الحصاد أو ترك الدرنات مكشوفة في الحقل ليلاً.",
            activeIngredient = "الترقيع الترابي الجيد لتغطية الدرنات + كلورانترانيليبرول أو سبينوساد أو دلتاميثرين",
            phiDays = "3 - 14 يوماً"
        ),

        // ==================== CITRUS (4) ====================
        DiseaseFlashcard(
            id = "citrus_leafminer",
            cropAr = "الحمضيات",
            diseaseNameAr = "صانعة أنفاق أوراق الحمضيات",
            scientificName = "Phyllocnistis citrella",
            pathogenType = "حشري (حرشفية الأجنحة)",
            severityTypical = "مرتفعة في المشاتل وموجات النمو الصيفية والخريفية",
            keySymptoms = "أنفاق متعرجة فضية لامعة تحت بشرة الأوراق الحديثة تسبب التواء النصل وجفافه.",
            diagnosticClue = "يرقة صفراء دقيقة تتحرك وسط النفق المحتوي على خط مركزي من الفضلات.",
            activeIngredient = "أبامكتين + زيت معدني صيفي / إيمامكتين بنزوات / سبينوساد",
            phiDays = "7 - 14 يوماً",
            drawableRes = R.drawable.img_crop_citrus
        ),
        DiseaseFlashcard(
            id = "citrus_tristeza",
            cropAr = "الحمضيات",
            diseaseNameAr = "مرض التريستيزا (التدهور السريع للحمضيات)",
            scientificName = "Citrus tristeza virus (CTV)",
            pathogenType = "فيروسي (Closterovirus)",
            severityTypical = "حرجة في البساتين المطعمة على النارنج (Bigaradier)",
            keySymptoms = "تدهور سريع أو بطيء للشجرة، اصفرار العروق، وتنقّر خشب الساق تحت القلف مع انتفاخ فوق منطقة التطعيم.",
            diagnosticClue = "ينتقل عبر التطعيم المصاب وبحشرات المن (Aphis gossypii / Toxoptera citricida).",
            activeIngredient = "استبدال أصل النارنج بأصول مقاومة (سيترانج تروير / كاريزو) + شتلات معتمدة ومكافحة المن",
            phiDays = "21 يوماً لمكافحة الناقل",
            drawableRes = R.drawable.img_crop_citrus
        ),
        DiseaseFlashcard(
            id = "citrus_gummosis",
            cropAr = "الحمضيات",
            diseaseNameAr = "التصمغ الفيتوفثوري (عفن طوق وجذور الحمضيات)",
            scientificName = "Phytophthora citrophthora / P. nicotianae",
            pathogenType = "فطري (Oomycete)",
            severityTypical = "مرتفعة في الترب الثقيلة سيئة الصرف في متيجة",
            keySymptoms = "تشقق القلف عند قاعدة الجذع قرب سطح التربة مع إفرازات صمغية عنبرية داكنة واصفرار عروق الأوراق.",
            diagnosticClue = "تغرق مياه الري الجذع مباشرة؛ عند كشط القلف يظهر الخشب بنياً رطباً.",
            activeIngredient = "فوسيتيل الألمنيوم (ألييت فلاش) رشاً أو سقياً + طلاء الجذع بعجينة النحاس (هيدروكسيد النحاس)",
            phiDays = "14 يوماً",
            drawableRes = R.drawable.img_crop_citrus
        ),
        DiseaseFlashcard(
            id = "citrus_red_scale",
            cropAr = "الحمضيات",
            diseaseNameAr = "القرمزية الحمراء والحشرة القشرية في الحمضيات",
            scientificName = "Aonidiella aurantii",
            pathogenType = "حشري (نصفية الأجنحة - قشريات)",
            severityTypical = "مرتفعة (تشوه الثمار وتجفف الأغصان)",
            keySymptoms = "حراشف دائرية مسطحة بنية محمرة ملتصقة بقوة على الثمار والأوراق والأغصان تمتص العصارة وتسبب يباس الفروع.",
            diagnosticClue = "لا تفرز ندوة عسلية بخلاف القشرية السوداء؛ يُستهدف طور الحوريات المتحركة (Crawlers).",
            activeIngredient = "زيت معدني صيفي بارافيني + مبيد منظم نمو حشري (بيريبروكسيفين) أو إيميداكلوبريد عند الحاجة",
            phiDays = "7 - 21 يوماً",
            drawableRes = R.drawable.img_crop_citrus
        ),

        // ==================== CEREALS: WHEAT & BARLEY (4) ====================
        DiseaseFlashcard(
            id = "wheat_yellow_rust",
            cropAr = "القمح",
            diseaseNameAr = "الصدأ الأصفر (الصدأ المخطط) في القمح",
            scientificName = "Puccinia striiformis f. sp. tritici",
            pathogenType = "فطري (Basidiomycete)",
            severityTypical = "حرجة (وبائي في الهضاب العليا والشمال الجزائري)",
            keySymptoms = "بثرات صفراء برتقالية متراصة في خطوط طولية متوازية بين عروق الورقة تعطي مظهراً مخططاً.",
            diagnosticClue = "يظهر مبكراً في الربيع البارد الرطب (10-18°م) ويمسح بالغبار البرتقالي عند لمس الورقة.",
            activeIngredient = "بروثيوكونازول + تيبوكونازول (بروسارو) / أزوكسيستروبين + ديفينوكونازول",
            phiDays = "35 يوماً"
        ),
        DiseaseFlashcard(
            id = "wheat_septoria",
            cropAr = "القمح",
            diseaseNameAr = "تبقع أوراق القمح السبطوري",
            scientificName = "Zymoseptoria tritici (Septoria tritici)",
            pathogenType = "فطري (Ascomycete)",
            severityTypical = "مرتفعة في المناطق الممطرة (قسنطينة، قالمة، البويرة)",
            keySymptoms = "بقع طولية صفراء ثم بنية رمادية على الأوراق السفلية تتوسطها نقاط سوداء دقيقة هي الأجسام الثمرية (Pycnidia).",
            diagnosticClue = "النقاط السوداء الدقيقة (Pycnidia) داخل البقعة المتنخرة تفرز جراثيم مع قطرات المطر المتناثرة.",
            activeIngredient = "بروثيوكونازول + تيبوكونازول / أزوكسيستروبين + سيبروكونازول عند بروز الورقة العلم",
            phiDays = "35 يوماً"
        ),
        DiseaseFlashcard(
            id = "wheat_powdery_mildew",
            cropAr = "القمح",
            diseaseNameAr = "البياض الدقيقي في القمح والشعير",
            scientificName = "Blumeria graminis f. sp. tritici / hordei",
            pathogenType = "فطري (Ascomycete)",
            severityTypical = "متوسطة إلى مرتفعة في الحقول الكثيفة المسمّدة آزوتياً بإفراط",
            keySymptoms = "وسادات قطنية بيضاء دقيقة على السطح العلوي للأوراق السفلية وأغمادها تتحول للرمادي مع نقاط سوداء (Cleistothecia).",
            diagnosticClue = "ينمو خارجياً على البشرة ويزول جزئياً بالمسح، بخلاف الصدأ والسبتوريا.",
            activeIngredient = "كبريت ميكروني / تيبوكونازول / ديفينوكونازول",
            phiDays = "3 - 35 يوماً حسب المادة"
        ),
        DiseaseFlashcard(
            id = "wheat_fusarium",
            cropAr = "القمح",
            diseaseNameAr = "جرب ولفحة سنابل القمح الفيوزاريومي",
            scientificName = "Fusarium graminearum / F. culmorum",
            pathogenType = "فطري (تربة وبقايا محصول)",
            severityTypical = "حرجة (ينتج سموماً فطرية DON ويُضمر الحبوب)",
            keySymptoms = "ابيضاض مبكر لسنبلة كاملة أو عدة سنيبلات وسط السنبلة الخضراء مع تلون وردي برتقالي عند قواعد السنيبلات في الرطوبة.",
            diagnosticClue = "تُصاب السنبلة حصراً أثناء الإزهار إذا تزامن مع أمطار دافئة؛ الحبوب الناتجة منكمشة شاحبة (Tombstone kernels).",
            activeIngredient = "معاملة البذور وقائياً + الرش ببروثيوكونازول + تيبوكونازول عند بداية الإزهار (BBCH 61-65)",
            phiDays = "35 يوماً"
        )
    )
}
