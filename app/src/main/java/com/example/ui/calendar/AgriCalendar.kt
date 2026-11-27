package com.example.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.LocaleHelper
import androidx.compose.ui.unit.dp
import java.util.Calendar

private class CropCalendar(val name: String, val months: List<List<String>>)

private val MONTH_NAMES = listOf(
    "جانفي", "فيفري", "مارس", "أفريل", "ماي", "جوان",
    "جويلية", "أوت", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"
)


private fun calendarTranslation(text: String, language: String): String {
    if (language == "ar" || language == "system") return text
    val translations = mapOf(
        "جانفي" to ("Janvier" to "January"), "فيفري" to ("Février" to "February"),
        "مارس" to ("Mars" to "March"), "أفريل" to ("Avril" to "April"),
        "ماي" to ("Mai" to "May"), "جوان" to ("Juin" to "June"),
        "جويلية" to ("Juillet" to "July"), "أوت" to ("Août" to "August"),
        "سبتمبر" to ("Septembre" to "September"), "أكتوبر" to ("Octobre" to "October"),
        "نوفمبر" to ("Novembre" to "November"), "ديسمبر" to ("Décembre" to "December"),
        "الزيتون" to ("Olivier" to "Olive"), "الطماطم" to ("Tomate" to "Tomato"),
        "البطاطا" to ("Pomme de terre" to "Potato"), "الحمضيات" to ("Agrumes" to "Citrus"),
        "القمح" to ("Blé" to "Wheat"),
        "إنهاء جني الثمار المتأخرة" to ("Terminer la récolte des fruits tardifs" to "Finish harvesting late fruit"),
        "فحص الإصابة بعين الطاووس وتكرار الرش النحاسي عند الحاجة" to ("Vérifier la maladie de l’œil de paon et renouveler le traitement cuprique si nécessaire" to "Check for peacock eye disease and repeat copper spray if needed"),
        "تقليم الشتاء بعد انتهاء الجني (تجنّب أيام الصقيع)" to ("Taille d’hiver après la récolte (éviter les jours de gel)" to "Winter pruning after harvest (avoid frost days)"),
        "إزالة الأغصان الجافة والمصابة" to ("Supprimer les branches sèches et malades" to "Remove dry and diseased branches"),
        "إكمال التقليم" to ("Terminer la taille" to "Complete pruning"),
        "الدفعة الأولى من التسميد الآزوتي قبل الإزهار" to ("Première dose d’azote avant la floraison" to "First nitrogen application before flowering"),
        "مكافحة الأعشاب" to ("Désherbage" to "Weed control"),
        "متابعة التبرعم" to ("Surveiller le débourrement" to "Monitor bud break"),
        "رشة نحاسية وقائية قبل الإزهار عند الأجواء الرطبة" to ("Traitement préventif au cuivre avant floraison par temps humide" to "Preventive copper spray before flowering in humid weather"),
        "فترة الإزهار: قلّل الرش الكيميائي وتجنّب الإجهاد المائي" to ("Floraison : limiter les traitements chimiques et éviter le stress hydrique" to "Flowering: limit chemical spraying and avoid water stress"),
        "البوتاسيوم والبورون عند نقصهما (حسب تحليل الأوراق)" to ("Potassium et bore en cas de carence (selon l’analyse foliaire)" to "Apply potassium and boron if deficient (based on leaf analysis)"),
        "بداية عقد الثمار: ري تكميلي في الأراضي الجافة" to ("Début de nouaison : irrigation d’appoint en sol sec" to "Fruit set begins: supplementary irrigation in dry soils"),
        "مراقبة الحشرة القشرية السوداء" to ("Surveiller la cochenille noire" to "Monitor black scale insect"),
        "ري منتظم لنمو الثمار" to ("Irrigation régulière pour la croissance des fruits" to "Water regularly for fruit growth"),
        "تركيب مصائد ذبابة الزيتون" to ("Installer des pièges contre la mouche de l’olive" to "Install olive fruit fly traps"),
        "مراقبة ذبابة الزيتون وتقدير الإصابة" to ("Surveiller et évaluer les attaques de la mouche de l’olive" to "Monitor and assess olive fruit fly infestation"),
        "تغطية التربة للحفاظ على الرطوبة" to ("Pailler le sol pour conserver l’humidité" to "Mulch the soil to retain moisture"),
        "مكافحة ذبابة الزيتون عند بلوغ عتبة الإصابة" to ("Lutter contre la mouche de l’olive lorsque le seuil d’intervention est atteint" to "Control olive fruit fly when the action threshold is reached"),
        "ري قبل الجني للثمار الخضراء" to ("Irriguer avant la récolte des olives vertes" to "Water before harvesting green olives"),
        "بداية جني زيتون المائدة" to ("Début de la récolte des olives de table" to "Begin harvesting table olives"),
        "تجهيز الصناديق ومعدات الجني" to ("Préparer les caisses et le matériel de récolte" to "Prepare harvest crates and equipment"),
        "ذروة جني الزيتون للزيت" to ("Pleine récolte des olives à huile" to "Peak harvest of oil olives"),
        "تسميد عضوي بعد الجني" to ("Fertilisation organique après récolte" to "Apply organic fertilizer after harvest"),
        "إتمام الجني" to ("Terminer la récolte" to "Finish harvesting"),
        "رشة نحاسية بعد الجني" to ("Traitement cuprique après récolte" to "Copper spray after harvest"),
        "تحضير الأرض والتسميد العضوي" to ("Préparer le sol et apporter de la matière organique" to "Prepare soil and apply organic fertilizer"),
        "تجهيز مشتل البذور في بيت محمي" to ("Préparer la pépinière sous serre" to "Prepare the seed nursery in a greenhouse"),
        "زراعة البذور في المشتل" to ("Semer les graines en pépinière" to "Sow seeds in the nursery"),
        "تجهيز الحقل وإضافة الأسمدة القاعدية" to ("Préparer la parcelle et incorporer les engrais de fond" to "Prepare the field and apply basal fertilizers"),
        "نقل الشتلات بعد زوال خطر الصقيع" to ("Repiquer après tout risque de gel" to "Transplant seedlings after frost risk has passed"),
        "نقل الشتلات (حتى منتصف الشهر)" to ("Repiquage des plants (jusqu’à mi-mois)" to "Transplant seedlings (until mid-month)"),
        "ري بالتنقيط وتغطية التربة" to ("Irrigation goutte-à-goutte et paillage" to "Drip irrigation and soil mulching"),
        "تدعيم النباتات وإزالة الأفرع الجانبية" to ("Tuteurer les plants et supprimer les pousses latérales" to "Stake plants and remove side shoots"),
        "تسميد آزوتي بوتاسي دوري حسب النمو" to ("Apports réguliers d’azote et de potassium selon la croissance" to "Apply nitrogen and potassium regularly according to growth"),
        "بداية الجني" to ("Début de la récolte" to "Harvest begins"),
        "مراقبة حفار الطماطم (Tuta absoluta) بالمصائد الفرمونية" to ("Surveiller la mineuse de la tomate (Tuta absoluta) avec des pièges à phéromones" to "Monitor tomato leafminer (Tuta absoluta) with pheromone traps"),
        "ذروة الجني والري المنتظم" to ("Pleine récolte et irrigation régulière" to "Peak harvest and regular irrigation"),
        "مراقبة اللفحة المتأخرة والعناكب" to ("Surveiller le mildiou et les acariens" to "Monitor late blight and mites"),
        "الجني المتواصل" to ("Poursuivre la récolte" to "Continue harvesting"),
        "تجهيز مشتل العروة الخريفية" to ("Préparer la pépinière pour la culture d’automne" to "Prepare the nursery for the autumn crop"),
        "نقل شتلات العروة الخريفية" to ("Repiquer les plants de la culture d’automne" to "Transplant autumn-crop seedlings"),
        "إزالة بقايا المحصول السابق" to ("Éliminer les résidus de la culture précédente" to "Remove previous crop residues"),
        "جني العروة الخريفية وحمايتها من البرودة" to ("Récolter la culture d’automne et la protéger du froid" to "Harvest the autumn crop and protect it from cold"),
        "إزالة بقايا النباتات المصابة" to ("Éliminer les résidus des plantes infectées" to "Remove infected plant residues"),
        "تحضير الأرض للموسم القادم" to ("Préparer le sol pour la prochaine saison" to "Prepare the soil for the next season"),
        "التناوب الزراعي" to ("Rotation des cultures" to "Crop rotation"),
        "اقتناء تقاوي معتمدة" to ("Acheter des plants de semence certifiés" to "Purchase certified seed potatoes"),
        "زراعة الموسم الرئيسي في المناطق الدافئة" to ("Planter la culture principale dans les régions chaudes" to "Plant the main crop in warm regions"),
        "زراعة الموسم الرئيسي" to ("Planter la culture principale" to "Plant the main crop"),
        "تسميد قاعدي (فوسفور وبوتاسيوم)" to ("Fertilisation de fond (phosphore et potassium)" to "Basal fertilization (phosphorus and potassium)"),
        "العزق والتتريب" to ("Binage et buttage" to "Hoeing and earthing up"),
        "تسميد آزوتي" to ("Fertilisation azotée" to "Nitrogen fertilization"),
        "مراقبة الإنبات" to ("Surveiller la levée" to "Monitor emergence"),
        "الوقاية من اللفحة المتأخرة (الميلديو) بعد الأمطار والرطوبة" to ("Prévenir le mildiou après les pluies et en période humide" to "Prevent late blight after rain and in humid conditions"),
        "ري منتظم" to ("Irrigation régulière" to "Regular irrigation"),
        "الجني عند جفاف العروش ونضج الدرنات" to ("Récolter lorsque les fanes sèchent et que les tubercules sont mûrs" to "Harvest when vines dry and tubers mature"),
        "تخزين الدرنات في مكان بارد مظلم" to ("Stocker les tubercules dans un endroit frais et sombre" to "Store tubers in a cool, dark place"),
        "تجهيز الأرض لزراعة العروة المتأخرة" to ("Préparer le sol pour la culture tardive" to "Prepare the soil for the late crop"),
        "زراعة العروة المتأخرة (أواخر أوت إلى سبتمبر)" to ("Planter la culture tardive (fin août à septembre)" to "Plant the late crop (late August to September)"),
        "التتريب والتسميد" to ("Buttage et fertilisation" to "Earthing up and fertilizing"),
        "مراقبة الميلديو" to ("Surveiller le mildiou" to "Monitor late blight"),
        "ري خفيف وحماية من الصقيع" to ("Irrigation légère et protection contre le gel" to "Light irrigation and frost protection"),
        "الجني المبكر للعروة المتأخرة (ديسمبر إلى جانفي)" to ("Récolte précoce de la culture tardive (décembre à janvier)" to "Early harvest of the late crop (December to January)"),
        "جني البرتقال والمندرين" to ("Récolter les oranges et les mandarines" to "Harvest oranges and mandarins"),
        "تقليم خفيف بعد الجني" to ("Taille légère après récolte" to "Light pruning after harvest"),
        "حماية الأشجار الصغيرة من الصقيع" to ("Protéger les jeunes arbres du gel" to "Protect young trees from frost"),
        "تسميد آزوتي قبل الإزهار" to ("Apport d’azote avant floraison" to "Apply nitrogen before flowering"),
        "الإزهار وعقد الثمار: ري منتظم" to ("Floraison et nouaison : irrigation régulière" to "Flowering and fruit set: regular irrigation"),
        "مراقبة المن وصانعة أنفاق الأوراق" to ("Surveiller les pucerons et la mineuse des feuilles" to "Monitor aphids and citrus leafminer"),
        "معالجة الحشرات عند وجود الإصابة (حسب بطاقة المنتج)" to ("Traiter les insectes en cas d’infestation (selon l’étiquette du produit)" to "Treat insects when infestation is present (follow the product label)"),
        "فترة تساقط الثمار الفيزيولوجي: لا تهمل الري" to ("Chute physiologique des fruits : maintenir l’irrigation" to "Physiological fruit drop: maintain irrigation"),
        "دفعة تسميد ثانية" to ("Deuxième apport d’engrais" to "Second fertilizer application"),
        "ري مكثف" to ("Irrigation intensive" to "Intensive irrigation"),
        "مراقبة الحشرات القشرية والعناكب" to ("Surveiller les cochenilles et les acariens" to "Monitor scale insects and mites"),
        "مراقبة ذبابة الفاكهة المتوسطية بالمصائد" to ("Surveiller la mouche méditerranéenne des fruits avec des pièges" to "Monitor Mediterranean fruit fly with traps"),
        "تسميد بوتاسي لنضج الثمار" to ("Apport de potassium pour la maturation des fruits" to "Apply potassium to support fruit ripening"),
        "مكافحة ذبابة الفاكهة" to ("Lutter contre la mouche des fruits" to "Control fruit fly"),
        "بداية جني الأصناف المبكرة" to ("Début de la récolte des variétés précoces" to "Begin harvesting early varieties"),
        "جني الأصناف المبكرة" to ("Récolter les variétés précoces" to "Harvest early varieties"),
        "جني البرتقال ومراقبة خطر الصقيع" to ("Récolter les oranges et surveiller le risque de gel" to "Harvest oranges and watch for frost risk"),
        "مرحلة التفريع: مراقبة الأعشاب الضارة" to ("Tallage : surveiller les mauvaises herbes" to "Tillering: monitor weeds"),
        "تصريف مياه الأمطار الزائدة" to ("Drainer l’excès d’eau de pluie" to "Drain excess rainwater"),
        "مكافحة الأعشاب بعد الإنبات حسب نوعها" to ("Désherber après la levée selon le type de mauvaises herbes" to "Control weeds after emergence according to weed type"),
        "الدفعة الأولى من الآزوت عند التفريع" to ("Première dose d’azote au tallage" to "First nitrogen application at tillering"),
        "الدفعة الثانية من الآزوت عند استطالة الساق" to ("Deuxième dose d’azote à l’allongement de la tige" to "Second nitrogen application at stem elongation"),
        "مراقبة الصدأ الأصفر" to ("Surveiller la rouille jaune" to "Monitor yellow rust"),
        "طرد السنابل: مراقبة الأمراض الفطرية (الصدأ، سبتوريا)" to ("Épiaison : surveiller les maladies fongiques (rouilles, septoriose)" to "Heading: monitor fungal diseases (rust, Septoria)"),
        "امتلاء الحبوب: ري تكميلي عند الجفاف إن توفر" to ("Remplissage des grains : irrigation d’appoint en cas de sécheresse si possible" to "Grain filling: supplementary irrigation during drought if available"),
        "الحصاد عند نضج الحبوب ورطوبة مناسبة" to ("Récolter à maturité avec une humidité adaptée du grain" to "Harvest when grain is mature and moisture is suitable"),
        "إتمام الحصاد" to ("Terminer la moisson" to "Complete the harvest"),
        "تخزين الحبوب جافة" to ("Stocker les grains bien secs" to "Store grain when dry"),
        "حرث ما بعد الحصاد" to ("Labour après récolte" to "Plough after harvest"),
        "إدارة بقايا المحصول" to ("Gérer les résidus de culture" to "Manage crop residues"),
        "تجهيز الأرض وأخذ عينات التربة" to ("Préparer le sol et prélever des échantillons" to "Prepare the field and take soil samples"),
        "اختيار بذور معتمدة" to ("Choisir des semences certifiées" to "Select certified seeds"),
        "معالجة البذور" to ("Traiter les semences" to "Treat the seeds"),
        "الحرث بعد أول الأمطار" to ("Labourer après les premières pluies" to "Plough after the first rains"),
        "بداية البذر بعد أول الأمطار (حسب المنطقة)" to ("Début des semis après les premières pluies (selon la région)" to "Begin sowing after the first rains (depending on the region)"),
        "إتمام البذر" to ("Terminer les semis" to "Complete sowing"),
        "خطتك الزراعية على مدار السنة" to ("Votre plan agricole pour toute l’année" to "Your year-round farming plan"),
        "اختر المحصول والشهر لعرض الأعمال المناسبة" to ("Choisissez une culture et un mois pour afficher les travaux adaptés" to "Choose a crop and month to view recommended tasks"),
        "اختر المحصول" to ("Choisir la culture" to "Choose crop"),
        "اختر الشهر" to ("Choisir le mois" to "Choose month"),
        "التقويم الزراعي" to ("Calendrier agricole" to "Agricultural calendar"),
        "إغلاق" to ("Fermer" to "Close"),
        "إرشادات عامة للمناطق المتوسطية في شمال الجزائر، وتختلف حسب المنطقة والصنف والطقس. راجع مهندساً زراعياً قبل التنفيذ." to ("Conseils généraux pour les régions méditerranéennes du nord de l’Algérie. Ils varient selon la région, la variété et la météo. Consultez un agronome avant application." to "General guidance for Mediterranean areas of northern Algeria. Tasks vary by region, variety and weather. Consult an agronomist before carrying them out.")
    )
    val pair = translations[text] ?: return text
    return if (language == "fr") pair.first else pair.second
}

