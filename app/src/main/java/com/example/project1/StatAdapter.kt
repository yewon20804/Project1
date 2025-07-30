package com.example.project1

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// 통계 전달 클래스
class StatAdapter(private val statList: List<SubjectStat>) :
    RecyclerView.Adapter<StatAdapter.StatViewHolder>() {

    class StatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val subjectName: TextView = itemView.findViewById(R.id.tvSubjectName)
        val puzzleIcon: ImageView = itemView.findViewById(R.id.ivPuzzleIcon)
        val puzzleCount: TextView = itemView.findViewById(R.id.tvPuzzleCount)
        val studyTime: TextView = itemView.findViewById(R.id.tvStudyTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_stat, parent, false)
        return StatViewHolder(view)
    }

    override fun onBindViewHolder(holder: StatViewHolder, position: Int) {
        val item = statList[position]
        holder.subjectName.text = item.name
        holder.puzzleCount.text = "${item.puzzleCount}개"
        holder.studyTime.text = formatTime(item.studyTime)

        // 퍼즐 아이콘 색상을 과목 색상으로
        holder.puzzleIcon.setColorFilter(item.color)
    }

    override fun getItemCount(): Int = statList.size

    // 초 포맷
    private fun formatTime(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, secs)
    }
}