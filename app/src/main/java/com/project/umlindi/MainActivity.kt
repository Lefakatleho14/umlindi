package com.project.umlindi

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * The only activity. It hosts the navigation graph, and each screen is a
 * fragment (Android Developers, n.d.d).
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}