// كل محصول فيه 12 قائمة: من جانفي (0) إلى ديسمبر (11)
private val CALENDARS = listOf(
    CropCalendar(
        "الزيتون",
        listOf(
            listOf("إنهاء جني الثمار المتأخرة", "فحص الإصابة بعين الطاووس وتكرار الرش النحاسي عند الحاجة"),
            listOf("تقليم الشتاء بعد انتهاء الجني (تجنّب أيام الصقيع)", "إزالة الأغصان الجافة والمصابة"),
            listOf("إكمال التقليم", "الدفعة الأولى من التسميد الآزوتي قبل الإزهار", "مكافحة الأعشاب"),
            listOf("متابعة التبرعم", "رشة نحاسية وقائية قبل الإزهار عند الأجواء الرطبة"),
            listOf("فترة الإزهار: قلّل الرش الكيميائي وتجنّب الإجهاد المائي", "البوتاسيوم والبورون عند نقصهما (حسب تحليل الأوراق)"),
            listOf("بداية عقد الثمار: ري تكميلي في الأراضي الجافة", "مراقبة الحشرة القشرية السوداء"),
            listOf("ري منتظم لنمو الثمار", "تركيب مصائد ذبابة الزيتون"),
            listOf("مراقبة ذبابة الزيتون وتقدير الإصابة", "تغطية التربة للحفاظ على الرطوبة"),
            listOf("مكافحة ذبابة الزيتون عند بلوغ عتبة الإصابة", "ري قبل الجني للثمار الخضراء"),
            listOf("بداية جني زيتون المائدة", "تجهيز الصناديق ومعدات الجني"),
            listOf("ذروة جني الزيتون للزيت", "تسميد عضوي بعد الجني"),
            listOf("إتمام الجني", "رشة نحاسية بعد الجني")
        )
    ),
    CropCalendar(
        "الطماطم",
        listOf(
            listOf("تحضير الأرض والتسميد العضوي", "تجهيز مشتل البذور في بيت محمي"),
            listOf("زراعة البذور في المشتل"),
            listOf("تجهيز الحقل وإضافة الأسمدة القاعدية", "نقل الشتلات بعد زوال خطر الصقيع"),
            listOf("نقل الشتلات (حتى منتصف الشهر)", "ري بالتنقيط وتغطية التربة"),
            listOf("تدعيم النباتات وإزالة الأفرع الجانبية", "تسميد آزوتي بوتاسي دوري حسب النمو"),
            listOf("بداية الجني", "مراقبة حفار الطماطم (Tuta absoluta) بالمصائد الفرمونية"),
            listOf("ذروة الجني والري المنتظم", "مراقبة اللفحة المتأخرة والعناكب"),
            listOf("الجني المتواصل", "تجهيز مشتل العروة الخريفية"),
            listOf("نقل شتلات العروة الخريفية", "إزالة بقايا المحصول السابق"),
            listOf("جني العروة الخريفية وحمايتها من البرودة"),
            listOf("إزالة بقايا النباتات المصابة"),
            listOf("تحضير الأرض للموسم القادم", "التناوب الزراعي")
        )
    ),
    CropCalendar(
        "البطاطا",
        listOf(
            listOf("تجهيز الأرض والتسميد العضوي", "اقتناء تقاوي معتمدة"),
            listOf("زراعة الموسم الرئيسي في المناطق الدافئة"),
            listOf("زراعة الموسم الرئيسي", "تسميد قاعدي (فوسفور وبوتاسيوم)"),
            listOf("العزق والتتريب", "تسميد آزوتي", "مراقبة الإنبات"),
            listOf("الوقاية من اللفحة المتأخرة (الميلديو) بعد الأمطار والرطوبة", "ري منتظم"),
            listOf("الجني عند جفاف العروش ونضج الدرنات"),
            listOf("تخزين الدرنات في مكان بارد مظلم"),
            listOf("تجهيز الأرض لزراعة العروة المتأخرة"),
            listOf("زراعة العروة المتأخرة (أواخر أوت إلى سبتمبر)"),
            listOf("التتريب والتسميد", "مراقبة الميلديو"),
            listOf("ري خفيف وحماية من الصقيع"),
            listOf("الجني المبكر للعروة المتأخرة (ديسمبر إلى جانفي)")
        )
    ),
    CropCalendar(
        "الحمضيات",
        listOf(
            listOf("جني البرتقال والمندرين", "تسميد عضوي بعد الجني"),
            listOf("تقليم خفيف بعد الجني", "حماية الأشجار الصغيرة من الصقيع"),
            listOf("تسميد آزوتي قبل الإزهار", "مكافحة الأعشاب"),
            listOf("الإزهار وعقد الثمار: ري منتظم", "مراقبة المن وصانعة أنفاق الأوراق"),
            listOf("معالجة الحشرات عند وجود الإصابة (حسب بطاقة المنتج)", "ري منتظم"),
            listOf("فترة تساقط الثمار الفيزيولوجي: لا تهمل الري", "دفعة تسميد ثانية"),
            listOf("ري مكثف", "مراقبة الحشرات القشرية والعناكب"),
            listOf("مراقبة ذبابة الفاكهة المتوسطية بالمصائد", "ري منتظم"),
            listOf("تسميد بوتاسي لنضج الثمار", "مكافحة ذبابة الفاكهة"),
            listOf("بداية جني الأصناف المبكرة"),
            listOf("جني الأصناف المبكرة"),
            listOf("جني البرتقال ومراقبة خطر الصقيع")
        )
    ),
    CropCalendar(
        "القمح",
        listOf(
            listOf("مرحلة التفريع: مراقبة الأعشاب الضارة", "تصريف مياه الأمطار الزائدة"),
            listOf("مكافحة الأعشاب بعد الإنبات حسب نوعها", "الدفعة الأولى من الآزوت عند التفريع"),
            listOf("الدفعة الثانية من الآزوت عند استطالة الساق", "مراقبة الصدأ الأصفر"),
            listOf("طرد السنابل: مراقبة الأمراض الفطرية (الصدأ، سبتوريا)"),
            listOf("امتلاء الحبوب: ري تكميلي عند الجفاف إن توفر"),
            listOf("الحصاد عند نضج الحبوب ورطوبة مناسبة"),
            listOf("إتمام الحصاد", "تخزين الحبوب جافة"),
            listOf("حرث ما بعد الحصاد", "إدارة بقايا المحصول"),
            listOf("تجهيز الأرض وأخذ عينات التربة", "اختيار بذور معتمدة"),
            listOf("معالجة البذور", "الحرث بعد أول الأمطار"),
            listOf("بداية البذر بعد أول الأمطار (حسب المنطقة)"),
            listOf("إتمام البذر", "متابعة الإنبات")
        )
    )
)

