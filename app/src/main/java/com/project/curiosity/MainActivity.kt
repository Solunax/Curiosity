package com.project.curiosity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.project.curiosity.databinding.MainActivityBinding
import com.project.curiosity.fragmentAdapter.StateAdapter
import com.project.curiosity.model.Request
import com.project.curiosity.viewModel.ViewModel
import java.util.*
import kotlin.Exception
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(){
    private lateinit var binding : MainActivityBinding
    private val deviceNameList = ArrayList<String>()
    private val tabIcon = arrayOf(R.drawable.cam, R.drawable.temp2, R.drawable.gps)
    private lateinit var deviceSpinner:Spinner
    val viewModel : ViewModel by viewModels()

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewPager = binding.viewPager
        val tabLayout = binding.tabLayout
        val addDeviceButton = binding.addDevice
        deviceSpinner = binding.deviceNameSpinner

        val manager = supportFragmentManager
        viewPager.adapter = StateAdapter(manager, lifecycle)

        // 우측 상단 추가 버튼 클릭 시
        addDeviceButton.setOnClickListener {
            val intent = Intent(applicationContext, RoverListActivity::class.java)
            intent.putExtra("deviceList", deviceNameList)
            startActivity(intent)
        }

        // TabLayout, ViewPager 연결
        TabLayoutMediator(tabLayout, viewPager){tab, position ->
//            변경 전 기존 코드(주석)
//            tab.icon = AppCompatResources.getDrawable(applicationContext, tabIcon[position])
            val iconView = layoutInflater.inflate(R.layout.custom_tab_icon, null)
            iconView.findViewById<ImageView>(R.id.tab_icon).setImageResource(tabIcon[position])
            setColors(iconView, false)

            tab.customView = iconView
        }.attach()

        // 탭이 select / unselect 시 상단 TabLayout 의 아이콘 색상 변경, CustomView 라서 별도의 리스너 추가가 필요함
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val view = tab?.customView
                setColors(view, true)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                val view = tab?.customView
                setColors(view, false)

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }
        })

        // 상단의 리스너 설정 이후에 설정해야 의도대로 동작함
        viewPager.currentItem = 1

        // 내부 DB의 device DATA 관측
        viewModel.nameData.observe(this) {
            deviceNameList.clear()
            it.forEach { v ->
                deviceNameList.add(v.deviceID)
            }
            val adapter = ArrayAdapter(applicationContext, R.layout.spinner_item, deviceNameList)
            deviceSpinner.adapter = adapter
        }


        // 장치 ID 변경시 GPS 위치 갱신
        deviceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                getData()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

        // ?초 주기로 서버에서 데이터를 가져옴
        val timer = Timer()
        timer.schedule(object: TimerTask(){
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

    // 서버에서 최신값 정보 가져오기
    private fun getData(){
        val nameData = getSpinnerData()
        if(nameData != "ERROR")
            viewModel.getData(Request(nameData, ""), "latest")
    }

    fun getDataCalendar(timestamp:String){
        val nameData = getSpinnerData()
        if(nameData != "ERROR")
            viewModel.getData(Request(nameData, timestamp), "specific")
    }

    // TabLayout 아이콘 색상 변경 함수
    private fun setColors(view : View?, flag : Boolean){
        val icon: ImageView? = view?.findViewById(R.id.tab_icon)

        when(flag){
            true -> {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    icon?.colorFilter = BlendModeColorFilter(ContextCompat.getColor(applicationContext, R.color.main_yellow), BlendMode.SRC_IN)
                else
                    icon?.setColorFilter(R.color.main_yellow, PorterDuff.Mode.SRC_IN)
            }
            false ->{
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    icon?.colorFilter = BlendModeColorFilter(Color.parseColor("#343434"), BlendMode.SRC_IN)
                else
                    icon?.setColorFilter(Color.parseColor("#343434"), PorterDuff.Mode.SRC_IN)
            }
        }
    }
}