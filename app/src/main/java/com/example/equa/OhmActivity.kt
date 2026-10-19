package com.example.equa

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import com.example.equa.databinding.ActivityOhmBinding

class OhmActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOhmBinding
    private var ultimaCorriente: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityOhmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.apply {
            tvBack.setOnClickListener { finish() }

            etVoltage.addTextChangedListener { actualizarFormulario() }
            etResistance.addTextChangedListener { actualizarFormulario() }

            btnCalculate.setOnClickListener {
                if (validarCampos(true)) {
                    val voltaje = leerNumero(etVoltage.text.toString())!!
                    val resistencia = leerNumero(etResistance.text.toString())!!
                    val corriente = calcularCorriente(voltaje, resistencia)
                    mostrarResultado(corriente)
                }
            }

            btnClear.setOnClickListener {
                etVoltage.text.clear()
                etResistance.text.clear()
                etVoltage.error = null
                etResistance.error = null
                ultimaCorriente = null
                cardResult.visibility = View.GONE
                etVoltage.requestFocus()
            }
        }

        actualizarFormulario()
    }

    private fun leerNumero(texto: String): Double? {
        // Admitimos coma o punto decimal, como en el formulario de IMC
        return texto.trim().replace(',', '.').toDoubleOrNull()
    }

    private fun validarCampos(mostrarErrores: Boolean): Boolean {
        val textoVoltaje = binding.etVoltage.text.toString().trim()
        val textoResistencia = binding.etResistance.text.toString().trim()
        val voltaje = leerNumero(textoVoltaje)
        val resistencia = leerNumero(textoResistencia)

        val errorVoltaje = when {
            textoVoltaje.isEmpty() -> R.string.error_required
            voltaje == null || !voltaje.isFinite() -> R.string.error_invalid_number
            voltaje < 0.0 || voltaje > 1000.0 -> R.string.ohm_voltage_range_error
            else -> null
        }

        val errorResistencia = when {
            textoResistencia.isEmpty() -> R.string.error_required
            resistencia == null || !resistencia.isFinite() -> R.string.error_invalid_number
            resistencia < 0.1 || resistencia > 100000.0 -> R.string.ohm_resistance_range_error
            else -> null
        }

        if (mostrarErrores) {
            binding.etVoltage.error = errorVoltaje?.let { getString(it) }
            binding.etResistance.error = errorResistencia?.let { getString(it) }
        } else {
            binding.etVoltage.error = null
            binding.etResistance.error = null
        }

        return errorVoltaje == null && errorResistencia == null
    }

    private fun actualizarFormulario() {
        ultimaCorriente = null
        binding.cardResult.visibility = View.GONE

        val hayDatos = binding.etVoltage.text.isNotEmpty() ||
                binding.etResistance.text.isNotEmpty()
        binding.btnCalculate.isEnabled = validarCampos(hayDatos)
    }

    private fun calcularCorriente(voltaje: Double, resistencia: Double): Double {
        return voltaje / resistencia
    }

    private fun mostrarResultado(corriente: Double) {
        ultimaCorriente = corriente
        binding.apply {
            tvCurrentResult.text = getString(R.string.result_current, corriente)
            cardResult.visibility = View.VISIBLE
        }
    }

    // Conservamos el resultado
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        ultimaCorriente?.let { outState.putDouble("corriente_guardada", it) }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        if (savedInstanceState.containsKey("corriente_guardada") && validarCampos(false)) {
            mostrarResultado(savedInstanceState.getDouble("corriente_guardada"))
        }
    }
}
