package com.project.curiosity.fragment

import com.project.curiosity.MediaScanner
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.os.*
import android.util.Log
import android.view.LayoutInflater
import android.view.PixelCopy
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import com.project.curiosity.databinding.CameraFragmentBinding
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat

class CameraFragment : Fragment() {
    private lateinit var binding : CameraFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = CameraFragmentBinding.inflate(inflater, container, false)

        val lay = binding.layouts
        val web = binding.webView
        web.settings.javaScriptEnabled = true
        web.webViewClient = WebViewClient()
        web.loadUrl("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")

        binding.c.setOnClickListener{
            pixelCopy(web.rootView){bitmap ->
                save(bitmap)
            }
        }

        return binding.root
    }

    private fun save(bitmap: Bitmap){
        val time = SimpleDateFormat("yyyyMMddHHmmss").format(System.currentTimeMillis())
        val uploadFolder = Environment.getExternalStoragePublicDirectory("/DCIM/Camera")

        if(!uploadFolder.exists())
            uploadFolder.mkdir()

        val path = Environment.getExternalStorageDirectory().absolutePath + "/DCIM/Camera/"
        try{
            val fos = FileOutputStream("$path$time.jpg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                bitmap?.compress(Bitmap.CompressFormat.JPEG, 80, fos)
            }
            val file = File("$path$time.jpg")
            val scanner = MediaScanner(context, file)
            Log.d("EE", "YY")
        }catch (e:Exception){
            Log.d("EE", "ERROR ${e.message}")
        }
    }

    private fun pixelCopy(view: View, callback: (Bitmap) -> Unit) {
        requireActivity().window?.let { window ->
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            val locationOfViewWindow = IntArray(2)
            view.getLocationInWindow(locationOfViewWindow)
            try{
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    PixelCopy.request(
                        window,
                        Rect(
                            locationOfViewWindow[0],
                            locationOfViewWindow[1],
                            locationOfViewWindow[0] + view.width,
                            locationOfViewWindow[1] + view.height
                        ), bitmap, { copyResult ->
                            if (copyResult == PixelCopy.SUCCESS) {
                                callback(bitmap)
                            }
                        }, Handler(Looper.getMainLooper()))
                }
            }catch (e:Exception){
            }
        }
    }
}