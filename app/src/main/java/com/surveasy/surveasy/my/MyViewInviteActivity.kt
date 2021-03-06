package com.surveasy.surveasy.my

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.surveasy.surveasy.databinding.ActivityMyviewinviteBinding

class MyViewInviteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyviewinviteBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMyviewinviteBinding.inflate(layoutInflater)

        setContentView(binding.root)

        setSupportActionBar(binding.ToolbarMyViewInvite)

        if(supportActionBar != null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowTitleEnabled(false)
        }

        binding.ToolbarMyViewInvite.setNavigationOnClickListener {
            onBackPressed()
        }

    }
}