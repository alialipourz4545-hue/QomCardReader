
package com.example.qomcardreader

import android.app.Activity
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.MifareClassic
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable

class MainActivity : Activity(), NfcAdapter.ReaderCallback {

    private var nfcAdapter: NfcAdapter? = null

    private lateinit var statusText: TextView
    private lateinit var uidText: TextView
    private lateinit var typeText: TextView
    private lateinit var techText: TextView
    private lateinit var memoryText: TextView
    private lateinit var balanceText: TextView
    private lateinit var blockText: TextView

    private var lastCardUid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        buildScreen()
        updateNfcStatus()
    }

    private fun buildScreen() {

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(28), dp(22), dp(24))
            setBackgroundColor(Color.rgb(244, 247, 251))
            layoutDirection = View.LAYOUT_DIRECTION_RTL
        }

        val scroll = ScrollView(this)
        scroll.addView(root)

        val title = TextView(this).apply {
            text = "قم‌کارت"
            textSize = 30f
            setTextColor(Color.rgb(20, 48, 78))
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }
        root.addView(title, matchWidth())

        val subtitle = TextView(this).apply {
            text = "بررسی اطلاعات کارت NFC"
            textSize = 14f
            setTextColor(Color.GRAY)
            gravity = Gravity.CENTER
            setPadding(0, dp(6), 0, dp(24))
        }
        root.addView(subtitle, matchWidth())

        statusText = TextView(this).apply {
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(21, 101, 192))
            gravity = Gravity.CENTER
            setPadding(dp(12), dp(18), dp(12), dp(18))
            background = roundedBox(Color.rgb(232, 241, 252), 16)
        }
        root.addView(statusText, matchWidth())

        addSectionTitle(root, "اطلاعات کارت")

        uidText = addInfoCard(
            root, "شناسه کارت (UID)", "در انتظار اسکن"
        )

        typeText = addInfoCard(
            root, "نوع کارت", "در انتظار اسکن"
        )

        techText = addInfoCard(
            root, "فناوری کارت", "در انتظار اسکن"
        )

        addSectionTitle(root, "مشخصات حافظه")

        memoryText = addInfoCard(
            root, "اطلاعات حافظه", "منتظر اسکن کارت"
        )

        addSectionTitle(root, "خواندن بلوک‌های حافظه")

        val scanButton = Button(this).apply {
            text = "آماده اسکن کارت"
            isEnabled = false
        }
        root.addView(scanButton, matchWidth())

        blockText = TextView(this).apply {
            text = "برای شروع، کارت را پشت گوشی قرار بده."
            textSize = 13f
            setTextColor(Color.DKGRAY)
            setPadding(dp(14), dp(16), dp(14), dp(16))
            background = roundedBox(Color.WHITE, 16)
            textDirection = View.TEXT_DIRECTION_LTR
        }
        root.addView(blockText, matchWidth())

        addSectionTitle(root, "وضعیت موجودی")

        balanceText = TextView(this).apply {
            text = "موجودی واقعی هنوز مشخص نشده است."
            textSize = 15f
            gravity = Gravity.CENTER
            setTextColor(Color.DKGRAY)
            setPadding(dp(16), dp(20), dp(16), dp(20))
            background = roundedBox(Color.WHITE, 16)
        }
        root.addView(balanceText, matchWidth())

        val note = TextView(this).apply {
            text = "این نسخه فقط خواندن را امتحان می‌کند. هیچ کلیدی حدس زده نمی‌شود و هیچ اطلاعاتی روی کارت نوشته یا پاک نمی‌شود."
            textSize = 12f
            setTextColor(Color.GRAY)
            gravity = Gravity.CENTER
            setPadding(dp(8), dp(24), dp(8), dp(8))
        }
        root.addView(note, matchWidth())

        setContentView(scroll)
    }

    private fun addSectionTitle(
        root: LinearLayout,
        title: String
    ) {
        val text = TextView(this).apply {
            this.text = title
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(25, 55, 85))
            setPadding(0, dp(24), 0, dp(12))
        }
        root.addView(text, matchWidth())
    }

    private fun addInfoCard(
        root: LinearLayout,
        title: String,
        value: String
    ): TextView {

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(14), dp(16), dp(14))
            background = roundedBox(Color.WHITE, 16)
        }

        val heading = TextView(this).apply {
            text = title
            textSize = 13f
            setTextColor(Color.GRAY)
        }

        val result = TextView(this).apply {
            text = value
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(35, 45, 55))
            setPadding(0, dp(8), 0, 0)
            textDirection = View.TEXT_DIRECTION_LTR
        }

        card.addView(heading, matchWidth())
        card.addView(result, matchWidth())

        val params = matchWidth()
        params.bottomMargin = dp(10)
        root.addView(card, params)

        return result
    }

    private fun roundedBox(
        color: Int,
        radius: Int
    ): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private fun matchWidth(): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun updateNfcStatus() {
        when {
            nfcAdapter == null ->
                statusText.text = "این گوشی NFC ندارد."

            nfcAdapter?.isEnabled == false ->
                statusText.text = "NFC خاموش است."

            else ->
                statusText.text =
                    "آماده اسکن؛ کارت را پشت گوشی نزدیک کن."
        }
    }

    override fun onResume() {
        super.onResume()

        updateNfcStatus()

        val adapter = nfcAdapter ?: return
        if (!adapter.isEnabled) return

        val flags =
            NfcAdapter.FLAG_READER_NFC_A or
            NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS

        adapter.enableReaderMode(this, this, flags, null)
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableReaderMode(this)
    }

    override fun onTagDiscovered(tag: Tag) {

        val uid = tag.id.joinToString(":") {
            "%02X".format(it)
        }

        val techs = tag.techList.map {
            it.substringAfterLast('.')
        }.distinct()

        val cardType = when {
            techs.contains("MifareClassic") ->
                "MIFARE Classic"

            techs.contains("MifareUltralight") ->
                "MIFARE Ultralight"

            techs.contains("IsoDep") ->
                "ISO-DEP"

            else ->
                "کارت NFC؛ نوع دقیق نامشخص"
        }

        var memoryInfo = "MIFARE Classic شناسایی نشد."
        var blockReport = "اطلاعات بلوک‌ها موجود نیست."

        val mifare = MifareClassic.get(tag)

        if (mifare != null) {

            try {
                mifare.connect()

                memoryInfo = """
                    نوع حافظه: Classic
                    ظرفیت: ${mifare.size} بایت
                    تعداد سکتورها: ${mifare.sectorCount}
                    تعداد بلوک‌ها: ${mifare.blockCount}
                    اندازه هر بلوک: ${MifareClassic.BLOCK_SIZE} بایت
                    ارتباط: موفق
                """.trimIndent()

                val report = StringBuilder()

                var successCount = 0
                var failedCount = 0

                for (block in 0 until mifare.blockCount) {

                    val blockInSector = block % 4

                    // بلوک چهارم هر سکتور، اطلاعات امنیتی دارد.
                    // در این نسخه آن را نمی‌خوانیم.
                    if (blockInSector == 3) {
                        continue
                    }

                    report.append("بلوک ")
                        .append(block)
                        .append(": ")

                    try {
                        // هیچ احراز هویتی انجام نمی‌شود.
                        // فقط خواندن معمولی امتحان می‌شود.
                        val data = mifare.readBlock(block)

                        val hex = data.joinToString(" ") {
                            "%02X".format(it.toInt() and 0xFF)
                        }

                        report.append(hex)
                        report.append("\n")

                        successCount++

                    } catch (e: Exception) {

                        report.append("خواندن ناموفق")
                        report.append("\n")

                        failedCount++
                    }
                }

                blockReport =
                    "نتیجه خواندن بدون احراز هویت\n" +
                    "خواندن موفق: $successCount\n" +
                    "خواندن ناموفق: $failedCount\n\n" +
                    report.toString()

            } catch (e: Exception) {

                memoryInfo =
                    "خطا در ارتباط با کارت:\n" +
                    (e.message ?: "نامشخص")

            } finally {
                try {
                    mifare.close()
                } catch (_: Exception) {
                }
            }
        }

        runOnUiThread {

            lastCardUid = uid

            statusText.text = "اسکن کارت پایان یافت."

            uidText.text = uid
            typeText.text = cardType
            techText.text = techs.joinToString("، ")
            memoryText.text = memoryInfo
            blockText.text = blockReport

            balanceText.text =
                "موجودی واقعی هنوز از داده‌های خوانده‌شده قابل تشخیص نیست."
        }
    }
}
