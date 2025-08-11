package tech.airobotics.dd_receiver.ui.circle

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import tech.airobotics.dd_receiver.R
import tech.airobotics.dd_receiver.common.CircleWithRadiusView

class CircleFragment : Fragment() {

    private lateinit var circleView: CircleWithRadiusView
    private lateinit var angleInput: EditText
    private lateinit var drawButton: Button
    private lateinit var cleanButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_circle, container, false)

        circleView = view.findViewById(R.id.circleView)
        angleInput = view.findViewById(R.id.angleInput)
        drawButton = view.findViewById(R.id.drawButton)
        cleanButton = view.findViewById(R.id.cleanButton)

        drawButton.setOnClickListener {
            val angle = angleInput.text.toString().toFloatOrNull()
            angle?.let {
                circleView.setAngle(it)
            }
        }

        cleanButton.setOnClickListener {
            circleView.clean()
        }

        return view
    }
}