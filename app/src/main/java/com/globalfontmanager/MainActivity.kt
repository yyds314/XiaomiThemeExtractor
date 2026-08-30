package com.globalfontmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.globalfontmanager.ui.GlobalFontManagerApp
import com.globalfontmanager.ui.theme.GlobalFontManagerTheme
import com.globalfontmanager.ui.viewmodel.FontManagerViewModel
import com.globalfontmanager.service.GlobalExceptionHandler

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        Thread.setDefaultUncaughtExceptionHandler(GlobalExceptionHandler(this))
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: FontManagerViewModel = viewModel()
            GlobalFontManagerTheme(darkTheme = viewModel.isDarkMode) {
                GlobalFontManagerApp(viewModel = viewModel)
            }
        }
    }
}
