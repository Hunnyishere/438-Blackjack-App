package com.example.cse438.cse438_assignment4

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.cse438.cse438_assignment4.fragments.LoginFragment
import com.example.cse438.cse438_assignment4.fragments.RegisterFragment
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var fragmentAdapter: MyPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        fragmentAdapter = MyPagerAdapter(supportFragmentManager)
        viewPager.adapter=fragmentAdapter
        tab_main.setupWithViewPager(viewPager)
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null){
            val bundle = Bundle()
            bundle.putString("username", currentUser?.email)
            intent.putExtras(bundle)
            startActivity(intent)
        }
    }

    class MyPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getCount() : Int {
            return 2
        }

        override fun getItem(position: Int) : Fragment {
            return when (position) {
                0 -> {
                    LoginFragment()
                }
                else ->
                    RegisterFragment()
            }
        }

        override fun getPageTitle(position: Int): CharSequence {
            return when (position) {
                0 -> "Login"
                else ->"Sign Up"
            }
        }
    }

}
