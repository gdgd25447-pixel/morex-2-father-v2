# MOREX Father (morex-2-father) — دفعة 1: تحصين الأمان

تطبيق الأب (Android، Kotlin، Material 3، RTL، خط Cairo). Package: `com.morex.father` — minSdk 21 / targetSdk 34.

> **الحالة (دفعة 2 من 3):** الأمان والبناء مكتملان، والنماذج و`ApiService.kt` مطابقة لحقول السيرفر، وأغلب الشاشات تقرأ بيانات حقيقية. **لم أترجم المشروع** (لا Android SDK ولا إنترنت هنا). فحصت آلياً: أقواس Kotlin متوازنة، كل XML سليم، كل `R.string.*` موجود، كل view id مستخدم موجود في تخطيطه، وكل Activity في الـManifest لها ملف. أخطاء الترجمة الفعلية متوقعة وعليك إرسالها لي من Android Studio.

## الإعداد قبل البناء
1. **google-services.json**: انسخ `app/google-services.json.example` إلى `app/google-services.json` واملأه من Firebase. الملف في `.gitignore`. للـ CI: أضف السر `GOOGLE_SERVICES_JSON_BASE64`.
2. **التوقيع (release)**: ضع `app/morex-father.jks` ثم `MOREX_KEYSTORE_PASSWORD` و`MOREX_KEY_ALIAS` و`MOREX_KEY_PASSWORD` في `~/.gradle/gradle.properties` أو كمتغيرات بيئة (`KEYSTORE_PASSWORD`/`KEY_ALIAS`/`KEY_PASSWORD`). لم يعد هناك كلمة مرور افتراضية في الكود؛ بدونها يُبنى release غير موقّع.
3. **Certificate pinning**: شغّل `./scripts/get_pins.sh morex-1-server.onrender.com` وضع اثنين على الأقل في `MOREX_CERT_PINS`. بدونها يبقى التحقق العادي من الشهادة فقط (HTTPS إجباري).

## ما تغيّر في هذه الدفعة
| الملف | التغيير |
|---|---|
| `build.gradle` (app) | حُذفت كلمات مرور التوقيع الافتراضية؛ `minifyEnabled` و`shrinkResources` = true في release؛ حقول `CERT_PINS` و`EXPECTED_SIGNATURE_SHA256`؛ مكتبة bcrypt |
| `proguard-rules.pro` | قواعد كاملة (Gson، Retrofit، OkHttp، Socket.io، Tink/Security-Crypto، bcrypt، Firebase، Room، Glide، ViewBinding) + إزالة `Log.v/d/i` من release |
| `AndroidManifest.xml` | `usesCleartextTraffic=false`، `allowBackup=false`، ربط `networkSecurityConfig` وقواعد استبعاد النسخ الاحتياطي |
| `network_security_config.xml` | HTTPS فقط (كان يسمح بـ HTTP لنطاق السيرفر). ملف debug منفصل يسمح بـ 10.0.2.2 للتطوير |
| `ApiClient.kt` | لا تسجيل للتوكن ولا لمحتوى الطلبات (كان يكتب `Authorization` وجسم الطلب، أي OTP وPIN، في ملف). pinning. أُزيل `Content-Type: application/json` القسري الذي كان **يكسر رفع الملفات** (multipart) |
| `EncryptedPrefs.kt` / `Prefs.kt` | التوكن كان في SharedPreferences **بنص صريح** → صار في `EncryptedSharedPreferences` (AES-256-GCM) مع ترحيل تلقائي للقيمة القديمة |
| `PinHasher.kt` + `PinLockActivity.kt` | الـ PIN كان بنص صريح → bcrypt (cost 12) في التخزين المشفّر، والتحقق خارج الخيط الرئيسي. PIN القديم يُرحَّل عند أول فتح |
| `FileLogger.kt` | الملف في التخزين الداخلي (كان خارجياً) وبحد 512KB |
| `SocketManager.kt` | أُزيل التوكن من query الاتصال (يظهر في سجلات السيرفر)؛ السيرفر يقرؤه من `auth.token` |
| `SplashActivity.kt` | كشف جذر/مصحح أخطاء/تعديل (release فقط) + فحص توقيع اختياري |
| `.gitignore`, `google-services.json.example` | الملف الحقيقي أُزيل من التسليم |
| `.github/workflows/build.yml` | يفك `google-services.json` من سر، ويمرّر `MOREX_CERT_PINS` |

