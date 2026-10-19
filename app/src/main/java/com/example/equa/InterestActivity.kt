package com.example.equa

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import com.example.equa.databinding.ActivityInterestBinding
import kotlin.math.pow

class InterestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInterestBinding
    private var ultimoCapitalFinal: Double? = null
    private var ultimoInteres: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityInterestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.apply {
            tvBack.setOnClickListener { finish() }

            etCapital.addTextChangedListener { actualizarFormulario() }
            etRate.addTextChangedListener { actualizarFormulario() }
            etPeriods.addTextChangedListener { actualizarFormulario() }

            btnCalculate.setOnClickListener {
                if (validarCampos(true)) {
                    val capitalInicial = leerNumero(etCapital.text.toString())!!
                    val tasaPorcentaje = leerNumero(etRate.text.toString())!!
                    val periodos = etPeriods.text.toString().trim().toInt()

                    // El usuario captura porcentaje para que sea más fácil
                    val tasaDecimal = tasaPorcentaje / 100.0

                    val capitalFinal = calcularCapitalFinal(
                        capitalInicial, tasaDecimal, periodos
                    )
                    val interesGanado = calcularInteresGanado(
                        capitalInicial, tasaDecimal, periodos
                    )
                    mostrarResultados(capitalFinal, interesGanado)
                }
            }

            btnClear.setOnClickListener {
                etCapital.text.clear()
                etRate.text.clear()
                etPeriods.text.clear()
                etCapital.error = null
                etRate.error = null
                etPeriods.error = null
                ultimoCapitalFinal = null
                ultimoInteres = null
                cardResult.visibility = View.GONE
                etCapital.requestFocus()
            }
        }

        actualizarFormulario()
    }

    private fun leerNumero(texto: String): Double? {
        return texto.trim().replace(',', '.').toDoubleOrNull()
    }

    private fun validarCampos(mostrarErrores: Boolean): Boolean {
        val textoCapital = binding.etCapital.text.toString().trim()
        val textoTasa = binding.etRate.text.toString().trim()
        val textoPeriodos = binding.etPeriods.text.toString().trim()

        val capital = leerNumero(textoCapital)
        val tasa = leerNumero(textoTasa)
        val periodos = textoPeriodos.toIntOrNull()

        val errorCapital = when {
            textoCapital.isEmpty() -> R.string.error_required
            capital == null || !capital.isFinite() -> R.string.error_invalid_number
            capital <= 0.0 || capital > 1_000_000_000.0 -> R.string.interest_capital_range_error
            else -> null
        }

        val errorTasa = when {
            textoTasa.isEmpty() -> R.string.error_required
            tasa == null || !tasa.isFinite() -> R.string.error_invalid_number
            tasa < 0.0 || tasa > 100.0 -> R.string.error_rate_range
            else -> null
        }

        val errorPeriodos = when {
            textoPeriodos.isEmpty() -> R.string.error_required
            periodos == null -> R.string.error_periods_integer
            periodos < 0 || periodos > 100 -> R.string.interest_periods_range_error
            else -> null
        }

        if (mostrarErrores) {
            binding.etCapital.error = errorCapital?.let { getString(it) }
            binding.etRate.error = errorTasa?.let { getString(it) }
            binding.etPeriods.error = errorPeriodos?.let { getString(it) }
        } else {
            binding.etCapital.error = null
            binding.etRate.error = null
            binding.etPeriods.error = null
        }

        return errorCapital == null && errorTasa == null && errorPeriodos == null
    }

    private fun actualizarFormulario() {
        ultimoCapitalFinal = null
        ultimoInteres = null
        binding.cardResult.visibility = View.GONE

        val hayDatos = binding.etCapital.text.isNotEmpty() ||
                binding.etRate.text.isNotEmpty() ||
                binding.etPeriods.text.isNotEmpty()

        binding.btnCalculate.isEnabled = validarCampos(hayDatos)
    }

    private fun calcularCapitalFinal(
        capitalInicial: Double, tasaDecimal: Double, periodos: Int
    ): Double {
        // Cf = Ci * (1 + i)^n
        return capitalInicial * (1.0 + tasaDecimal).pow(periodos)
    }

    private fun calcularInteresGanado(
        capitalInicial: Double, tasaDecimal: Double, periodos: Int
    ): Double {
        // I = Ci * ((1 + i)^n - 1)
        return capitalInicial * ((1.0 + tasaDecimal).pow(periodos) - 1.0)
    }

    private fun mostrarResultados(capitalFinal: Double, interesGanado: Double) {
        ultimoCapitalFinal = capitalFinal
        ultimoInteres = interesGanado
        binding.apply {
            tvFinalCapitalResult.text = getString(R.string.result_final_capital, capitalFinal)
            tvInterestResult.text = getString(R.string.result_interest, interesGanado)
            cardResult.visibility = View.VISIBLE
        }
    }

    // conservamos ambos resultados
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        ultimoCapitalFinal?.let { outState.putDouble("capital_final_guardado", it) }
        ultimoInteres?.let { outState.putDouble("interes_guardado", it) }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        if (savedInstanceState.containsKey("capital_final_guardado") &&
            savedInstanceState.containsKey("interes_guardado") &&
            validarCampos(false)
        ) {
            mostrarResultados(
                savedInstanceState.getDouble("capital_final_guardado"),
                savedInstanceState.getDouble("interes_guardado")
            )
        }
    }
}
