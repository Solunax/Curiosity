package com.project.curiosity

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.project.curiosity.databinding.AddDeviceBinding
import com.project.curiosity.room.AppDataBase
import com.project.curiosity.room.Device
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class AddDeviceActivity:Activity() {
    private lateinit var binding : AddDeviceBinding
    private lateinit var job:Job

    override fun onDestroy() {
        super.onDestroy()
        if(::job.isInitialized)
            job.cancel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AddDeviceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val deviceName = binding.deviceName
        val addButton = binding.addDevice

        val deviceNameList = intent.getStringArrayListExtra("deviceList")
        Log.d("DATA", deviceNameList.toString())

        addButton.setOnClickListener {
            if(deviceName.text.isNotBlank()){
                if(!deviceNameList!!.contains(deviceName.text.toString())){
                    val deviceDB = AppDataBase.getInstance(this)

                    job = CoroutineScope(Dispatchers.IO).launch {
                        val device = Device(deviceName.text.toString())
                        deviceDB!!.DeviceDAO().insertDeviceData(device)
                    }
                    setResult(RESULT_OK)
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