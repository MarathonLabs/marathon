package com.example.app_horizont_impl

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.app_horizont_impl.ui.theme.SeparateTestModuleAppTheme

class AppHorizontActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SeparateTestModuleAppTheme {
                Layout()
            }
        }
    }
}

@Composable
fun Layout() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = "app_horizont")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SeparateTestModuleAppTheme {
        Layout()
    }
}