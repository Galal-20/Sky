package com.galal.weather.UI

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.galal.weather.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {
    private val binding: ActivitySplashBinding by lazy {
        ActivitySplashBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        Handler(Looper.getMainLooper()).postDelayed({
            Intent(this, MainActivity::class.java).also {
                startActivity(it)
                finish()
            }
        },6000)
        yoyo()
    }

    private fun yoyo() {
        YoYo.with(Techniques.FadeIn).duration(4000).playOn(binding.imageSplash)
        YoYo.with(Techniques.FadeIn).duration(3000).playOn(binding.textSplash)
    }
}