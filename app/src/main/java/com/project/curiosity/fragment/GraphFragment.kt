package com.project.curiosity.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.project.curiosity.databinding.GraphFragmentBinding

class GraphFragment : Fragment() {
    private lateinit var binding : GraphFragmentBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = GraphFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }
}