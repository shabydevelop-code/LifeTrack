package com.shabydevelop.eattrack

fun reasonOptions(type: EventType): List<EatingReason> = if (type == EventType.AVOIDED) listOf(EatingReason.GOAL, EatingReason.ALTERNATIVE, EatingReason.DISTRACTION, EatingReason.DISTANCE, EatingReason.WAITED, EatingReason.NOT_HUNGRY, EatingReason.OTHER) else EatingReason.entries.takeWhile { it != EatingReason.GOAL }
