package com.bek.lvlapp.helpers

import com.bek.lvlapp.R
import com.bek.lvlapp.models.Icon

class IconsManager {



    companion object {
        private var iconList: ArrayList<Icon> = arrayListOf<Icon>(
            Icon(R.drawable.ic_icon_etc, "Interests"),
            Icon(R.drawable.ic_icon_rocket, "Rocket"),
            Icon(R.drawable.ic_icon_meditation, "Meditation"),
            Icon(R.drawable.ic_icon_code, "Code"),
            Icon(R.drawable.ic_icon_run, "Run"),
            Icon(R.drawable.ic_icon_palette, "Art"),
            Icon(R.drawable.ic_icon_music, "Music"),
            Icon(R.drawable.ic_icon_psychology, "Psychology"),
            Icon(R.drawable.ic_icon_open_book, "Book"),
            Icon(R.drawable.ic_icon_dumbbell, "Fitness"),
            Icon(R.drawable.ic_icon_sleep, "Sleep"),
            Icon(R.drawable.ic_icon_star, "Star"),
            Icon(R.drawable.ic_icon_time, "Time"),
            Icon(R.drawable.ic_icon_education, "Education"),
            Icon(R.drawable.ic_icon_cook, "Cook"),
            Icon(R.drawable.ic_icon_money, "Money"),
            Icon(R.drawable.ic_icon_api, "Control"),
            Icon(R.drawable.ic_icon_fire, "Fire"),
            Icon(R.drawable.ic_icon_movie, "Movie"),
            Icon(R.drawable.ic_icon_love, "Love"),
            Icon(R.drawable.ic_icon_thumbs_up, "Thumbs up"),
            Icon(R.drawable.ic_icon_flash, "Flash"),
            Icon(R.drawable.ic_icon_science, "Science"),
            Icon(R.drawable.ic_icon_screentone, "Screen tone"),
            Icon(R.drawable.ic_icon_savings, "Savings"),
            Icon(R.drawable.ic_icon_gamepad, "Gamepad"),
            Icon(R.drawable.ic_icon_sport_ball, "Sport"),
        )

        fun GetAllIcons(): ArrayList<Icon> {
            return iconList
        }
    }
}