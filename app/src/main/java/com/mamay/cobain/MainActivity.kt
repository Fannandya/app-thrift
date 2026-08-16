package com.mamay.cobain

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.mamay.cobain.presentation.ui.MainScreen
import com.mamay.cobain.presentation.viewmodel.ThriftViewModel
import com.mamay.cobain.presentation.viewmodel.ThriftViewModelFactory
import com.mamay.cobain.ui.theme.CobainTheme

class MainActivity : ComponentActivity() {

    private val viewModel: ThriftViewModel by viewModels {
        ThriftViewModelFactory((application as CobainApplication).container.thriftItemRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CobainTheme {
                MainScreen(viewModel)
            }
        }
    }
}
