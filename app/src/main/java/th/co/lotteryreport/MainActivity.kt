package th.co.lotteryreport

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import th.co.lotteryreport.data.LotteryRepository
import th.co.lotteryreport.data.local.DatabaseProvider
import th.co.lotteryreport.data.local.DrawResultEntity
import th.co.lotteryreport.data.local.DrawScheduleEntity
import th.co.lotteryreport.domain.DrawResultValidator
import th.co.lotteryreport.domain.PrizeValue
import java.time.Instant

class MainActivity : ComponentActivity() {
    private val repository: LotteryRepository by lazy {
        LotteryRepository(DatabaseProvider.get(applicationContext))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            runCatching { repository.seedEstimatedSchedule() }
        }
        setContent { MaterialTheme { LotteryReportApp(repository) } }
    }
}

private val tabs = listOf("ภาพรวม", "ปฏิทิน", "ผลรางวัล", "ตั้งค่า")

@Composable
private fun LotteryReportApp(repository: LotteryRepository) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val resultFlow = remember(repository) { repository.observeAllResults() }
    val scheduleFlow = remember(repository) { repository.observeSchedules() }
    val results by resultFlow.collectAsState(initial = emptyList())
    val schedules by scheduleFlow.collectAsState(initial = emptyList())

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = {},
                        label = { Text(title) }
                    )
                }
            }
        }
    ) { padding ->
        Surface(
            modifier = Modifier.fillMaxSize().padding(padding),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Lottery Report", style = MaterialTheme.typography.headlineMedium)
                Text("ติดตามกำหนดวันออกรางวัลและผลสลากกินแบ่งรัฐบาล")
                when (selectedTab) {
                    0 -> OverviewScreen(results.size, schedules.size)
                    1 -> ScheduleScreen(schedules)
                    2 -> ResultsScreen(results, repository)
                    else -> SettingsScreen(repository)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "ข้อมูลจะแสดงเป็นข้อมูลยืนยันเมื่อผ่านการตรวจสอบจากแหล่งทางการเท่านั้น",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
                Text("แอปนี้ไม่ใช่แอปอย่างเป็นทางการของสำนักงานสลากกินแบ่งรัฐบาล")
            }
        }
    }
}

@Composable
private fun OverviewScreen(resultCount: Int, scheduleCount: Int) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("สถานะข้อมูล", style = MaterialTheme.typography.titleLarge)
            Text("ระบบฐานข้อมูลภายในเครื่องพร้อมใช้งาน")
            Text("กำหนดการที่บันทึก: $scheduleCount")
            Text("รายการผลรางวัลที่บันทึก: $resultCount")
            Text("เขตเวลา: Asia/Bangkok")
            Text("การเชื่อมต่อ GLO: ยังไม่เปิดใช้งาน")
        }
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SummaryCard("กำหนดการ", scheduleCount.toString())
        SummaryCard("ผลรางวัล", resultCount.toString())
    }
}

@Composable
private fun ScheduleScreen(items: List<DrawScheduleEntity>) {
    val context = LocalContext.current
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("ปฏิทินวันออกรางวัล", style = MaterialTheme.typography.titleLarge)
            if (items.isEmpty()) {
                Text("ยังไม่มีวันออกรางวัลที่บันทึก")
                Text("จะไม่สร้างวันยืนยันจากการคาดเดา")
            } else {
                items.forEach { item ->
                    Text(item.drawDate)
                    Text(if (item.isOfficiallyConfirmed) "ยืนยันจากแหล่งทางการ" else "วันประมาณการ")
                    Text("ตรวจสอบล่าสุด: ${item.checkedAtEpochMillis}")
                }
            }
            Text("วันปกติ 1 และ 16 เป็นเพียงวันประมาณการ จนกว่า GLO จะประกาศยืนยัน")
            Text("วันพิเศษหรือวันเลื่อนต้องรอประกาศจาก GLO")
            Button(onClick = {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.glo.or.th/service/faq/award")))
            }) { Text("เปิดข้อมูลกำหนดวันจาก GLO") }
        }
    }
}

