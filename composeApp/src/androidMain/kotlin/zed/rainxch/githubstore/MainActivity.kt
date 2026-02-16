package zed.rainxch.githubstore

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.util.Consumer

class MainActivity : ComponentActivity() {

    private var deepLinkUri by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        enableEdgeToEdge()

        super.onCreate(savedInstanceState)

        deepLinkUri = intent?.data?.toString()

        setContent {
            DisposableEffect(Unit) {
                val listener = Consumer<Intent> { newIntent ->
                    newIntent.data?.toString()?.let {
                        deepLinkUri = it
                    }
                }
                addOnNewIntentListener(listener)
                onDispose {
                    removeOnNewIntentListener(listener)
                }
            }

            App(deepLinkUri = deepLinkUri)
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}