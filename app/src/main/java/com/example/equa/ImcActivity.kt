package com.example.equa

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import com.example.equa.databinding.ActivityImcBinding

class ImcActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImcBinding
    private var ultimoImc: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityImcBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.apply {
            tvBack.setOnClickListener { finish() }

            // actualiza el botón cuando cambia una entrada
            etWeight.addTextChangedListener { actualizarFormulario() }
            etHeight.addTextChangedListener { actualizarFormulario() }

            btnCalculate.setOnClickListener {
                if (validateFields(true)) {
                    val peso = leerNumero(etWeight.text.toString())!!
                    val estatura = leerNumero(etHeight.text.toString())!!
                    val imc = calcularImc(peso, estatura)
                    mostrarResultado(imc)
                }
            }

            btnClear.setOnClickListener {
                etWeight.text.clear()
                etHeight.text.clear()
                etWeight.error = null
                etHeight.error = null
                ultimoImc = null
                cardResult.visibility = View.GONE
                etWeight.requestFocus()
            }
        }
        actualizarFormulario()
    }

    private fun leerNumero(texto: String): Double? {
        // Acepta punto o coma para decimales
        return texto.trim().replace(',', '.').toDoubleOrNull()
    }

    private fun validateFields(mostrarErrores: Boolean): Boolean {
        val pesoTexto = binding.etWeight.text.toString().trim()
        val estaturaTexto = binding.etHeight.text.toString().trim()
        val peso = leerNumero(pesoTexto)
        val estatura = leerNumero(estaturaTexto)

        val errorPeso = when {
            pesoTexto.isEmpty() -> R.string.error_required
            peso == null || !peso.isFinite() -> R.string.error_invalid_number
            peso < 20.0 || peso > 350.0 -> R.string.imc_weight_range_error
            else -> null
        }

        val errorEstatura = when {
            estaturaTexto.isEmpty() -> R.string.error_required
            estatura == null || !estatura.isFinite() -> R.string.error_invalid_number
            estatura < 0.8 || estatura > 2.5 -> R.string.imc_height_range_error
            else -> null
        }

        if (mostrarErrores) {
            binding.etWeight.error = errorPeso?.let { getString(it) }
            binding.etHeight.error = errorEstatura?.let { getString(it) }
        } else {
            binding.etWeight.error = null
            binding.etHeight.error = null
        }

        return errorPeso == null && errorEstatura == null
    }

    private fun actualizarFormulario() {
        ultimoImc = null
        binding.cardResult.visibility = View.GONE

        val hayDatos = binding.etWeight.text.isNotEmpty() || binding.etHeight.text.isNotEmpty()
        binding.btnCalculate.isEnabled = validateFields(hayDatos)
    }

    private fun calcularImc(peso: Double, estatura: Double): Double {
        return peso / (estatura * estatura)
    }

    private fun mostrarResultado(imc: Double) {
        ultimoImc = imc

        val clasificacion = when {
            imc < 18.5 -> R.string.imc_underweight
            imc < 25.0 -> R.string.imc_normal
            imc < 30.0 -> R.string.imc_overweight
            else -> R.string.imc_obesity
        }

        binding.apply {
            tvImcResult.text = getString(R.string.imc_result_with_unit, imc)
            tvClassification.text = getString(
                R.string.result_classification,
                getString(clasificacion)
            )
            cardResult.visibility = View.VISIBLE
        }
    }

    // conservar el resultado
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        ultimoImc?.let { outState.putDouble("imc_guardado", it) }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        if (savedInstanceState.containsKey("imc_guardado") && validateFields(false)) {
            mostrarResultado(savedInstanceState.getDouble("imc_guardado"))
        }
    }
}
