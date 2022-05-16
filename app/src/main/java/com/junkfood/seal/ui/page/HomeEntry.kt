package com.junkfood.seal.ui.page

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.junkfood.seal.ui.core.Route
import com.junkfood.seal.ui.page.download.DownloadPage
import com.junkfood.seal.ui.page.settings.AboutPage
import com.junkfood.seal.ui.page.settings.SettingsPage
import com.junkfood.seal.ui.page.settings.download.DownloadPreferences
import com.junkfood.seal.ui.page.videolist.VideoListPage
import com.junkfood.ui.animatedComposable

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HomeEntry(
) {
    val useDarkTheme = isSystemInDarkTheme()

    val systemUiController = rememberSystemUiController()

    systemUiController.run {
        setStatusBarColor(Color.Transparent, !useDarkTheme)
        setSystemBarsColor(Color.Transparent, !useDarkTheme)
        setNavigationBarColor(Color.Transparent, !useDarkTheme)
    }


    val navController = rememberAnimatedNavController()
    AnimatedNavHost(navController = navController, startDestination = Route.HOME) {
        animatedComposable(Route.HOME) {
            DownloadPage(navController = navController)
        }
        animatedComposable(Route.SETTINGS) { SettingsPage(navController) }
        animatedComposable(Route.DOWNLOAD_PREFERENCES) { DownloadPreferences(navController) }
        animatedComposable(Route.DOWNLOADS) { VideoListPage(navController) }
        animatedComposable(Route.ABOUT){ AboutPage(navController) }
    }

}