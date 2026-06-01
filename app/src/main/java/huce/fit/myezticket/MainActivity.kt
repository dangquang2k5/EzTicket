package huce.fit.myezticket

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier

// Các thư viện Android
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController

// Các file trong dự án của bạn
import huce.fit.myezticket.ui.SetupNavGraph
import huce.fit.myezticket.ui.theme.MyEzTicketTheme
import huce.fit.myezticket.ui.viewmodel.EventViewModel // THIẾU DÒNG NÀY TRONG CODE CỦA BẠN

import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Bật tính năng tràn viền để app trông hiện đại hơn
        enableEdgeToEdge()

        setContent {
            MyEzTicketTheme {
                val navController = rememberNavController()

                // 2. Surface làm nền cho toàn bộ ứng dụng
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SetupNavGraph(
                        navController = navController
                    )
                }
            }
        }
    }
}