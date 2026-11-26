package com.example.ui

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.outlined.Biotech
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Grass
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.R
import com.example.data.ai.PlantDoctorRepository
import com.example.data.local.AgriProDatabase
import com.example.data.local.ExperimentRepository
import com.example.data.local.FertilizerCalculationRepository
import com.example.data.local.ThemePreference
import com.example.data.local.ThemePreferencesRepository
import com.example.ui.calculator.CalculatorScreen
import com.example.ui.calculator.CalculatorViewModel
import com.example.ui.encyclopedia.EncyclopediaScreen
import com.example.ui.encyclopedia.EncyclopediaViewModel
import com.example.ui.fertilizer.FertilizerCalculatorScreen
import com.example.ui.fertilizer.FertilizerCalculatorViewModel
import com.example.ui.notebook.NotebookScreen
import com.example.ui.notebook.NotebookViewModel
import com.example.ui.plantdoctor.PlantDoctorScreen
import com.example.ui.plantdoctor.PlantDoctorViewModel
import com.example.ui.theme.AgriProTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

sealed class AgriDestination(
    val route: String,
    @param:StringRes val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    data object PlantDoctor : AgriDestination(
        route = "plant_doctor",
        labelRes = R.string.nav_plant_doctor,
        selectedIcon = Icons.Filled.Biotech,
        unselectedIcon = Icons.Outlined.Biotech,
        testTag = "nav_plant_doctor"
    )

    data object FertilizerCalculator : AgriDestination(
        route = "fertilizer_calculator",
        labelRes = R.string.nav_fertilizer_calculator,
        selectedIcon = Icons.Filled.Grass,
        unselectedIcon = Icons.Outlined.Grass,
        testTag = "nav_fertilizer_calculator"
    )

    data object Calculator : AgriDestination(
        route = "calculator",
        labelRes = R.string.nav_calculator,
        selectedIcon = Icons.Filled.Calculate,
        unselectedIcon = Icons.Outlined.Calculate,
        testTag = "nav_calculator"
    )

    data object Encyclopedia : AgriDestination(
        route = "encyclopedia",
        labelRes = R.string.nav_encyclopedia,
        selectedIcon = Icons.Filled.MenuBook,
        unselectedIcon = Icons.Outlined.MenuBook,
        testTag = "nav_encyclopedia"
    )

    data object Notebook : AgriDestination(
        route = "notebook",
        labelRes = R.string.nav_notebook,
        selectedIcon = Icons.Filled.Science,
        unselectedIcon = Icons.Outlined.Science,
        testTag = "nav_notebook"
    )
}

private val destinations = listOf(
    AgriDestination.PlantDoctor,
    AgriDestination.FertilizerCalculator,
    AgriDestination.Calculator,
    AgriDestination.Encyclopedia,
    AgriDestination.Notebook
)

// منحنى حركة حريري فائق السلاسة (Ultra-Smooth Quintic Easing)
private val UltraSmoothEasing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)

private data class AgriReminder(
    val id: Long,
    val title: String,
    val category: String,
    val dateTimeText: String,
    val isDone: Boolean = false
)

private fun loadSavedReminders(context: Context): List<AgriReminder> {
    val prefs = context.getSharedPreferences("agropro_reminders_prefs", Context.MODE_PRIVATE)
    val raw = prefs.getString("reminders_json", null) ?: return emptyList()
    return try {
        val array = JSONArray(raw)
        List(array.length()) { index ->
            val obj = array.getJSONObject(index)
            AgriReminder(
                id = obj.getLong("id"),
                title = obj.getString("title"),
                category = obj.optString("category", "عام"),
                dateTimeText = obj.optString("dateTimeText", ""),
                isDone = obj.optBoolean("isDone", false)
            )
        }
    } catch (_: Exception) {
        emptyList()
    }
}

