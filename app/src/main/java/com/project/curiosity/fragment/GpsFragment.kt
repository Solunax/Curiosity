package com.project.curiosity.fragment

import android.os.Bundle
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
import com.project.curiosity.MainActivity
import com.project.curiosity.api.ApiClient
import com.project.curiosity.databinding.GpsFragmentBinding
import com.project.curiosity.model.Body
import com.project.curiosity.model.Request
import kotlinx.coroutines.*
import java.util.*

class GpsFragment: Fragment(), OnMapReadyCallback {
    private lateinit var binding: GpsFragmentBinding
    private var job: Job? = null
    private lateinit var roverMap:MapView
    private lateinit var timer:Timer
    private lateinit var map: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = GpsFragmentBinding.inflate(inflater, container, false)
        roverMap = binding.mapView

        roverMap.onCreate(savedInstanceState)
        roverMap.getMapAsync(this)
//        1분 단위로 서버로부터 데이터를 받아옴
        timer = Timer()
        timer.schedule(object:TimerTask(){
            override fun run() {
                val id = (activity as MainActivity).getSpinnerData()
                if(id == "ERROR")
                    Toast.makeText(context, "장치 이름을 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
                else
                    getData(id, "")
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
            if(response.isSuccessful && response.body()!!.statusCode ==200)
                addMarker(response.body()!!.body[0])
        }
    }

    private fun addMarker(dataArray: Body){
        val location = LatLng(dataArray.latitude, dataArray.longitude)
        val marker = MarkerOptions()
        marker.position(location)
        requireActivity().runOnUiThread{
//            마커 초기화(삭제)
            map.clear()
            map.addMarker(marker)
//            map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 13f))
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 13f))
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
        super.onDestroy()
    }
}