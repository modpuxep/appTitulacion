package com.walksafe.app_titulacion.ui

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.Menu
import android.view.View
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.appcompat.app.AppCompatActivity
import com.walksafe.app_titulacion.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

//    private lateinit var appBarConfiguration: AppBarConfiguration

    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_App_Titulacion)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }


    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {

        return super.onCreateView(name, context, attrs)
    }


}