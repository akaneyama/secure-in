package com.daffaadityapurwanto.securein

import android.os.Bundle
import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.daffaadityapurwanto.securein.databinding.ActivityMainDashboardBinding
import com.daffaadityapurwanto.securein.fragmentdashboard.BackuprestoreFragment
import com.daffaadityapurwanto.securein.fragmentdashboard.DashboardFragment
import com.daffaadityapurwanto.securein.fragmentdashboard.SettingFragment
import com.daffaadityapurwanto.securein.fragmentdashboard.mypasswordFragment


class MainDashboard : AppCompatActivity() {
    enum class FragmentType {
        DASHBOARD,
        MYPASSWORD,
        BACKUP,
        SETTING
    }

    private var currentFragmentType = FragmentType.DASHBOARD
    private lateinit var binding: ActivityMainDashboardBinding
    private lateinit var viewPager: ViewPager2
    private var currentFragment: Fragment? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainDashboardBinding.inflate(layoutInflater)
        //setContentView(R.layout.activity_main_dashboard)
        setContentView(binding.root)
        viewPager = binding.fragmentContainerView
        binding.tombolhome.background = ContextCompat.getDrawable(this, R.drawable.bulat)
        val fragmentList = listOf(
            DashboardFragment(),
            mypasswordFragment(),
            BackuprestoreFragment(),
            SettingFragment()
        )
        viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = fragmentList.size
            override fun createFragment(position: Int): Fragment = fragmentList[position]

        }
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                // Bersihkan semua background dulu
                bersihintombol()

                // berdasar posisi
                when (position) {
                    0 -> {
                        binding.tombolhome.background = ContextCompat.getDrawable(this@MainDashboard, R.drawable.bulat)
                        binding.buattambahindata.visibility = View.VISIBLE
                    }
                    1 -> {
                        binding.tombolmypassword.background = ContextCompat.getDrawable(this@MainDashboard, R.drawable.bulat)
                        binding.buattambahindata.visibility = View.VISIBLE
                    }
                    2 -> {
                        binding.tombolbackup.background = ContextCompat.getDrawable(this@MainDashboard, R.drawable.bulat)
                        binding.buattambahindata.visibility = View.GONE
                    }
                    3 -> {
                        binding.tombolsetting.background = ContextCompat.getDrawable(this@MainDashboard, R.drawable.bulat)
                        binding.buattambahindata.visibility = View.GONE
                    }

                }
            }
        })
        binding.tombolhome.setOnClickListener {
            viewPager.setCurrentItem(0, true)
            bersihintombol()
            binding.tombolhome.background = ContextCompat.getDrawable(this, R.drawable.bulat)
        }

        binding.tombolmypassword.setOnClickListener {
            viewPager.setCurrentItem(1, true)
            bersihintombol()
            binding.tombolmypassword.background = ContextCompat.getDrawable(this, R.drawable.bulat)
        }

        binding.tombolbackup.setOnClickListener {
            bersihintombol()
            viewPager.setCurrentItem(2, true)
            binding.tombolbackup.background = ContextCompat.getDrawable(this, R.drawable.bulat)
        }

        binding.tombolsetting.setOnClickListener {
            bersihintombol()
            viewPager.setCurrentItem(3, true)
            binding.tombolsetting.background = ContextCompat.getDrawable(this, R.drawable.bulat)
        }


    }
    private fun bersihintombol() {
        binding.tombolhome.background = null
        binding.tombolmypassword.background = null
        binding.tombolbackup.background = null
        binding.tombolsetting.background = null
    }
    //Toast.makeText(this, "Menu Setting belum tersedia", Toast.LENGTH_SHORT).show()
//    private fun goToFrgmentActivity(targetFragment: Fragment, targetType: FragmentType) {
//        val transaction = supportFragmentManager.beginTransaction()
//
//        val enterAnim: Int
//        val exitAnim: Int
//        val popEnterAnim: Int
//        val popExitAnim: Int
//
//        if (targetType.ordinal > currentFragmentType.ordinal) {
//            // ngeser nganan uruan
//            enterAnim = R.anim.slide_in_right
//            exitAnim = R.anim.slide_out_left
//            popEnterAnim = R.anim.slide_in_left
//            popExitAnim = R.anim.slide_out_right
//        } else if (targetType.ordinal < currentFragmentType.ordinal) {
//            // ngeser nang kiri mbek urutann
//            enterAnim = R.anim.slide_in_left
//            exitAnim = R.anim.slide_out_right
//            popEnterAnim = R.anim.slide_in_right
//            popExitAnim = R.anim.slide_out_left
//        } else {
//            // Tidak berubah
//            return
//        }
//
//        transaction.setCustomAnimations(enterAnim, exitAnim, popEnterAnim, popExitAnim)
//            .replace(R.id.fragmentContainerView, targetFragment)
//            .commit()
//
//        currentFragmentType = targetType
//    }


}