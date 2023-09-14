package ionut.andras.community.cgm.follower

import android.os.Bundle
import ionut.andras.community.cgm.follower.core.AppCompatActivityWrapper

class AddNewUserEventActivity : AppCompatActivityWrapper() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_user_event)
    }
}