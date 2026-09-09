package ionut.andras.community.cgm.follower.core

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ionut.andras.community.cgm.follower.LoginActivity
import ionut.andras.community.cgm.follower.MainActivity
import ionut.andras.community.cgm.follower.R

open class AppCompatActivityWrapper(private val menuLayoutId: Int? = null): AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Force Night mode before setting the layout
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        super.onCreate(savedInstanceState)
    }

    /**
     * Since Android 15 (targetSdk 35+) every activity is drawn edge-to-edge, i.e. under the
     * status bar and the navigation bar. None of the layouts handle window insets, so the
     * toolbars ended up under the status bar. Apply the system bar insets once, centrally,
     * to the activity content so every screen (with or without a toolbar) keeps clear of them.
     */
    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        applySystemBarsInsets()
    }

    private fun applySystemBarsInsets() {
        val content = findViewById<View>(android.R.id.content) ?: return

        ViewCompat.setOnApplyWindowInsetsListener(content) { view, windowInsets ->
            val insets = windowInsets.getInsets(
                WindowInsetsCompat.Type.systemBars()
                        or WindowInsetsCompat.Type.displayCutout()
                        or WindowInsetsCompat.Type.ime()
            )
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            WindowInsetsCompat.CONSUMED
        }
        ViewCompat.requestApplyInsets(content)
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
        switchToMainActivity()
    }

    fun switchToMainActivity() {
        val intent = Intent(applicationContext, MainActivity::class.java)
        startActivity(intent)
    }

    fun displayLoginForm(message: String? = null) {
        val intent = Intent(applicationContext, LoginActivity::class.java)
        intent.putExtra(
            getString(R.string.variableNameLoginFormMessage),
            message
        ).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        finish()
        startActivity(intent)
    }
}
