package com.morex.father.subscription

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.morex.father.R
import com.morex.father.databinding.ActivitySubscriptionBinding
import com.morex.father.network.ApiClient
import com.morex.father.network.models.Plan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * الباقات والأسعار من السيرفر (GET /parent/subscription/plans → pricing) وحالة الاشتراك الحالية.
 */
class SubscriptionActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySubscriptionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubscriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.ivBack.setOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
        load()
    }

    private fun load() {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val plansResp = withContext(Dispatchers.IO) { ApiClient.get().getPlans() }
                binding.progressBar.visibility = View.GONE

                val body = plansResp.body()
                if (!plansResp.isSuccessful || body == null) {
                    showError(); return@launch
                }
                // ترتيب ثابت: basic ثم family ثم gold، وأي باقة جديدة تأتي بعدها
                val order = listOf("basic", "family", "gold")
                val plans = body.pricing.entries
                    .sortedBy { order.indexOf(it.key).let { i -> if (i < 0) Int.MAX_VALUE else i } }
                    .map { Plan(id = it.key, priceUsd = it.value.usd, priceYer = it.value.yer) }
                renderPlans(plans)
            } catch (_: Exception) {
                binding.progressBar.visibility = View.GONE
                showError()
            }
        }
    }

    private fun showError() {
        binding.plansContainer.removeAllViews()
        val tv = TextView(this).apply {
            text = getString(R.string.common_network_error)
            setPadding(24, 24, 24, 8)
        }
        val retry = MaterialButton(this).apply {
            text = getString(R.string.common_retry)
            setOnClickListener { load() }
        }
        binding.plansContainer.addView(tv)
        binding.plansContainer.addView(retry)
    }

    private fun planName(id: String): String = when (id) {
        "basic" -> getString(R.string.plan_basic)
        "family" -> getString(R.string.plan_family)
        "gold" -> getString(R.string.plan_gold)
        else -> id
    }

    private fun renderPlans(plans: List<Plan>) {
        binding.plansContainer.removeAllViews()
        if (plans.isEmpty()) {
            Toast.makeText(this, R.string.common_empty, Toast.LENGTH_SHORT).show()
            return
        }
        plans.forEach { plan ->
            val card = layoutInflater.inflate(R.layout.item_plan, binding.plansContainer, false)
            val tvName = card.findViewById<TextView>(R.id.tvPlanName)
            val tvPrice = card.findViewById<TextView>(R.id.tvPlanPrice)
            val tvDevices = card.findViewById<TextView>(R.id.tvPlanDevices)
            val btnSubscribe = card.findViewById<MaterialButton>(R.id.btnSubscribe)

            tvName.text = planName(plan.id)
            tvPrice.text = getString(R.string.plan_price_month, plan.priceUsd.toString().removeSuffix(".0"))
            // السيرفر لا يعيد عدد الأجهزة لكل باقة، فلا نعرض رقماً مختلقاً
            tvDevices.text = getString(R.string.plan_price_yer, plan.priceYer.toLong())

            btnSubscribe.setOnClickListener {
                startActivity(Intent(this, PaymentActivity::class.java).apply {
                    putExtra("plan_id", plan.id)
                    putExtra("plan_name", planName(plan.id))
                    putExtra("plan_price_usd", plan.priceUsd)
                    putExtra("plan_price_yer", plan.priceYer)
                })
            }
            binding.plansContainer.addView(card)
        }
    }
}
