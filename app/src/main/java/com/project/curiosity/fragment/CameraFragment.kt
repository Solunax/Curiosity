package com.project.curiosity.fragment

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.project.curiosity.databinding.CameraFragmentBinding
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class CameraFragment : Fragment() {
    private lateinit var binding : CameraFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = CameraFragmentBinding.inflate(inflater, container, false)

        val img = binding.imageView

        binding.c.setOnClickListener{
            val bitmap = requestCapture(img)
            val time = SimpleDateFormat("yyyyMMddHHmmss").format(System.currentTimeMillis())
            val uploadFolder = Environment.getExternalStoragePublicDirectory("/DCIM/Camera")
            if(!uploadFolder.exists())
                uploadFolder.mkdir()

            val path = Environment.getExternalStorageDirectory().absolutePath + "/DCIM/Camera/"
            try{
                val fos = FileOutputStream("$path$time.jpg")
                bitmap?.compress(Bitmap.CompressFormat.JPEG, 80, fos)
                Log.d("EE", "YY")
            }catch (e:Exception){
                Log.d("EE", "ERROR ${e.message}")
            }
        }

        return binding.root
    }

    private fun requestCapture(view:View):Bitmap?{
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }
}