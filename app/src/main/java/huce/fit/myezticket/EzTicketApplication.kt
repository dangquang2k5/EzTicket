package huce.fit.myezticket

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class EzTicketApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
