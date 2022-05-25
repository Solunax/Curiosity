package com.project.curiosity.fragment

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.project.curiosity.R
import com.project.curiosity.api.ApiClient
import com.project.curiosity.databinding.GraphFragmentBinding
import com.project.curiosity.model.Request
import com.project.curiosity.model.Request2
import com.project.curiosity.yongapi.ApiClient1
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

private var sensorList = ArrayList<sensor>()
private var globalstring :String = ""
private var globaltemp :String = ""
private var globalhumi :String = ""

class GraphFragment : Fragment() {
    private lateinit var binding: GraphFragmentBinding
    private lateinit var lineChart: LineChart
    private var job: Job? = null


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = GraphFragmentBinding.inflate(inflater, container, false)
        val temp = binding.textViewTemp
        val humi = binding.textViewHumi
        val imageButton_temp = binding.imageButtonTemp
        val imageButton_humi = binding.imageButtonHumi
        val res = binding.result
        val refresh = binding.refresh

        lineChart = binding.lineChart

        refresh.setOnClickListener{
            var a = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) //"yyyy-MM-dd HH:mm:ss"
            getData1("curiosity", a)
        }

        imageButton_temp.setOnClickListener {
            temp.setText("25")
            change_string()
        }

        imageButton_humi.setOnClickListener {
            humi.setText(globalstring)
        }

        initLineChart()
        setDataToLineChart()

        return binding.root
    }

    private fun main(args: Array<String>){
        val date = Date()
    }


    private fun initLineChart() {

        lineChart.axisLeft.setDrawGridLines(false)
        val xAxis: XAxis = lineChart.xAxis
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(false)

        //remove right y-axis
        lineChart.axisRight.isEnabled = false

        //remove legend
        lineChart.legend.isEnabled = false


        //remove description label
        lineChart.description.isEnabled = false


        //add animation
        lineChart.animateX(1000, Easing.EaseInSine)

        // to draw label on xAxis
        xAxis.setDrawAxisLine(true)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.valueFormatter = MyAxisFormatter()
        xAxis.setDrawLabels(true)
        xAxis.granularity = 1f
        xAxis.axisLineColor

    }


    inner class MyAxisFormatter : IndexAxisValueFormatter() {

        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            val index = value.toInt()
            return if (index < sensorList.size) {
                sensorList[index].name
            } else {
                ""
            }
        }
    }

    private fun change_string() {
        globalstring = "hello"
        print(globalstring)
        Log.d("globalstring2:","${globalstring}")
    }


    private fun setDataToLineChart() {
        //now draw bar chart with dynamic data
        val entries: ArrayList<Entry> = ArrayList()

        sensorList = getsensorList()

        //you can replace this data object with  your custom object
        for (i in sensorList.indices) {
            val sensor = sensorList[i]
            entries.add(Entry(i.toFloat(), sensor.temp.toFloat()))
        }

        val lineDataSet = LineDataSet(entries, "")

        val data = LineData(lineDataSet)
        lineChart.data = data

        lineDataSet.setDrawFilled(true)
        lineDataSet.fillDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.gradient)
        lineDataSet.setColor(Color.parseColor("#6441A5"))
        lineDataSet.setCircleColor(Color.DKGRAY);

        lineChart.invalidate()
    }

    // simulate api call
    // we are initialising it directly
    private fun getsensorList(): ArrayList<sensor> {
        sensorList.add(sensor("05:00", 30))
        sensorList.add(sensor("06:00", 20))
//        sensorList.add(sensor("07:00", 24))
//        sensorList.add(sensor("08:00", 36))
//        sensorList.add(sensor("09:00", 25))

        return sensorList
    }


    private fun getData1(nameValue: String, timeValue: String) {
            job = CoroutineScope(Dispatchers.IO).launch {
                val request = Request2(nameValue, timeValue)
                val response = ApiClient1.getApiClient1().getData1(request)
                if (response.isSuccessful && response.body()!!.statusCode == 200)
                    requireActivity().runOnUiThread {
//                        binding.result.text = response.body().toString()
                        globalstring = response.body().toString()
                        val number = globalstring.replace("[^0-9]".toRegex(), "")
                        Log.d("number","${globalstring}")
                        val number1 = number.substring(number.length-4, number.length)
                        Log.d("number1","${number1}")
                        globaltemp = number1.substring(0 until 2)
                        globalhumi = number1.substring(2 until 4)
                        //202205253332
                        binding.textViewTemp.setText(globaltemp)
                        binding.textViewHumi.setText(globalhumi)
                    }
        }
    }
}




