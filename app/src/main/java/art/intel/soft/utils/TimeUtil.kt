package art.intel.soft.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val TAG = "TimeUtil"
const val STANDART_FORMAT = "yyyy_MM_dd_HHmmss"

fun getCurrentHour(): Int =
        SimpleDateFormat("HH", Locale.getDefault()).format(Date()).toIntOrNull() ?: 0

fun getCurrentMills(): Long = Date().time

fun getTime(format: String = STANDART_FORMAT): String =
        SimpleDateFormat(format, Locale.getDefault()).format(Date())
