package com.morex.father.children

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.morex.father.databinding.ActivityChildrenListBinding
import com.morex.father.network.ApiClient
import com.morex.father.network.models.Child
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChildrenListActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "ChildrenList"
    }

    private lateinit var binding: ActivityChildrenListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChildrenListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvChildren.layoutManager = LinearLayoutManager(this)

        binding.ivBack.setOnClickListener { finish() }
        binding.ivAdd.setOnClickListener {
            startActivity(Intent(this, AddChildActivity::class.java))
        }

        loadChildren()
    }

    override fun onResume() {
        super.onResume()
        loadChildren()
    }

    private fun loadChildren() {
        binding.progressBar.visibility = View.VISIBLE
        binding.emptyView.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val resp = withContext(Dispatchers.IO) {
                    ApiClient.get().getChildren()
                }

                binding.progressBar.visibility = View.GONE

                if (resp.isSuccessful) {
                                        val children: List<Child> = resp.body() ?: emptyList()

                    Log.d(TAG, "📥 loaded ${children.size} children")

                    binding.rvChildren.adapter = ChildCardAdapter(children) { child ->
                        val i = Intent(this@ChildrenListActivity, ChildDetailActivity::class.java)
                        i.putExtra("child_id", child.id)
                        startActivity(i)
                    }

                    // ⭐ الإصلاح: التحقق الصحيح من القائمة الفارغة
                    if (children.isEmpty()) {
                        binding.emptyView.visibility = View.VISIBLE
                        binding.rvChildren.visibility = View.GONE
                    } else {
                        binding.emptyView.visibility = View.GONE
                        binding.rvChildren.visibility = View.VISIBLE
                    }
                } else {
                    Log.e(TAG, "❌ HTTP ${resp.code()}")
                    Toast.makeText(
                        this@ChildrenListActivity,
                        "فشل التحميل (${resp.code()})",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Log.e(TAG, "❌ ${e.message}", e)
                Toast.makeText(
                    this@ChildrenListActivity,
                    "خطأ: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
