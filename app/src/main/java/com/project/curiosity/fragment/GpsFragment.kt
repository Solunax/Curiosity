package com.project.curiosity.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import java.util.*

class GpsFragment: Fragment(), OnMapReadyCallback {
    private var binding: GpsFragmentBinding? = null
    private lateinit var roverMap:MapView
    private lateinit var map: GoogleMap
    private lateinit var changeMap: FloatingActionButton
    private val locationArray = LinkedList<LatLng>()
    // 다른 로버가 선택되었는지 확인하기 위한 변수 now
    private var now = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = GpsFragmentBinding.inflate(inflater, container, false)

        roverMap = binding!!.mapView
        changeMap = binding!!.mapChange

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

        // MainActivity 의 ViewModel 공유해서 사용
        (activity as MainActivity).viewModel.roverData.observe(viewLifecycleOwner){
            // map 이 late init 이기에 초기화 후에 접근해야 함
            if(::map.isInitialized){
                if(it.deviceID != now) {
                    now = it.deviceID
                    map.clear()
                    locationArray.clear()
                }
                addMarker(it.latitude, it.longitude)
            }
        }

        return binding!!.root
    }

    private fun addMarker(latitude : Double, longitude : Double) {
        val location = LatLng(latitude, longitude)
        locationArray.add(location)

        if(locationArray.size == 1){
            val marker = MarkerOptions()
            marker.position(location)
            map.addMarker(marker)
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
        }else if(locationArray.size > 1 && locationArray[locationArray.size - 1] != locationArray[locationArray.size - 2]){
            val marker = MarkerOptions()
            marker.position(location)
            map.addMarker(marker)
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
            map.addPolyline(PolylineOptions().add(locationArray[locationArray.size - 2], locationArray[locationArray.size - 1]).width(15f))
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
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
        locationArray.clear()
        binding = null
        super.onDestroy()
    }
}