package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.db.AlarmEntity
import com.example.data.db.VisitEntity
import com.example.data.db.VitalLogEntity
import com.example.data.db.MedicationItem
import com.example.data.db.MedicationDb
import com.example.ui.AlarmViewModel
import com.example.ui.theme.MyApplicationTheme
import java.util.Calendar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAlarmAppScreen()
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainAlarmAppScreen() {
    val viewModel: AlarmViewModel = viewModel()
    val ringingState by viewModel.ringingAlarm.collectAsState()
    val toastMsg by viewModel.toastMessage.collectAsState()

    val isDark by viewModel.isDarkTheme.collectAsState()
    val isSenior by viewModel.isSeniorMode.collectAsState()
    val isContrast by viewModel.isHighContrast.collectAsState()

    val currentScreen by viewModel.currentScreen.collectAsState()

    val context = LocalContext.current

    val scaffoldGrad = if (isContrast) {
        Brush.verticalGradient(colors = listOf(Color.Black, Color.Black))
    } else if (!isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFAF6F0),
                Color(0xFFF2ECE2),
                Color(0xFFEBE4D5)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0A1628),
                Color(0xFF14243B),
                Color(0xFF070E1A)
            )
        )
    }

    val textPrimary = when {
        isContrast -> Color(0xFFFFFFFF)
        !isDark -> Color(0xFF1A2433)
        else -> Color(0xFFFFFFFF)
    }

    val textSecondary = when {
        isContrast -> Color(0xFFFFD700)
        !isDark -> Color(0xFF5A6675)
        else -> Color(0xFF9EACC0)
    }

    val surfaceColor = when {
        isContrast -> Color(0xFF111111)
        !isDark -> Color(0xFFFFFFFF)
        else -> Color(0xFF1A2D42)
    }

    val accentGold = when {
        isContrast -> Color(0xFFFFD700)
        else -> Color(0xFFD4AF37)
    }

    val cardBorder = if (isContrast) {
        BorderStroke(2.dp, Color(0xFFFFD700))
    } else {
        BorderStroke(1.dp, Color(0xFFD4AF37).copy(alpha = 0.25f))
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(scaffoldGrad)
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Surface(
                    color = if (isContrast) Color.Black else surfaceColor.copy(alpha = 0.85f),
                    border = cardBorder,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isDark && !isContrast) Color(0xFFD4AF37).copy(alpha = 0.15f) else Color.Transparent,
                                modifier = Modifier.clickable { viewModel.toggleLightTheme() }
                            ) {
                                Text(
                                    text = if (!isDark) "☀️ نهار" else "🌙 ليل",
                                    color = accentGold,
                                    fontSize = if (isSenior) 14.sp else 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSenior) Color(0xFFD4AF37).copy(alpha = 0.25f) else Color.Transparent,
                                modifier = Modifier.clickable { viewModel.toggleSeniorMode() }
                            ) {
                                Text(
                                    text = "👴 كبار السن",
                                    color = if (isSenior) Color.White else accentGold,
                                    fontSize = if (isSenior) 14.sp else 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isContrast) Color(0xFFFFD700) else Color.Transparent,
                                modifier = Modifier.clickable { viewModel.toggleHighContrast() }
                            ) {
                                Text(
                                    text = "🔲 تباين",
                                    color = if (isContrast) Color.Black else accentGold,
                                    fontSize = if (isSenior) 14.sp else 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "⏰ نبض الموعَد",
                                color = accentGold,
                                fontSize = if (isSenior) 18.sp else 15.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    when (currentScreen) {
                        "pin" -> PinScreen(viewModel, textPrimary, textSecondary, surfaceColor, accentGold, cardBorder, isSenior)
                        "clock" -> ClockGateScreen(viewModel, textPrimary, textSecondary, surfaceColor, accentGold, cardBorder, isSenior)
                        "medications" -> MedicationsScreen(viewModel, textPrimary, textSecondary, surfaceColor, accentGold, cardBorder, isSenior)
                        "dosageCalc" -> DosageCalcScreen(viewModel, textPrimary, textSecondary, surfaceColor, accentGold, cardBorder, isSenior)
                        "voice" -> VoiceAssistantScreen(viewModel, textPrimary, textSecondary, surfaceColor, accentGold, cardBorder, isSenior)
                        "visits" -> VisitsScreen(viewModel, textPrimary, textSecondary, surfaceColor, accentGold, cardBorder, isSenior)
                        "addVisit" -> AddVisitScreen(viewModel, textPrimary, textSecondary, surfaceColor, accentGold, cardBorder, isSenior)
                        "visitDetail" -> VisitDetailScreen(viewModel, textPrimary, textSecondary, surfaceColor, accentGold, cardBorder, isSenior)
                        "monitoring" -> MonitoringScreen(viewModel, textPrimary, textSecondary, surfaceColor, accentGold, cardBorder, isSenior)
                        "doctors" -> DoctorsScreen(viewModel, textPrimary, textSecondary, surfaceColor, accentGold, cardBorder, isSenior)
                        "prescriptions" -> PrescriptionsScreen(viewModel, textPrimary, textSecondary, surfaceColor, accentGold, cardBorder, isSenior)
                        "tests" -> TestsScreen(viewModel, textPrimary, textSecondary, surfaceColor, accentGold, cardBorder, isSenior)
                        "reports" -> ReportsScreen(viewModel, textPrimary, textSecondary, surfaceColor, accentGold, cardBorder, isSenior)
                        "settings" -> SettingsScreen(viewModel, textPrimary, textSecondary, surfaceColor, accentGold, cardBorder, isSenior)
                    }
                }
            }

            AnimatedVisibility(
                visible = ringingState != null,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                ringingState?.let { activeAlarm ->
                    RingingAlarmFullScreen(
                        alarm = activeAlarm,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

// ============================================================
// PANEL 1: SECURE LOCK PIN SCREEN
// ============================================================
@Composable
fun PinScreen(
    vm: AlarmViewModel,
    textPrimary: Color,
    textSecondary: Color,
    surfaceColor: Color,
    accentGold: Color,
    cardBorder: BorderStroke,
    isSenior: Boolean
) {
    val currentPin by vm.pinCode.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = "قفل نبض الموعد",
            tint = accentGold,
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "أدخل رمز الدخول الآمن",
            color = textPrimary,
            fontSize = if (isSenior) 22.sp else 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = "يرجى كتابة الرمز لحماية ملف المريض الطبي (الرمز: 1234)",
            color = textSecondary,
            fontSize = if (isSenior) 14.sp else 11.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0 until 4) {
                val filled = i < currentPin.length
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(if (filled) accentGold else Color.Transparent)
                        .border(2.dp, accentGold, CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        val numKeys = listOf(
            listOf('1', '2', '3'),
            listOf('4', '5', '6'),
            listOf('7', '8', '9')
        )

        numKeys.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                modifier = Modifier.padding(vertical = 6.dp)
            ) {
                row.forEach { num ->
                    Surface(
                        shape = CircleShape,
                        color = surfaceColor,
                        border = cardBorder,
                        modifier = Modifier
                            .size(70.dp)
                            .clickable {
                                vm.appendPin(num)
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = num.toString(),
                                color = textPrimary,
                                fontSize = if (isSenior) 26.sp else 22.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            modifier = Modifier.padding(vertical = 6.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = surfaceColor,
                border = cardBorder,
                modifier = Modifier
                    .size(70.dp)
                    .clickable { vm.clearPin() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "مسح",
                        color = textSecondary,
                        fontSize = if (isSenior) 13.sp else 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Surface(
                shape = CircleShape,
                color = surfaceColor,
                border = cardBorder,
                modifier = Modifier
                    .size(70.dp)
                    .clickable { vm.appendPin('0') }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "0",
                        color = textPrimary,
                        fontSize = if (isSenior) 26.sp else 22.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Surface(
                shape = CircleShape,
                color = accentGold,
                modifier = Modifier
                    .size(70.dp)
                    .clickable { vm.navigateTo("clock") }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "تجاوز",
                        color = Color.Black,
                        fontSize = if (isSenior) 14.sp else 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ============================================================
// PANEL 2: THERAPEUTIC ANALOG CLOCK GATE CENTER
// ============================================================
@Composable
fun ClockGateScreen(
    vm: AlarmViewModel,
    textPrimary: Color,
    textSecondary: Color,
    surfaceColor: Color,
    accentGold: Color,
    cardBorder: BorderStroke,
    isSenior: Boolean
) {
    val currentTime by vm.currentTimeString.collectAsState()
    val currentDate by vm.currentDateString.collectAsState()

    val cal = Calendar.getInstance()
    val tickSeconds = cal.get(Calendar.SECOND) * 6f
    val tickMinutes = cal.get(Calendar.MINUTE) * 6f + cal.get(Calendar.SECOND) * 0.1f
    val tickHours = (cal.get(Calendar.HOUR) % 12) * 30f + cal.get(Calendar.MINUTE) * 0.5f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceColor.copy(alpha = 0.6f)),
            border = cardBorder,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "﴿ وَإِذَا مَرِضْتُ فَهُوَ يَشْفِينِ ﴾",
                    color = accentGold,
                    fontSize = if (isSenior) 18.sp else 15.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "نبض الموعد – الرفيق الصيدلي الأمين لصحتك وسلامة مريضك",
                    color = textSecondary,
                    fontSize = if (isSenior) 11.sp else 9.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(50),
            color = surfaceColor,
            border = cardBorder
        ) {
            Text(
                text = "بوابة المواعيد الدائرية",
                color = accentGold,
                fontSize = if (isSenior) 12.sp else 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            )
        }

        Box(
            modifier = Modifier
                .size(310.dp)
                .border(6.dp, accentGold, CircleShape)
                .background(surfaceColor.copy(alpha = 0.35f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(240.dp)) {
                val center = Offset(size.width / 2, size.height / 2)

                val hourLength = size.width * 0.23f
                val hourAngleRad = Math.toRadians((tickHours - 90f).toDouble())
                val hourEnd = Offset(
                    (center.x + hourLength * Math.cos(hourAngleRad)).toFloat(),
                    (center.y + hourLength * Math.sin(hourAngleRad)).toFloat()
                )
                drawLine(accentGold, center, hourEnd, strokeWidth = 10f)

                val minuteLength = size.width * 0.35f
                val minuteAngleRad = Math.toRadians((tickMinutes - 90f).toDouble())
                val minuteEnd = Offset(
                    (center.x + minuteLength * Math.cos(minuteAngleRad)).toFloat(),
                    (center.y + minuteLength * Math.sin(minuteAngleRad)).toFloat()
                )
                drawLine(textPrimary, center, minuteEnd, strokeWidth = 6f)

                val secondLength = size.width * 0.42f
                val secondAngleRad = Math.toRadians((tickSeconds - 90f).toDouble())
                val secondEnd = Offset(
                    (center.x + secondLength * Math.cos(secondAngleRad)).toFloat(),
                    (center.y + secondLength * Math.sin(secondAngleRad)).toFloat()
                )
                drawLine(Color(0xFFFF2A6D), center, secondEnd, strokeWidth = 3f)

                drawCircle(accentGold, radius = 12f, center = center)
            }

            val dialModules = listOf(
                Triple("medications", "💊", 0f),
                Triple("dosageCalc", "🧮", 30f),
                Triple("voice", "🎙️", 60f),
                Triple("visits", "🏥", 90f),
                Triple("addVisit", "➕", 120f),
                Triple("monitoring", "📈", 150f),
                Triple("doctors", "👨‍⚕️", 180f),
                Triple("prescriptions", "📄", 210f),
                Triple("tests", "🧪", 240f),
                Triple("sos", "🆘", 270f),
                Triple("reports", "📊", 300f),
                Triple("settings", "⚙️", 330f)
            )

            dialModules.forEach { (route, label, deg) ->
                val rad = Math.toRadians((deg - 90f).toDouble())
                val radiusOffset = 118f
                val x = (radiusOffset * Math.cos(rad)).toFloat()
                val y = (radiusOffset * Math.sin(rad)).toFloat()

                Surface(
                    shape = CircleShape,
                    color = surfaceColor,
                    border = BorderStroke(2.dp, accentGold),
                    modifier = Modifier
                        .offset(x = x.dp, y = y.dp)
                        .size(44.dp)
                        .clickable { vm.navigateTo(route) }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = label,
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = currentTime.ifBlank { "07:30:00 ص" },
                fontSize = if (isSenior) 36.sp else 30.sp,
                fontWeight = FontWeight.Black,
                color = textPrimary,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = currentDate.ifBlank { "السبت" },
                fontSize = if (isSenior) 14.sp else 11.sp,
                fontWeight = FontWeight.Bold,
                color = textSecondary
            )
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceColor),
            border = cardBorder,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "التحديات اليقظة النشطة",
                        color = textPrimary,
                        fontSize = if (isSenior) 16.sp else 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "إيقاف ذكي",
                        color = accentGold,
                        fontSize = if (isSenior) 13.sp else 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf(
                        "الحساب" to "Math",
                        "الاهتزاز" to "Shake",
                        "النمط" to "Pattern"
                    ).forEach { (display, type) ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = surfaceColor.copy(alpha = 0.5f),
                            border = cardBorder
                        ) {
                            Text(
                                display,
                                color = textSecondary,
                                fontSize = if (isSenior) 11.sp else 9.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================
// PANEL 3: CLINICAL DRUG ENCYCLOPEDIA (60 DRUGS)
// ============================================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MedicationsScreen(
    vm: AlarmViewModel,
    textPrimary: Color,
    textSecondary: Color,
    surfaceColor: Color,
    accentGold: Color,
    cardBorder: BorderStroke,
    isSenior: Boolean
) {
    var searchQuery by remember { mutableStateOf("") }
    val curCat by vm.curCat.collectAsState()
    val selectedDrug by vm.selectedDrug.collectAsState()

    val categories = listOf(
        "all" to "الكل",
        "مسكنات" to "مسكنات",
        "مضادات حيوية" to "مضادات",
        "قلب" to "قلب",
        "سكري" to "سكري",
        "تنفسي" to "تنفسي",
        "هضمي" to "هضمي",
        "أعصاب" to "أعصاب",
        "فطريات" to "فطريات",
        "فيتامينات" to "فيتامينات",
        "كورتيزونات" to "كورتيزونات",
        "ملاريا" to "ملاريا",
        "جرب" to "جرب",
        "مناعة" to "مناعة"
    )

    val drugsList = MedicationDb.list.filter { d ->
        (curCat == "all" || d.category == curCat) &&
        (searchQuery.isBlank() || d.name.contains(searchQuery) || d.category.contains(searchQuery))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { vm.goBack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = accentGold)
            }
            Text(
                text = "قاعدة بيانات 60 دواءً معتمد",
                color = textPrimary,
                fontSize = if (isSenior) 20.sp else 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("ابحث عن الدواء بجهد أقل...", color = textSecondary) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = accentGold) },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "صوت",
                    tint = accentGold,
                    modifier = Modifier.clickable {
                        searchQuery = "أسبرين"
                        vm.triggerToast("🎤 تم استلام البحث الصوتي: أسبرين")
                    }
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = textPrimary,
                unfocusedTextColor = textPrimary,
                focusedBorderColor = accentGold,
                unfocusedBorderColor = accentGold.copy(alpha = 0.5f)
            )
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            categories.forEach { (catId, display) ->
                val active = curCat == catId
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (active) accentGold else surfaceColor,
                    border = if (active) null else cardBorder,
                    modifier = Modifier
                        .clickable { vm.setCurCat(catId) }
                        .padding(vertical = 2.dp)
                ) {
                    Text(
                        text = display,
                        color = if (active) Color.Black else textPrimary,
                        fontSize = if (isSenior) 12.sp else 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(drugsList) { drug ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { vm.selectDrug(drug) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = surfaceColor),
                    border = if (selectedDrug?.id == drug.id) BorderStroke(1.5.dp, Color(0xFFFF2A6D)) else BorderStroke(1.dp, Color.Transparent)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = drug.category,
                            color = accentGold,
                            fontSize = if (isSenior) 11.sp else 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "💊 ${drug.name}",
                            color = textPrimary,
                            fontSize = if (isSenior) 16.sp else 13.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        AnimatedVisibility(visible = selectedDrug != null) {
            selectedDrug?.let { drug ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = surfaceColor),
                    border = BorderStroke(1.dp, Color(0xFFFF2A6D).copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "إضافة للمريض",
                                color = accentGold,
                                fontSize = if (isSenior) 13.sp else 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable {
                                    vm.triggerToast("✅ تم إضافة الدواء ${drug.name} لجدول المريض المعتمد!")
                                    vm.selectDrug(null)
                                }
                            )
                            Text(
                                text = "دواء مفسّر: ${drug.name}",
                                color = textPrimary,
                                fontSize = if (isSenior) 16.sp else 14.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Text(
                            text = "الجرعة بالروتين: ${drug.dosage}",
                            color = textSecondary,
                            fontSize = if (isSenior) 12.sp else 10.sp,
                            textAlign = TextAlign.Right
                        )
                        Text(
                            text = "طريقة التناول: ${drug.route}",
                            color = textSecondary,
                            fontSize = if (isSenior) 12.sp else 10.sp,
                            textAlign = TextAlign.Right
                        )
                        Text(
                            text = "تحذير: ${drug.warnings}",
                            color = Color(0xFFFF2A6D),
                            fontSize = if (isSenior) 11.sp else 9.sp,
                            textAlign = TextAlign.Right,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    vm.selectCalcDrug(drug)
                                    vm.navigateTo("dosageCalc")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2A6D)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("محاكي حاسبة الجرعات", color = Color.White, fontSize = if (isSenior) 12.sp else 10.sp)
                            }
                            Button(
                                onClick = { vm.selectDrug(null) },
                                colors = ButtonDefaults.buttonColors(containerColor = textSecondary.copy(alpha = 0.2f)),
                                modifier = Modifier.width(80.dp)
                            ) {
                                Text("إغلاق", color = textPrimary, fontSize = if (isSenior) 12.sp else 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================
// PANEL 4: WEIGHT-BASED DOSAGE CALCULATOR
// ============================================================
@Composable
fun DosageCalcScreen(
    vm: AlarmViewModel,
    textPrimary: Color,
    textSecondary: Color,
    surfaceColor: Color,
    accentGold: Color,
    cardBorder: BorderStroke,
    isSenior: Boolean
) {
    val selectedCalcDrug by vm.selectedCalcDrug.collectAsState()
    val dosageResult by vm.dosageResult.collectAsState()

    var weightInput by remember { mutableStateOf("70") }
    var ageInput by remember { mutableStateOf("65") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { vm.goBack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = accentGold)
            }
            Text(
                text = "حاسبة الجرعات حسب الوزن",
                color = textPrimary,
                fontSize = if (isSenior) 20.sp else 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = "اختر الدواء المطلوب لحساب فوري دقيق:",
            color = textPrimary,
            fontSize = if (isSenior) 14.sp else 12.sp,
            fontWeight = FontWeight.Bold
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .border(cardBorder, RoundedCornerShape(12.dp)),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(MedicationDb.list) { drug ->
                Surface(
                    color = if (selectedCalcDrug?.id == drug.id) accentGold else surfaceColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { vm.selectCalcDrug(drug) }
                ) {
                    Text(
                        text = "💊 ${drug.name} (${drug.category})",
                        color = if (selectedCalcDrug?.id == drug.id) Color.Black else textPrimary,
                        fontSize = if (isSenior) 13.sp else 11.sp,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = weightInput,
                onValueChange = {
                    weightInput = it
                    vm.calculateDosage(it.toDoubleOrNull() ?: 70.0, ageInput.toDoubleOrNull() ?: 65.0)
                },
                modifier = Modifier.weight(1f),
                label = { Text("الوزن (كجم)", color = textSecondary) },
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = textPrimary, unfocusedTextColor = textPrimary, focusedBorderColor = accentGold, unfocusedBorderColor = accentGold.copy(0.4f))
            )

            OutlinedTextField(
                value = ageInput,
                onValueChange = {
                    ageInput = it
                    vm.calculateDosage(weightInput.toDoubleOrNull() ?: 70.0, it.toDoubleOrNull() ?: 65.0)
                },
                modifier = Modifier.weight(1f),
                label = { Text("العمر", color = textSecondary) },
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = textPrimary, unfocusedTextColor = textPrimary, focusedBorderColor = accentGold, unfocusedBorderColor = accentGold.copy(0.4f))
            )
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceColor),
            border = cardBorder,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "تقرير تحليلي دقيق للجرعة",
                    color = accentGold,
                    fontSize = if (isSenior) 16.sp else 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = dosageResult,
                    color = textPrimary,
                    fontSize = if (isSenior) 14.sp else 12.sp,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Button(
            onClick = {
                vm.calculateDosage(weightInput.toDoubleOrNull() ?: 70.0, ageInput.toDoubleOrNull() ?: 65.0)
                vm.triggerToast("📊 تم احتساب المعادلة الطبية وحفظ السيرة المورفية للمريض")
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2A6D)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("تثبيت وحفظ الجرعة", color = Color.White, fontSize = if (isSenior) 16.sp else 13.sp)
        }
    }
}

// ============================================================
// PANEL 5: DYNAMIC TEXT-TO-SPEECH VOICE ASSISTANT
// ============================================================
@Composable
fun VoiceAssistantScreen(
    vm: AlarmViewModel,
    textPrimary: Color,
    textSecondary: Color,
    surfaceColor: Color,
    accentGold: Color,
    cardBorder: BorderStroke,
    isSenior: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { vm.goBack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = accentGold)
            }
            Text(
                text = "المساعد الصوتي وملاحة النطق",
                color = textPrimary,
                fontSize = if (isSenior) 20.sp else 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceColor.copy(0.6f)),
            border = cardBorder,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎙️ تحدث بصوت مسموع",
                    color = accentGold,
                    fontSize = if (isSenior) 18.sp else 15.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "انقر على الأسئلة السريعة أدناه لنطق الإجابة بصوت عالي لمساعدة المرضى كبار السن وضمان وصول التوجيهات الطبية دون أخطاء.",
                    color = textSecondary,
                    fontSize = if (isSenior) 12.sp else 10.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        listOf(
            "موعد" to "🗣️ متى أقرب موعد طبي لي مع الدكتور؟",
            "طبيب" to "🗣️ ما هي تعليمات الطبيب الأخيرة المسجلة؟",
            "أعمل" to "🗣️ ما الذي يجب علي القيام به اليوم للحفاظ على صحتي؟",
            "تقرير" to "🗣️ أرسل تقرير قياس ضغط الدم الأخير فوراً للطبيب",
            "دواء" to "🗣️ ابحث لي ببحث سريع عن قاعدة أدوية الضغط والسكري",
            "جرعة" to "🗣️ احسب لي جرعة الميتفورمين بناءً على وزني",
            "سكر" to "🗣️ احفظ وسجل لي قراءة السكر صائم وتأكد من سلامتي"
        ).forEach { (param, question) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { vm.executeVoiceCommand(param) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                border = cardBorder
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = accentGold)
                    Text(
                        text = question,
                        color = textPrimary,
                        fontSize = if (isSenior) 14.sp else 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Right
                    )
                }
            }
        }
    }
}

// ============================================================
// PANEL 6: VISIT MANAGER (FLIP 3D CAPABILITY)
// ============================================================
@Composable
fun VisitsScreen(
    vm: AlarmViewModel,
    textPrimary: Color,
    textSecondary: Color,
    surfaceColor: Color,
    accentGold: Color,
    cardBorder: BorderStroke,
    isSenior: Boolean
) {
    val visits by vm.visitList.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { vm.goBack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = accentGold)
            }
            Text(
                text = "جدول مواعيد الأطباء والزيارات",
                color = textPrimary,
                fontSize = if (isSenior) 20.sp else 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val total = visits.size
            val upcoming = visits.count { it.status == "upcoming" }
            val completed = visits.count { it.status == "completed" }

            listOf(
                "المجموع" to total,
                "قادمة" to upcoming,
                "مكتملة" to completed
            ).forEach { (label, count) ->
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = surfaceColor),
                    border = cardBorder
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = count.toString(), color = accentGold, fontSize = if (isSenior) 24.sp else 20.sp, fontWeight = FontWeight.Black)
                        Text(text = label, color = textSecondary, fontSize = if (isSenior) 11.sp else 9.sp)
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(visits) { visit ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            vm.selectVisit(visit.id)
                            vm.navigateTo("visitDetail")
                        },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = surfaceColor),
                    border = cardBorder
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (visit.status == "upcoming") Color.Green.copy(0.12f) else Color.Blue.copy(0.12f)
                        ) {
                            Text(
                                text = if (visit.status == "upcoming") "قادمة" else "مكتملة",
                                color = if (visit.status == "upcoming") Color.Green else Color.Cyan,
                                fontSize = if (isSenior) 11.sp else 9.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = visit.patientName, color = textPrimary, fontSize = if (isSenior) 16.sp else 14.sp, fontWeight = FontWeight.Bold)
                            Text(text = "${visit.doctorName} (${visit.specialty})", color = accentGold, fontSize = if (isSenior) 13.sp else 11.sp)
                            Text(text = "${visit.date} · ${visit.time}", color = textSecondary, fontSize = if (isSenior) 11.sp else 9.sp)
                        }
                    }
                }
            }
        }

        Button(
            onClick = { vm.navigateTo("addVisit") },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2A6D)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("➕ إضافة زيارة جديدة", color = Color.White, fontSize = if (isSenior) 16.sp else 13.sp)
        }
    }
}

// ============================================================
// PANEL 7: ADD NEW VISIT FORM WITH CALENDAR & TIME LOGIC
// ============================================================
@Composable
fun AddVisitScreen(
    vm: AlarmViewModel,
    textPrimary: Color,
    textSecondary: Color,
    surfaceColor: Color,
    accentGold: Color,
    cardBorder: BorderStroke,
    isSenior: Boolean
) {
    var doctorSelected by remember { mutableStateOf("د. سامر (قلب)") }
    var specialtySelected by remember { mutableStateOf("قلب") }
    var clinicInput by remember { mutableStateOf("مستشفى السلام") }
    var reasonInput by remember { mutableStateOf("مراجعة دورية") }
    var selectedDate by remember { mutableStateOf("2026-06-21") }
    var selectedTime by remember { mutableStateOf("10:00 ص") }

    var showCalendarDialog by remember { mutableStateOf(false) }
    var showTimeDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.End
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { vm.goBack() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
            ) {
                Text("⬅ عودة", color = Color.White)
            }
            Text("🏥 إضافة زيارة جديدة", color = accentGold, fontSize = 20.sp, fontWeight = FontWeight.Black)
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text("طبيب الموعد:", color = textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("د. سامر", "د. خالد", "د. نورة").forEach { dr ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (doctorSelected.contains(dr)) accentGold else surfaceColor,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            doctorSelected = when (dr) {
                                "د. سامر" -> "د. سامر (قلب)"
                                "د. خالد" -> "د. خالد (عظام)"
                                else -> "د. نورة (عيون)"
                            }
                            specialtySelected = when (dr) {
                                "د. سامر" -> "قلب"
                                "د. خالد" -> "عظام"
                                else -> "عيون"
                            }
                        }
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(10.dp)) {
                        Text(dr, color = if (doctorSelected.contains(dr)) Color.Black else textPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = clinicInput,
            onValueChange = { clinicInput = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("المستشفى أو المركز الطبي", color = textSecondary) },
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = textPrimary, unfocusedTextColor = textPrimary, focusedBorderColor = accentGold, unfocusedBorderColor = accentGold.copy(0.4f))
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = reasonInput,
            onValueChange = { reasonInput = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("الغرض من الزيارة", color = textSecondary) },
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = textPrimary, unfocusedTextColor = textPrimary, focusedBorderColor = accentGold, unfocusedBorderColor = accentGold.copy(0.4f))
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { showCalendarDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = surfaceColor),
                border = cardBorder
            ) {
                Text("📅 اختر من التقويم", color = accentGold, fontSize = 11.sp)
            }
            Text("التاريخ المحدد: $selectedDate", color = textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { showTimeDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = surfaceColor),
                border = cardBorder
            ) {
                Text("⏰ اختر الوقت المفضل", color = accentGold, fontSize = 11.sp)
            }
            Text("الوقت المحدد: $selectedTime", color = textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                vm.addNewVisit(
                    patient = "الحاجة مريم",
                    doctor = doctorSelected,
                    specialty = specialtySelected,
                    clinic = clinicInput,
                    date = selectedDate,
                    time = selectedTime,
                    reason = reasonInput
                )
                vm.triggerToast("✅ تم حفظ الزيارة بنجاح في قاعدة البيانات!")
                vm.navigateTo("visits")
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2A6D)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("💾 حفظ الزيارة وتثبيت موعد التذكير", color = Color.White, fontSize = if (isSenior) 16.sp else 13.sp, fontWeight = FontWeight.Black)
        }
    }

    if (showCalendarDialog) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(0.7f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                border = cardBorder,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("اختر تاريخ المراجعة", color = accentGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(6.dp))

                    val daysOfWeekStr = listOf("س", "ج", "خ", "ر", "ث", "ن", "ح")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        daysOfWeekStr.forEach { Text(it, color = accentGold, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        for (w in 0..4) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                for (d in 0..6) {
                                    val index = w * 7 + d
                                    if (index < dimDaysOffset) {
                                        Spacer(modifier = Modifier.size(34.dp))
                                    } else {
                                        val dayNum = index - dimDaysOffset + 1
                                        if (dayNum <= 30) {
                                            val dateKey = "2026-06-${String.format("%02d", dayNum)}"
                                            val isSel = selectedDate == dateKey
                                            Surface(
                                                shape = CircleShape,
                                                color = if (isSel) accentGold else Color.Transparent,
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clickable {
                                                        selectedDate = dateKey
                                                        showCalendarDialog = false
                                                    }
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        dayNum.toString(),
                                                        color = if (isSel) Color.Black else textPrimary,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        } else {
                                            Spacer(modifier = Modifier.size(34.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Button(
                        onClick = { showCalendarDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = accentGold)
                    ) {
                        Text("إغلاق", color = Color.Black)
                    }
                }
            }
        }
    }

    if (showTimeDialog) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(0.7f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                border = cardBorder,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("اختر وقت المراجعة المفضل", color = accentGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("08:00 ص", "10:00 ص", "12:00 م", "02:00 م", "04:00 م").forEach { time ->
                            val isSel = selectedTime == time
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSel) accentGold else Color.Transparent,
                                border = if (isSel) null else cardBorder,
                                modifier = Modifier.clickable {
                                    selectedTime = time
                                    showTimeDialog = false
                                }
                            ) {
                                Text(time, color = if (isSel) Color.Black else textPrimary, fontSize = 10.sp, modifier = Modifier.padding(8.dp))
                            }
                        }
                    }
                    Button(
                        onClick = { showTimeDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = accentGold)
                    ) {
                        Text("إغلاق", color = Color.Black)
                    }
                }
            }
        }
    }
}

val dimDaysOffset = 1

// ============================================================
// PANEL 8: DETAILED CLINICAL VISIT PANEL WITH FLIP 3D
// ============================================================
@Composable
fun VisitDetailScreen(
    vm: AlarmViewModel,
    textPrimary: Color,
    textSecondary: Color,
    surfaceColor: Color,
    accentGold: Color,
    cardBorder: BorderStroke,
    isSenior: Boolean
) {
    val visits by vm.visitList.collectAsState()
    val activeId by vm.selectedVisitId.collectAsState()

    val currentVisit = visits.find { it.id == activeId } ?: visits.firstOrNull()

    var isFlipped by remember { mutableStateOf(false) }

    val visitTab by vm.selectedVisitTab.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { vm.goBack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = accentGold)
            }
            Text(
                text = "ملف مراجعة وتناضح الزيارة",
                color = textPrimary,
                fontSize = if (isSenior) 20.sp else 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        currentVisit?.let { visit ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isFlipped = !isFlipped },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                border = cardBorder
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (!isFlipped) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("🔄 Flip 3D", color = accentGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(text = "بطاقة المريض", color = textSecondary, fontSize = 11.sp)
                        }

                        Text(text = "المريض: ${visit.patientName}", color = textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Black)
                        Text(text = "الطبيب المعالج: ${visit.doctorName}", color = accentGold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(text = "التخصص الطبي: ${visit.specialty}", color = textPrimary, fontSize = 13.sp)
                        Text(text = "العيادة: ${visit.clinicName}", color = textSecondary, fontSize = 11.sp)
                        Text(text = "تاريخ الحجز: ${visit.date} · ${visit.time}", color = textSecondary, fontSize = 11.sp)
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("🔄 العودة للمواصفات", color = accentGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(text = "التشخيص الذكي والمقترحات", color = Color(0xFFFF2A6D), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Text(text = "تعليمات الطبيب الكاملة:", color = textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(text = visit.reason, color = textSecondary, fontSize = 12.sp, textAlign = TextAlign.Right)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "💡 تحليل الذكاء الاصطناعي التبادلي (معالج Gemini):", color = accentGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(text = "استمر على العلاج الدوائي بانتظام، راقب ضغط الدم مرتين يومياً بالصباح والمساء، وراجع الطبيب فوراً حال حدوث قصور تنفسي أو ألم شديد.", color = textSecondary, fontSize = 11.sp, textAlign = TextAlign.Right)
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("أدوية" to 0, "وصفات" to 1, "فحوصات" to 2, "أشعة" to 3).forEach { (label, index) ->
                    val isSel = visitTab == index
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSel) Color(0xFFFF2A6D) else surfaceColor,
                        border = if (isSel) null else cardBorder,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { vm.setVisitTab(index) }
                            .padding(horizontal = 4.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(10.dp)) {
                            Text(text = label, color = if (isSel) Color.White else textPrimary, fontSize = if (isSenior) 12.sp else 10.sp)
                        }
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                border = cardBorder,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                ) {
                    when (visitTab) {
                        0 -> {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("الأدوية الموصى بها في هذه الزيارة:", color = accentGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = surfaceColor.copy(alpha = 0.5f))
                                ) {
                                    Text(
                                        text = "💊 أسبرين وقائي (75 مجم)\nالاستخدام: قرص واحد فموياً يومياً بعد طعام الغداء مباشرة.",
                                        color = textPrimary,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(8.dp),
                                        textAlign = TextAlign.Right
                                    )
                                }
                            }
                        }
                        1 -> {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("صور فوتوغرافية للوصفة الورقية:", color = accentGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Button(onClick = { vm.triggerToast("📸 فتح الكاميرا لتصوير الروشتة") }, modifier = Modifier.weight(1f)) {
                                        Text("كاميرا", fontSize = 10.sp)
                                    }
                                    Button(onClick = { vm.triggerToast("🖼️ تصفح معرض الصور لإرفاق الوصفة") }, modifier = Modifier.weight(1f)) {
                                        Text("معرض", fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                        2 -> {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("الفحوصات العاجلة المطلوبة:", color = accentGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("🧪 تخطيط كهربائية القلب (ECG)\nالحالة: مطلوب إجراؤه قبل موعد الاستشارة القادم لتقييم صدمات القلب.", color = textPrimary, fontSize = 11.sp, textAlign = TextAlign.Right)
                            }
                        }
                        else -> {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("الأشعة المطلوبة:", color = accentGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("🩻 تصوير الصدر بالأشعة السينية (X-Ray)\nالحالة: طبيعي، تم مطابقتها بالأسبوع المنصرم بنجاح.", color = textPrimary, fontSize = 11.sp, textAlign = TextAlign.Right)
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        vm.updateVisitStatus(visit.id, "cancelled", "تم الإلغاء وتعديل المواعيد")
                        vm.triggerToast("❌ تم إلغاء الزيارة في السجل، وتأكيد الإخطار للمشرف والممرض المعالج!")
                        vm.navigateTo("visits")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("إلغاء الموعد", color = Color.White, fontSize = if (isSenior) 12.sp else 10.sp)
                }

                Button(
                    onClick = {
                        vm.triggerToast("📤 تم تصدير ملف الموعد وملاحظات الدكتور كتقرير PDF وحفظه في ملفات الهاتف!")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentGold),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("تصدير تقرير", color = Color.Black, fontSize = if (isSenior) 12.sp else 10.sp)
                }
            }
        }
    }
}

// ============================================================
// PANEL 9: PHYSIOLOGICAL CLINICAL TELEMETRY MONITORING
// ============================================================
@Composable
fun MonitoringScreen(
    vm: AlarmViewModel,
    textPrimary: Color,
    textSecondary: Color,
    surfaceColor: Color,
    accentGold: Color,
    cardBorder: BorderStroke,
    isSenior: Boolean
) {
    val vitals by vm.vitalsList.collectAsState()

    var sysValue by remember { mutableStateOf("125") }
    var diaValue by remember { mutableStateOf("82") }
    var sugarValue by remember { mutableStateOf("115") }
    var pulseValue by remember { mutableStateOf("72") }
    var selectedMood by remember { mutableStateOf("😊") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.End
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { vm.goBack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = accentGold)
            }
            Text(
                text = "المتابعة السريرية اليومية",
                color = textPrimary,
                fontSize = if (isSenior) 20.sp else 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceColor),
            border = cardBorder,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text("المؤشرات المسجلة لليوم:", color = accentGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)

                vitals.firstOrNull()?.let { log ->
                    Text("🩺 ضغط الدم الشرياني: ${log.systolic} / ${log.diastolic} ملم زئبق (طبيعي مستقر)", color = textPrimary, fontSize = 12.sp)
                    Text("🩸 مستوى السكر بالدم: ${log.bloodSugar} ملغ/دسل (حالة جيدة صائمة)", color = textPrimary, fontSize = 12.sp)
                    Text("💓 معدل ضربات القلب: ${log.pulseRate} نبضة/دقيقة", color = textPrimary, fontSize = 12.sp)
                    Text("🎭 المزاج والراحة النفسية: ${log.mood}", color = textPrimary, fontSize = 12.sp)
                } ?: Text("لا توجد قراءات مسجلة لليوم، يرجى كتابتها أدناه لتجنب النسيان وتنبيه طبيبك.", color = textSecondary, fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text("سجل قياس جديد الآن:", color = textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = sysValue,
                onValueChange = { sysValue = it },
                modifier = Modifier.weight(1f),
                label = { Text("الضغط الانقباضي", color = textSecondary, fontSize = 10.sp) },
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = textPrimary, unfocusedTextColor = textPrimary, focusedBorderColor = accentGold, unfocusedBorderColor = accentGold.copy(0.4f))
            )

            OutlinedTextField(
                value = diaValue,
                onValueChange = { diaValue = it },
                modifier = Modifier.weight(1f),
                label = { Text("الضغط الانبساطي", color = textSecondary, fontSize = 10.sp) },
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = textPrimary, unfocusedTextColor = textPrimary, focusedBorderColor = accentGold, unfocusedBorderColor = accentGold.copy(0.4f))
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = sugarValue,
                onValueChange = { sugarValue = it },
                modifier = Modifier.weight(1f),
                label = { Text("مستوى السكر بالدم", color = textSecondary, fontSize = 10.sp) },
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = textPrimary, unfocusedTextColor = textPrimary, focusedBorderColor = accentGold, unfocusedBorderColor = accentGold.copy(0.4f))
            )

            OutlinedTextField(
                value = pulseValue,
                onValueChange = { pulseValue = it },
                modifier = Modifier.weight(1f),
                label = { Text("نبضات القلب BPM", color = textSecondary, fontSize = 10.sp) },
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = textPrimary, unfocusedTextColor = textPrimary, focusedBorderColor = accentGold, unfocusedBorderColor = accentGold.copy(0.4f))
            )
        }

        Text("كيف يشعر المريض حالياً؟", color = textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            listOf(
                "😊 مميز" to "😊",
                "😐 مستقر" to "😐",
                "😣 ألم" to "😣",
                "🤒 تعب" to "🤒",
                "🤢 مغص" to "🤢",
                "😢 حزين" to "😢"
            ).forEach { (display, emoji) ->
                val active = selectedMood == emoji
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (active) accentGold else surfaceColor,
                    border = if (active) null else cardBorder,
                    modifier = Modifier
                        .clickable { selectedMood = emoji }
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(emoji, fontSize = 18.sp)
                        Text(display, color = if (active) Color.Black else textSecondary, fontSize = 8.sp)
                    }
                }
            }
        }

        Button(
            onClick = {
                val sys = sysValue.toIntOrNull() ?: 120
                val dia = diaValue.toIntOrNull() ?: 80
                val sugar = sugarValue.toIntOrNull() ?: 110
                val pulse = pulseValue.toIntOrNull() ?: 72
                vm.addNewVitalLog(sys, dia, sugar, false, pulse, selectedMood)
                vm.triggerToast("📊 تم تسجيل قياسات العلامات الحيوية وحفظها في قاعدة البيانات الملحقة!")
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2A6D)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("💾 تثبيت وحفظ القراءة الحيوية للتقرير", color = Color.White, fontSize = if (isSenior) 16.sp else 13.sp)
        }
    }
}

