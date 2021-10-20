package com.abw.random_app

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_range.*
import java.util.*

class RangeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_range)

        txtMax.setOnEditorActionListener { view, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                btnGeneratePressed(view)
            }
            return@setOnEditorActionListener false
        }
    }

    fun btnGeneratePressed(view: View) {
        val min = Integer.parseInt(txtMin.text.toString())
        val max = Integer.parseInt(txtMax.text.toString())
        val random = Random()
        val randVal: Int = random.nextInt((max + 1) - min) + min

        valueResult.text = randVal.toString()
    }
}