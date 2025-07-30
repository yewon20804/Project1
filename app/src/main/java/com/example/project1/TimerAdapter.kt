package com.example.project1

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// 타이머 연결 어뎁터
class TimerAdapter(
    private val subjects: List<SubjectTimer>,
    // 클릭 콜백 추가
    private val onItemClick: (SubjectTimer) -> Unit
) : RecyclerView.Adapter<TimerAdapter.TimerViewHolder>() {

    inner class TimerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.subjectIcon)
        val name: TextView = itemView.findViewById(R.id.subjectName)
        val time: TextView = itemView.findViewById(R.id.subjectTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_timer, parent, false)
        return TimerViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimerViewHolder, position: Int) {
        val subject = subjects[position]
        holder.icon.setColorFilter(subject.color)
        holder.name.text = subject.name

        // 실시간 누적 시간 가져오기
        val realTimeMillis = TimerDetailActivity.studyTimeMap[subject.name] ?: 0L
        val realTimeInSeconds = (realTimeMillis / 1000).toInt()
        holder.time.text = formatSeconds(realTimeInSeconds)

        // 클릭 시 콜백 전달
        holder.itemView.setOnClickListener {
            onItemClick(subject)
        }
    }

    override fun getItemCount(): Int = subjects.size

    private fun formatSeconds(seconds: Int): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return String.format("%02d:%02d:%02d", h, m, s)
    }
}