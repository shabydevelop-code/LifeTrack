package com.shabydevelop.eattrack

enum class EatingReason(val label: String) {
    HUNGER("רעב"), SWEET_CRAVING("חשק למתוק"), BOREDOM("שעמום"), STRESS("לחץ"),
    FATIGUE("עייפות"), HABIT("הרגל"), TEMPTATION("פיתוי"), SOCIAL("אירוע חברתי"),
    NO_TIME("חוסר זמן"), EMOTION("מצב רגשי"), OTHER("אחר"), UNKNOWN("לא ברור לי"), GOAL("נזכרתי במטרה שלי"), ALTERNATIVE("בחרתי בחלופה בריאה"), DISTRACTION("הסחתי את דעתי"), DISTANCE("התרחקתי מהפיתוי"), WAITED("המתנתי והחשק חלף"), NOT_HUNGRY("הבנתי שאינני רעב")
}
