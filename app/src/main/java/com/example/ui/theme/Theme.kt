package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

val ZangaDarkColorScheme = darkColorScheme(
    primary = ZangaPrimaryBlue,
    onPrimary = ZangaWhite,
    primaryContainer = ZangaDeepBlue,
    onPrimaryContainer = ZangaWhite,
    secondary = ZangaSecondaryBlue,
    onSecondary = ZangaWhite,
    secondaryContainer = ZangaDarkAccent,
    onSecondaryContainer = ZangaWhite,
    tertiary = ZangaSecondaryBlue,
    onTertiary = ZangaWhite,
    background = ZangaPureBlack,
    onBackground = ZangaWhite,
    surface = ZangaDarkSurface,
    onSurface = ZangaWhite,
    surfaceVariant = ZangaDarkCard,
    onSurfaceVariant = ZangaTextMutedDark,
    outline = ZangaDarkCardBorder,
    outlineVariant = ZangaDividerDark
)

val ZangaLightColorScheme = lightColorScheme(
    primary = ZangaPrimaryBlue,
    onPrimary = ZangaWhite,
    primaryContainer = ZangaDeepBlue,
    onPrimaryContainer = ZangaWhite,
    secondary = ZangaSecondaryBlue,
    onSecondary = ZangaWhite,
    secondaryContainer = ZangaLightSurfaceSubtle,
    onSecondaryContainer = ZangaDeepBlue,
    tertiary = ZangaDeepBlue,
    onTertiary = ZangaWhite,
    background = ZangaLightBackground,
    onBackground = ZangaPureBlack,
    surface = ZangaLightSurface,
    onSurface = ZangaPureBlack,
    surfaceVariant = ZangaLightSurfaceSubtle,
    onSurfaceVariant = ZangaTextMutedLight,
    outline = ZangaLightCardBorder,
    outlineVariant = ZangaDividerLight
)

@Composable
fun ZangaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) ZangaDarkColorScheme else ZangaLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    ZangaTheme(darkTheme = darkTheme, content = content)
}