## دفعة 2: ما تغيّر
- **النماذج (`network/models`) و`ApiService.kt`**: أُعيدت كتابتها لتطابق السيرفر حرفياً (`lat/lng/radius_m`, `list_type` = black/white, `full_name`, `media_type/file_url`, `content`, ...). 71 دالة، **لا `Response<Any>`**. السيرفر هو المرجع (تطبيق الابن يعتمد عليه). التفاصيل في `docs/ENDPOINTS_MAP.md`.
- **التنبيهات**: السيرفر لا يرسل title/message، فالنص يُبنى محلياً في `utils/AlertText` من `type` + `payload`.
- **شاشات أُصلحت لتقرأ الرد الصحيح**: الأبناء، التنبيهات (+ تفاصيل، SOS، سرعة، كلمات)، الخريطة وسجل المواقع، المكالمات، الرسائل، الويب (black/white)، التطبيقات (استخدام/حظر)، الوسائط، الجدولة، السياج، الاشتراك، الدفع، الإحالة، الدعم، الملف الشخصي.
- **الدفع**: كان يرفع صورة بمسار خاطئ (`uri.path`) ولا يرسل wallet/reference/sender التي يشترطها السيرفر (كان الطلب يُرفض دائماً). صار: محافظ من السيرفر، نسخ الصورة للكاش بحد 5MB، وكل الحقول الإلزامية.
- **شاشات جديدة كانت فارغة**: `GeofenceEdit`, `GeofenceAlerts`, `Screenshot`, `AudioStream`, `LockScreen`. الكاميرا/الميكروفون/اللقطة: تأكيد من الأب ثم طلب للسيرفر؛ الموافقة الفعلية تتم على جهاز الابن.
- **Loading / Empty / Error / SwipeRefresh**: قالب `activity_simple_list.xml` مطبّق على `GeofenceAlerts`. **بقية الشاشات القديمة ما زالت بأنماطها السابقة** (progress + empty فقط، بلا زر إعادة محاولة ولا SwipeRefresh).
- **Socket.io**: أحداث السيرفر الفعلية فقط (`network/SocketManager.Events`) مع `SharedFlow`. مربوطة: التنبيهات، SOS، الخريطة، قائمة الأبناء.
- **Certificate pinning**: تسجيل فقط افتراضياً (حسب قرارك): عند عدم التطابق يُكتب `PIN_MISMATCH` في السجل ولا يُقفل التطبيق. **هذا الوضع لا يحمي من MITM.** للإنفاذ: `-PMOREX_PIN_ENFORCE=true`.
- حُذف `DisguiseControlActivity` (بموافقتك).
- إصلاح: عدّاد التنبيهات غير المقروءة في اللوحة كان يطلب تنبيهاً واحداً فقط (`limit=1`).

## ما لم يُنجز بعد (دفعة 3)
- دوال `ApiService` غير مربوطة بشاشة: `getSubscription`, `getReferralHistory`, `applyReferral`, `getAppRequests/respondAppRequest` (شاشة طلبات التطبيقات), `getKeywords/addKeyword`, `addSchedule/updateSchedule/deleteSchedule` (لا إضافة جدول من الواجهة), `getPairingStatus`, `getLocationSummary`, `getCommandHistory`, `setPin/verifyPin` (PIN المحلي فقط حالياً)، `refreshToken`، `logout` من السيرفر، `getSupportConversations`, `uploadSupportImage`, `getBlockStatus`.
- `MapFragment` (15 سطراً) بلا محتوى، والخريطة الحالية نصية + زر فتح خرائط خارجية (لا خريطة مدمجة).
- نماذج `DashboardData/NextPrayer/QuranProgress` موروثة وغير مستخدمة ولا تقابل السيرفر.
- مطابقة التخطيطات للصور (`docs/designs`) **لم تُجرَ**: لم أقارن أي شاشة بصورتها بعد.
- Loading/Empty/Error/SwipeRefresh لبقية الشاشات. `strings.xml` فيه نصوص عربية مضمّنة مباشرة في كثير من التخطيطات القديمة.
- Biometric binding (CryptoObject).

## ملاحظات
- **Certificate pinning "RSA 4096":** لا أستطيع تثبيت نوع المفتاح؛ شهادة Render يصدرها مزوّد خارجي وتتجدد، ولذلك الدبوس على SPKI للشهادة **الوسيطة** مع دبوس احتياطي. الدبوس على الورقة وحدها قد يقفل التطبيق عند التجديد.
- `DisguiseControlActivity.kt` ملف فارغ (10 أسطر) غير موجود في قائمة الشاشات، واسمه ("تمويه") يتعارض مع مبدأ الشفافية. **لم أحذفه** حسب تعليمتك؛ أنصح بحذفه.
- `settings.gradle` يستخدم مرايا `maven.aliyun.com` بجانب المستودعات الرسمية. تركتها، لكنها طرف ثالث إضافي في سلسلة التوريد.
- مفتاح Firebase API كان مكتوباً في `google-services.json` داخل الـ ZIP الذي أرسلته. هذا المفتاح ليس سرياً بالكامل، لكن يُنصح بتقييده بحزمة التطبيق وبصمة التوقيع من Google Cloud Console.
