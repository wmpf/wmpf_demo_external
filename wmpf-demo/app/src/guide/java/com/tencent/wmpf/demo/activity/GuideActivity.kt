package com.tencent.wmpf.demo.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import com.tencent.wmpf.demo.R
import com.tencent.wmpf.demo.ui.DocumentActivity
import com.tencent.wmpf.demo.ui.FastExperienceActivity
import com.tencent.wmpf.demo.ui.DetailActivity
import com.tencent.wmpf.demo.ui.QAActivity

class GuideActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_guide)

        findViewById<Button>(R.id.btn_fast_experience).setOnClickListener {
            startActivity(Intent(this, FastExperienceActivity::class.java))
        }

        findViewById<Button>(R.id.btn_detail).setOnClickListener {
            startActivity(Intent(this, DetailActivity::class.java))
        }

        findViewById<Button>(R.id.btn_document).setOnClickListener {
            startActivity(Intent(this, DocumentActivity::class.java))
        }

        findViewById<Button>(R.id.btn_qa).setOnClickListener {
            startActivity(Intent(this, QAActivity::class.java))
        }

        findViewById<Button>(R.id.btn_music).setOnClickListener {
            startActivity(Intent(this, WMPFMusicControllerActivity::class.java))
        }

        findViewById<Button>(R.id.btn_new_api).setOnClickListener {
            startActivity(Intent(this, WMPFApiActivity::class.java))
        }
    }

}