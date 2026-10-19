package com.example.equa

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import com.example.equa.databinding.ActivityDistanceBinding

class DistanceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDistanceBinding
    private var ultimaDistancia: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityDistanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.apply {
            tvBack.setOnClickListener { finish() }

            // revisar los campos cada vez que se escribe
            etSpeed.addTextChangedListener { actualizarFormulario() }
            etTime.addTextChangedListener { actualizarFormulario() }

            btnCalculate.setOnClickListener {
                if (validarCampos(true)) {
                    val velocidad = leerNumero(etSpeed.text.toString())!!
                    val tiempo = leerNumero(etTime.text.toString())!!
                    val distancia = calcularDistancia(velocidad, tiempo)
                    mostrarResultado(distancia)
                }
            }

            btnClear.setOnClickListener {
                etSpeed.text.clear()
                etTime.text.clear()
                etSpeed.error = null
                etTime.error = null
                ultimaDistancia = null
                cardResult.visibility = View.GONE
                etSpeed.requestFocus()
            }
        }

        actualizarFormulario()
    }

    private fun leerNumero(texto: String): Double? {
        //  punto o coma
        return texto.trim().replace(',', '.').toDoubleOrNull()
    }

    private fun validarCampos(mostrarErrores: Boolean): Boolean {
        val textoVelocidad = binding.etSpeed.text.toString().trim()
        val textoTiempo = binding.etTime.text.toString().trim()
        val velocidad = leerNumero(textoVelocidad)
        val tiempo = leerNumero(textoTiempo)

        val errorVelocidad = when {
            textoVelocidad.isEmpty() -> R.string.error_required
            velocidad == null || !velocidad.isFinite() -> R.string.error_invalid_number
            velocidad < 0.0 || velocidad > 1000.0 -> R.string.distance_speed_range_error
            else -> null
        }

        val errorTiempo = when {
            textoTiempo.isEmpty() -> R.string.error_required
            tiempo == null || !tiempo.isFinite() -> R.string.error_invalid_number
            tiempo < 0.0 || tiempo > 86400.0 -> R.string.distance_time_range_error
            else -> null
        }

        if (mostrarErrores) {
            binding.etSpeed.error = errorVelocidad?.let { getString(it) }
            binding.etTime.error = errorTiempo?.let { getString(it) }
        } else {
            binding.etSpeed.error = null
            binding.etTime.error = null
        }

        return errorVelocidad == null && errorTiempo == null
    }

    private fun actualizarFormulario() {
        ultimaDistancia = null
        binding.cardResult.visibility = View.GONE

        val hayDatos = binding.etSpeed.text.isNotEmpty() ||
                binding.etTime.text.isNotEmpty()
        binding.btnCalculate.isEnabled = validarCampos(hayDatos)
    }

    private fun calcularDistancia(velocidad: Double, tiempo: Double): Double {
        return velocidad * tiempo
    }

    private fun mostrarResultado(distancia: Double) {
        ultimaDistancia = distancia
        binding.apply {
            tvDistanceResult.text = getString(R.string.result_distance, distancia)
            cardResult.visibility = View.VISIBLE
        }
    }

    // Conservamos el resultado
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        ultimaDistancia?.let { outState.putDouble("distancia_guardada", it) }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        if (savedInstanceState.containsKey("distancia_guardada") && validarCampos(false)) {
            mostrarResultado(savedInstanceState.getDouble("distancia_guardada"))
        }
    }
}
