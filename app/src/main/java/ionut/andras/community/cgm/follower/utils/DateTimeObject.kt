package ionut.andras.community.cgm.follower.utils

class DateTimeObject (year: Int = 1979, month: Int = 0, day: Int = 1, hour: Int = 0, minute: Int = 0, second: Int = 0) {

    var second: Int
    var minute: Int
    var hour: Int
    var day: Int
    var month: Int
    var year: Int

    init {
        // Rewrite companion variables
        this.year = year
        this.month = month
        this.day = day
        this.hour = hour
        this.minute = minute
        this.second = second
    }

    override fun toString(): String {
        return String.format("{hour: %02d, minute: %02d, second: %02d, year: %04d, month: %02d, day: %02d}", hour, minute, second, year, month, day)
    }
}