package com.project.curiosity

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.project.curiosity.databinding.MainActivityBinding
import com.project.curiosity.fragment.GpsFragment
import com.project.curiosity.fragmentAdapter.StateAdapter
import com.project.curiosity.room.AppDataBase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.lang.Exception
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(){
    private lateinit var binding : MainActivityBinding
    private lateinit var getResultActivity: ActivityResultLauncher<Intent>
    private lateinit var job:Job
    private val deviceNameList = ArrayList<String>()
    private val tabIcon = arrayOf(R.drawable.cam, R.drawable.temp, R.drawable.gps)
    private lateinit var deviceSpinner:Spinner
    private var fragmentIndex = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewPager = binding.viewPager
        val tabLayout = binding.tabLayout
        val addDeviceButton = binding.addDevice
        deviceSpinner = binding.deviceNameSpinner

        val deviceDB = AppDataBase.getInstance(this)
        getDeviceID(deviceDB!!)

        getResultActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            if(it.resultCode == RESULT_OK)
                getDeviceID(deviceDB)
        }

        val manager = supportFragmentManager
        viewPager.adapter = StateAdapter(manager, lifecycle)
        viewPager.currentItem = 1

        addDeviceButton.setOnClickListener {
            val intent = Intent(applicationContext, RoverListActivity::class.java)
            intent.putExtra("deviceList", deviceNameList)
            getResultActivity.launch(intent)
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                fragmentIndex = tab!!.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })

        TabLayoutMediator(tabLayout, viewPager){tab, position ->
            tab.icon = AppCompatResources.getDrawable(applicationContext, tabIcon[position])
        }.attach()

        deviceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val fragment: GpsFragment? = supportFragmentManager.findFragmentByTag("f2") as GpsFragment?
                fragment?.drawRoute(true)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }
    }

    fun getSpinnerData():String{
        return try{
            deviceSpinner.selectedItem.toString()
        }catch (e:Exception){
            "ERROR"
        }
    }

    fun getFragmentLocation():Int{
        return fragmentIndex
    }

    private fun getDeviceID(db:AppDataBase){
        deviceNameList.clear()
        job = CoroutineScope(Dispatchers.IO).launch {
            val deviceList = db.DeviceDAO().getDeviceData()
            deviceList.forEach {
                deviceNameList.add(it.deviceID)
            }
            val adapter = ArrayAdapter(applicationContext, R.layout.spinner_item, deviceNameList)
            runOnUiThread { deviceSpinner.adapter = adapter }
        }
    }
}