// ============================================================
// PANEL 10: CONSULTING CLINICIANS DIRECTORY
// ============================================================
@Composable
fun DoctorsScreen(
    vm: AlarmViewModel,
    textPrimary: Color,
    textSecondary: Color,
    surfaceColor: Color,
    accentGold: Color,
    cardBorder: BorderStroke,
    isSenior: Boolean
) {
    val doctors = listOf(
        Triple("الدكتور سامر (أخصائي القلب الشرياني)", "مستشفى السلام التخصصي، الدور الثالث، عيادة القلب", "0791234567"),
        Triple("الدكتور خالد (استشاري جراحة المفاصل والعظام)", "مركز العظام والمفاصل الدولي، الدور الخامس", "0792345678"),
        Triple("الدكتورة ليلى (أخصائي طب الأطفال وحديثي الولادة)", "عيادة الأطفال الأهلية الحديثة، الشارع العام", "0793456789")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { vm.goBack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = accentGold)
            }
            Text(
                text = "دليل الأطباء والمختصين وتواصلهم",
                color = textPrimary,
                fontSize = if (isSenior) 20.sp else 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(doctors) { doc ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = surfaceColor),
                    border = cardBorder,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(text = doc.first, color = accentGold, fontSize = if (isSenior) 16.sp else 13.sp, fontWeight = FontWeight.Bold)
                        Text(text = "العنوان: ${doc.second}", color = textSecondary, fontSize = if (isSenior) 12.sp else 10.sp, textAlign = TextAlign.Right)
                        Text(text = "رقم الهاتف للتواصل: ${doc.third}", color = textPrimary, fontSize = if (isSenior) 13.sp else 11.sp, textAlign = TextAlign.Right)

                        Spacer(modifier = Modifier.height(4.dp))
                        Button(
                            onClick = { vm.triggerToast("📞 جاري الاتصال بالطبيب المعالج: ${doc.first}") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2A6D)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Call, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("اتصال طارئ بالطبيب", color = Color.White, fontSize = if (isSenior) 12.sp else 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================
// PANEL 11: PRESCRIPTION PHOTO ARCHIVE
// ============================================================
@Composable
fun PrescriptionsScreen(
    vm: AlarmViewModel,
    textPrimary: Color,
    textSecondary: Color,
    surfaceColor: Color,
    accentGold: Color,
    cardBorder: BorderStroke,
    isSenior: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.End
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { vm.goBack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = accentGold)
            }
            Text(
                text = "أرشيف الوصفات الطبية المصورة",
                color = textPrimary,
                fontSize = if (isSenior) 20.sp else 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceColor),
            border = BorderStroke(2.dp, accentGold),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("📸 تصوير روشتة طبيب جديدة", color = accentGold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "يرجى التقاط صورة واضحة للوصفة الطبية لحفظها بملف المريض والوصول السريع ومطابقتها التلقائية بالأدوية.",
                    color = textSecondary,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { vm.triggerToast("📸 تم تفعيل محاكي كاميرا الهاتف لالتقاط الروشتة") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2A6D)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("التقاط صورة", color = Color.White)
                    }
                    Button(
                        onClick = { vm.triggerToast("🖼️ تم فتح معرض الصور لاختيار الوصفة") },
                        colors = ButtonDefaults.buttonColors(containerColor = surfaceColor),
                        border = cardBorder,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("اختر من المعرض", color = textPrimary)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text("الوصفات السابقة المحفوظة:", color = textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceColor),
            border = cardBorder,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text("وصفة الحاجة مريم - صمام القلب الشرياني", color = accentGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("تاريخ الحفظ: 10 يونيو 2026", color = textSecondary, fontSize = 11.sp)
                Text("الدواء المرتبط: أسبرين وقائي (75 مجم)", color = textPrimary, fontSize = 12.sp)
            }
        }
    }
}

// ============================================================
// PANEL 12: CLINICAL LABORATORY TESTS CHECKLIST
// ============================================================
@Composable
fun TestsScreen(
    vm: AlarmViewModel,
    textPrimary: Color,
    textSecondary: Color,
    surfaceColor: Color,
    accentGold: Color,
    cardBorder: BorderStroke,
    isSenior: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.End
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { vm.goBack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = accentGold)
            }
            Text(
                text = "الفحوصات والتحاليل الطبية اللازمة",
                color = textPrimary,
                fontSize = if (isSenior) 20.sp else 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        listOf(
            Triple("تخطيط القلب الكهربائي (ECG)", "تاريخ الالتحاق: 25 يونيو 2026", "🔴 قيد فحص المختبر"),
            Triple("تحليل الدم الشامل والفيتامينات (CBC + Vit D)", "تاريخ القراءة: 18 يونيو 2026", "🟢 سليمة ومطابقة للنسب الطبيعية"),
            Triple("تحليل السكر التراكمي (HbA1c) ومستويات الكرياتينين", "تاريخ الحفظ: 15 يونيو 2026", "🟢 حالة مستقرة (6.2%)")
        ).forEach { test ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                border = cardBorder,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(test.third, color = if (test.third.contains("🟢")) Color.Green else Color(0xFFFF2A6D), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(test.first, color = textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(test.second, color = textSecondary, fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = { vm.triggerToast("➕ تم فتح نموذج إضافة فحص مختبري جديد للمريض") },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2A6D)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("➕ إضافة فحص مطلوب جديد", color = Color.White, fontSize = if (isSenior) 16.sp else 13.sp)
        }
    }
}

// ============================================================
// PANEL 13: HELPLINE EMERGENCY SCREEN (SOS)
// ============================================================
@Composable
fun SosScreen(
    vm: AlarmViewModel,
    textPrimary: Color,
    textSecondary: Color,
    surfaceColor: Color,
    accentGold: Color,
    cardBorder: BorderStroke,
    isSenior: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { vm.goBack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = accentGold)
            }
            Text(
                text = "طلب الطوارئ والإسراع العاجل",
                color = textPrimary,
                fontSize = if (isSenior) 20.sp else 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFD32F2F).copy(0.12f)),
            border = BorderStroke(2.dp, Color(0xFFD32F2F)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("🆘 تفعيل طوارئ المريض الفورية", color = Color(0xFFD32F2F), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "عند نقر الزر أدناه، سيتم محاكاة اتصال فوري بالمشرف والممرض ومشاركة موقع الهاتف والسجل العلاجي الكامل لتجنب المضاعفات الخطيرة.",
                    color = textPrimary,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Right
                )
            }
        }

        val infiniteTransition = rememberInfiniteTransition(label = "pulsating_sos")
        val sosScale by infiniteTransition.animateFloat(
            initialValue = 0.9f,
            targetValue = 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "sos_scale"
        )

        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(Color(0xFFD32F2F).copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = CircleShape,
                color = Color(0xFFD32F2F),
                modifier = Modifier
                    .size((110 * sosScale).dp)
                    .clickable {
                        vm.triggerToast("🆘 🚨 تم إطلاق نداء الطوارئ ومحاكاة رسالة للمشرف بوضع المريض ومكانه!")
                    }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "SOS",
                        color = Color.White,
                        fontSize = if (isSenior) 30.sp else 24.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        Text(
            text = "أرسل سريعاً إشارات مزاج وتألم المريض بنقرة:",
            color = textPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("😊 ممتاز", "😐 مستقر", "😣 ألم").forEach { feel ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = surfaceColor,
                    border = cardBorder,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { vm.triggerToast("🆘 تم إرسال نبض تواصل فوري للمشرف: المريض يشعر بـ $feel") }
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(10.dp)) {
                        Text(feel, color = textPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("🤒 تعب", "🤢 غثيان", "😢 حزن").forEach { feel ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = surfaceColor,
                    border = cardBorder,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { vm.triggerToast("🆘 تم إرسال نبض تواصل فوري للمشرف: المريض يشعر بـ $feel") }
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(10.dp)) {
                        Text(feel, color = textPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ============================================================
// PANEL 14: PATIENT DIARY REPORT & INTUITIVE GRAPHS
// ============================================================
@Composable
fun ReportsScreen(
    vm: AlarmViewModel,
    textPrimary: Color,
    textSecondary: Color,
    surfaceColor: Color,
    accentGold: Color,
    cardBorder: BorderStroke,
    isSenior: Boolean
) {
    val reportTab by vm.selectedReportTab.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { vm.goBack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = accentGold)
            }
            Text(
                text = "تقارير الالتزام ومخزون المريض",
                color = textPrimary,
                fontSize = if (isSenior) 20.sp else 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            listOf("📈 الالتزام" to 0, "💊 المخزون" to 1, "🧾 الملخص" to 2).forEach { (label, index) ->
                val isSel = reportTab == index
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSel) accentGold else surfaceColor,
                    border = if (isSel) null else cardBorder,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { vm.setReportTab(index) }
                        .padding(horizontal = 4.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(10.dp)) {
                        Text(text = label, color = if (isSel) Color.Black else textPrimary, fontSize = if (isSenior) 12.sp else 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceColor),
            border = cardBorder,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
            ) {
                when (reportTab) {
                    0 -> {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier.verticalScroll(rememberScrollState())
                        ) {
                            Text("معدل الالتزام بتناول الجرعات الحالية: 86.5%", color = accentGold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("رسم بياني شريطي لجرعات الأسبوع المنصرم:", color = textPrimary, fontSize = 12.sp)

                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                            ) {
                                val barWidth = size.width / 14f
                                val bars = listOf(80f, 90f, 100f, 75f, 85f, 90f, 100f)
                                bars.forEachIndexed { i, adherence ->
                                    val leftX = i * (barWidth * 2f) + barWidth
                                    val valHeight = size.height * (adherence / 100f)
                                    drawRect(
                                        color = accentGold,
                                        topLeft = Offset(leftX, size.height - valHeight),
                                        size = androidx.compose.ui.geometry.Size(barWidth, valHeight)
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                listOf("أحد", "اثنين", "ثلاثاء", "أربعاء", "خميس", "جمعة", "سبت").forEach {
                                    Text(it, color = textSecondary, fontSize = 9.sp)
                                }
                            }
                        }
                    }
                    1 -> {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.verticalScroll(rememberScrollState())
                        ) {
                            Text("تنبيه مستويات المخزون المنخفضة:", color = accentGold, fontSize = 14.sp, fontWeight = FontWeight.Bold)

                            listOf(
                                "💊 أسبرين وقائي (75 مجم)" to "متبقي 4 حبات فقط (قارب على النفاد)",
                                "💊 دواء ميتفورمين للسكر" to "متبقي 12 حبة بالصندوق المعتمد"
                            ).forEach { (drug, alert) ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = surfaceColor.copy(alpha = 0.5f)),
                                    border = BorderStroke(1.dp, Color(0xFFFF2A6D).copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Button(
                                            onClick = { vm.triggerToast("🔄 تم إرسال رسالة آلية لتكرار تعبئة الدواء $drug") },
                                            colors = ButtonDefaults.buttonColors(containerColor = accentGold)
                                        ) {
                                            Text("طلب تعبئة", color = Color.Black, fontSize = 9.sp)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(drug, color = textPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Text(alert, color = Color(0xFFFF2A6D), fontSize = 9.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    else -> {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier.verticalScroll(rememberScrollState())
                        ) {
                            Text("ملخص النشاط العلاجي لليوم:", color = accentGold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("Intake Taken: 3 جرعات بنجاح (أسبرين، ميتفورمين صباح ومساء).", color = textPrimary, fontSize = 11.sp)
                            Text("Missed Logs: لم يتأخر المريض عن أي جرود علاج اليوم.", color = textPrimary, fontSize = 11.sp)
                            Text("العلامات المسجلة: الضغط مستقر 128 / 80 ومزاج جيد مريح.", color = textPrimary, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        Button(
            onClick = { vm.triggerToast("📤 تم تصدير التقرير الطبي لليلتزم بصوت PDF ومشاركته عبر واتساب المريض بنجاح!") },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2A6D)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("📤 تصدير الطبيب التقرير الحركي", color = Color.White, fontSize = if (isSenior) 16.sp else 13.sp)
        }
    }
}

// ============================================================
// PANEL 15: SETTINGS & THERAPEUTIC DIAGNOSTICS LOG
// ============================================================
@Composable
fun SettingsScreen(
    vm: AlarmViewModel,
    textPrimary: Color,
    textSecondary: Color,
    surfaceColor: Color,
    accentGold: Color,
    cardBorder: BorderStroke,
    isSenior: Boolean
) {
    val testingResults by vm.unitTestResults.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.End
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { vm.goBack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = accentGold)
            }
            Text(
                text = "إعدادات الأمان ومحاكي الفحص",
                color = textPrimary,
                fontSize = if (isSenior) 20.sp else 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceColor),
            border = cardBorder,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("تعديل تفضيلات التطبيق السريعة:", color = accentGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(checked = isSenior, onCheckedChange = { vm.toggleSeniorMode() })
                    Text("وضع كبار السن (تكبير القراءة والرموز)", color = textPrimary, fontSize = 12.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(checked = !isSenior, onCheckedChange = { vm.toggleLightTheme() })
                    Text("الوضع النهاري (Light Mode)", color = textPrimary, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceColor),
            border = BorderStroke(1.5.dp, Color(0xFFFF2A6D)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text("🧪 فحص واختبار تشغيل المنبه السريري", color = Color(0xFFFF2A6D), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("يمكنك تشغيل فحوصات تشخيصية معملية فورية للتأكد من خلو ملفات التطبيق البرمجية للـ 60 دواء من الأخطاء كلياً.", color = textSecondary, fontSize = 11.sp, textAlign = TextAlign.Right)

                Button(
                    onClick = { vm.runSettingsDiagnostics() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2A6D)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("تشغيل الفحوصات والتحقق", color = Color.White)
                }

                if (testingResults.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("نتائج التشخيص السريعة:", color = accentGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    testingResults.forEach { (test, success) ->
                        Text(
                            text = if (success) "✅ $test: PASS" else "❌ $test: FAILED",
                            color = if (success) Color.Green else Color.Red,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

// ============================================================
// COMPONENT: IMMERSIVE FULL-SCREEN REAL ringing ALARM
// ============================================================
@Composable
fun RingingAlarmFullScreen(alarm: AlarmEntity, viewModel: AlarmViewModel) {
    val isContrast by viewModel.isHighContrast.collectAsState()
    val isSenior by viewModel.isSeniorMode.collectAsState()

    val surfaceColor = if (isContrast) Color.Black else Color(0xFF101B2B)
    val textPrimary = Color.White
    val textSecondary = Color(0xFF9EACC0)
    val accentGold = if (isContrast) Color(0xFFFFD700) else Color(0xFFD4AF37)
    val cardBorder = BorderStroke(2.dp, accentGold)

    val mathQuestion by viewModel.mathQuestion.collectAsState()
    val challengeInput by viewModel.challengeInput.collectAsState()
    val shakeCount by viewModel.shakeCount.collectAsState()
    val targetShakeGoal = viewModel.targetShakeGoal

    val tapPatternTarget by viewModel.tapPatternTarget.collectAsState()
    val tapPatternUser by viewModel.tapPatternUser.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF060D1A))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = Color(0xFFFF2A6D),
                modifier = Modifier
                    .size(90.dp)
            )

            Text(
                text = "🚨 منبه حقيقي نشط الآن 🚨",
                color = Color(0xFFFF2A6D),
                fontSize = if (isSenior) 26.sp else 22.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )

            Text(
                text = alarm.label,
                color = textPrimary,
                fontSize = if (isSenior) 22.sp else 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "يحظر النوم! يجب حل التحدي التالي لضمان يقظتك الكاملة وتناول الدواء في وقته المحدد.",
                color = textSecondary,
                fontSize = if (isSenior) 14.sp else 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            when (alarm.challengeType) {
                "Math" -> {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = surfaceColor),
                        border = cardBorder,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "تحدي حل المسألة الرياضية:",
                                color = accentGold,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = mathQuestion,
                                color = textPrimary,
                                fontSize = if (isSenior) 36.sp else 30.sp,
                                fontWeight = FontWeight.Black
                            )

                            OutlinedTextField(
                                value = challengeInput,
                                onValueChange = { viewModel.challengeInput.value = it },
                                placeholder = { Text("أدخل الإجابة هنا", color = textSecondary) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = textPrimary, unfocusedTextColor = textPrimary, focusedBorderColor = accentGold, unfocusedBorderColor = accentGold.copy(0.4f))
                            )

                            Button(
                                onClick = {
                                    val solved = viewModel.submitMathChoice()
                                    if (!solved) {
                                        viewModel.triggerToast("❌ إجابة خاطئة! حاول مرة أخرى بتركيز.")
                                    } else {
                                        viewModel.triggerToast("✅ تم حل التحدي بنجاح وإيقاف المنبه!")
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2A6D)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("إرسال الإجابة وتأكيد اليقظة", color = Color.White)
                            }
                        }
                    }
                }
                "Shake" -> {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = surfaceColor),
                        border = cardBorder,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "تحدي اهتزاز الهاتف اليقظ الحالي:",
                                color = accentGold,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$shakeCount / $targetShakeGoal",
                                color = textPrimary,
                                fontSize = if (isSenior) 44.sp else 36.sp,
                                fontWeight = FontWeight.Black
                            )

                            Button(
                                onClick = { viewModel.incrementShake() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2A6D)),
                                modifier = Modifier.size(110.dp),
                                shape = CircleShape
                            ) {
                                Text("هز الهاتف", color = Color.White, textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
                "Pattern" -> {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = surfaceColor),
                        border = cardBorder,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "تحدي مطابقة النمط الذاكرتي الحالي:",
                                color = accentGold,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                tapPatternTarget.forEachIndexed { idx, it ->
                                    val inputCount = tapPatternUser.size
                                    val active = idx < inputCount
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(if (active) Color.Green else Color.Gray)
                                    )
                                }
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier.padding(vertical = 10.dp)
                            ) {
                                listOf(1, 2, 3, 4).forEach { btn ->
                                    Button(
                                        onClick = {
                                            val success = viewModel.inputPatternButton(btn)
                                            if (!success) {
                                                viewModel.triggerToast("❌ نمط خاطئ! تمت إعادة التصفير تلقائياً.")
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = accentGold),
                                        modifier = Modifier.size(54.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(text = btn.toString(), color = Color.Black, fontWeight = FontWeight.Black, fontSize = 16.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = { viewModel.dismissAlarmRing() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("تجاوز التحدي الفوري (خيار طبي)", color = Color.White)
            }
        }
    }
}
