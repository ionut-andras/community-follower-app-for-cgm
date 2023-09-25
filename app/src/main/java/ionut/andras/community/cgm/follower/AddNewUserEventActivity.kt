package ionut.andras.community.cgm.follower

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.DatePicker
import android.widget.TimePicker
import ionut.andras.community.cgm.follower.core.AppCompatActivityWrapper
import ionut.andras.community.cgm.follower.toast.ToastWrapper
import ionut.andras.community.cgm.follower.utils.DateTimeConversion
import ionut.andras.community.cgm.follower.utils.DateTimeObject


class AddNewUserEventActivity : AppCompatActivityWrapper(R.menu.add_new_user_event_menu) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_user_event)

        // Set Action bar
        setSupportActionBar(findViewById(R.id.addUserEventActivityActionToolbar))

        enableAddEventActivityListeners()
    }

    private fun enableAddEventActivityListeners() {
        val logoutButton = findViewById<Button>(R.id.btnAddEventSave)
        logoutButton.setOnClickListener{
            btnAddEventSaveOnClick()
        }
    }

    private fun btnAddEventSaveOnClick() {
        // Save data locally
        // @TODO

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        val timePicker = findViewById<View>(R.id.timePickerEventTime) as TimePicker
        val datePicker = findViewById<View>(R.id.datePickerEventDate) as DatePicker

        val dateTimeObject = DateTimeObject(
            datePicker.year,
            datePicker.month,
            datePicker.dayOfMonth,
            timePicker.hour,
            timePicker.minute,
            0
        )

        val timestamp =  DateTimeConversion(dateTimeObject).getLocalTimestamp()

        ToastWrapper(applicationContext).displayMessageToast(findViewById(R.id.btnAddEventSave), "${dateTimeObject.toString()} = $timestamp")
        /////////////////////////////////////////////////////////////////////////////////////////////////////////// */

        // Redirect to main view
        // val intent = Intent(applicationContext, MainActivity::class.java)
        // startActivity(intent)
    }
}