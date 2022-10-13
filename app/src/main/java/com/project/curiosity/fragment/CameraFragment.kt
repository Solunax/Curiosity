package com.project.curiosity.fragment

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.*
import android.util.Log
import android.view.LayoutInflater
import android.view.PixelCopy
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import com.project.curiosity.MediaScanner
import com.project.curiosity.R
import com.project.curiosity.api.ApiClient
import com.project.curiosity.databinding.CameraFragmentBinding
import com.project.curiosity.model.LedRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat


class CameraFragment : Fragment() {
    private lateinit var binding : CameraFragmentBinding
    private lateinit var job: Job
    private var ledState = false

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(

        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = CameraFragmentBinding.inflate(inflater, container, false)

        val web = binding.webView
        val flashButton = binding.flash

        web.settings.javaScriptEnabled = true
        setDesktopMode(web, true)
        web.webViewClient = WebViewClient()
        web.loadUrl("https://www.youtube.com/watch?v=qmsjvBivRkU")
//        web.loadUrl("http://192.168.82.188:81/stream")

        binding.capture.setOnClickListener{
            pixelCopy(web){bitmap ->
                save(bitmap)
            }
        }

        flashButton.setOnClickListener{
            job = CoroutineScope(Dispatchers.IO).launch {
                val request = LedRequest(if(!ledState) "1" else "0")
                val response = ApiClient.getApiClient().changeLedState(request)
                if(response.body()!!.statusCode == 200){
                    ledState = !ledState
                    changeIcon(ledState, flashButton)
                }
            }
        }

        return binding.root
    }

    private fun changeIcon(state:Boolean, view:ImageButton){
        if(state)
            view.setImageResource(R.drawable.ic_baseline_flash_on_24)
        else
            view.setImageResource(R.drawable.ic_baseline_flash_off_24)
    }

    @SuppressLint("SimpleDateFormat")
    private fun save(bitmap: Bitmap){
        val time = SimpleDateFormat("yyyyMMddHHmmss").format(System.currentTimeMillis())
        val uploadFolder = Environment.getExternalStoragePublicDirectory("/DCIM/CURIOSITY")

        if(!uploadFolder.exists())
            uploadFolder.mkdir()

        val path = Environment.getExternalStorageDirectory().absolutePath + "/DCIM/CURIOSITY/"
        try{
            val fos = FileOutputStream("$path$time.jpg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos)
            }
            val file = File("$path$time.jpg")
            MediaScanner(context, file)
        }catch (e:Exception){
            Log.d("Error", "ERROR ${e.message}")
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
                        }, Handler(Looper.getMainLooper())
                    )
                }
            }catch (e:Exception){
            }
        }
    }

    private fun setDesktopMode(webView: WebView, enabled: Boolean) {
        var newUserAgent: String? = webView.settings.userAgentString
        if (enabled) {
            try {
                val ua: String = webView.settings.userAgentString
                val androidOSString: String = webView.settings.userAgentString.substring(
                    ua.indexOf("("),
                    ua.indexOf(")") + 1
                )
                newUserAgent = webView.settings.userAgentString.replace(androidOSString, "(X11; Linux x86_64)")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            newUserAgent = null
        }
        webView.settings.apply {
            userAgentString = newUserAgent
            useWideViewPort = enabled
            loadWithOverviewMode = enabled
        }
        webView.reload()
    }

}