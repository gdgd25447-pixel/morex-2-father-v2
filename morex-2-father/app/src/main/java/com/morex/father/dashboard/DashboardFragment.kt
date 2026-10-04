package com.morex.father.dashboard

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.morex.father.children.ChildCardAdapter
import com.morex.father.children.ChildrenListActivity
import com.morex.father.data.Prefs
import com.morex.father.databinding.FragmentDashboardBinding
import com.morex.father.location.MapActivity
import com.morex.father.monitor.AlertsActivity
import com.morex.father.network.models.Child
import com.morex.father.utils.Helpers

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DashboardViewModel by viewModels()
    private var firstChild: Child? = null

    companion object {
        private const val TAG = "Dashboard"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, s: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupObservers()

        val name = Prefs.getParentName(requireContext()) ?: "مستخدم"
        viewModel.loadData(name)
    }

    private fun setupUI() {
        binding.tvGreeting.text = Helpers.getGreeting()
        binding.tvParentName.text = Prefs.getParentName(requireContext()) ?: "مستخدم"

        Prefs.getParentAvatar(requireContext())?.let {
            Glide.with(this).load(it).circleCrop().into(binding.ivParentAvatar)
        }

        binding.rvChildren.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        binding.tvViewAll.setOnClickListener {
            startActivity(Intent(requireContext(), ChildrenListActivity::class.java))
        }
        binding.ivNotifications.setOnClickListener {
            startActivity(Intent(requireContext(), AlertsActivity::class.java))
        }
        binding.btnViewMap.setOnClickListener {
            val i = Intent(requireContext(), MapActivity::class.java)
            firstChild?.id?.let { i.putExtra("child_id", it) }
            startActivity(i)
        }
        binding.ivSettings.setOnClickListener {
            (activity as? com.morex.father.MainActivity)?.navigateTo(
                com.morex.father.R.id.nav_settings
            )
        }
    }

    private fun setupObservers() {
        viewModel.parentName.observe(viewLifecycleOwner) {
            binding.tvParentName.text = it
        }

        viewModel.children.observe(viewLifecycleOwner) { children ->
            val adapter = ChildCardAdapter(children) { child ->
                val intent = Intent(
                    requireContext(),
                    com.morex.father.children.ChildDetailActivity::class.java
                )
                intent.putExtra("child_id", child.id)
                startActivity(intent)
            }
            binding.rvChildren.adapter = adapter

            updateMapCard(children)
        }

        viewModel.loading.observe(viewLifecycleOwner) { loading ->
            binding.swipeRefresh.isRefreshing = loading
        }
    }

    private fun updateMapCard(children: List<Child>) {
        Log.i(TAG, "Children count: ${children.size}")
        children.forEach {
            Log.i(TAG, "  ${it.name} | isPaired=${it.isPaired} | isOnline=${it.isOnline} | deviceId=${it.deviceId} | battery=${it.batteryLevel}")
        }

        // ⭐ الإصلاح: نبحث عن أي طفل لديه device_id (يعني مربوط)
        val paired = children.firstOrNull { !it.deviceId.isNullOrEmpty() }

        if (paired == null) {
            binding.tvMapTitle.text = "آخر موقع معروف"
            binding.tvMapSubtitle.text = "لا يوجد طفل مربوط"
            binding.tvMapPlaceholder.text = "اربط جهازاً لعرض الموقع"
            firstChild = null
            return
        }

        firstChild = paired
        binding.tvMapTitle.text = "موقع ${paired.name ?: "الطفل"} الآن"

        val statusText = if (paired.isOnline) "متصل" else "غير متصل"
        val battText = "بطارية ${paired.batteryLevel}%"
        binding.tvMapSubtitle.text = "$statusText · $battText"

        binding.tvMapPlaceholder.text =
            if (paired.isOnline) "اضغط على الزر أدناه لعرض الموقع"
            else "الجهاز غير متصل حالياً"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
