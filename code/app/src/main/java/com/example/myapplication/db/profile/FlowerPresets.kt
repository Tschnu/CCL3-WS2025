package com.example.myapplication.ui.profile

import com.example.myapplication.R

object FlowerPresets {

    val flowers = listOf(
        R.drawable.flower_1,
        R.drawable.flower_2,
        R.drawable.flower_3,
        R.drawable.flower_4,
        R.drawable.flower_5
    )

    fun getFlower(index: Int): Int {
        return flowers.getOrElse(index) { flowers.first() }
    }
}
