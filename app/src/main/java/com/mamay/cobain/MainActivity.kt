package com.mamay.cobain

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.mamay.cobain.data.dao.ThriftItemStorage
import com.mamay.cobain.data.repository.ThriftItemRepository
import com.mamay.cobain.presentation.ui.MainScreen
import com.mamay.cobain.presentation.viewmodel.ThriftViewModel
import com.mamay.cobain.presentation.viewmodel.ThriftViewModelFactory
import com.mamay.cobain.ui.theme.CobainTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val storage = ThriftItemStorage(this)
        val repository = ThriftItemRepository(storage)
        val factory = ThriftViewModelFactory(repository)
        val viewModel = ViewModelProvider(this, factory).get(ThriftViewModel::class.java)

        setContent {
            CobainTheme {
                MainScreen(viewModel)
            }
        }
    }
}