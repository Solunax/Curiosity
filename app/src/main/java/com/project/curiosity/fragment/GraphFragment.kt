package com.project.curiosity.fragment

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import com.project.curiosity.MainActivity
import com.project.curiosity.R
import com.project.curiosity.api.ApiClient
import com.project.curiosity.databinding.GraphFragmentBinding
import com.project.curiosity.model.Body
import com.project.curiosity.model.Request
import com.project.curiosity.model.Request2
import com.project.curiosity.yongapi.ApiClient1
import kotlinx.coroutines.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

private var sensorList = ArrayList<sensor>()
private var sensorList1 = ArrayList<sensor1>()
private var sensorList2 = ArrayList<sensor>() // 특정 날짜 temperature
private var sensorList3 = ArrayList<sensor1>() // 특정 날짜 humidity
private var globalString :String = ""
private var globalTime :String = ""
private var globalTemperature :Int = 0
private var globalHumidity :Int = 0
var globalCount = 0
var globalState = 1
var calendarState = 1
var state = 0
var dateString = ""

@RequiresApi(Build.VERSION_CODES.O)
private var local_time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH"))

class GraphFragment : Fragment() {
    private lateinit var binding: GraphFragmentBinding
    private lateinit var lineChart: LineChart
    private lateinit var lineChart2: LineChart
    private lateinit var lineChart3: LineChart
    private var job: Job? = null
    private val calendar = Calendar.getInstance()
    private lateinit var dateSetListener : DatePickerDialog.OnDateSetListener

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = GraphFragmentBinding.inflate(inflater, container, false)
        val temperatureText = binding.temp
        val humidityText = binding.humitext1
        val imageButtonTemperature = binding.imageButtonTemp
        val imageButtonHumidity = binding.imageButtonHumi
        val imageButtonTemperatureSearch = binding.imageButton7
        val imageButtonHumiditySearch = binding.imageButton6

        lineChart = binding.lineChart
        lineChart2 = binding.lineChart2
        lineChart3 = binding.lineChart3

//        timer(initialDelay = 1000, period = 10000) {
//            setGraph((activity as MainActivity).recentBody!!)
//        }

//        val timer = Timer()
//        timer.schedule(object:TimerTask(){
//            override fun run() {
//                setGraph((activity as MainActivity).recentBody!!)
//            }
//        }, 2000, 10000)

        dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            dateString = if(month + 1 <= 9){
                if(dayOfMonth < 10)
                    "${year}-${"0" + (month+1)}-${"0$dayOfMonth"}"
                else
                    "${year}-${"0" + (month+1)}-${dayOfMonth}"
            }
            else{
                if(dayOfMonth < 10)
                    "${year}-${month+1}-${"0$dayOfMonth"}"
                else
                    "${year}-${month+1}-${dayOfMonth}"
            }
            getSpecificData((activity as MainActivity).getSpinnerData(), dateString)
            if(calendarState == 1)
                temperatureText.text = dateString
            else if(calendarState == 2)
                humidityText.text = dateString
        }

        imageButtonTemperature.setOnClickListener {
            globalState = 1
            setDataToLineChartRenew()
        }

        imageButtonHumidity.setOnClickListener {
            globalState = 2
            setDataToLineChartRenewHumidity()
        }

        imageButtonTemperatureSearch.setOnClickListener {
            getDate(it)
        }

        imageButtonHumiditySearch.setOnClickListener {
            getDate(it)
        }

        initLineChart()
        setDataToLineChart()
        setDataToLineChartHumidity()
        initLineChart2()
        setDataToLineChart2()
        initLineChart3()
        setDataToLineChart3()

        return binding.root
    }

    private fun getDate(view:View){
        if(view.id.toString() == binding.imageButton7.id.toString())
            calendarState = 1
        else if((view.id.toString() == binding.imageButton6.id.toString()))
            calendarState = 2

        DatePickerDialog(requireActivity(), dateSetListener, calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH)).show()
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

        lineChart.isDragXEnabled = true

        // to draw label on xAxis
        xAxis.setDrawAxisLine(true)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.valueFormatter = MyAxisFormatter()
        xAxis.setDrawLabels(true)
        xAxis.granularity = 1f
        xAxis.axisLineColor

    }

    private fun initLineChartHumidity() {
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

        lineChart.isDragXEnabled = true

        // to draw label on xAxis
        xAxis.setDrawAxisLine(true)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.valueFormatter = MyAxisFormatter1()
        xAxis.setDrawLabels(true)
        xAxis.granularity = 1f
        xAxis.axisLineColor

    }

    private fun initLineChart2() {

        lineChart2.axisLeft.setDrawGridLines(false)
        val xAxis: XAxis = lineChart2.xAxis
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(false)

        //remove right y-axis
        lineChart2.axisRight.isEnabled = false

        //remove legend
        lineChart2.legend.isEnabled = false


        //remove description label
        lineChart2.description.isEnabled = false


        //add animation
        lineChart2.animateX(1000, Easing.EaseInSine)

        lineChart2.isDragXEnabled = true

        // to draw label on xAxis
        xAxis.setDrawAxisLine(true)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.valueFormatter = MyAxisFormatter2()
        xAxis.setDrawLabels(true)
        xAxis.granularity = 1f
        xAxis.axisLineColor

    }

    private fun initLineChart3() {

        lineChart3.axisLeft.setDrawGridLines(false)
        val xAxis: XAxis = lineChart3.xAxis
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(false)

        //remove right y-axis
        lineChart3.axisRight.isEnabled = false

        //remove legend
        lineChart3.legend.isEnabled = false


        //remove description label
        lineChart3.description.isEnabled = false


        //add animation
        lineChart3.animateX(1000, Easing.EaseInSine)

        lineChart3.isDragEnabled = true

        // to draw label on xAxis
        xAxis.setDrawAxisLine(true)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.valueFormatter = MyAxisFormatter3()
        xAxis.setDrawLabels(true)
        xAxis.granularity = 1f
        xAxis.axisLineColor

    }

    //humidity
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
    // temp1
    inner class MyAxisFormatter2 : IndexAxisValueFormatter() {
        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            val index = value.toInt()
            return if (index < sensorList2.size) {
                sensorList2[index].name
            } else {
                ""
            }
        }
    }
    //humidity1
    inner class MyAxisFormatter3 : IndexAxisValueFormatter() {
        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            val index = value.toInt()
            return if (index < sensorList3.size) {
                sensorList3[index].name
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

        sensorList = getSensorList()

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
        lineDataSet.color = Color.parseColor("#6441A5")
        lineDataSet.setCircleColor(Color.DKGRAY)

        lineChart.invalidate()
    }
    //humidity
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setDataToLineChartHumidity() {
        //now draw bar chart with dynamic data
        val entries1: ArrayList<Entry> = ArrayList()

        sensorList1 = getSensorList1()

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
        lineDataSet.color = Color.parseColor("#6441A5")
        lineDataSet.setCircleColor(Color.DKGRAY)

        lineChart.invalidate()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setDataToLineChart2() {
        //now draw bar chart with dynamic data
        val entries: ArrayList<Entry> = ArrayList()

        sensorList2 = getSensorList2()

        //you can replace this data object with  your custom object
        for (i in sensorList2.indices) {
            val sensor = sensorList2[i]
            entries.add(Entry(i.toFloat(), sensor.temp.toFloat()))
        }

        val lineDataSet = LineDataSet(entries, "")

        val data = LineData(lineDataSet)
        lineChart2.data = data

        lineDataSet.setDrawFilled(true)
        lineDataSet.fillDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.gradient)
        lineDataSet.color = Color.parseColor("#6441A5")
        lineDataSet.setCircleColor(Color.DKGRAY)

        lineChart2.invalidate()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setDataToLineChart3() {
        //now draw bar chart with dynamic data
        val entries: ArrayList<Entry> = ArrayList()

        sensorList3 = getSensorList3()

        //you can replace this data object with  your custom object
        for (i in sensorList3.indices) {
            val sensor1 = sensorList3[i]
            entries.add(Entry(i.toFloat(), sensor1.humi.toFloat()))
        }

        val lineDataSet = LineDataSet(entries, "")

        val data = LineData(lineDataSet)
        lineChart3.data = data

        lineDataSet.setDrawFilled(true)
        lineDataSet.fillDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.gradient)
        lineDataSet.color = Color.parseColor("#6441A5")
        lineDataSet.setCircleColor(Color.DKGRAY)
        lineChart3.invalidate()
    }

    // temp
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setDataToLineChartRenew() {
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
        lineDataSet.color = Color.parseColor("#6441A5")
        lineDataSet.setCircleColor(Color.DKGRAY)

        lineChart.invalidate()
    }


    //humidity
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setDataToLineChartRenewHumidity() {
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
        lineDataSet.color = Color.parseColor("#6441A5")
        lineDataSet.setCircleColor(Color.DKGRAY)

        lineChart.invalidate()
    }
    // temp1
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setDataToLineChartRenewTemperature() {
        //now draw bar chart with dynamic data
        val entries: ArrayList<Entry> = ArrayList()

        //you can replace this data object with  your custom object
        for (i in sensorList2.indices) {
            val sensor = sensorList2[i]
            entries.add(Entry(i.toFloat(), sensor.temp.toFloat()))
        }

        val lineDataSet = LineDataSet(entries, "")

        val data = LineData(lineDataSet)
        lineChart2.data = data

        lineDataSet.setDrawFilled(true)
        lineDataSet.fillDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.gradient)
        lineDataSet.color = Color.parseColor("#6441A5")
        lineDataSet.setCircleColor(Color.DKGRAY)

        lineChart2.invalidate()
    }

    // humidity1
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setDataToLineChartRenewHumidity1() {
        //now draw bar chart with dynamic data
        val entries: ArrayList<Entry> = ArrayList()

        //you can replace this data object with  your custom object
        for (i in sensorList3.indices) {
            val sensor1 = sensorList3[i]
            entries.add(Entry(i.toFloat(), sensor1.humi.toFloat()))
        }

        val lineDataSet = LineDataSet(entries, "")

        val data = LineData(lineDataSet)
        lineChart3.data = data

        lineDataSet.setDrawFilled(true)
        lineDataSet.fillDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.gradient1)
        lineDataSet.color = Color.parseColor("#6441A5")
        lineDataSet.setCircleColor(Color.DKGRAY)

        lineChart3.invalidate()
    }

    //temp
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getSensorList(): ArrayList<sensor> {
        sensorList.add(sensor("", 0))
        sensorList.add(sensor("", 0))

        return sensorList
    }

    // humidity
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getSensorList1(): ArrayList<sensor1> {
        sensorList1.add(sensor1("", 0))
        sensorList1.add(sensor1("", 0))

        return sensorList1
    }
    // temp1
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getSensorList2(): ArrayList<sensor> {
        sensorList2.add(sensor("", 0))
        sensorList2.add(sensor("", 0))
        return sensorList2
    }
    // humidity1
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getSensorList3(): ArrayList<sensor1> {
        sensorList3.add(sensor1("", 0))
        sensorList3.add(sensor1("", 0))
        return sensorList3
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getSpecificData(deviceId:String, time:String){
        job = CoroutineScope(Dispatchers.IO).launch {
            val request = Request(deviceId, time)
            val response = ApiClient.getApiClient().getData(request)
            if (response.isSuccessful && response.body()!!.statusCode == 201) {
                var i = 0
                val count = response.body()!!.length
                sensorList2.clear()
                sensorList3.clear()
                sensorList2.add(sensor("", 0))
                sensorList3.add(sensor1("", 0))
                while(i < count) {
                    var a = response.body()!!.body[i].timestamp
                    val b = response.body()!!.body[i].temperature
                    val c = response.body()!!.body[i].humidity
                    val time2 = a.substring(a.length - 5, a.length)
                    a = time2.substring(0 until 2)
                    sensorList2.add(sensor(a, b))
                    sensorList3.add(sensor1(a, c))
                    i += 1
                }
                if(calendarState == 1)
                    setDataToLineChartRenewTemperature()
                else
                    setDataToLineChartRenewHumidity1()
            }else{
                requireActivity().runOnUiThread { Toast.makeText(requireActivity(), "선택한 날짜의 저장된 정보가 없습니다.", Toast.LENGTH_SHORT).show() }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setGraph(data:Body) {
        if(state == 0) {
            sensorList.removeAt(1)
            sensorList1.removeAt(1)
        }
        globalTime = data.timestamp
        globalTemperature = data.temperature
        globalHumidity = data.humidity

        val time1 = globalTime.substring(globalTime.length -8, globalTime.length)
        globalTime = time1.substring(0 until 2)
        val compareName = sensorList[globalCount].name
        val compareTemperature = sensorList[globalCount].temp
        val compareHumidity = sensorList1[globalCount].humi
        if (globalTime == compareName) {
            if (compareTemperature < globalTemperature) {
                sensorList[globalCount] = sensor(globalTime, globalTemperature)
            }
            if(compareHumidity < globalHumidity){
                sensorList1[globalCount] = sensor1(globalTime, globalHumidity)
            }
        }
        else {
            globalCount += 1
            state += 1
            sensorList.add(sensor(globalTime, globalTemperature))
            sensorList1.add(sensor1(globalTime, globalHumidity))
        }
        requireActivity().runOnUiThread {
            binding.textViewTemp.text = globalTemperature.toString()
            binding.textViewHumi.text = globalHumidity.toString()
            if (globalState == 1)
                setDataToLineChartRenew()
            else
                setDataToLineChartRenewHumidity()
        }
    }
}