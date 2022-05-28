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
import com.project.curiosity.databinding.GraphFragmentBinding
import com.project.curiosity.model.Request2
import com.project.curiosity.yongapi.ApiClient1
import kotlinx.coroutines.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.timer

private var sensorList = ArrayList<sensor>()
private var sensorList1 = ArrayList<sensor1>()
private var globalstring :String = ""
private var globaltemp :String = ""
private var globalhumi :String = ""
var globalcount = 1
@RequiresApi(Build.VERSION_CODES.O)
private var local_time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH"))


private var globaltime :String = ""

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

        lineChart = binding.lineChart

        val timer = timer(period = 10000) {
            getData1("curiosity", "")
        }

        //var a = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) //"yyyy-MM-dd HH:mm:ss"

        imageButton_temp.setOnClickListener {
            setDataToLineChart_renew()
            lineChart.invalidate();
        }

        imageButton_humi.setOnClickListener {
            setDataToLineChart_renew_humi()
            lineChart.invalidate();
        }

        initLineChart()
        setDataToLineChart()
        setDataToLineChart_humi()

        return binding.root
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

        lineChart.setDragXEnabled(true);

        // to draw label on xAxis
        xAxis.setDrawAxisLine(true)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.valueFormatter = MyAxisFormatter()
        xAxis.setDrawLabels(true)
        xAxis.granularity = 1f
        xAxis.axisLineColor

    }

    private fun initLineChart_humi() {

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

        lineChart.setDragXEnabled(true);

        // to draw label on xAxis
        xAxis.setDrawAxisLine(true)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.valueFormatter = MyAxisFormatter1()
        xAxis.setDrawLabels(true)
        xAxis.granularity = 1f
        xAxis.axisLineColor

    }

    //humi
    inner class MyAxisFormatter1 : IndexAxisValueFormatter() {

        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            val index = value.toInt()
            return if (index < sensorList1.size) {
                sensorList1[index].name
            } else {
                ""
            }
        }
    }

    //temp
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
    //temp
    @RequiresApi(Build.VERSION_CODES.O)
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
    //humi
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setDataToLineChart_humi() {
        //now draw bar chart with dynamic data
        val entries1: ArrayList<Entry> = ArrayList()

        sensorList1 = getsensorList1()

        //you can replace this data object with  your custom object
        for (i in sensorList1.indices) {
            val sensor1 = sensorList1[i]
            entries1.add(Entry(i.toFloat(), sensor1.humi.toFloat()))
        }

        val lineDataSet = LineDataSet(entries1, "")

        val data = LineData(lineDataSet)
        lineChart.data = data

        lineDataSet.setDrawFilled(true)
        lineDataSet.fillDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.gradient)
        lineDataSet.setColor(Color.parseColor("#6441A5"))
        lineDataSet.setCircleColor(Color.DKGRAY);

        lineChart.invalidate()
    }

    //temp
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setDataToLineChart_renew() {
        //now draw bar chart with dynamic data
        val entries: ArrayList<Entry> = ArrayList()

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


    //humi
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setDataToLineChart_renew_humi() {
        //now draw bar chart with dynamic data
        val entries: ArrayList<Entry> = ArrayList()

        //you can replace this data object with  your custom object
        for (i in sensorList1.indices) {
            val sensor1 = sensorList1[i]
            entries.add(Entry(i.toFloat(), sensor1.humi.toFloat()))
        }

        val lineDataSet = LineDataSet(entries, "")

        val data = LineData(lineDataSet)
        lineChart.data = data

        lineDataSet.setDrawFilled(true)
        lineDataSet.fillDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.gradient1)
        lineDataSet.setColor(Color.parseColor("#6441A5"))
        lineDataSet.setCircleColor(Color.DKGRAY);

        lineChart.invalidate()
    }

    // temp
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getsensorList(): ArrayList<sensor> {
        sensorList.add(sensor("00", 0))
        sensorList.add(sensor(local_time, 20))

        return sensorList
    }

    // humi
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getsensorList1(): ArrayList<sensor1> {
        sensorList1.add(sensor1("00", 0))
        sensorList1.add(sensor1(local_time, 40))

        return sensorList1
    }




    private fun getData1(nameValue: String, timeValue: String) {
        job = CoroutineScope(Dispatchers.IO).launch {
            val request = Request2(nameValue, timeValue)
            val response = ApiClient1.getApiClient1().getData1(request)
            if (response.isSuccessful && response.body()!!.statusCode == 200)
                globalstring = response.body().toString()
            val number = globalstring.replace("[^0-9]".toRegex(), "")
            val number1 = number.substring(number.length - 4, number.length)
            var time1 = number.substring(number.length - 8, number.length)
            globaltemp = number1.substring(0 until 2)
            globalhumi = number1.substring(2 until 4)
            globaltime = time1.substring(0 until 2)
            requireActivity().runOnUiThread {
                binding.textViewTemp.setText(globaltemp)
                binding.textViewHumi.setText(globalhumi)
                var compare_name = sensorList.get(globalcount).name
                var compare_temp = sensorList.get(globalcount).temp
                var compare_humi = sensorList1.get(globalcount).humi
                Log.d("비교", "${globaltime},${compare_name}")
                if (globaltime == compare_name) {
                    if (compare_temp < Integer.parseInt(globaltemp)) {
                        sensorList.set(globalcount, sensor(globaltime, Integer.parseInt(globaltemp)))
                        if(compare_humi < Integer.parseInt(globalhumi)){
                            sensorList1.set(globalcount, sensor1(globaltime, Integer.parseInt(globalhumi)))
                        }
                    }
                }
                else {
                    globalcount += 1
                    sensorList.add(sensor(globaltime, Integer.parseInt(globaltemp)))
                    sensorList1.add(sensor1(globaltime, Integer.parseInt(globalhumi)))
                    }

                Log.d("list", "${sensorList},${globalcount}")

            }
        }
    }
}