@Composable
fun AgriCalendarDialog(onDismiss: () -> Unit) {
    var cropIndex by remember { mutableIntStateOf(0) }
    var month by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.MONTH)) }
    val currentMonth = remember { Calendar.getInstance().get(Calendar.MONTH) }
    val crop = CALENDARS[cropIndex]
    val language = LocaleHelper.getLanguage(LocalContext.current)

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(calendarTranslation("إغلاق", language)) }
        },
        title = { Text(calendarTranslation("التقويم الزراعي", language)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(calendarTranslation("خطتك الزراعية على مدار السنة", language), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                            Text(calendarTranslation("اختر المحصول والشهر لعرض الأعمال المناسبة", language), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                Text(text = calendarTranslation("اختر المحصول", language), style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CALENDARS.forEachIndexed { index, item ->
                        FilterChip(
                            selected = cropIndex == index,
                            onClick = { cropIndex = index },
                            label = { Text(calendarTranslation(item.name, language)) }
                        )
                    }
                }

                Text(text = calendarTranslation("اختر الشهر", language), style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MONTH_NAMES.forEachIndexed { index, name ->
                        FilterChip(
                            selected = month == index,
                            onClick = { month = index },
                            label = {
                                Text(if (index == currentMonth) "${calendarTranslation(name, language)} (${if (language == "fr") "actuel" else if (language == "en") "current" else "الحالي"})" else calendarTranslation(name, language))
                            }
                        )
                    }
                }

                HorizontalDivider()

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(calendarTranslation(crop.name, language), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                                Text(MONTH_NAMES[month], style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(
                                text = "${crop.months[month].size} ${if (language == "fr") "tâches" else if (language == "en") "tasks" else "مهام"}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(50)).padding(horizontal = 12.dp, vertical = 7.dp)
                            )
                        }
                        crop.months[month].forEachIndexed { index, task ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = when {
                                        task.contains("ري") || task.contains("ماء") -> Icons.Default.WaterDrop
                                        task.contains("تسميد") || task.contains("سماد") -> Icons.Default.Eco
                                        task.contains("جني") || task.contains("حصاد") -> Icons.Default.Agriculture
                                        else -> Icons.Default.CheckCircle
                                    },
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text("${if (language == "fr") "Tâche" else if (language == "en") "Task" else "المهمة"} ${index + 1}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                    Text(calendarTranslation(task, language), style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                            if (index != crop.months[month].lastIndex) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
                        }
                    }
                }

                HorizontalDivider()

                Text(
                    text = calendarTranslation("إرشادات عامة للمناطق المتوسطية في شمال الجزائر، وتختلف حسب المنطقة والصنف والطقس. راجع مهندساً زراعياً قبل التنفيذ.", language),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    )
}
