package com.williaaaam.habits.usage

/** A foreground (resumed) or background (paused) transition of some app. */
data class FgEvent(val packageName: String, val foreground: Boolean, val timeMillis: Long)

object UsageMath {
    /**
     * Sums foreground time per package from time-ordered events. An app still in the foreground
     * at the end is counted up to [nowMillis]; a background event with no matching foreground
     * event (the app was already open when the window began) is ignored.
     */
    fun foregroundTotals(events: List<FgEvent>, nowMillis: Long): Map<String, Long> {
        val openSince = HashMap<String, Long>()
        val totals = HashMap<String, Long>()
        for (e in events) {
            if (e.foreground) {
                openSince.putIfAbsent(e.packageName, e.timeMillis)
            } else {
                val start = openSince.remove(e.packageName) ?: continue
                totals.merge(e.packageName, e.timeMillis - start, Long::plus)
            }
        }
        for ((pkg, start) in openSince) {
            totals.merge(pkg, nowMillis - start, Long::plus)
        }
        return totals
    }
}
