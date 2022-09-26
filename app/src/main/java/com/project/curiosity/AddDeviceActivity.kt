package com.project.curiosity

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.project.curiosity.databinding.AddDeviceBinding
import com.project.curiosity.room.Device
import com.project.curiosity.viewModel.ViewModel

class AddDeviceActivity:AppCompatActivity() {
    private lateinit var binding : AddDeviceBinding
    private val viewModel : ViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AddDeviceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val deviceName = binding.deviceName
        val addButton = binding.addDevice

        val deviceNameList = intent.getStringArrayListExtra("deviceList")
        Log.d("DATA", deviceNameList.toString())

        addButton.setOnClickListener {
            // 이름란이 비어있나 확인
            if(deviceName.text.isNotBlank()){
                // 이미 있는 이름인지 확인
                if(!deviceNameList!!.contains(deviceName.text.toString())){
                    viewModel.insertDeviceData(Device(deviceName.text.toString()))
                    finish()
                }else{
                    Toast.makeText(applicationContext, "이미 등록된 이름입니다.", Toast.LENGTH_SHORT).show()
                    deviceName.requestFocus()
                }
            }else{
                Toast.makeText(applicationContext, "장비 이름을 입력하세요", Toast.LENGTH_SHORT).show()
                deviceName.requestFocus()
            }
        }
    }
}