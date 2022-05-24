package com.project.curiosity.fragment

import android.os.Bundle
import android.util.Log
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
import com.project.curiosity.MainActivity
import com.project.curiosity.api.ApiClient
import com.project.curiosity.databinding.GpsFragmentBinding
import com.project.curiosity.model.Request
import kotlinx.coroutines.*

class GpsFragment: Fragment(), OnMapReadyCallback {
    private lateinit var binding: GpsFragmentBinding
    private var job: Job? = null
    private lateinit var roverMap:MapView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = GpsFragmentBinding.inflate(inflater, container, false)

        val name = binding.deviceName
        val search = binding.search
        val time = binding.time
        val result = binding.result
        roverMap = binding.mapView

        roverMap.onCreate(savedInstanceState)
        roverMap.getMapAsync(this)

        search.setOnClickListener {
            val id = (activity as MainActivity).getSpinnerData()
            val wantTime = time.text.toString()
            getData(id, wantTime)
        }

        return binding.root
    }

    override fun onMapReady(googleMap: GoogleMap) {
        val location = LatLng(37.419780, 127.204483)
        val markers = MarkerOptions()
        markers.position(location)
        googleMap.addMarker(markers)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 13f))
    }

    private fun getData(nameValue: String, timeValue:String) {
        job = CoroutineScope(Dispatchers.IO).launch {
            val request = Request(nameValue, timeValue)
            val response = ApiClient.getApiClient().getData(request)
            if(response.isSuccessful && response.body()!!.statusCode ==200)
                requireActivity().runOnUiThread{ binding.result.text = response.body().toString() }
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
        super.onDestroy()
    }
}