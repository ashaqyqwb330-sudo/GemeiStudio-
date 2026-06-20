package com.example.ui

import android.app.Application
import android.media.AudioManager
import android.media.ToneGenerator
import android.speech.tts.TextToSpeech
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AlarmEntity
import com.example.data.db.AlarmRepository
import com.example.data.db.AppDatabase
import com.example.data.db.VisitEntity
import com.example.data.db.VitalLogEntity
import com.example.data.db.MedicationItem
import com.example.data.db.MedicationDb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale
import kotlin.random.Random

class AlarmViewModel(application: Application) : AndroidViewModel(application), TextToSpeech.OnInitListener {

    private val db = AppDatabase.getDatabase(application)
    private val repository = AlarmRepository(db.alarmDao(), db.visitDao(), db.vitalLogDao())
    private var tts: TextToSpeech? = null

    // --- Database Streams ---
    val alarmList: StateFlow<List<AlarmEntity>> = repository.allAlarms
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val visitList: StateFlow<List<VisitEntity>> = repository.allVisits
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val vitalsList: StateFlow<List<VitalLogEntity>> = repository.allVitals
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- Navigation Flow ---
    private val _currentScreen = MutableStateFlow("pin")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()
    private val navHistory = mutableListOf<String>()

    fun navigateTo(screen: String) {
        if (_currentScreen.value != "pin" && screen != _currentScreen.value) {
            navHistory.add(_currentScreen.value)
        }
        _currentScreen.value = screen
    }

    fun goBack() {
        if (navHistory.isNotEmpty()) {
            val prev = navHistory.removeAt(navHistory.size - 1)
            _currentScreen.value = prev
        } else {
            _currentScreen.value = "clock"
        }
    }

    // --- Theme & Mode Flows ---
    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _isSeniorMode = MutableStateFlow(false)
    val isSeniorMode: StateFlow<Boolean> = _isSeniorMode.asStateFlow()

    private val _isHighContrast = MutableStateFlow(false)
    val isHighContrast: StateFlow<Boolean> = _isHighContrast.asStateFlow()

    fun toggleLightTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun toggleSeniorMode() {
        _isSeniorMode.value = !_isSeniorMode.value
    }

    fun toggleHighContrast() {
        _isHighContrast.value = !_isHighContrast.value
    }

    // --- Locked PIN Flow ---
    private val _pinCode = MutableStateFlow("")
    val pinCode: StateFlow<String> = _pinCode.asStateFlow()

    fun appendPin(char: Char): Boolean {
        if (_pinCode.value.length < 4) {
            _pinCode.value += char
            if (_pinCode.value == "1234") {
                _pinCode.value = ""
                navigateTo("clock")
                return true
            } else if (_pinCode.value.length == 4) {
                _pinCode.value = ""
                return false
            }
        }
        return true
    }

    fun clearPin() {
        _pinCode.value = ""
    }

    // --- Active Trigger Alarm State ---
    private val _ringingAlarm = MutableStateFlow<AlarmEntity?>(null)
    val ringingAlarm: StateFlow<AlarmEntity?> = _ringingAlarm.asStateFlow()

    // --- Current Real Time Stream ---
    private val _currentTimeString = MutableStateFlow("")
    val currentTimeString: StateFlow<String> = _currentTimeString.asStateFlow()

    private val _currentDateString = MutableStateFlow("")
    val currentDateString: StateFlow<String> = _currentDateString.asStateFlow()

    // --- Smart Challenge details ---
    private val _mathQuestion = MutableStateFlow("")
    val mathQuestion: StateFlow<String> = _mathQuestion.asStateFlow()
    private var mathAnswer = 0
    val challengeInput = MutableStateFlow("")
    
    private val _shakeCount = MutableStateFlow(0)
    val shakeCount: StateFlow<Int> = _shakeCount.asStateFlow()
    val targetShakeGoal = 15

