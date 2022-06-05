package com.project.curiosity.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.project.curiosity.MainActivity
import com.project.curiosity.api.ApiClient
import com.project.curiosity.databinding.GpsFragmentBinding
import com.project.curiosity.model.Body
import com.project.curiosity.model.Request
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.concurrent.thread
import kotlinx.coroutines.*
import java.util.*

class GpsFragment: Fragment(), OnMapReadyCallback {
    private lateinit var binding: GpsFragmentBinding
    private var job: Job? = null
    private lateinit var roverMap:MapView
    private lateinit var timer:Timer
    private lateinit var map: GoogleMap
    private lateinit var changeMap: FloatingActionButton
    private val locationArray = LinkedList<LatLng>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = GpsFragmentBinding.inflate(inflater, container, false)

        roverMap = binding.mapView
        changeMap = binding.mapChange

        changeMap.setOnClickListener {
//            위성지도, 일반지도 변경
            if(map.mapType == 1){
                map.mapType = GoogleMap.MAP_TYPE_HYBRID
                roverMap.invalidate()
            } else{
                map.mapType = GoogleMap.MAP_TYPE_NORMAL
                roverMap.invalidate()
            }
        }

        roverMap.onCreate(savedInstanceState)
        roverMap.getMapAsync(this)
//        1분 단위로 서버로부터 데이터를 받아옴
        timer = Timer()
        timer.schedule(object:TimerTask(){
            override fun run() {
                if((activity as MainActivity).getFragmentLocation() == 2)
                    drawRoute(false)
            }
        }, 0, 10000)

        return binding.root

    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
    }

    private fun getData(nameValue: String, timeValue:String) {
        job = CoroutineScope(Dispatchers.IO).launch {
            val request = Request(nameValue, timeValue)
            val response = ApiClient.getApiClient().getData(request)
            if(response.isSuccessful && response.body()!!.statusCode == 200)
                addMarker(response.body()!!.body[0])
        }
    }

    private fun addMarker(dataArray: Body){
        val location = LatLng(dataArray.latitude, dataArray.longitude)
        locationArray.add(location)

        if(locationArray.size == 1){
            val marker = MarkerOptions()
            marker.position(location)
            requireActivity().runOnUiThread{
                map.addMarker(marker)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 20f))
            }
        }else if(locationArray.size > 1 && locationArray[locationArray.size - 1] != locationArray[locationArray.size - 2]){
            val marker = MarkerOptions()
            marker.position(location)
            requireActivity().runOnUiThread{
                map.addMarker(marker)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 20f))
                map.addPolyline(PolylineOptions().add(locationArray[locationArray.size - 2], locationArray[locationArray.size - 1]).width(10f))
            }
        }
    }

    fun drawRoute(changeFlag:Boolean) {
        Log.d("DD", "YYYY")
        val id = (activity as MainActivity).getSpinnerData()
        if(!changeFlag){
            if((activity as MainActivity).getFragmentLocation() == 2){
                if(id == "ERROR")
                    requireActivity().runOnUiThread { Toast.makeText(context, "장치 이름을 불러올 수 없습니다.", Toast.LENGTH_SHORT).show() }
                else
                    getData(id, "")
            }
        }else{
            requireActivity().runOnUiThread { map.clear() }
            locationArray.clear()
            getData(id, "")
        }
    }

    override fun onStart() {
        super.onStart()
        roverMap.onStart()
    }

    override fun onStop() {
        super.onStop()
        roverMap.onStop()
    }

    override fun onResume() {
        super.onResume()
        roverMap.onResume()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        roverMap.onLowMemory()
    }

    override fun onDestroy() {
        roverMap.onDestroy()
        if(job != null){
            job?.cancel()
            job = null
        }
        locationArray.clear()
        super.onDestroy()
    }
}