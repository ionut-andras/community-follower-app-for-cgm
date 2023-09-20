package ionut.andras.community.cgm.follower.core

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import ionut.andras.community.cgm.follower.MainActivity
import ionut.andras.community.cgm.follower.R

open class AppCompatActivityWrapper(private val menuLayoutId: Int? = null): AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Force Night mode before setting the layout
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        super.onCreate(savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (null != menuLayoutId) {
            // Inflate the menu; this adds items to the action bar if it is present.
            menuInflater.inflate(menuLayoutId, menu)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.iconBack -> {
                iconBackOnClick()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun iconBackOnClick() {
        val intent = Intent(applicationContext, MainActivity::class.java)
        startActivity(intent)
    }
}