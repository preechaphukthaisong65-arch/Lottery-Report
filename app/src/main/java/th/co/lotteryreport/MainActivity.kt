package th.co.lotteryreport

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import th.co.lotteryreport.data.LotteryRepository
import th.co.lotteryreport.data.local.DatabaseProvider
import th.co.lotteryreport.data.local.DrawResultEntity
import th.co.lotteryreport.data.local.DrawScheduleEntity

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
                    2 -> ResultsScreen(results)
                    else -> SettingsScreen()
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
                    Text("ตรวจสอบล่าสุด: \${item.checkedAtEpochMillis}")
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

@Composable
private fun ResultsScreen(items: List<DrawResultEntity>) {
    val context = LocalContext.current
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("ผลรางวัล", style = MaterialTheme.typography.titleLarge)
            if (items.isEmpty()) {
                Text("ยังไม่มีผลรางวัลที่ผ่านการตรวจสอบ")
            } else {
                items.groupBy { it.drawDate }.forEach { (date, values) ->
                    Text("งวดวันที่ $date", style = MaterialTheme.typography.titleMedium)
                    values.forEach { value ->
                        Text("\${value.category}: \${value.number}")
                    }
                }
            }
            Text("ข้อมูลที่บันทึกจะเก็บไว้ในเครื่องและแสดงได้เมื่อออฟไลน์")
            Button(onClick = {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.glo.or.th/mission/reward-payment/check-reward")))
            }) { Text("เปิดหน้าตรวจผลรางวัลทางการ") }
        }
    }
}

@Composable
private fun SettingsScreen() {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("ตั้งค่า", style = MaterialTheme.typography.titleLarge)
            Text("เขตเวลา: Asia/Bangkok")
            Text("ภาษา: ไทย")
            Text("การแจ้งเตือน: ยังไม่เปิดใช้งาน")
            Text("ซิงก์อัตโนมัติ: ยังไม่เปิดใช้งาน")
            Text("แหล่งข้อมูล: สำนักงานสลากกินแบ่งรัฐบาล (GLO) — รอการเชื่อมต่อ")
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
