package ai.openclaw.app.ui

import ai.openclaw.app.AppearanceTextScale
import ai.openclaw.app.AppearanceThemeMode
import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Density
import androidx.core.view.WindowCompat

internal val LocalResolvedAppearanceIsDark = staticCompositionLocalOf { false }

/**
 * App theme wrapper that resolves the requested appearance for system surfaces and child themes.
 *
 * Dynamic color schemes (Material You) require API 31+. On Android 11 (API 30) fall back to a
 * static Material color scheme so theme initialization does not throw Resources$NotFoundException.
 */
@Composable
fun OpenClawTheme(
  themeMode: AppearanceThemeMode = AppearanceThemeMode.Dark,
  textScale: AppearanceTextScale = AppearanceTextScale.Standard,
  content: @Composable () -> Unit,
) {
  val context = LocalContext.current
  val isDark = themeMode.isDark(systemDark = isSystemInDarkTheme())
  val colorScheme =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
      if (isDark) darkColorScheme(context) else lightColorScheme(context)
    }

  val systemDensity = LocalDensity.current
  val textDensity =
    remember(systemDensity, textScale) {
      // Keep Compose's platform SP conversion and leave DP geometry unchanged.
      if (textScale == AppearanceTextScale.Standard) systemDensity else Density(systemDensity.density, systemDensity.fontScale * textScale.factor)
    }

  OpenClawSystemBarAppearance(lightAppearance = !isDark)

  CompositionLocalProvider(
    LocalResolvedAppearanceIsDark provides isDark,
    LocalDensity provides textDensity,
  ) {
    MaterialTheme(colorScheme = colorScheme, content = content)
  }
}

@Composable
internal fun OpenClawSystemBarAppearance(lightAppearance: Boolean) {
  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as? Activity)?.window ?: return@SideEffect
      WindowCompat.getInsetsController(window, window.decorView).apply {
        isAppearanceLightStatusBars = lightAppearance
        isAppearanceLightNavigationBars = lightAppearance
      }
    }
  }
}
