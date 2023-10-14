package com.surveasy.surveasy.register

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import com.surveasy.surveasy.R
import com.surveasy.surveasy.databinding.ActivityRegisterBinding


import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.surveasy.surveasy.register.agree.RegisterAgree1Fragment
import com.surveasy.surveasy.register.agree.RegisterAgree2Fragment

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    val db = Firebase.firestore
    val registerModel by viewModels<RegisterInfo1ViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val transaction = supportFragmentManager.beginTransaction()
        setContentView(binding.root)
        transaction.add(R.id.fl_register_main, RegisterAgree1Fragment()).commit()


        val registerToolbar: Toolbar? = findViewById(R.id.tb_register)
        setSupportActionBar(binding.tbRegister)
        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)

            supportActionBar?.setDisplayShowTitleEnabled(false)


        }
        binding.tbRegister.setNavigationOnClickListener {
            onBackPressed()
        }

    }

    fun fin() {
        finishAffinity()
    }

    fun toolbarHide() {
        supportActionBar?.hide()
    }

    fun goAgree2() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fl_register_main, RegisterAgree2Fragment())
            .commit()


    }


    fun goToRegister1() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fl_register_main, Register1Fragment())
            .commit()


    }


    fun goToRegister2() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fl_register_main, Register2Fragment())
            .commit()


    }

    fun goToRegisterFin() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fl_register_main, RegisterFinFragment())
            .commit()
    }
}