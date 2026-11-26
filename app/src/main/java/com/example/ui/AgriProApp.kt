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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Science
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.launch

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgriProApp() {
    val context = LocalContext.current
    val view = LocalView.current
    val scope = rememberCoroutineScope()

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
                            modifier = Modifier.weight(1f),
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
            }
        }
    }
}

@Composable
private fun LiquidGlassNavigationBar(
    destinations: List<AgriDestination>,
    currentRoute: String,
    useDarkTheme: Boolean,
    onSelectDestination: (AgriDestination) -> Unit
) {
    val glassShape = RoundedCornerShape(32.dp)
    val selectedIndex = destinations.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)

    val glassBackgroundBrush = if (useDarkTheme) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF1F2B24).copy(alpha = 0.42f),
                Color(0xFF121A15).copy(alpha = 0.58f)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.55f),
                Color(0xFFF1F7F3).copy(alpha = 0.42f)
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
