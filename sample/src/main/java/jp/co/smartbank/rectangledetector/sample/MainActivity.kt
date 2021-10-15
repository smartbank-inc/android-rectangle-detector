package jp.co.smartbank.rectangledetector.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import jp.co.smartbank.rectangledetector.sample.ui.MainContent
import jp.co.smartbank.rectangledetector.sample.ui.theme.RectangleDetectorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RectangleDetectorTheme {
                MainContent()
            }
        }
    }
}
