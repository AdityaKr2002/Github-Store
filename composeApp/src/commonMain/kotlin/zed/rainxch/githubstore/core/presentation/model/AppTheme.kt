package zed.rainxch.githubstore.core.presentation.model

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import zed.rainxch.githubstore.core.presentation.theme.amberOrangeDark
import zed.rainxch.githubstore.core.presentation.theme.amberOrangeLight
import zed.rainxch.githubstore.core.presentation.theme.deepPurpleDark
import zed.rainxch.githubstore.core.presentation.theme.deepPurpleLight
import zed.rainxch.githubstore.core.presentation.theme.forestGreenDark
import zed.rainxch.githubstore.core.presentation.theme.forestGreenLight
import zed.rainxch.githubstore.core.presentation.theme.oceanBlueDark
import zed.rainxch.githubstore.core.presentation.theme.oceanBlueLight
import zed.rainxch.githubstore.core.presentation.theme.slateGrayDark
import zed.rainxch.githubstore.core.presentation.theme.slateGrayLight

enum class AppTheme(
    val displayName: String,
    val lightScheme: ColorScheme?,
    val darkScheme: ColorScheme?,
    val primaryColor: Color?
) {
    DYNAMIC(
        displayName = "Dynamic",
        lightScheme = null,
        darkScheme = null,
        primaryColor = null
    ),
    OCEAN(
        displayName = "Ocean",
        lightScheme = oceanBlueLight,
        darkScheme = oceanBlueDark,
        primaryColor = Color(0xFF2A638A)
    ),
    PURPLE(
        displayName = "Purple",
        lightScheme = deepPurpleLight,
        darkScheme = deepPurpleDark,
        primaryColor = Color(0xFF6750A4)
    ),
    FOREST(
        displayName = "Forest",
        lightScheme = forestGreenLight,
        darkScheme = forestGreenDark,
        primaryColor = Color(0xFF356859)
    ),
    SLATE(
        displayName = "Slate",
        lightScheme = slateGrayLight,
        darkScheme = slateGrayDark,
        primaryColor = Color(0xFF535E6C)
    ),
    AMBER(
        displayName = "Amber",
        lightScheme = amberOrangeLight,
        darkScheme = amberOrangeDark,
        primaryColor = Color(0xFF8B5000)
    );

    companion object {
        fun fromName(name: String?): AppTheme {
            return entries.find { it.name == name } ?: OCEAN
        }
    }
}