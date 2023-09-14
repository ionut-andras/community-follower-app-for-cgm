package ionut.andras.community.cgm.follower.alarms

import ionut.andras.community.cgm.follower.R

class DexcomAlarmSoundMap: DexcomAlarmType() {
    companion object{
        val SOUND_MAP = mapOf(
            normalizeValue(URGENT_LOW) to R.raw.glucose_urgent_low,
            normalizeValue(LOW) to R.raw.glucose_low,
            normalizeValue(HIGH) to R.raw.glucose_high,
            normalizeValue(RISING_FAST) to R.raw.glucose_risedrop_fast,
            normalizeValue(DROPPING_FAST) to R.raw.glucose_risedrop_fast,
            normalizeValue(NORMAL) to 0
        )
    }
}