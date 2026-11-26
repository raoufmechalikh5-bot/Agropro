package com.example.ui.calendar

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
import androidx.compose.ui.unit.dp
import java.util.Calendar

private class CropCalendar(val name: String, val months: List<List<String>>)

private val MONTH_NAMES = listOf(
    "جانفي", "فيفري", "مارس", "أفريل", "ماي", "جوان",
    "جويلية", "أوت", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"
)

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

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("إغلاق") }
        },
        title = { Text("التقويم الزراعي") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(text = "المحصول", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CALENDARS.forEachIndexed { index, item ->
                        FilterChip(
                            selected = cropIndex == index,
                            onClick = { cropIndex = index },
                            label = { Text(item.name) }
                        )
                    }
                }

                Text(text = "الشهر", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MONTH_NAMES.forEachIndexed { index, name ->
                        FilterChip(
                            selected = month == index,
                            onClick = { month = index },
                            label = {
                                Text(if (index == currentMonth) "$name (الحالي)" else name)
                            }
                        )
                    }
                }

                HorizontalDivider()

                Text(
                    text = crop.name + " - " + MONTH_NAMES[month],
                    style = MaterialTheme.typography.titleMedium
                )
                crop.months[month].forEach { task ->
                    Text(
                        text = "• $task",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                HorizontalDivider()

                Text(
                    text = "إرشادات عامة للمناطق المتوسطية في شمال الجزائر، وتختلف حسب المنطقة والصنف والطقس. راجع مهندساً زراعياً قبل التنفيذ.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    )
}
