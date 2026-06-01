package huce.fit.myezticket.ui.components

import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun HtmlText(html: String, modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier.fillMaxWidth().alpha(0.99f),
        factory = { context ->
            WebView(context).apply {
                // Đảm bảo chiều cao bao bọc nội dung
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                settings.apply {
                    javaScriptEnabled = false // Vô hiệu hóa JavaScript giúp nạp WebView nhanh và nhẹ hơn nhiều lần
                    loadsImagesAutomatically = true
                    useWideViewPort = false
                    loadWithOverviewMode = false
                    defaultTextEncodingName = "UTF-8"
                }
                // Ẩn thanh cuộn để Compose xử lý cuộn
                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        // Bắt WebView tính toán lại chiều cao khi ảnh đã tải xong
                        view?.requestLayout()
                    }
                }
            }
        },
        update = { webView ->
            // CSS giúp ảnh co giãn vừa màn hình, chữ dễ đọc, và hỗ trợ HTML tùy chỉnh
            val styledHtml = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
                    <style>
                        body { 
                            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif; 
                            font-size: 15px; 
                            color: #424242; 
                            line-height: 1.6;
                            margin: 0;
                            padding: 0;
                            word-wrap: break-word;
                        }
                        img { 
                            max-width: 100%; 
                            height: auto !important; 
                            border-radius: 8px;
                            margin-top: 8px;
                            margin-bottom: 8px;
                            display: block;
                        }
                        a { color: #1976D2; text-decoration: none; }
                        h1, h2, h3 { color: #212121; margin-top: 16px; margin-bottom: 8px; }
                        p { margin-top: 0; margin-bottom: 12px; }
                    </style>
                </head>
                <body>
                    ${html.ifEmpty { "Đang cập nhật..." }}
                </body>
                </html>
            """.trimIndent()
            
            // Tối ưu hóa: Chỉ gọi loadData khi nội dung HTML thực sự thay đổi
            // Tránh việc reload lại webview liên tục khi màn hình Compose bị recompose
            if (webView.tag != styledHtml) {
                webView.tag = styledHtml
                webView.loadDataWithBaseURL(null, styledHtml, "text/html", "UTF-8", null)
            }
        }
    )
}