private fun persistReminders(context: Context, items: List<AgriReminder>) {
    val prefs = context.getSharedPreferences("agropro_reminders_prefs", Context.MODE_PRIVATE)
    val array = JSONArray()
    items.forEach { item ->
        val obj = JSONObject().apply {
            put("id", item.id)
            put("title", item.title)
            put("category", item.category)
            put("dateTimeText", item.dateTimeText)
            put("isDone", item.isDone)
        }
        array.put(obj)
    }
    prefs.edit().putString("reminders_json", array.toString()).apply()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgriProApp() {
    val context = LocalContext.current
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    var showReminders by remember { mutableStateOf(false) }

    val themePrefsRepo = remember { ThemePreferencesRepository(context) }
    val themePref by themePrefsRepo.themePreference.collectAsStateWithLifecycle(
        initialValue = ThemePreference.SYSTEM
    )
    val systemDark = isSystemInDarkTheme()
    val useDarkTheme = when (themePref) {
        ThemePreference.DARK -> true
        ThemePreference.LIGHT -> false
        ThemePreference.SYSTEM -> systemDark
    }

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !useDarkTheme
                controller.isAppearanceLightNavigationBars = !useDarkTheme
            }
        }
    }

    val database = remember { AgriProDatabase.getInstance(context) }
    val experimentRepo = remember {
        ExperimentRepository(
            experimentDao = database.experimentDao(),
            measurementDao = database.measurementDao(),
            diagnosisHistoryDao = database.diagnosisHistoryDao(),
            pesticideNoteDao = database.pesticideNoteDao(),
            customAgrochemicalDao = database.customAgrochemicalDao(),
            prefsRepo = themePrefsRepo
        )
    }
    val fertilizerCalcRepo = remember {
        FertilizerCalculationRepository(database.fertilizerCalculationDao())
    }
    val aiRepo = remember { PlantDoctorRepository() }
    val hazeState = remember { HazeState() }

    val plantDoctorViewModel: PlantDoctorViewModel = viewModel(
        factory = PlantDoctorViewModel.Factory(aiRepo, experimentRepo)
    )
    val fertilizerCalculatorViewModel: FertilizerCalculatorViewModel = viewModel(
        factory = FertilizerCalculatorViewModel.Factory(fertilizerCalcRepo)
    )
    val calculatorViewModel: CalculatorViewModel = viewModel(
        factory = CalculatorViewModel.Factory(themePrefsRepo)
    )
    val encyclopediaViewModel: EncyclopediaViewModel = viewModel(
        factory = EncyclopediaViewModel.Factory(experimentRepo)
    )
    val notebookViewModel: NotebookViewModel = viewModel(
        factory = NotebookViewModel.Factory(experimentRepo)
    )

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        AgriProTheme(darkTheme = useDarkTheme) {
            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route ?: AgriDestination.PlantDoctor.route

            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val useNavigationRail = maxWidth >= 680.dp

                Scaffold(
                    contentWindowInsets = WindowInsets.safeDrawing,
                    topBar = {
                        Column {
                            CenterAlignedTopAppBar(
                                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
                                ),
                                navigationIcon = {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f),
                                        shape = RoundedCornerShape(50),
                                        modifier = Modifier.padding(start = 12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Eco,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = "ميداني",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                },
                                title = {
                                    Text(
                                        text = "Agro pro",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                },
                                actions = {
                                    IconButton(
                                        onClick = { showReminders = true },
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Notifications,
                                            contentDescription = "التذكيرات"
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            scope.launch {
                                                val next = if (useDarkTheme) ThemePreference.LIGHT else ThemePreference.DARK
                                                themePrefsRepo.setThemePreference(next)
                                            }
                                        },
                                        modifier = Modifier
                                            .size(48.dp)
                                            .testTag("theme_toggle_button")
                                    ) {
                                        Icon(
                                            imageVector = if (useDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                            contentDescription = if (useDarkTheme) "تفعيل الوضع النهاري" else "تفعيل الوضع الليلي"
                                        )
                                    }
                                }
                            )
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                            )
                        }
                    },
                    bottomBar = {
                        if (!useNavigationRail) {
                            LiquidGlassNavigationBar(
                                destinations = destinations,
                                currentRoute = currentRoute,
                                useDarkTheme = useDarkTheme,
                                hazeState = hazeState,
                                onSelectDestination = { dest ->
                                    if (currentRoute != dest.route) {
                                        navController.navigate(dest.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                top = innerPadding.calculateTopPadding(),
                                bottom = if (useNavigationRail) innerPadding.calculateBottomPadding() else 0.dp
                            )
                    ) {
                        if (useNavigationRail) {
                            NavigationRail(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            ) {
                                destinations.forEach { dest ->
                                    val selected = currentRoute == dest.route
                                    val label = stringResource(dest.labelRes)
                                    NavigationRailItem(
                                        selected = selected,
                                        onClick = {
                                            navController.navigate(dest.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        icon = {
                                            Icon(
                                                imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                                contentDescription = label
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = label,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        },
                                        modifier = Modifier.testTag(dest.testTag)
                                    )
                                }
                            }
                        }

                        NavHost(
                            navController = navController,
                            startDestination = AgriDestination.PlantDoctor.route,
                            modifier = Modifier.weight(1f).hazeSource(state = hazeState),
                            enterTransition = {
                                val fromIndex = destinations.indexOfFirst { it.route == initialState.destination.route }
                                val toIndex = destinations.indexOfFirst { it.route == targetState.destination.route }
                                val direction = if (toIndex >= fromIndex) -1 else 1
                                slideInHorizontally(
                                    animationSpec = tween(durationMillis = 360, easing = UltraSmoothEasing),
                                    initialOffsetX = { fullWidth -> direction * (fullWidth / 6) }
                                ) + fadeIn(
                                    animationSpec = tween(durationMillis = 320, easing = UltraSmoothEasing)
                                ) + scaleIn(
                                    initialScale = 0.985f,
                                    animationSpec = tween(durationMillis = 360, easing = UltraSmoothEasing)
                                )
                            },
                            exitTransition = {
                                val fromIndex = destinations.indexOfFirst { it.route == initialState.destination.route }
                                val toIndex = destinations.indexOfFirst { it.route == targetState.destination.route }
                                val direction = if (toIndex >= fromIndex) 1 else -1
                                slideOutHorizontally(
                                    animationSpec = tween(durationMillis = 340, easing = UltraSmoothEasing),
                                    targetOffsetX = { fullWidth -> direction * (fullWidth / 8) }
                                ) + fadeOut(
                                    animationSpec = tween(durationMillis = 240, easing = UltraSmoothEasing)
                                ) + scaleOut(
                                    targetScale = 0.99f,
                                    animationSpec = tween(durationMillis = 340, easing = UltraSmoothEasing)
                                )
                            },
                            popEnterTransition = {
                                fadeIn(animationSpec = tween(durationMillis = 300, easing = UltraSmoothEasing))
                            },
                            popExitTransition = {
                                fadeOut(animationSpec = tween(durationMillis = 240, easing = UltraSmoothEasing))
                            }
                        ) {
                            composable(AgriDestination.PlantDoctor.route) {
                                PlantDoctorScreen(
                                    viewModel = plantDoctorViewModel,
                                    onOpenInNpkCalculator = { cropNameAr, diseaseNameAr ->
                                        fertilizerCalculatorViewModel.applyDiagnosisContext(cropNameAr, diseaseNameAr)
                                        navController.navigate(AgriDestination.FertilizerCalculator.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    onNavigateToNotebook = {
                                        navController.navigate(AgriDestination.Notebook.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                            composable(AgriDestination.FertilizerCalculator.route) {
                                FertilizerCalculatorScreen(viewModel = fertilizerCalculatorViewModel)
                            }
                            composable(AgriDestination.Calculator.route) {
                                CalculatorScreen(
                                    viewModel = calculatorViewModel,
                                    onOpenNpkCalculator = {
                                        navController.navigate(AgriDestination.FertilizerCalculator.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                            composable(AgriDestination.Encyclopedia.route) {
                                EncyclopediaScreen(viewModel = encyclopediaViewModel)
                            }
                            composable(AgriDestination.Notebook.route) {
                                NotebookScreen(viewModel = notebookViewModel)
                            }
                        }
                    }
                }

                if (showReminders) {
                    AgriRemindersDialog(
                        onDismiss = { showReminders = false }
                    )
                }
            }
        }
    }
}

@Composable
private fun AgriRemindersDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val reminders = remember {
        mutableStateListOf<AgriReminder>().apply {
            addAll(loadSavedReminders(context))
        }
    }

    var titleInput by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ري") }
    var selectedDateTime by remember { mutableStateOf("") }

    val categories = listOf("ري", "تسميد", "رش مبيد", "فحص حقلي")

    fun pickDateTime() {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                TimePickerDialog(
                    context,
                    { _, hourOfDay, minute ->
                        selectedDateTime = String.format(
                            Locale.US,
                            "%04d/%02d/%02d - %02d:%02d",
                            year,
                            month + 1,
                            dayOfMonth,
                            hourOfDay,
                            minute
                        )
                    },
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),
                    true
                ).show()
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "التذكيرات الزراعية",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = titleInput,
                    onValueChange = { titleInput = it },
                    label = { Text("عنوان التذكير (مثلاً: رش نحاس للزيتون)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { pickDateTime() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (selectedDateTime.isEmpty()) "تحديد الموعد" else selectedDateTime,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    Button(
                        onClick = {
                            val cleanTitle = titleInput.trim()
                            if (cleanTitle.isNotEmpty()) {
                                val newItem = AgriReminder(
                                    id = System.currentTimeMillis(),
                                    title = cleanTitle,
                                    category = selectedCategory,
                                    dateTimeText = selectedDateTime.ifEmpty { "بدون موعد محدد" },
                                    isDone = false
                                )
                                reminders.add(0, newItem)
                                persistReminders(context, reminders)
                                titleInput = ""
                                selectedDateTime = ""
                            }
                        },
                        enabled = titleInput.trim().isNotEmpty()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إضافة")
                    }
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                if (reminders.isEmpty()) {
                    Text(
                        text = "لا توجد تذكيرات محفوظة. أضف أول تذكير زراعي أعلاه.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(reminders, key = { it.id }) { item ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerLow,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = item.isDone,
                                        onCheckedChange = { checked ->
                                            val idx = reminders.indexOfFirst { it.id == item.id }
                                            if (idx != -1) {
                                                reminders[idx] = item.copy(isDone = checked)
                                                persistReminders(context, reminders)
                                            }
                                        }
                                    )

                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(horizontal = 4.dp)
                                    ) {
                                        Text(
                                            text = item.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            textDecoration = if (item.isDone) TextDecoration.LineThrough else null,
                                            color = if (item.isDone) {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            } else {
                                                MaterialTheme.colorScheme.onSurface
                                            }
                                        )
                                        Text(
                                            text = "${item.category} • ${item.dateTimeText}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            reminders.removeAll { it.id == item.id }
                                            persistReminders(context, reminders)
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "حذف",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق")
            }
        }
    )
}

@Composable
private fun LiquidGlassNavigationBar(
    destinations: List<AgriDestination>,
    currentRoute: String,
    useDarkTheme: Boolean,
    hazeState: HazeState,
    onSelectDestination: (AgriDestination) -> Unit
) {
    val glassShape = RoundedCornerShape(32.dp)
    val selectedIndex = destinations.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)

    val glassBackgroundBrush = if (useDarkTheme) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF1F2B24).copy(alpha = 0.18f),
                Color(0xFF121A15).copy(alpha = 0.30f)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.25f),
                Color(0xFFF1F7F3).copy(alpha = 0.18f)
            )
        )
    }

    val specularBorderBrush = if (useDarkTheme) {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.28f),
                Color.White.copy(alpha = 0.06f)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.95f),
                MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(66.dp)
                .shadow(
                    elevation = 14.dp,
                    shape = glassShape,
                    ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                    spotColor = Color.Black.copy(alpha = 0.22f)
                )
                .clip(glassShape)
                .hazeEffect(
                    state = hazeState,
                    style = HazeStyle(
                        backgroundColor = MaterialTheme.colorScheme.surface,
                        tint = HazeTint(
                            MaterialTheme.colorScheme.surface.copy(alpha = if (useDarkTheme) 0.35f else 0.45f)
                        ),
                        blurRadius = 24.dp,
                        noiseFactor = 0.04f
                    )
                )
                .background(brush = glassBackgroundBrush)
                .border(width = 1.2.dp, brush = specularBorderBrush, shape = glassShape)
                .padding(6.dp)
        ) {
            val itemWidth = maxWidth / destinations.size

            // انزلاق الكبسولة الزجاجية السائلة بسلاسة بين الخانات
            val indicatorOffset by animateDpAsState(
                targetValue = itemWidth * selectedIndex,
                animationSpec = spring(
                    dampingRatio = 0.82f,
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = "SlidingLiquidPillOffset"
            )

            // الكبسولة الزجاجية المضيئة المتحركة في الخلفية
            Box(
                modifier = Modifier
                    .offset(x = indicatorOffset)
                    .width(itemWidth)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = if (useDarkTheme) {
                                listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.34f),
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                                )
                            } else {
                                listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.20f),
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                                )
                            }
                        )
                    )
                    .border(
                        width = 0.9.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.38f),
                        shape = CircleShape
                    )
            )

            // الأيقونات والنصوص فوق الكبسولة المتحركة
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                destinations.forEach { dest ->
                    val selected = currentRoute == dest.route
                    val label = stringResource(dest.labelRes)

                    val scale by animateFloatAsState(
                        targetValue = if (selected) 1.07f else 0.96f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        label = "SmoothIconScale"
                    )

                    val contentColor by animateColorAsState(
                        targetValue = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        animationSpec = tween(durationMillis = 300, easing = UltraSmoothEasing),
                        label = "SmoothContentColor"
                    )

                    val interactionSource = remember { MutableInteractionSource() }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null,
                                onClick = { onSelectDestination(dest) }
                            )
                            .testTag(dest.testTag),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                            contentDescription = label,
                            tint = contentColor,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = label,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            color = contentColor
                        )
                    }
                }
            }
        }
    }
}
