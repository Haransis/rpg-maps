package fr.gradignan.rpgmaps.core.common

import co.touchlab.kermit.Logger
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.roundToInt

fun Float.format(n: Int): String {
    val unit = this.roundToInt()
    val dec = abs(this - unit)
    val decimals = dec.toString().substring(2,min(n+2, dec.toString().length))
    return "$unit.$decimals"
}
