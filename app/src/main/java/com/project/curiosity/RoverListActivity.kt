package com.project.curiosity

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.project.curiosity.databinding.RoverListBinding
import com.project.curiosity.room.Device
import com.project.curiosity.viewModel.ViewModel
import kotlin.collections.ArrayList

class RoverListActivity:AppCompatActivity() {
    private lateinit var binding: RoverListBinding
    private lateinit var deviceNameList: ArrayList<String>
    private lateinit var listAdapter: ArrayAdapter<String>
    private lateinit var idList:ListView
    private val viewModel : ViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = RoverListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val addDevice = binding.addDevice
        idList = binding.roverListview

        deviceNameList = intent.getStringArrayListExtra("deviceList")!!
        listAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, deviceNameList)
        idList.adapter = listAdapter

        viewModel.nameData.observe(this){
            listAdapter.clear()
            it.forEach { v->
                listAdapter.add(v.deviceID)
            }
            listAdapter.notifyDataSetChanged()
        }

        addDevice.setOnClickListener {
            val intent = Intent(applicationContext, AddDeviceActivity::class.java)
            intent.putExtra("deviceList", deviceNameList)
            startActivity(intent)
        }

        idList.setOnItemLongClickListener { _, _, i, _ ->
            showDialog(listAdapter.getItem(i).toString())
            true
        }
    }

    // Device ID를 꾹 눌렀을 때 삭제메뉴
    private fun showDialog(deviceID:String){
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("장치 삭제")
        dialog.setMessage("선택한 장치 ${deviceID}를 삭제하시겠습니까?")

        dialog.setPositiveButton("삭제") { _, _ ->
            viewModel.deleteDeviceData(Device(deviceID))
            listAdapter.remove(deviceID)
            Toast.makeText(this, "삭제되었습니다.", Toast.LENGTH_SHORT).show()
        }
        dialog.setNegativeButton("취소"){ _, _ -> }
        dialog.show()
    }
}