    // Tap pattern memory challenge
    private val _tapPatternTarget = MutableStateFlow(listOf<Int>())
    val tapPatternTarget: StateFlow<List<Int>> = _tapPatternTarget.asStateFlow()
    private val _tapPatternUser = MutableStateFlow(listOf<Int>())
    val tapPatternUser: StateFlow<List<Int>> = _tapPatternUser.asStateFlow()

    private var toneGenerator: ToneGenerator? = null

    // --- App States (Medications, Calculator, Details etc.) ---
    private val _curCat = MutableStateFlow("all")
    val curCat: StateFlow<String> = _curCat.asStateFlow()

    private val _selectedDrug = MutableStateFlow<MedicationItem?>(null)
    val selectedDrug: StateFlow<MedicationItem?> = _selectedDrug.asStateFlow()

    private val _selectedCalcDrug = MutableStateFlow<MedicationItem?>(null)
    val selectedCalcDrug: StateFlow<MedicationItem?> = _selectedCalcDrug.asStateFlow()

    private val _dosageResult = MutableStateFlow("اختر دواءً لحساب الجرعة")
    val dosageResult: StateFlow<String> = _dosageResult.asStateFlow()

    private val _selectedVisitId = MutableStateFlow<Int?>(null)
    val selectedVisitId: StateFlow<Int?> = _selectedVisitId.asStateFlow()

    private val _selectedVisitTab = MutableStateFlow(0)
    val selectedVisitTab: StateFlow<Int> = _selectedVisitTab.asStateFlow()

    private val _selectedReportTab = MutableStateFlow(0)
    val selectedReportTab: StateFlow<Int> = _selectedReportTab.asStateFlow()

    private val _unitTestResults = MutableStateFlow<List<Pair<String, Boolean>>>(emptyList())
    val unitTestResults: StateFlow<List<Pair<String, Boolean>>> = _unitTestResults.asStateFlow()

    // Sound alert channels simulation
    private val _toastMessage = MutableStateFlow("")
    val toastMessage: StateFlow<String> = _toastMessage.asStateFlow()

    fun triggerToast(msg: String) {
        _toastMessage.value = msg
        viewModelScope.launch {
            delay(3000)
            if (_toastMessage.value == msg) {
                _toastMessage.value = ""
            }
        }
    }

