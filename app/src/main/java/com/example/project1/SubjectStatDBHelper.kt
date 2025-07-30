package com.example.project1

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log


// 데베 연결 클래스
// 데베 테이블 이름 : subject_stats
class SubjectStatDBHelper(context: Context) :
    SQLiteOpenHelper(context, "subject_stats.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE subject_stats (
                name TEXT PRIMARY KEY,
                color INTEGER,
                study_time INTEGER,
                puzzle_count INTEGER
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // 나중에 테이블 구조 변경할 때 사용
        db.execSQL("DROP TABLE IF EXISTS subject_stats")
        onCreate(db)
    }

    fun upsertStat(name: String, color: Int, addStudyTime: Long, addPuzzle: Int) {
        val db = writableDatabase

        // 기존 데이터 조회
        val cursor = db.rawQuery("SELECT * FROM subject_stats WHERE name = ?", arrayOf(name))
        Log.d("DB업데이트", "업데이트 시도 과목 이름: $name")

        if (cursor.moveToFirst()) {
            val prevTime = cursor.getLong(cursor.getColumnIndexOrThrow("study_time"))
            val prevPuzzle = cursor.getInt(cursor.getColumnIndexOrThrow("puzzle_count"))

            val newTime = prevTime + addStudyTime
            val newPuzzle = prevPuzzle + addPuzzle

            Log.d("DB저장", "과목: $name / 기존 퍼즐: $prevPuzzle → 저장 퍼즐: $newPuzzle")

            db.execSQL(
                "UPDATE subject_stats SET study_time = ?, puzzle_count = ?, color = ? WHERE name = ?",
                arrayOf(newTime, newPuzzle, color, name)
            )
        } else {
            db.execSQL(
                "INSERT INTO subject_stats (name, color, study_time, puzzle_count) VALUES (?, ?, ?, ?)",
                arrayOf(name, color, addStudyTime, addPuzzle)
            )
        }

        cursor.close()
    }
    fun getStatByName(name: String): SubjectStat? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM subject_stats WHERE name = ?", arrayOf(name))

        var stat: SubjectStat? = null
        if (cursor.moveToFirst()) {
            stat = SubjectStat(
                name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                color = cursor.getInt(cursor.getColumnIndexOrThrow("color")),
                studyTime = cursor.getLong(cursor.getColumnIndexOrThrow("study_time")),
                puzzleCount = cursor.getInt(cursor.getColumnIndexOrThrow("puzzle_count"))
            )
        }
        cursor.close()
        return stat
    }
    fun getAllStats(): List<SubjectStat> {
        val list = mutableListOf<SubjectStat>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM subject_stats", null)

        while (cursor.moveToNext()) {
            val stat = SubjectStat(
                name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                color = cursor.getInt(cursor.getColumnIndexOrThrow("color")),
                studyTime = cursor.getLong(cursor.getColumnIndexOrThrow("study_time")),
                puzzleCount = cursor.getInt(cursor.getColumnIndexOrThrow("puzzle_count"))
            )
            list.add(stat)
        }

        cursor.close()
        return list
    }

    fun deleteSubjectByName(name: String) {
        val db = writableDatabase
        db.delete("subject_stats", "name = ?", arrayOf(name))
    }
}