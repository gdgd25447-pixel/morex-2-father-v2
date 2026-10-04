package com.morex.father.children

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.morex.father.network.ApiClient
import com.morex.father.network.models.Child
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChildrenViewModel : ViewModel() {

    private val _children = MutableLiveData<List<Child>>(emptyList())
    val children: LiveData<List<Child>> = _children

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadChildren() {
        _loading.value = true

        viewModelScope.launch {
            try {
                val resp = withContext(Dispatchers.IO) {
                    ApiClient.get().getChildren()
                }

                if (resp.isSuccessful) {
                    val list = resp.body() ?: emptyList()
                    _children.value = list
                } else {
                    _error.value = "فشل التحميل (${resp.code()})"
                    _children.value = emptyList()
                }
            } catch (e: Exception) {
                _error.value = e.message
                _children.value = emptyList()
            } finally {
                _loading.value = false
            }
        }
    }
}
