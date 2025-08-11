package tech.airobotics.dd_receiver.ui.circle

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import tech.airobotics.dd_receiver.R
import tech.airobotics.dd_receiver.common.CircleWithRadiusView
import tech.airobotics.dd_receiver.common.ObjectsAdapter

class CircleFragment : Fragment() {

    private lateinit var adapter: ObjectsAdapter
    private lateinit var objectsList: RecyclerView
    private lateinit var azimuthInput: EditText
    private lateinit var distanceInput: EditText
    private lateinit var heightInput: EditText
    private lateinit var azimuthView: CircleWithRadiusView
    private lateinit var sendButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_circle, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        objectsList = view.findViewById(R.id.objectsList)
        azimuthInput = view.findViewById(R.id.azimuthInput)
        distanceInput = view.findViewById(R.id.distanceInput)
        heightInput = view.findViewById(R.id.heightInput)
        azimuthView = view.findViewById(R.id.azimuthView)
        sendButton = view.findViewById(R.id.sendButton)
        adapter = ObjectsAdapter(mutableListOf())
        objectsList.layoutManager = LinearLayoutManager(requireContext())
        objectsList.adapter = adapter

        // Обработка кнопки
        sendButton.setOnClickListener {
            val azimuth = azimuthInput.text.toString().toFloatOrNull() ?: 0f
            val distance = distanceInput.text.toString().toFloatOrNull() ?: 0f
            val height = heightInput.text.toString().toFloatOrNull() ?: 0f

            azimuthView.setAngle(azimuth)
            adapter.addItem(distance, height)

            // Очистка полей ввода
            distanceInput.text.clear()
            heightInput.text.clear()
        }
    }
}