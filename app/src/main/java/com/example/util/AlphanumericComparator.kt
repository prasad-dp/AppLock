package com.example.util

import com.example.GridAppInfo
import com.example.data.LockedApp

object AlphanumericComparator {

    fun compareNames(name1: String, name2: String): Int {
        val s1 = name1.trim()
        val s2 = name2.trim()

        var i1 = 0
        var i2 = 0
        val len1 = s1.length
        val len2 = s2.length

        while (i1 < len1 && i2 < len2) {
            val c1 = s1[i1]
            val c2 = s2[i2]

            val isDigit1 = c1.isDigit()
            val isDigit2 = c2.isDigit()

            if (isDigit1 && isDigit2) {
                var numStart1 = i1
                while (i1 < len1 && s1[i1].isDigit()) i1++
                val numEnd1 = i1
                while (numStart1 < numEnd1 - 1 && s1[numStart1] == '0') numStart1++

                var numStart2 = i2
                while (i2 < len2 && s2[i2].isDigit()) i2++
                val numEnd2 = i2
                while (numStart2 < numEnd2 - 1 && s2[numStart2] == '0') numStart2++

                val numLen1 = numEnd1 - numStart1
                val numLen2 = numEnd2 - numStart2

                if (numLen1 != numLen2) {
                    return numLen1.compareTo(numLen2)
                }

                for (k in 0 until numLen1) {
                    val nc1 = s1[numStart1 + k]
                    val nc2 = s2[numStart2 + k]
                    if (nc1 != nc2) return nc1.compareTo(nc2)
                }
            } else if (isDigit1 != isDigit2) {
                // Digits sort before letters/symbols
                return if (isDigit1) -1 else 1
            } else {
                val lc1 = c1.lowercaseChar()
                val lc2 = c2.lowercaseChar()
                if (lc1 != lc2) {
                    return lc1.compareTo(lc2)
                }
                i1++
                i2++
            }
        }

        val remaining1 = len1 - i1
        val remaining2 = len2 - i2
        if (remaining1 != remaining2) {
            return remaining1.compareTo(remaining2)
        }

        return s1.compareTo(s2)
    }

    val GRID_APP_COMPARATOR = Comparator<GridAppInfo> { a1, a2 ->
        val comp = compareNames(a1.appName, a2.appName)
        if (comp != 0) comp else a1.packageName.compareTo(a2.packageName, ignoreCase = true)
    }

    val LOCKED_APP_COMPARATOR = Comparator<LockedApp> { a1, a2 ->
        val comp = compareNames(a1.appName, a2.appName)
        if (comp != 0) comp else a1.packageName.compareTo(a2.packageName, ignoreCase = true)
    }
}
