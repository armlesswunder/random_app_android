package com.abw.random_app

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.abw.random_app.MainActivity.Companion.prefs
import kotlinx.android.synthetic.main.activity_range.*
import java.util.*

class RangeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_range)
        supportActionBar?.setBackgroundDrawable(resources.getDrawable(R.drawable.toolbar_background, null))

        val min = prefs.getInt("min", 1)
        val max = prefs.getInt("max", 10)
        txtMin.setText(min.toString())
        txtMax.setText(max.toString())
        txtMax.setOnEditorActionListener { view, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                btnGeneratePressed(view)
            }
            return@setOnEditorActionListener false
        }
    }

    fun btnGeneratePressed(view: View) {
        try {
            val min = Integer.parseInt(txtMin.text.toString())
            val max = Integer.parseInt(txtMax.text.toString())
            val random = Random(System.currentTimeMillis())
            val randVal: Int = random.nextInt((max + 1) - min) + min

            valueResult.text = randVal.toString()
            prefs.edit().apply {
                putInt("min", min)
                putInt("max", max)
                apply()
            }
        } catch (e: Throwable) {
            valueResult.text = "Error: " + e.message.toString()
        }
    }
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.home -> {
                setResult(1)
                finish()
                true
            }
            else -> {
                setResult(1)
                finish()
                true
            }
        }
    }
}