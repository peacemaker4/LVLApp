package com.bek.lvlapp.helpers

class LevelCalculator {
    companion object {
        fun Equate(xp: Double): Int {
            return Math.floor(
                xp + 300 * Math.pow(2.0, xp / 7)
            ).toInt()
        }

        fun LevelToXP(level: Int): Int {
            var xp = 0.0
            for (i in 1 until level) xp += Equate(i.toDouble()).toDouble()
            return Math.floor(xp / 4).toInt()
        }

        fun XPToLevel(xp: Int): Int {
            var level = 0
            while (LevelToXP(level) < xp + 1) level++
            return level - 1
        }

        fun XPAmountToNextLevel(xp: Int): Int{
            var curr_level = XPToLevel(xp)
            var next_level_xp = LevelToXP(curr_level+1)
            return next_level_xp - xp
        }

        fun LevelProgress(xp: Int): Int{
            if (xp == 0)
                return 0
            var curr_level = XPToLevel(xp)
            var curr_level_xp = LevelToXP(curr_level)

            return (100/(XPAmountToNextLevel(curr_level_xp).toDouble()/(xp - curr_level_xp))).toInt()
        }
    }

}
