package com.daffaadityapurwanto.securein

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.daffaadityapurwanto.securein.databinding.ActivityMainBinding
import com.daffaadityapurwanto.securein.databinding.ActivityMenusettingBinding
import com.daffaadityapurwanto.securein.fragment.fragmentcoba

class menusetting : AppCompatActivity() {
    private lateinit var fragmentManager: FragmentManager
    private lateinit var binding: ActivityMenusettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenusettingBinding.inflate(layoutInflater)
//        setContentView(R.layout.activity_menusetting)
        setContentView(binding.root)
        binding.tombolcobasetting.setOnClickListener {
            goToFragment(fragmentcoba())
        }
    }
    private fun goToFragment(fragment: Fragment){
        fragmentManager = supportFragmentManager
        fragmentManager.beginTransaction().replace(R.id.fragmentcontainer, fragment).commit()

    }
}