package org.sweetchips.platform.common

class RootUnit(
    val status: Status,
    unit: IUnit
) : IUnit by unit {

    enum class Status {
        NOTCHANGED,
        ADDED,
        CHANGED,
        REMOVED,
    }
}