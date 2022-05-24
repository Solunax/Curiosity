package com.project.curiosity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.project.curiosity.databinding.MainActivityBinding
import com.project.curiosity.fragmentAdapter.StateAdapter
import com.project.curiosity.room.AppDataBase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*

class MainActivity : AppCompatActivity(){
    private lateinit var binding : MainActivityBinding
    private lateinit var getResultActivity: ActivityResultLauncher<Intent>
    private lateinit var job:Job
    private val deviceNameList = LinkedList<String>()
    private val tabIcon = arrayOf(R.drawable.cam, R.drawable.temp, R.drawable.gps)
    private lateinit var deviceSpinner:Spinner

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

        viewPager.adapter = StateAdapter(supportFragmentManager, lifecycle)
        viewPager.currentItem = 1

        addDeviceButton.setOnClickListener {
            val intent = Intent(applicationContext, AddDeviceActivity::class.java)
            intent.putExtra("deviceList", deviceNameList)
            getResultActivity.launch(intent)
        }


        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }
        })

        TabLayoutMediator(tabLayout, viewPager){tab, position ->
            tab.icon = AppCompatResources.getDrawable(applicationContext, tabIcon[position])
        }.attach()
    }

    fun getSpinnerData():String{
        return deviceSpinner.selectedItem.toString()
    }

    private fun getDeviceID(db:AppDataBase){
        deviceNameList.clear()
        job = CoroutineScope(Dispatchers.IO).launch {
            val deviceList = db.DeviceDAO().getDeviceData()

            deviceList.forEach {
                Log.d("DD", it.deviceID)
                deviceNameList.add(it.deviceID)
            }
            val adapter = ArrayAdapter(applicationContext, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, deviceNameList)
            runOnUiThread { deviceSpinner.adapter = adapter }
        }
    }
}