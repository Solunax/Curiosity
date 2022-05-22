package com.project.curiosity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.project.curiosity.databinding.SplashBinding

@SuppressLint("CustomSplashScreen")
class SplashActivity:Activity() {
    private lateinit var binding: SplashBinding
    private var handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mainText = binding.title
        val rover = binding.splashRover

        YoYo.with(Techniques.FadeIn).duration(1000).playOn(mainText)
        YoYo.with(Techniques.FadeIn).duration(1000).playOn(rover)

        handler.postDelayed({
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, 1500)

    }
}