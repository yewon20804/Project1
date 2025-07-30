package com.example.project1

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TimetableAdapter(
    private val subjectList: List<Subject>
) : RecyclerView.Adapter<TimetableAdapter.CellViewHolder>() {

    // 각 셀의 ViewHolder 정의
    class CellViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val subjectName: TextView = itemView.findViewById(R.id.tvSubject)
    }

    // 셀 레이아웃 inflate
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CellViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_timetable_cell, parent, false)
        return CellViewHolder(view)
    }

    // 데이터 바인딩
    override fun onBindViewHolder(holder: CellViewHolder, position: Int) {
        val cell = subjectList[position]

        if (cell.name.isNotEmpty()) {
            holder.subjectName.text = cell.name
            holder.subjectName.setBackgroundColor(cell.color ?: Color.LTGRAY)
            holder.subjectName.setTextColor(Color.BLACK)
        } else {
            holder.subjectName.text = ""
            holder.subjectName.setBackgroundColor(Color.parseColor("#222222"))
        }
    }

    // 총 셀 수
    override fun getItemCount(): Int = subjectList.size
}
