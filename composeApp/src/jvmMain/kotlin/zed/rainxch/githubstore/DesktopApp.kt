package zed.rainxch.githubstore

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.jetbrains.compose.resources.painterResource
import zed.rainxch.githubstore.app.di.initKoin
import githubstore.composeapp.generated.resources.Res
import githubstore.composeapp.generated.resources.app_icon
import java.awt.Desktop
import java.net.URI

fun main(args: Array<String>) = application {
    initKoin()

    // Deep link state — can come from CLI args or macOS open-url event
    var deepLinkUri by mutableStateOf(args.firstOrNull())

    // Register macOS URI scheme handler (githubstore://)
    // When the packaged .app is opened via a URL, macOS delivers it here
    if (Desktop.isDesktopSupported()) {
        Desktop.getDesktop().let { desktop ->
            if (desktop.isSupported(Desktop.Action.APP_OPEN_URI)) {
                desktop.setOpenURIHandler { event ->
                    deepLinkUri = event.uri.toString()
                }
            }
        }
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "GitHub Store",
        icon = painterResource(Res.drawable.app_icon)
    ) {
        App(deepLinkUri = deepLinkUri)
    }
}
