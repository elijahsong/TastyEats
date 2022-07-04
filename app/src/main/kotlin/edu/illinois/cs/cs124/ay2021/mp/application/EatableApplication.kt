package edu.illinois.cs.cs124.ay2021.mp.application

import android.app.Application
import android.os.Build
import edu.illinois.cs.cs124.ay2021.mp.network.Server

/*
 * One instance of the Application class is created when the app is launched and persists throughout its lifetime.
 * This is unlike activities, which are created and destroyed as the user navigates to different screens in the app.
 * As a result, the Application class is a good place to store constants and initialize things that are potentially
 * needed by multiple activities.
 *
 * You may not need to change the code in this file, but definitely not until MP3.
 */

class EatableApplication : Application() {

    /*
     * onCreate is called when the instance of the Application class is created.
     * We use it to initialize any state that the Application class should store.
     * For this app we also use it as an opportunity to start both our restaurant API server.
     */
    override fun onCreate() {
        super.onCreate()
        if (Build.FINGERPRINT == "robolectric") {
            Server.start()
        } else {
            Thread { Server.start() }.start()
        }
    }

    companion object {
        // Default port for the restaurant API server
        // You may modify this if needed to work around a conflict with another server running on your machine
        const val DEFAULT_SERVER_PORT = 8989

        // Default server URL
        // You should not need to modify this
        const val SERVER_URL = "http://localhost:$DEFAULT_SERVER_PORT/"
    }
}
