package muz.all

import android.content.Context
import androidx.multidex.MultiDex
import com.google.android.gms.security.ProviderInstaller
import dagger.android.DaggerApplication
import dagger.android.HasAndroidInjector
import muz.all.component.DaggerAppComponent
import javax.net.ssl.SSLContext

class App : DaggerApplication(), HasAndroidInjector {

    override fun onCreate() {
        super.onCreate()
//        try {
//            // Google Play will install latest OpenSSL
//            ProviderInstaller.installIfNeeded(applicationContext);
//            val sslContext = SSLContext.getInstance("TLSv1.2");
//            sslContext.init(null, null, null);
//            sslContext.createSSLEngine();
//        } catch (e: Exception) {
//            e.printStackTrace();
//        }
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun applicationInjector() = DaggerAppComponent.factory().create(this)
}