    init {
        // Initialize TTS
        try {
            tts = TextToSpeech(application, this)
        } catch (e: Exception) { e.printStackTrace() }

        // Seed initial alarms, visits, and physical vitals if empty
        viewModelScope.launch(Dispatchers.IO) {
            if (repository.getCount() == 0) {
                repository.insert(
                    AlarmEntity(
                        hour = 5,
                        minute = 10,
                        label = "صلاة الفجر - Rise for Fajr",
                        repeatDays = "Daily",
                        challengeType = "Math",
                        difficulty = "Medium",
                        isEnabled = true
                    )
                )
                repository.insert(
                    AlarmEntity(
                        hour = 7,
                        minute = 45,
                        label = "العمل - Morning Focus",
                        repeatDays = "Once",
                        challengeType = "Shake",
                        difficulty = "Easy",
                        isEnabled = true
                    )
                )
                repository.insert(
                    AlarmEntity(
                        hour = 22,
                        minute = 30,
                        label = "أذكار النوم - Peace & Reflect",
                        repeatDays = "Daily",
                        challengeType = "Pattern",
                        difficulty = "Hard",
                        isEnabled = true
                    )
                )
            }

            if (repository.getVisitCount() == 0) {
                repository.insertVisit(
                    VisitEntity(
                        patientName = "الحاجة مريم",
                        doctorName = "د. سامر",
                        specialty = "قلب",
                        clinicName = "مستشفى السلام",
                        date = "2026-06-21",
                        time = "10:00 ص",
                        reason = "مراجعة دورية",
                        status = "upcoming"
                    )
                )
                repository.insertVisit(
                    VisitEntity(
                        patientName = "الحاج أحمد",
                        doctorName = "د. خالد",
                        specialty = "عظام",
                        clinicName = "مركز المفاصل الدولي",
                        date = "2026-06-22",
                        time = "02:00 م",
                        reason = "فحص دقة العظام",
                        status = "upcoming"
                    )
                )
                repository.insertVisit(
                    VisitEntity(
                        patientName = "الحاجة مريم",
                        doctorName = "د. سامر",
                        specialty = "قلب",
                        clinicName = "مستشفى السلام",
                        date = "2026-06-10",
                        time = "09:30 ص",
                        reason = "قسطرة تشخيصية",
                        status = "completed"
                    )
                )
            }

            if (repository.getVitalsCount() == 0) {
                repository.insertVitalLog(
                    VitalLogEntity(
                        systolic = 135,
                        diastolic = 85,
                        bloodSugar = 120,
                        isPostPrandial = false,
                        pulseRate = 72,
                        mood = "😊"
                    )
                )
                repository.insertVitalLog(
                    VitalLogEntity(
                        systolic = 128,
                        diastolic = 80,
                        bloodSugar = 160,
                        isPostPrandial = true,
                        pulseRate = 74,
                        mood = "😐"
                    )
                )
            }
        }

        // Ticker for current details and checks
        viewModelScope.launch(Dispatchers.Default) {
            while (true) {
                val cal = Calendar.getInstance()
                val hr = cal.get(Calendar.HOUR_OF_DAY)
                val min = cal.get(Calendar.MINUTE)
                val sec = cal.get(Calendar.SECOND)

                val amPm = if (cal.get(Calendar.AM) == Calendar.AM) "ص" else "م"
                val hrFormatted = String.format("%02d", if (cal.get(Calendar.HOUR) == 0) 12 else cal.get(Calendar.HOUR))
                val minFormatted = String.format("%02d", min)
                val secFormatted = String.format("%02d", sec)

                _currentTimeString.value = "$hrFormatted:$minFormatted:$secFormatted $amPm"

                val dayOfWeek = when (cal.get(Calendar.DAY_OF_WEEK)) {
                    Calendar.SUNDAY -> "الأحد - Sunday"
                    Calendar.MONDAY -> "الاثنين - Monday"
                    Calendar.TUESDAY -> "الثلاثاء - Tuesday"
                    Calendar.WEDNESDAY -> "الأربعاء - Wednesday"
                    Calendar.THURSDAY -> "الخميس - Thursday"
                    Calendar.FRIDAY -> "الجمعة - Friday"
                    else -> "السبت - Saturday"
                }
                val dateStr = "${cal.get(Calendar.DAY_OF_MONTH)}/${cal.get(Calendar.MONTH) + 1}/${cal.get(Calendar.YEAR)} | $dayOfWeek"
                _currentDateString.value = dateStr

                // Auto Trigger alarms if matching
                if (_ringingAlarm.value == null && sec == 0) {
                    val activeAlarms = alarmList.value
                    val matching = activeAlarms.firstOrNull { it.isEnabled && it.hour == hr && it.minute == min }
                    if (matching != null) {
                        launch(Dispatchers.Main) {
                            triggerAlarmRing(matching)
                        }
                    }
                }

                delay(1000)
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale("ar", "SA")
        }
    }

    // --- Actions ---
    fun addNewAlarm(hour: Int, minute: Int, label: String, repeatDays: String, challenge: String, difficulty: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(
                AlarmEntity(
                    hour = hour,
                    minute = minute,
                    label = label,
                    repeatDays = repeatDays,
                    challengeType = challenge,
                    difficulty = difficulty,
                    isEnabled = true
                )
            )
        }
    }

