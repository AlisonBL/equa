package com.example.equa

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.equa.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // inicializamos ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Cada opción del menu abre una Activity distinta
        binding.apply {
            cardImc.setOnClickListener {
                startActivity(Intent(this@MainActivity, ImcActivity::class.java))
            }

            cardOhm.setOnClickListener {
                startActivity(Intent(this@MainActivity, OhmActivity::class.java))
            }

            cardDistance.setOnClickListener {
                startActivity(Intent(this@MainActivity, DistanceActivity::class.java))
            }

            cardInterest.setOnClickListener {
                startActivity(Intent(this@MainActivity, InterestActivity::class.java))
            }
        }
    }
}