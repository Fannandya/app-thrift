package com.mamay.cobain.presentation.viewmodel

import androidx.lifecycle.ViewModelProvider
import com.mamay.cobain.data.repository.ThriftItemRepository

class ThriftViewModelFactory(private val repository: ThriftItemRepository) :
    ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ThriftViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ThriftViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
