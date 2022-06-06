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
import com.project.curiosity.databinding.GpsFragmentBinding
import com.project.curiosity.model.Body
import kotlinx.coroutines.Job
import java.util.*

class GpsFragment: Fragment(), OnMapReadyCallback {
    private lateinit var binding: GpsFragmentBinding
    private var job: Job? = null
    private lateinit var roverMap:MapView
    private lateinit var map: GoogleMap
    private lateinit var changeMap: FloatingActionButton
    private val locationArray = LinkedList<LatLng>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = GpsFragmentBinding.inflate(inflater, container, false)

        roverMap = binding.mapView
        changeMap = binding.mapChange

        changeMap.setOnClickListener {
            // 위성지도, 일반지도 변경
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

        return binding.root
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
    }

    fun drawRoute(changeFlag:Boolean, data:Body) {
        val id = (activity as MainActivity).getSpinnerData()
        // 장치 ID 변경 확인
        if((activity as MainActivity).changeGpsFlag){
            requireActivity().runOnUiThread { map.clear() }
            locationArray.clear()
            addMarker(data)
            (activity as MainActivity).changeGpsFlag = false
            return
        }

        if(!changeFlag){
            if(id == "ERROR")
                requireActivity().runOnUiThread { Toast.makeText(context, "장치 이름을 불러올 수 없습니다.", Toast.LENGTH_SHORT).show() }
            else
                addMarker(data)
        }else{
            requireActivity().runOnUiThread { map.clear() }
            locationArray.clear()
            addMarker(data)
        }
    }

    private fun addMarker(data: Body){
        val location = LatLng(data.latitude, data.longitude)
        locationArray.add(location)

        if(locationArray.size == 1){
            val marker = MarkerOptions()
            marker.position(location)
            requireActivity().runOnUiThread{
                map.addMarker(marker)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
            }
        }else if(locationArray.size > 1 && locationArray[locationArray.size - 1] != locationArray[locationArray.size - 2]){
            val marker = MarkerOptions()
            marker.position(location)
            requireActivity().runOnUiThread{
                map.addMarker(marker)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
                map.addPolyline(PolylineOptions().add(locationArray[locationArray.size - 2], locationArray[locationArray.size - 1]).width(15f))
            }
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