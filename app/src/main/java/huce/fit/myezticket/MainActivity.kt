package huce.fit.myezticket

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier

// Các thư viện Android
import androidx.navigation.compose.rememberNavController

// Các file trong dự án của bạn
import huce.fit.myezticket.ui.SetupNavGraph
import huce.fit.myezticket.ui.theme.MyEzTicketTheme

import dagger.hilt.android.AndroidEntryPoint
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.widget.Toast

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // Bật tính năng tràn viền để app trông hiện đại hơn
        enableEdgeToEdge()

        connectivityManager = getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        // Kiểm tra kết nối mạng ban đầu
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        val hasInternet = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        if (!hasInternet) {
            Toast.makeText(this, "Không có kết nối mạng. Đang hoạt động ngoại tuyến.", Toast.LENGTH_LONG).show()
        }

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            private var wasOffline = !hasInternet

            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                if (wasOffline) {
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Đã kết nối Internet", Toast.LENGTH_SHORT).show()
                    }
                    wasOffline = false
                }
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                runOnUiThread {
                    Toast.makeText(applicationContext, "Mất kết nối Internet. Bạn đang sử dụng chế độ ngoại tuyến.", Toast.LENGTH_LONG).show()
                }
                wasOffline = true
            }
        }

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)

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

        // Tối ưu hóa: Khởi tạo trước WebView ở chế độ chạy ngầm sau khi màn hình chính vẽ xong
        // Việc này giúp tránh bị khựng/delay ở màn hình chi tiết sự kiện do nạp thư viện Chromium
        window.decorView.post {
            try {
                android.webkit.WebView(applicationContext)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}