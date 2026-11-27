package com.example.ui

import android.app.Activity
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
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.DrawerValue
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Biotech
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Grass
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.reminders.RemindersDialog
import androidx.compose.material.icons.filled.CalendarMonth
import com.example.ui.calendar.AgriCalendarDialog
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
import com.example.ui.live.AgroLiveScreen
import com.example.ui.live.AgroLiveViewModel
import com.example.ui.theme.AgriProTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.hazeEffect
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

sealed class AgriDestination(
    val route: String,
    @param:StringRes val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    data object Home : AgriDestination(
        route = "home",
        labelRes = R.string.nav_home,
        selectedIcon = Icons.Filled.Eco,
        unselectedIcon = Icons.Outlined.Grass,
        testTag = "nav_home"
    )

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
    AgriDestination.Home,
    AgriDestination.PlantDoctor,
    AgriDestination.FertilizerCalculator,
    AgriDestination.Calculator,
    AgriDestination.Encyclopedia,
    AgriDestination.Notebook
)

// منحنى حركة حريري فائق السلاسة (Ultra-Smooth Quintic Easing)
private val UltraSmoothEasing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgriProApp() {
    val context = LocalContext.current
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    var showReminders by remember { mutableStateOf(false) }
    var showCalendar by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showSplash by remember { mutableStateOf(true) }

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

    val agroLiveViewModel: AgroLiveViewModel = viewModel()
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

    CompositionLocalProvider(LocalLayoutDirection provides if (com.example.LocaleHelper.getLanguage(context) == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr) {
        AgriProTheme(darkTheme = useDarkTheme) {
            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route ?: AgriDestination.Home.route
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val useNavigationRail = maxWidth >= 680.dp

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    gesturesEnabled = true,
                    drawerContent = {
                        ModalDrawerSheet(
                            drawerContainerColor = MaterialTheme.colorScheme.surface
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .verticalScroll(rememberScrollState())
                                    .padding(horizontal = 14.dp, vertical = 18.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "AgroPro",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                                )
                                Text(
                                    text = agroText("إدارة المزرعة"),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                                )
                                destinations.forEach { dest ->
                                    val label = stringResource(dest.labelRes)
                                    NavigationDrawerItem(
                                        label = { Text(label) },
                                        selected = currentRoute == dest.route,
                                        icon = {
                                            Icon(
                                                imageVector = if (currentRoute == dest.route) dest.selectedIcon else dest.unselectedIcon,
                                                contentDescription = null
                                            )
                                        },
                                        onClick = {
                                            scope.launch {
                                                drawerState.close()
                                                navController.navigate(dest.route) {
                                                    popUpTo(navController.graph.findStartDestination().id) {
                                                        saveState = true
                                                    }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
                                )
                                Text(
                                    text = agroText("التخطيط والتنبيهات"),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                                )
                                NavigationDrawerItem(
                                    label = { Text(agroText("التقويم الزراعي")) },
                                    selected = false,
                                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                                    onClick = {
                                        scope.launch {
                                            drawerState.close()
                                            showCalendar = true
                                        }
                                    }
                                )
                                NavigationDrawerItem(
                                    label = { Text(agroText("التنبيهات والتذكيرات")) },
                                    selected = false,
                                    icon = { Icon(Icons.Default.Notifications, contentDescription = null) },
                                    onClick = {
                                        scope.launch {
                                            drawerState.close()
                                            showReminders = true
                                        }
                                    }
                                )
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
                                )
                                Text(
                                    text = agroText("التخصيص"),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                                )
                                NavigationDrawerItem(
                                    label = { Text(if (useDarkTheme) "التبديل إلى الوضع النهاري" else "التبديل إلى الوضع الليلي") },
                                    selected = false,
                                    icon = {
                                        Icon(
                                            if (useDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                            contentDescription = null
                                        )
                                    },
                                    onClick = {
                                        scope.launch {
                                            drawerState.close()
                                            val next = if (useDarkTheme) ThemePreference.LIGHT else ThemePreference.DARK
                                            themePrefsRepo.setThemePreference(next)
                                        }
                                    }
                                )

                                NavigationDrawerItem(
                                    label = { Text(stringResource(R.string.settings_title)) },
                                    selected = false,
                                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                                    onClick = {
                                        scope.launch {
                                            drawerState.close()
                                            showSettings = true
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                ) {
                Scaffold(
                    contentWindowInsets = WindowInsets.safeDrawing,
                    topBar = {
                        if (currentRoute != "agro_live") {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            tonalElevation = 2.dp
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                            CenterAlignedTopAppBar(
                                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                    containerColor = Color.Transparent
                                ),
                                title = {
                                    Text(
                                        text = "AgroPro",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                },
                                navigationIcon = {
                                    ColorOsIconButton(
                                        onClick = { scope.launch { drawerState.open() } },
                                        imageVector = Icons.Default.Menu,
                                        contentDescription = agroText("فتح القائمة الرئيسية"),
                                        modifier = Modifier.size(48.dp).testTag("top_menu_button")
                                    )
                                },
                                actions = {
                                    ColorOsIconButton(
                                        onClick = { showReminders = true },
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = agroText("التنبيهات والتذكيرات"),
                                        modifier = Modifier.testTag("top_notifications_button")
                                    )
                                    ColorOsIconButton(
                                        onClick = { showCalendar = true },
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = agroText("التقويم الزراعي"),
                                        modifier = Modifier.testTag("top_calendar_button")
                                    )
                                }
                            )
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                            )
                            }
                        }
                        }
                    },
                    bottomBar = {
                        if (!useNavigationRail && currentRoute != "agro_live") {
                            AgroBottomNavigationBar(
                                hazeState = hazeState,
                                destinations = destinations,
                                currentRoute = currentRoute,
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
                        if (useNavigationRail && currentRoute != "agro_live") {
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
                            startDestination = AgriDestination.Home.route,
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
                            composable(AgriDestination.Home.route) {
                                AgroHomeScreen(
                                    onOpenPlantDoctor = { navController.navigate(AgriDestination.PlantDoctor.route) },
                                    onOpenLive = { navController.navigate("agro_live") },
                                    onOpenFertilizer = { navController.navigate(AgriDestination.FertilizerCalculator.route) },
                                    onOpenCalculator = { navController.navigate(AgriDestination.Calculator.route) },
                                    onOpenEncyclopedia = { navController.navigate(AgriDestination.Encyclopedia.route) },
                                    onOpenNotebook = { navController.navigate(AgriDestination.Notebook.route) }
                                )
                            }
                            composable(AgriDestination.PlantDoctor.route) {
                                PlantDoctorScreen(
                                    viewModel = plantDoctorViewModel,
                                    onOpenLiveAssistant = { navController.navigate("agro_live") },
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

                                )
                            }
                            composable("agro_live") {
                                AgroLiveScreen(
                                    viewModel = agroLiveViewModel,
                                    onClose = { navController.popBackStack() }
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
                    RemindersDialog(onDismiss = { showReminders = false })
                }
                if (showCalendar) {
                    AgriCalendarDialog(onDismiss = { showCalendar = false })
                }
                if (showSettings) {
                    AgroProSettingsDialog(
                        themePreference = themePref,
                        onThemeChange = { themePrefsRepo.setThemePreference(it) },
                        onLanguageChange = { language ->
                            com.example.LocaleHelper.setLocale(context, language)
                            (context as? Activity)?.recreate()
                        },
                        onDismiss = { showSettings = false }
                    )
                }
                }
                if (showSplash) {
                    AgroProSplash(onFinished = { showSplash = false })
                }
            }
        }
    }
}



@Composable
private fun AgroHomeScreen(
    onOpenPlantDoctor: () -> Unit,
    onOpenLive: () -> Unit,
    onOpenFertilizer: () -> Unit,
    onOpenCalculator: () -> Unit,
    onOpenEncyclopedia: () -> Unit,
    onOpenNotebook: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(agroText("مرحباً بك في"), style = MaterialTheme.typography.titleMedium, color = colors.onSurfaceVariant)
            Text("AgroPro", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = colors.primary)
            Text(agroText("كل أدوات مزرعتك في مكان واحد"), style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
        }
        Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), color = colors.primaryContainer) {
            Row(Modifier.padding(horizontal = 18.dp, vertical = 18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(Modifier.size(48.dp).clip(CircleShape).background(colors.primary.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Eco, contentDescription = null, tint = colors.primary, modifier = Modifier.size(28.dp))
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(agroText("إدارة زراعية أذكى"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = colors.onPrimaryContainer)
                    Text(agroText("تابع محاصيلك واتخذ قراراتك بثقة"), style = MaterialTheme.typography.bodySmall, color = colors.onPrimaryContainer.copy(alpha = 0.78f))
                }
            }
        }
        Text(agroText("خدماتك الرئيسية"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = colors.onSurface)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            HomeFeatureCard(Modifier.weight(1f), Icons.Default.Biotech, agroText("التشخيص الذكي"), agroText("حلّل صور نباتاتك"), onOpenPlantDoctor)
            HomeFeatureCard(Modifier.weight(1f), Icons.Default.Science, agroText("المساعد الزراعي"), agroText("اسأل واحصل على إرشاد"), onOpenPlantDoctor)
        }
        Surface(modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenLive), shape = RoundedCornerShape(22.dp), color = colors.primary) {
            Row(Modifier.padding(horizontal = 18.dp, vertical = 17.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(Modifier.size(46.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.16f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.White, modifier = Modifier.size(25.dp))
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("Live", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(agroText("مساعدك المباشر بالصوت والصورة"), style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.88f))
                }
                Icon(Icons.Default.Grass, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
            }
        }
        Text(agroText("أدواتك الزراعية"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = colors.onSurface)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            HomeFeatureCard(Modifier.weight(1f), Icons.Default.Grass, agroText("حاسبة NPK"), agroText("التسميد"), onOpenFertilizer)
            HomeFeatureCard(Modifier.weight(1f), Icons.Default.Calculate, agroText("الري والتربة"), agroText("الحسابات الزراعية"), onOpenCalculator)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            HomeFeatureCard(Modifier.weight(1f), Icons.Default.MenuBook, agroText("الموسوعة"), agroText("المعرفة الزراعية"), onOpenEncyclopedia)
            HomeFeatureCard(Modifier.weight(1f), Icons.Default.Science, agroText("دفتر المزرعة"), agroText("التجارب والقياسات"), onOpenNotebook)
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun HomeFeatureCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Surface(modifier = modifier.height(142.dp).clickable(onClick = onClick), shape = RoundedCornerShape(20.dp),
        color = colors.surfaceContainerLow, tonalElevation = 1.dp, shadowElevation = 2.dp) {
        Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Icon(icon, contentDescription = null, tint = colors.primary, modifier = Modifier.size(27.dp))
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = colors.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun AgroProSplash(onFinished: () -> Unit) {
    var contentVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        contentVisible = true
        delay(1900)
        contentVisible = false
        delay(420)
        onFinished()
    }
    val contentAlpha by animateFloatAsState(if (contentVisible) 1f else 0f, tween(420, easing = UltraSmoothEasing), label = "SplashContentAlpha")
    val contentScale by animateFloatAsState(if (contentVisible) 1f else 0.82f, tween(720, easing = UltraSmoothEasing), label = "SplashContentScale")
    Box(modifier = Modifier.fillMaxSize().background(Brush.radialGradient(listOf(Color(0xFF244B32), Color(0xFF0D1F16)))), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.graphicsLayer {
            alpha = contentAlpha
            scaleX = contentScale
            scaleY = contentScale
        }) {
            Image(painter = painterResource(id = R.drawable.agripro_brand_mark), contentDescription = "AgroPro", modifier = Modifier.size(164.dp))
            Spacer(modifier = Modifier.height(18.dp))
            Text(text = "AgroPro", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = "SMART AGRICULTURE", color = Color(0xFFB8D9C2), fontSize = 11.sp, letterSpacing = 3.sp)
        }
    }
}

@Composable
private fun AgroBottomNavigationBar(
    hazeState: HazeState,
    destinations: List<AgriDestination>,
    currentRoute: String,
    onSelectDestination: (AgriDestination) -> Unit
) {
    val barShape = RoundedCornerShape(24.dp)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .height(78.dp)
            .hazeEffect(
                state = hazeState,
                style = HazeStyle(
                    backgroundColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.78f),
                    blurRadius = 20.dp,
                    tints = emptyList(),
                    noiseFactor = 0f,
                    fallbackTint = HazeTint(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.78f))
                )
            )
            .clip(barShape),
        shape = barShape,
        color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.34f),
        tonalElevation = 0.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            destinations.forEachIndexed { index, dest ->
                val selected = currentRoute == dest.route
                val label = stringResource(dest.labelRes)
                val interactionSource = remember { MutableInteractionSource() }
                val pressed by interactionSource.collectIsPressedAsState()
                val scale by animateFloatAsState(
                    targetValue = if (pressed) 0.92f else 1f,
                    animationSpec = tween(durationMillis = if (pressed) 100 else 220, easing = UltraSmoothEasing),
                    label = "QuickAccessPressScale"
                )
                val contentColor by animateColorAsState(
                    targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    animationSpec = tween(durationMillis = 220),
                    label = "QuickAccessContentColor"
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(18.dp))
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = { onSelectDestination(dest) }
                        )
                        .testTag(dest.testTag)
                        .graphicsLayer { scaleX = scale; scaleY = scale },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(
                                if (selected) MaterialTheme.colorScheme.primaryContainer
                                else Color.Transparent
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                            contentDescription = label,
                            tint = contentColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    androidx.compose.animation.AnimatedVisibility(
                        visible = selected,
                        enter = fadeIn(tween(160)) + scaleIn(initialScale = 0.96f, animationSpec = tween(160)),
                        exit = fadeOut(tween(120))
                    ) {
                        Text(
                            text = label,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = FontWeight.SemiBold,
                            color = contentColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorOsIconButton(
    onClick: () -> Unit,
    imageVector: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = tween(
            durationMillis = if (pressed) 100 else 220,
            easing = UltraSmoothEasing
        ),
        label = "ColorOSIconButtonPress"
    )
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(46.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                spotColor = Color.Black.copy(alpha = 0.12f)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.16f),
                        MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.58f),
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.34f)
                    )
                )
            )
            .border(
                width = 0.9.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.38f),
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        interactionSource = interactionSource
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.primary
        )
    }
}
