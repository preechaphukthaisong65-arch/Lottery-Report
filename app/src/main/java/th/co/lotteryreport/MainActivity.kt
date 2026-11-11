package th.co.lotteryreport

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { LotteryReportApp() } }
    }
}

private val tabs = listOf("ภาพรวม", "ปฏิทิน", "ผลรางวัล", "ตั้งค่า")

@Composable
private fun LotteryReportApp() {
    var selectedTab by remember { mutableIntStateOf(0) }
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
                    0 -> OverviewScreen()
                    1 -> PlaceholderScreen("ปฏิทินวันออกรางวัล", "ยังไม่ได้เชื่อมต่อแหล่งข้อมูลกำหนดการ")
                    2 -> PlaceholderScreen("ผลรางวัล", "ยังไม่มีข้อมูลผลรางวัลที่ตรวจสอบจากแหล่งทางการ")
                    else -> PlaceholderScreen("ตั้งค่า", "การแจ้งเตือนและการซิงก์จะเปิดใช้งานในขั้นตอนถัดไป")
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "แอปนี้ยังไม่เชื่อมต่อ GLO และไม่มีข้อมูลรางวัลจริงในรุ่นนี้",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
                Text("แอปนี้ไม่ใช่แอปอย่างเป็นทางการของสำนักงานสลากกินแบ่งรัฐบาล")
            }
        }
    }
}

@Composable
private fun OverviewScreen() {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("สถานะข้อมูล", style = MaterialTheme.typography.titleLarge)
            Text("ยังไม่ได้ซิงก์ข้อมูล")
            Text("แหล่งข้อมูล: ยังไม่เชื่อมต่อ")
            Text("เขตเวลา: Asia/Bangkok")
            Button(onClick = { /* Sync will be implemented with the official source adapter. */ }) {
                Text("ตรวจสอบข้อมูล (ยังไม่พร้อมใช้งาน)")
            }
        }
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SummaryCard("กำหนดการ", "ยังไม่มีข้อมูล")
        SummaryCard("ผลรางวัล", "ยังไม่มีข้อมูล")
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

@Composable
private fun PlaceholderScreen(title: String, message: String) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text(message)
        }
    }
}
