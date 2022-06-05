package com.project.curiosity.fragmentAdapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.project.curiosity.fragment.CameraFragment
import com.project.curiosity.fragment.GpsFragment
import com.project.curiosity.fragment.GraphFragment

private const val numberOfTab = 3

class StateAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(fragmentManager, lifecycle){
    private val fragmentArray = arrayOf(CameraFragment(), GraphFragment(), GpsFragment())
    override fun getItemCount(): Int {
        return numberOfTab
    }

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> fragmentArray[0]
            1 -> fragmentArray[1]
            2 -> fragmentArray[2]
            else -> fragmentArray[1]
        }
    }
}