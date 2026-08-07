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
                val numStart1 = i1
                while (i1 < len1 && s1[i1].isDigit()) i1++
                val numStr1 = s1.substring(numStart1, i1)

                val numStart2 = i2
                while (i2 < len2 && s2[i2].isDigit()) i2++
                val numStr2 = s2.substring(numStart2, i2)

                val num1 = numStr1.trimStart('0').ifEmpty { "0" }
                val num2 = numStr2.trimStart('0').ifEmpty { "0" }

                if (num1.length != num2.length) {
                    return num1.length.compareTo(num2.length)
                }
                val numComp = num1.compareTo(num2)
                if (numComp != 0) return numComp
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
