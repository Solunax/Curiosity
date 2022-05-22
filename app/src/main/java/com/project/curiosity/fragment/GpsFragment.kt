package com.project.curiosity.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.project.curiosity.MainActivity
import com.project.curiosity.api.ApiClient
import com.project.curiosity.databinding.GpsFragmentBinding
import com.project.curiosity.model.Request
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class GpsFragment: Fragment() {
    private lateinit var binding: GpsFragmentBinding
    private var job: Job? = null

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

        search.setOnClickListener {
            val id = (activity as MainActivity).getSpinnerData()
            val wantTime = time.text.toString()
            getData(id, wantTime)
        }
        return binding.root
    }

    private fun getData(nameValue: String, timeValue:String) {
        job = CoroutineScope(Dispatchers.IO).launch {
            val request = Request(nameValue, timeValue)
            val response = ApiClient.getApiClient().getData(request)
            if(response.isSuccessful && response.body()!!.statusCode ==200)
                requireActivity().runOnUiThread{ binding.result.text = response.body().toString() }
        }
    }
}