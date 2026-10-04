package com.morex.father.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.morex.father.databinding.ActivityFaqBinding

class FaqActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFaqBinding

    private val faqs = listOf(
        "كيف أقوم بترقية باقتي؟" to "اذهب إلى: الإعدادات > الاشتراك > اختر الباقة ثم حوّل المبلغ إلى المحفظة الظاهرة وارفع صورة السند. سيتم التفعيل خلال دقائق.",
        "لماذا لا يصل كود التحقق؟" to "تأكد من رقم هاتفك، وتأكد من اتصالك بالإنترنت. يمكنك أيضاً تجربة إعادة الإرسال بعد 45 ثانية.",
        "كيف أربط هاتف ابني؟" to "ثبّت تطبيق MOREX Child على هاتف ابنك، افتحه واختر (الاقتران بكود)، ثم أدخل الكود الظاهر في تطبيقك.",
        "ما الفرق بين الباقات؟" to "الأساسية: 3 أجهزة. العائلية: 6 أجهزة + ميزات إضافية. الذهبية: 12 جهاز + AI + تقارير متقدمة.",
        "كيف أستخدم كود الإحالة؟" to "شارك كودك مع أصدقائك. عند تسجيلهم بكودك وإضافة طفل وجهاز، تحصل على 500 ريال رصيد.",
        "هل بياناتي آمنة؟" to "نعم. جميع البيانات مشفّرة بمعايير AES-256، والاتصالات محمية بـ SSL Pinning."
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFaqBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivBack.setOnClickListener { finish() }
        binding.rvFaqs.layoutManager = LinearLayoutManager(this)
        binding.rvFaqs.adapter = FaqAdapter(faqs)
    }
}