private val prizeInputDefinitions = listOf(
    "FIRST_PRIZE" to "รางวัลที่ 1 — 6 หลัก จำนวน 1 เลข",
    "FRONT_THREE" to "เลขหน้า 3 ตัว — จำนวน 2 เลข",
    "LAST_THREE" to "เลขท้าย 3 ตัว — จำนวน 2 เลข",
    "LAST_TWO" to "เลขท้าย 2 ตัว — จำนวน 1 เลข",
    "SECOND_PRIZE" to "รางวัลที่ 2 — จำนวน 5 เลข",
    "THIRD_PRIZE" to "รางวัลที่ 3 — จำนวน 10 เลข",
    "FOURTH_PRIZE" to "รางวัลที่ 4 — จำนวน 50 เลข",
    "FIFTH_PRIZE" to "รางวัลที่ 5 — จำนวน 100 เลข",
    "NEARBY_FIRST_PRIZE" to "เลขข้างเคียงรางวัลที่ 1 — จำนวน 2 เลข"
)

@Composable
private fun ResultsScreen(items: List<DrawResultEntity>, repository: LotteryRepository) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showEntry by remember { mutableStateOf(false) }
    var drawDate by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    val numberInputs = remember { mutableStateListOf<String>().apply { repeat(9) { add("") } } }

    if (showEntry) {
        AlertDialog(
            onDismissRequest = { showEntry = false },
            title = { Text("บันทึกผลรางวัลที่ตรวจสอบแล้ว") },
            text = {
                Column(
                    modifier = Modifier.heightIn(max = 520.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("เปิดผลรางวัลจากเว็บไซต์ GLO และตรวจเลขทุกหมวดก่อนกรอก วันที่ใช้รูปแบบ YYYY-MM-DD")
                    OutlinedTextField(
                        value = drawDate,
                        onValueChange = { drawDate = it.trim() },
                        label = { Text("วันที่งวด เช่น 2026-11-16") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    prizeInputDefinitions.forEachIndexed { index, definition ->
                        OutlinedTextField(
                            value = numberInputs[index],
                            onValueChange = { numberInputs[index] = it },
                            label = { Text(definition.second) },
                            placeholder = { Text("คั่นแต่ละเลขด้วยเครื่องหมายจุลภาค") },
                            minLines = 1,
                            maxLines = 3,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (errorMessage.isNotBlank()) {
                        Text(errorMessage, color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val values = prizeInputDefinitions.flatMapIndexed { index, definition ->
                        numberInputs[index]
                            .split(Regex("[,\\s]+"))
                            .filter { it.isNotBlank() }
                            .map { PrizeValue(definition.first, it) }
                    }
                    if (!DrawResultValidator.validate(drawDate, values)) {
                        errorMessage = "ข้อมูลไม่ครบหรือรูปแบบไม่ถูกต้อง ตรวจจำนวนเลขและจำนวนหลักของทุกหมวด"
                    } else {
                        errorMessage = ""
                        scope.launch {
                            val saved = repository.saveVerifiedResults(
                                drawDate = drawDate,
                                values = values,
                                sourceUrl = "https://www.glo.or.th/mission/reward-payment/check-reward",
                                verifiedAt = Instant.now()
                            )
                            if (saved) {
                                showEntry = false
                                numberInputs.indices.forEach { numberInputs[it] = "" }
                                drawDate = ""
                            } else {
                                errorMessage = "บันทึกไม่ได้: งวดนี้อาจมีข้อมูลอยู่แล้ว หรือข้อมูลไม่ผ่านการตรวจสอบ"
                            }
                        }
                    }
                }) { Text("ตรวจสอบและบันทึก") }
            },
            dismissButton = {
                TextButton(onClick = { showEntry = false }) { Text("ยกเลิก") }
            }
        )
    }

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("ผลรางวัล", style = MaterialTheme.typography.titleLarge)
            if (items.isEmpty()) {
                Text("ยังไม่มีผลรางวัลที่บันทึก")
            } else {
                items.groupBy { it.drawDate }.forEach { (date, values) ->
                    Text("งวดวันที่ $date", style = MaterialTheme.typography.titleMedium)
                    values.forEach { value ->
                        Text("${value.category}: ${value.number}")
                    }
                }
            }
            Text("ข้อมูลที่บันทึกจะเก็บไว้ในเครื่องและแสดงได้เมื่อออฟไลน์")
            Button(onClick = { showEntry = true }) { Text("กรอกผลรางวัลที่ตรวจสอบกับ GLO แล้ว") }
            Button(onClick = {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.glo.or.th/mission/reward-payment/check-reward")))
            }) { Text("เปิดหน้าตรวจผลรางวัลทางการ") }
        }
    }
}

@Composable
private fun SettingsScreen(repository: LotteryRepository) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var message by remember { mutableStateOf("ข้อมูลสำรองเก็บเฉพาะในไฟล์ที่คุณเลือกบันทึก") }
    var pendingRestoreUri by remember { mutableStateOf<Uri?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val result = runCatching {
                    val json = repository.exportBackupJson()
                    withContext(Dispatchers.IO) {
                        val output = context.contentResolver.openOutputStream(uri)
                            ?: error("เปิดไฟล์ปลายทางไม่ได้")
                        output.use { it.write(json.toByteArray(Charsets.UTF_8)) }
                    }
                }
                message = if (result.isSuccess) "สำรองข้อมูลสำเร็จ" else "สำรองข้อมูลไม่สำเร็จ: ${result.exceptionOrNull()?.message ?: "เกิดข้อผิดพลาด"}"
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        pendingRestoreUri = uri
    }

    if (pendingRestoreUri != null) {
        AlertDialog(
            onDismissRequest = { pendingRestoreUri = null },
            title = { Text("ยืนยันการกู้คืนข้อมูล") },
            text = { Text("การกู้คืนจะแทนที่ข้อมูลปัจจุบันทั้งหมดด้วยข้อมูลในไฟล์สำรอง ควรส่งออกข้อมูลปัจจุบันเก็บไว้ก่อน") },
            confirmButton = {
                TextButton(onClick = {
                    val uri = pendingRestoreUri
                    pendingRestoreUri = null
                    if (uri != null) scope.launch {
                        val result = runCatching {
                            val json = withContext(Dispatchers.IO) {
                                val input = context.contentResolver.openInputStream(uri)
                                    ?: error("เปิดไฟล์สำรองไม่ได้")
                                input.use { stream ->
                                    val output = java.io.ByteArrayOutputStream()
                                    val buffer = ByteArray(8_192)
                                    var total = 0
                                    while (true) {
                                        val count = stream.read(buffer)
                                        if (count < 0) break
                                        total += count
                                        require(total <= 5_000_000) { "ไฟล์สำรองมีขนาดเกินกำหนด" }
                                        output.write(buffer, 0, count)
                                    }
                                    output.toString("UTF-8")
                                }
                            }
                            check(repository.restoreBackupJson(json)) { "ไฟล์ไม่ถูกต้องหรือข้อมูลไม่ครบถ้วน" }
                        }
                        message = if (result.isSuccess) "กู้คืนข้อมูลสำเร็จ" else "กู้คืนไม่สำเร็จ: ${result.exceptionOrNull()?.message ?: "ไฟล์ไม่ถูกต้อง"}"
                    }
                }) { Text("กู้คืนข้อมูล") }
            },
            dismissButton = {
                TextButton(onClick = { pendingRestoreUri = null }) { Text("ยกเลิก") }
            }
        )
    }

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("ตั้งค่า", style = MaterialTheme.typography.titleLarge)
            Text("เขตเวลา: Asia/Bangkok")
            Text("ภาษา: ไทย")
            Text("การแจ้งเตือน: ยังไม่เปิดใช้งาน")
            Text("ซิงก์อัตโนมัติ: ยังไม่เปิดใช้งาน")
            Text("แหล่งข้อมูล: สำนักงานสลากกินแบ่งรัฐบาล (GLO) — รอการเชื่อมต่อ")
            Spacer(Modifier.height(8.dp))
            Text("สำรองและกู้คืนข้อมูล", style = MaterialTheme.typography.titleMedium)
            Text("ไฟล์สำรองมีเฉพาะกำหนดการและผลรางวัลที่บันทึกในเครื่อง")
            Button(onClick = { exportLauncher.launch("lottery-report-backup.json") }) {
                Text("ส่งออกไฟล์สำรอง")
            }
            Button(onClick = { importLauncher.launch(arrayOf("application/json", "text/json")) }) {
                Text("เลือกไฟล์เพื่อกู้คืน")
            }
            Text(message, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun SummaryCard(title: String, value: String) {
    Card(Modifier.fillMaxWidth(0.5f)) {
        Column(Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(value, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