    fun toggleAlarmStatus(alarm: AlarmEntity, isEnabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateEnabled(alarm.id, isEnabled)
        }
    }

    fun deleteAlarm(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(id)
        }
    }

    fun deleteVisit(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteVisit(id)
        }
    }

    fun addNewVisit(patient: String, doctor: String, specialty: String, clinic: String, date: String, time: String, reason: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertVisit(
                VisitEntity(
                    patientName = patient,
                    doctorName = doctor,
                    specialty = specialty,
                    clinicName = clinic,
                    date = date,
                    time = time,
                    reason = reason,
                    status = "upcoming"
                )
            )
        }
    }

    fun updateVisitStatus(id: Int, status: String, reason: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateVisitStatus(id, status, reason)
        }
    }

    fun addNewVitalLog(systolic: Int, diastolic: Int, sugar: Int, isPost: Boolean, pulse: Int, mood: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertVitalLog(
                VitalLogEntity(
                    systolic = systolic,
                    diastolic = diastolic,
                    bloodSugar = sugar,
                    isPostPrandial = isPost,
                    pulseRate = pulse,
                    mood = mood
                )
            )
        }
    }

    // Simulation trigger
    fun testMockAlarmRing(alarm: AlarmEntity) {
        triggerAlarmRing(alarm)
    }

    private fun triggerAlarmRing(alarm: AlarmEntity) {
        _ringingAlarm.value = alarm
        challengeInput.value = ""
        _shakeCount.value = 0
        _tapPatternUser.value = emptyList()

        when (alarm.challengeType) {
            "Math" -> generateMathQuestion(alarm.difficulty)
            "Shake" -> { /* No complex payload */ }
            "Pattern" -> generatePatternTarget(alarm.difficulty)
        }

        // Alarm beep sounds
        viewModelScope.launch(Dispatchers.Default) {
            try {
                toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 85)
                while (_ringingAlarm.value != null) {
                    toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 500)
                    delay(1200)
                }
            } catch (e: Exception) { }
        }
    }

    // --- Challenge Solvers ---
    private fun generateMathQuestion(diff: String) {
        val r = Random(System.currentTimeMillis())
        when (diff) {
            "Easy" -> {
                val a = r.nextInt(5, 15)
                val b = r.nextInt(3, 12)
                _mathQuestion.value = "$a + $b = ؟"
                mathAnswer = a + b
            }
            "Medium" -> {
                val a = r.nextInt(12, 35)
                val b = r.nextInt(8, 25)
                val action = if (r.nextBoolean()) "+" else "-"
                _mathQuestion.value = "$a $action $b = ؟"
                mathAnswer = if (action == "+") a + b else a - b
            }
            else -> {
                val a = r.nextInt(6, 12)
                val b = r.nextInt(5, 9)
                val c = r.nextInt(10, 30)
                _mathQuestion.value = "$a × $b + $c = ؟"
                mathAnswer = (a * b) + c
            }
        }
    }

    fun submitMathChoice(): Boolean {
        val userVal = challengeInput.value.toIntOrNull()
        if (userVal == mathAnswer) {
            dismissAlarmRing()
            return true
        }
        challengeInput.value = ""
        return false
    }

    private fun generatePatternTarget(diff: String) {
        val length = when (diff) {
            "Easy" -> 3
            "Medium" -> 4
            else -> 6
        }
        _tapPatternTarget.value = List(length) { Random.nextInt(1, 5) }
    }

    fun inputPatternButton(num: Int): Boolean {
        val updatedList = _tapPatternUser.value + num
        _tapPatternUser.value = updatedList

        val target = _tapPatternTarget.value
        for (i in updatedList.indices) {
            if (updatedList[i] != target[i]) {
                _tapPatternUser.value = emptyList()
                return false
            }
        }

        if (updatedList.size == target.size) {
            dismissAlarmRing()
            return true
        }
        return true
    }

    fun incrementShake() {
        val updated = _shakeCount.value + 1
        _shakeCount.value = updated
        if (updated >= targetShakeGoal) {
            dismissAlarmRing()
        }
    }

    fun dismissAlarmRing() {
        _ringingAlarm.value = null
        try {
            toneGenerator?.release()
            toneGenerator = null
        } catch (e: Exception) { }
    }

    // --- UI State Helpers ---
    fun setCurCat(cat: String) {
        _curCat.value = cat
    }

    fun selectDrug(drug: MedicationItem?) {
        _selectedDrug.value = drug
    }

    fun selectCalcDrug(drug: MedicationItem?) {
        _selectedCalcDrug.value = drug
        calculateDosage()
    }

    fun calculateDosage(customWeight: Double = 70.0, customAge: Double = 65.0) {
        val drug = _selectedCalcDrug.value ?: return
        val calculated = if (drug.isPerKg) {
            val total = drug.perKgDose * customWeight
            "${String.format("%.1f", total)} ${drug.dosageUnit} (محسوبة بناءً على الوزن ${customWeight} كجم)"
        } else {
            if (customAge < 15) {
                val childProp = (drug.dosageMin * customWeight) / 70.0
                val childMaxProp = (drug.dosageMax * customWeight) / 70.0
                "${String.format("%.1f", childProp)} - ${String.format("%.1f", childMaxProp)} ${drug.dosageUnit} (جرعة معدلة للأطفال)"
            } else {
                "${drug.dosageMin} - ${drug.dosageMax} ${drug.dosageUnit} (جرعة البالغين القياسية)"
            }
        }
        _dosageResult.value = "الجرعة المقترحة: $calculated\nطريقة تناول الدواء: ${drug.route}\nتحذيرات: ${drug.warnings}"
    }

    fun selectVisit(id: Int?) {
        _selectedVisitId.value = id
    }

    fun setVisitTab(tab: Int) {
        _selectedVisitTab.value = tab
    }

    fun setReportTab(tab: Int) {
        _selectedReportTab.value = tab
    }

    // --- Voice Command Execution (Simulated Voice Engine & TTS) ---
    fun executeVoiceCommand(cmd: String) {
        val replyText = when (cmd) {
            "موعد" -> "موعدكَ القَادم هو غداً السَاعة العاشرة صباحاً مع الدكتور سامر في مستشفى السلام لعلاج القلب."
            "طبيب" -> "آخر تَوْصِيات الدكتور سامر: استمرْ على جُرعات الكورتيزون، وتَجنَّب الإِجهاد البدني المفرَط."
            "أعمل" -> "الإِجرَاء المَطلوب هو الاستِرخاء التام وقِياس نَبضات القَلب ومُستويات سكر الخِيام يومياً."
            "تقرير" -> "تمَّ نقل تقرِير ضَغط الدَّم الأخير إِلى الطبيب بنجاح عبر البوابة الإِلكترونية."
            "دواء" -> "قَاعدة البيانات تحتوي على ستّين دَواءً نَشطاً، تصفَّح قِسم الأدوية لِلتفاصِيل."
            "جرعة" -> "تمَّ حساب جُرعة دَواء مَيتفُورمين المُناسبة لَكَ بناءً على البَيانات المُنقَّحة."
            "سكر" -> "تمَّ تسجيل قِراءة السكر الحالية، مائة وعشرون مِلِّيجرَام، الحَالة مُمتازة ومُطمئِنة."
            else -> "تمَّ تشغِيل المُساعد الصَوْتي، نَحْنُ في خدمتِك دائماً."
        }
        triggerToast("🎙️ $replyText")
        tts?.speak(replyText, TextToSpeech.QUEUE_FLUSH, null, "voice_cmd")
    }

    // --- Running Diagnostics / UI Unit Tests ---
    fun runSettingsDiagnostics() {
        viewModelScope.launch(Dispatchers.Default) {
            val list = mutableListOf<Pair<String, Boolean>>()
            list.add("التحقق من صحة قاعدة الأدوية الـ 60" to (MedicationDb.list.size == 60))
            list.add("حساب جرعة نابروكسين بالوزن لـ 70 كجم" to (MedicationDb.list.firstOrNull { it.id == 6 }?.perKgDose?.times(70.0) == 700.0))
            list.add("اختبار تطابق رمز PIN لحماية المرضى" to (appendPin('1') && appendPin('2') && appendPin('3') && appendPin('4')))
            _unitTestResults.value = list
            triggerToast("🧪 تم الانتهاء من تشغيل الفحوصات بنجاح!")
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            toneGenerator?.release()
        } catch (e: Exception) { }
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) { }
    }
}
