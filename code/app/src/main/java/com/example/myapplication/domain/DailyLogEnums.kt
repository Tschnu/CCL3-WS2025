package com.example.myapplication.domain  // or wherever you created it

enum class BloodFlow(val code: Int) {
    LIGHT(1),
    MEDIUM(2),
    HEAVY(3);

    companion object {
        fun fromCode(code: Int): BloodFlow {
            return entries.firstOrNull { it.code == code } ?: LIGHT
        }
    }
}