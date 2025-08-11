package tech.airobotics.dd_receiver.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import tech.airobotics.dd_receiver.R

class ObjectsAdapter(private val items: MutableList<Pair<Float, Float>>) :
    RecyclerView.Adapter<ObjectsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val angleView: AngleView = view.findViewById(R.id.angleView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_object, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (distance, height) = items[position]
        holder.angleView.setParams(distance, height)
    }

    override fun getItemCount() = items.size

    fun addItem(distance: Float, height: Float) {
        items.add(Pair(distance, height))
        notifyItemInserted(items.size - 1)
    }
}