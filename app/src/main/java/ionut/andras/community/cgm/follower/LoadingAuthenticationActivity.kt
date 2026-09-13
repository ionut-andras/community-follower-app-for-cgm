package ionut.andras.community.cgm.follower

import android.os.Bundle
import ionut.andras.community.cgm.follower.core.AppCompatActivityWrapper

class LoadingAuthenticationActivity : AppCompatActivityWrapper() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading_authentication)
    }
}
