
package com.example.qomcardreader

import android.app.Activity
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity(), NfcAdapter.ReaderCallback {

    private var nfcAdapter: NfcAdapter? = null
    private lateinit var statusText: TextView
    private lateinit var cardText: TextView
    private lateinit var balanceText: TextView
    private var lastCardUid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        buildScreen()

        when {
            nfcAdapter == null ->
                statusText.text = "این گوشی NFC ندارد."

            nfcAdapter?.isEnabled == false ->
                statusText.text = "NFC خاموش است. آن را روشن کن."

            else ->
                statusText.text = "آماده؛ قم‌کارت را پشت گوشی نزدیک کن."
        }
    }

    private fun buildScreen() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 36, 28, 24)
            setBackgroundColor(0xFFF7F9FC.toInt())
        }

        val title = TextView(this).apply {
            text = "قم‌کارت"
            textSize = 26f
            setTextColor(0xFF17324D.toInt())
            gravity = Gravity.CENTER
        }

        root.addView(title, matchWidth())

        statusText = TextView(this).apply {
            textSize = 17f
            setTextColor(0xFF1565C0.toInt())
            gravity = Gravity.CENTER
            setPadding(8, 28, 8, 20)
        }

        root.addView(statusText, matchWidth())

        cardText = TextView(this).apply {
            text = "برای شناسایی، کارت را پشت گوشی نزدیک کن."
            textSize = 16f
            setTextColor(0xFF202124.toInt())
            setPadding(16, 18, 16, 18)
            setBackgroundColor(0xFFFFFFFF.toInt())
        }

        root.addView(cardText, matchWidth())

        val balanceButton = Button(this).apply {
            text = "نمایش موجودی"
            setOnClickListener {
                showBalanceStatus()
            }
        }

        root.addView(balanceButton, matchWidth())

        balanceText = TextView(this).apply {
            text = "موجودی: هنوز بررسی نشده"
            textSize = 16f
            setTextColor(0xFF5F6368.toInt())
            gravity = Gravity.CENTER
            setPadding(8, 18, 8, 18)
        }

        root.addView(balanceText, matchWidth())

        val note = TextView(this).apply {
            text = "برای نمایش موجودی واقعی، اتصال به سامانه رسمی قم‌کارت لازم است."
            textSize = 13f
            setTextColor(0xFF5F6368.toInt())
            setPadding(4, 18, 4, 8)
        }

        root.addView(note, matchWidth())

        setContentView(root)
    }

    private fun matchWidth() =
        LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

    override fun onResume() {
        super.onResume()

        val adapter = nfcAdapter ?: return
        if (!adapter.isEnabled) return

        val flags =
            NfcAdapter.FLAG_READER_NFC_A or
            NfcAdapter.FLAG_READER_NFC_B or
            NfcAdapter.FLAG_READER_NFC_F or
            NfcAdapter.FLAG_READER_NFC_V or
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
            techs.any { it == "MifareClassic" } ->
                "MIFARE Classic (احتمالاً 1K یا 4K)"

            techs.any { it == "MifareUltralight" } ->
                "MIFARE Ultralight"

            techs.any { it == "IsoDep" } ->
                "ISO-DEP"

            else ->
                "کارت NFC؛ نوع دقیق مشخص نیست"
        }

        runOnUiThread {
            lastCardUid = uid

            statusText.text = "کارت شناسایی شد."

            cardText.text =
                "شناسه کارت (UID):\n$uid\n\n" +
                "نوع کارت: $cardType\n\n" +
                "فناوری‌ها:\n${techs.joinToString(", ")}"

            balanceText.text =
                "موجودی: برای این کارت هنوز قابل خواندن نیست"
        }
    }

    private fun showBalanceStatus() {
        if (lastCardUid == null) {
            Toast.makeText(
                this,
                "ابتدا کارت را به پشت گوشی نزدیک کن.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        balanceText.text =
            "موجودی واقعی در دسترس نیست؛ اتصال به سامانه رسمی قم‌کارت لازم است."
    }
}
