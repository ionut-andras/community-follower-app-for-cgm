package ionut.andras.community.cgm.follower

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
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

        enableActivityListeners()
    }

    private fun enableActivityListeners() {
        val logoutButton = findViewById<Button>(R.id.btnAddEventSave)
        logoutButton.setOnClickListener{
            btnAddEventSaveOnClick()
        }
    }

    private fun btnAddEventSaveOnClick() {
        val timePicker = findViewById<View>(R.id.timePickerEventTime) as TimePicker
        val datePicker = findViewById<View>(R.id.datePickerEventDate) as DatePicker
        val carbsCountEditText = findViewById<EditText>(R.id.editTextCarbsCount)
        val insulinCountEditText = findViewById<EditText>(R.id.editTextInsulinCount)

        val dateTimeObject = DateTimeObject(
            datePicker.year,
            datePicker.month,
            datePicker.dayOfMonth,
            timePicker.hour,
            timePicker.minute,
            0
        )

        val timestamp =  DateTimeConversion(dateTimeObject).getLocalTimestamp()

        val insulin = insulinCountEditText.text.toString().toFloatOrNull()
        val carbs = carbsCountEditText.text.toString().toIntOrNull()

        ToastWrapper(applicationContext).displayMessageToast(findViewById(R.id.btnAddEventSave), "Insulin: $insulin / Carbs: $carbs")

        // Save data locally in the same way dexcom does
        // @TODO

        // Redirect to main view
        // val intent = Intent(applicationContext, MainActivity::class.java)
        // startActivity(intent)
    }
}