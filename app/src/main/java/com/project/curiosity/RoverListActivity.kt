package com.project.curiosity

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.project.curiosity.databinding.RoverListBinding
import com.project.curiosity.room.AppDataBase
import com.project.curiosity.room.Device
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.collections.ArrayList

class RoverListActivity:AppCompatActivity() {
    private lateinit var binding: RoverListBinding
    private lateinit var getResultActivity: ActivityResultLauncher<Intent>
    private lateinit var deviceNameList: ArrayList<String>
    private lateinit var job: Job
    private lateinit var listAdapter: ArrayAdapter<String>
    private lateinit var idList:ListView

    override fun onBackPressed() {
        setResult(RESULT_OK)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = RoverListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val addDevice = binding.addDevice
        idList = binding.roverListview

        val deviceDB = AppDataBase.getInstance(this)

        deviceNameList = intent.getStringArrayListExtra("deviceList")!!
        listAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, deviceNameList)
        idList.adapter = listAdapter

        addDevice.setOnClickListener {
            val intent = Intent(applicationContext, AddDeviceActivity::class.java)
            intent.putExtra("deviceList", deviceNameList)
            getResultActivity.launch(intent)
        }

        getResultActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK)
                updateList(deviceDB!!, listAdapter)
        }

        idList.setOnItemLongClickListener { _, _, i, _ ->
            showDialog(listAdapter.getItem(i).toString(), deviceDB!!)
            true
        }
    }

    private fun updateList(db: AppDataBase, adapter: ArrayAdapter<String>) {
        job = CoroutineScope(Dispatchers.IO).launch {
            val deviceList = db.DeviceDAO().getDeviceData()
            runOnUiThread {
                adapter.clear()
                deviceList.forEach {
                    adapter.add(it.deviceID)
                }
                listAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun showDialog(deviceID:String, db:AppDataBase){
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("장치 삭제")
        dialog.setMessage("선택한 장치 ${deviceID}를 삭제하시겠습니까?")
        dialog.setPositiveButton("삭제") { _, _ ->
            job = CoroutineScope(Dispatchers.IO).launch {
                val device = Device(deviceID)
                db.DeviceDAO().deleteDeviceData(device)
                updateList(db, listAdapter)
            }
        }
        dialog.setNegativeButton("취소"){ _, _ -> }
        dialog.show()
    }
}