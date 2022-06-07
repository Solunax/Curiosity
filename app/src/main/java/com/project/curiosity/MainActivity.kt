package com.project.curiosity

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.content.res.AppCompatResources
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.project.curiosity.api.ApiClient
import com.project.curiosity.databinding.MainActivityBinding
import com.project.curiosity.fragment.GpsFragment
import com.project.curiosity.fragment.GraphFragment
import com.project.curiosity.fragmentAdapter.StateAdapter
import com.project.curiosity.model.Body
import com.project.curiosity.model.Request
import com.project.curiosity.model.Request2
import com.project.curiosity.room.AppDataBase
import com.project.curiosity.yongapi.ApiClient1
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*
import kotlin.Exception
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(){
    private lateinit var binding : MainActivityBinding
    private lateinit var getResultActivity: ActivityResultLauncher<Intent>
    private lateinit var job:Job
    private val deviceNameList = ArrayList<String>()
    private val tabIcon = arrayOf(R.drawable.cam, R.drawable.temp, R.drawable.gps)
    private lateinit var deviceSpinner:Spinner
    private var recentBody:Body? = null
    private var fragmentIndex = 1
    var changeGpsFlag = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewPager = binding.viewPager
        val tabLayout = binding.tabLayout
        val addDeviceButton = binding.addDevice
        deviceSpinner = binding.deviceNameSpinner

        // 내부 DB Instance 생성
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

        TabLayoutMediator(tabLayout, viewPager){tab, position ->
            tab.icon = AppCompatResources.getDrawable(applicationContext, tabIcon[position])
        }.attach()

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onTabSelected(tab: TabLayout.Tab?) {
                fragmentIndex = tab!!.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })

        // 장치 ID 변경시 GPS 위치 갱신
        deviceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                try{
                    changeGpsFlag = true
                    if(fragmentIndex == 1){
                        val graphFragment = supportFragmentManager.findFragmentByTag("f1") as GraphFragment
                        changeDevice(graphFragment)
                    }
                    else if(fragmentIndex == 2){
                        val gpsFragment = supportFragmentManager.findFragmentByTag("f2") as GpsFragment
                        changeDevice(gpsFragment)
                    }
                }catch (e:Exception){
                    e.printStackTrace()
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }
        // 10초 주기로 서버에서 데이터를 가져옴
        val timer = Timer()
        timer.schedule(object: TimerTask(){
            @RequiresApi(Build.VERSION_CODES.O)
            override fun run() {
                getData()
            }
        }, 100, 5000)
    }

    // 상단 장치 ID 스피너 값 가져오기
    fun getSpinnerData():String{
        return try{
            deviceSpinner.selectedItem.toString()
        }catch (e:Exception){
            "ERROR"
        }
    }

    // 내부 DB의 장치 ID 가져오기
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

    // 서버에서 최신값 하나만 정보 가져오기
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getData() {
        job = CoroutineScope(Dispatchers.IO).launch {
            val request = Request2(getSpinnerData(), "")
            val response = ApiClient1.getApiClient1().getData1(request)
            if(response.isSuccessful && response.body()!!.statusCode == 200){
                recentBody = response.body()!!.body[0]
                if(fragmentIndex == 1){
                    val graphFragment = supportFragmentManager.findFragmentByTag("f1") as GraphFragment
                    graphFragment.setGraph(recentBody!!)
                }else if(fragmentIndex == 2){
                    val gpsFragment = supportFragmentManager.findFragmentByTag("f2") as GpsFragment
                    gpsFragment.drawRoute(false, recentBody!!)
                }
            }
        }
    }

    // 장치 변경시 GPS 갱신
    private fun changeDevice(fragment:GpsFragment) {
        job = CoroutineScope(Dispatchers.IO).launch {
            val request = Request2(getSpinnerData(), "")
            val response = ApiClient1.getApiClient1().getData1(request)
            if(response.isSuccessful && response.body()!!.statusCode == 200){
                recentBody = response.body()!!.body[0]
                fragment.drawRoute(true, recentBody!!)
            }
        }
    }

    // 장치 변경시 그래프 갱신
    @RequiresApi(Build.VERSION_CODES.O)
    private fun changeDevice(fragment:GraphFragment) {
        job = CoroutineScope(Dispatchers.IO).launch {
            val request = Request2(getSpinnerData(), "")
            val response = ApiClient1.getApiClient1().getData1(request)
            if(response.isSuccessful && response.body()!!.statusCode == 200){
                recentBody = response.body()!!.body[0]
                fragment.setGraph2(recentBody!!)
            }
        }
    }
}