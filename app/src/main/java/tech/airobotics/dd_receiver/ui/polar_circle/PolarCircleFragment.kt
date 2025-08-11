package tech.airobotics.dd_receiver.ui.polar_circle

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import tech.airobotics.dd_receiver.R
import tech.airobotics.dd_receiver.common.PolarCircleView

class PolarCircleFragment : Fragment(R.layout.fragment_polar_circle) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val polarView = view.findViewById<PolarCircleView>(R.id.polarView)
        val angleInput = view.findViewById<EditText>(R.id.angleInput)
        val btnAdd = view.findViewById<Button>(R.id.btnAdd)
        val btnClean = view.findViewById<Button>(R.id.btnClean)

        // Опционально: цвета
        // polarView.setCircleColor(Color.GRAY)
        // polarView.setRadiusColor(Color.DKGRAY)
        // polarView.setLastRadiusColor(Color.BLACK)

        btnAdd.setOnClickListener {
            val text = angleInput.text?.toString()?.trim().orEmpty()
            val angle = text.toFloatOrNull()
            if (angle != null) {
                polarView.addRadius(angle)
                angleInput.text?.clear()
            } else {
                angleInput.error = "Введите число"
            }
        }

        btnClean.setOnClickListener {
            polarView.clean()
        }
    }
}
