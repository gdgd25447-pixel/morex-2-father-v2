package com.morex.father.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.morex.father.network.ApiClient
import com.morex.father.network.models.Child
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DashboardViewModel : ViewModel() {

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _children = MutableLiveData<List<Child>>(emptyList())
    val children: LiveData<List<Child>> = _children

    private val _unreadAlerts = MutableLiveData(0)
    val unreadAlerts: LiveData<Int> = _unreadAlerts

    private val _parentName = MutableLiveData("")
    val parentName: LiveData<String> = _parentName

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadData(parentName: String) {
        _parentName.value = parentName
        _loading.value = true

        viewModelScope.launch {
            try {
                val childrenResp = withContext(Dispatchers.IO) {
                    ApiClient.get().getChildren()
                }
                if (childrenResp.isSuccessful) {
                    _children.value = childrenResp.body() ?: emptyList()
                } else {
                    _children.value = emptyList()
                }

                val alertsResp = withContext(Dispatchers.IO) {
                    ApiClient.get().getAlerts(limit = 100)
                }
                if (alertsResp.isSuccessful) {
                    val alerts = alertsResp.body() ?: emptyList()
                    _unreadAlerts.value = alerts.count { !it.isRead }
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }
}
