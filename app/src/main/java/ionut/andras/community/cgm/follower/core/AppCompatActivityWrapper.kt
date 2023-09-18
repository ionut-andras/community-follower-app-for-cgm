package ionut.andras.community.cgm.follower.core

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

open class AppCompatActivityWrapper: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Force Night mode before setting the layout
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        super.onCreate(savedInstanceState)
    }
}