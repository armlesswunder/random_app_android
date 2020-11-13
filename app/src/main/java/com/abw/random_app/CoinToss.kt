package com.abw.random_app

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Guideline
import androidx.core.animation.doOnEnd
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.math.absoluteValue

class CoinToss : AppCompatActivity() {

    private var isSpinning = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coin_toss)
        supportActionBar?.setBackgroundDrawable(resources.getDrawable(R.drawable.toolbar_background, null))
        val coin = findViewById<ImageView>(R.id.coin)
        val face = findViewById<TextView>(R.id.face)
        val cl = findViewById<ConstraintLayout>(R.id.cl)
        isSpinning = false
        cl.setOnClickListener {
            if (!isSpinning) {
                isSpinning = true
                val va1 = ValueAnimator.ofFloat(0f, -450f)
                va1.addUpdateListener {
                    val value = it.animatedValue as Float
                    coin.translationY = value
                    face.translationY = value
                }
                val va2 = ValueAnimator.ofFloat(1f, -1f)
                va2.addUpdateListener {
                    val value = it.animatedValue as Float
                    coin.scaleY = value
                    face.scaleY = value
                }
                va2.interpolator = LinearInterpolator()
                va2.duration = 300
                va2.start()
                va1.interpolator = DecelerateInterpolator()
                va1.duration = 300
                va1.start()
                va1.doOnEnd {
                    val random = Random(Date().time)
                    var n = random.nextInt().absoluteValue
                    n %= 2
                    face.text = if (n == 0) "H" else "T"
                    val va3 = ValueAnimator.ofFloat(-450f, 0f)
                    va3.addUpdateListener {
                        val value = it.animatedValue as Float
                        coin.translationY = value
                        face.translationY = value
                    }
                    val va4 = ValueAnimator.ofFloat(-1f, 1f)
                    va4.addUpdateListener {
                        val value = it.animatedValue as Float
                        coin.scaleY = value
                        face.scaleY = value
                    }
                    va4.interpolator = LinearInterpolator()
                    va4.duration = 300
                    va4.start()
                    va3.interpolator = AccelerateInterpolator()
                    va3.duration = 300
                    va3.start()
                    va3.doOnEnd { isSpinning = false }
                }
            }
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
