package com.project.curiosity.fragment

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
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
import com.project.curiosity.databinding.GraphFragmentBinding
import com.project.curiosity.model.Body
import com.project.curiosity.viewModel.ViewModel
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList

private var sensorList = ArrayList<sensor>()
private var sensorList1 = ArrayList<sensor1>()
private var sensorList2 = ArrayList<sensor>() // 특정 날짜 temperature
private var sensorList3 = ArrayList<sensor1>() // 특정 날짜 humidity
private var globalTime :String = ""
private var globalTemperature :Int = 0
private var globalHumidity :Int = 0
var globalCount = 0
var globalState = 1
var calendarState = 1
var state = 0
var dateString = ""
var lineChartState = 0
var setDataToLineChartState = 0

class GraphFragment : Fragment() {
    private lateinit var binding: GraphFragmentBinding
    private lateinit var lineChart: LineChart
    private lateinit var lineChart2: LineChart
    private lateinit var lineChart3: LineChart
    private var job: Job? = null
    private val calendar = Calendar.getInstance()
    private lateinit var dateSetListener : DatePickerDialog.OnDateSetListener
    private lateinit var viewModel : ViewModel
    private var now = ""


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = GraphFragmentBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[ViewModel::class.java]

        val temperatureText = binding.temp
        val humidityText = binding.humitext1
        val imageButtonTemperature = binding.imageButtonTemp
        val imageButtonHumidity = binding.imageButtonHumi
        val imageButtonTemperatureSearch = binding.TemperatureButton
        val imageButtonHumiditySearch = binding.HumidityButton

        lineChart = binding.lineChart
        lineChart2 = binding.lineChart2
        lineChart3 = binding.lineChart3

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
            (activity as MainActivity).getDataCalendar(dateString)
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
            Log.d("checkGlobal","$it")
        }

        viewModel.roverData.observe(viewLifecycleOwner) {
            if(it.deviceID != now) {
                now = it.deviceID
                setGraph2(it)
            }
            setGraph(it)
        }

        viewModel.specificData.observe(viewLifecycleOwner) {
            if(it[0].deviceID != now) {
                now = it[0].deviceID
                setGraph2(it[0])
            }else {
                val type = "True"
                val dataSize = it.size
                getSpecificData(it, dataSize, type)
            }
        }

        viewModel.specificErrorData.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let{
                Log.d("처리중" , "데이터 변함!")
                requireActivity().runOnUiThread { Toast.makeText(context, "선택한 날짜의 정보가 없습니다.", Toast.LENGTH_SHORT).show() }
            }
        }

        initLineChart()
        setDataToLineChartHumidity()
        setDataToLineChart()
        initLineChart2()
        setDataToLineChart2()
        initLineChart3()
        setDataToLineChart3()

        return binding.root
    }

    private fun getDate(view:View){
        if(view.id.toString() == binding.TemperatureButton.id.toString())
            calendarState = 1
        else if((view.id.toString() == binding.HumidityButton.id.toString()))
            calendarState = 2

        DatePickerDialog(requireActivity(), R.style.DialogTheme, dateSetListener, calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    // 기본 그래프 초기화
    private fun basicLineChart() {

        var setChart = lineChart

        when (lineChartState) {
            0 -> setChart = lineChart
            1 -> setChart = lineChart
            2 -> setChart = lineChart2
            3 -> setChart = lineChart3
        }

        setChart.axisLeft.setDrawGridLines(false)
        val xAxis: XAxis = setChart.xAxis
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(false)

        //remove right y-axis
        setChart.axisRight.isEnabled = false

        //remove legend
        setChart.legend.isEnabled = false

        //remove description label
        setChart.description.isEnabled = false

        //add animation
        setChart.animateX(1000, Easing.EaseInSine)

        setChart.isDragXEnabled = true

        // to draw label on xAxis
        xAxis.setDrawAxisLine(true)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.valueFormatter = MyAxisFormatter()
        xAxis.setDrawLabels(true)
        xAxis.granularity = 1f
        xAxis.axisLineColor
    }

    // 실시간 온도 그래프 초기화
    private fun initLineChart() {
        lineChartState = 0
        basicLineChart()
    }
    // 실시간 습도 그래프 초기화
    private fun initLineChartHumidity() {
        lineChartState = 1
        basicLineChart()
    }

    // 달력 온도 그래프 초기화
    private fun initLineChart2() {
        lineChartState = 2
        basicLineChart()
    }

    // 달력 습도 그래프 초기화
    private fun initLineChart3() {
        lineChartState = 3
        basicLineChart()
    }

    //그래프 포멧
    inner class MyAxisFormatter : IndexAxisValueFormatter() {
        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            val index = value.toInt()
            when (lineChartState) {
                0 -> {
                    return if (index < sensorList.size) {
                        sensorList[sensorList.size-1].name
                    } else { "" }
                }
                1 -> {
                    return if (index < sensorList1.size) {
                        sensorList1[sensorList1.size-1].name
                    } else { "" }
                }
                2 -> {
                    return if (index < sensorList2.size) {
                        sensorList2[sensorList2.size-1].name
                    } else { "" }
                }
                else -> {
                    return if (index < sensorList3.size) {
                        sensorList3[sensorList3.size-1].name
                    } else { "" }
                }
            }
        }
    }
    //실시간 온드 그래프 설정
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setDataToLineChart() {

        if(setDataToLineChartState != 1) {
            sensorList = getSensorList()
        }

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

    // 실시간 온도 그래프 갱신
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setDataToLineChartRenew() {
        setDataToLineChartState = 1
        setDataToLineChart()
    }

    //실시간 습도 그래프 설정
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setDataToLineChartHumidity() {

        if (setDataToLineChartState != 3) {
            sensorList1 = getSensorList1()
        }
        //now draw bar chart with dynamic data
        val entries1: ArrayList<Entry> = ArrayList()

        //you can replace this data object with  your custom object
        for (i in sensorList1.indices) {
            val sensor1 = sensorList1[i]
            entries1.add(Entry(i.toFloat(), sensor1.humi.toFloat()))
        }

        val lineDataSet = LineDataSet(entries1, "")

        val data = LineData(lineDataSet)
        lineChart.data = data

        lineDataSet.setDrawFilled(true)
        lineDataSet.fillDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.gradient1)
        lineDataSet.color = Color.parseColor("#6441A5")
        lineDataSet.setCircleColor(Color.DKGRAY)

        lineChart.invalidate()
    }

    //실시간 습도 그래프 갱신
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setDataToLineChartRenewHumidity() {
        setDataToLineChartState = 3
        setDataToLineChartHumidity()
    }

    // 달력 온도 그래프 설정
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setDataToLineChart2() {
        //now draw bar chart with dynamic data
        val entries: ArrayList<Entry> = ArrayList()
        if(setDataToLineChartState != 5) {
            sensorList2 = getSensorList2()
        }
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

    // 달력 온도 그래프 갱신
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setDataToLineChartRenewTemperature() {
        setDataToLineChartState = 5
        setDataToLineChart2()
    }
    // 달력 습도 그래프
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setDataToLineChart3() {
        //now draw bar chart with dynamic data
        val entries: ArrayList<Entry> = ArrayList()
        if(setDataToLineChartState != 7) {
            sensorList3 = getSensorList3()
        }
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

    // 달력 습도 그래프 갱신
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setDataToLineChartRenewHumidity1() {
        setDataToLineChartState = 7
        setDataToLineChart3()
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

    // 날짜 그래프 갱신
    @RequiresApi(Build.VERSION_CODES.O)
    fun getSpecificData(data: MutableList<Body>, length: Int, type: String){
        job = CoroutineScope(Dispatchers.IO).launch {
            if(type == "True") {
                var i = 0
                sensorList2.clear()
                sensorList3.clear()
                sensorList2.add(sensor("", 0))
                sensorList3.add(sensor1("", 0))
                while (i < length) {
                    var a = data[i].timestamp
                    val b = data[i].temperature
                    val c = data[i].humidity
                    val time2 = a.substring(a.length - 8, a.length)
                    a = time2.substring(0 until 2)
                    sensorList2.add(sensor(a, b))
                    sensorList3.add(sensor1(a, c))
                    i += 1
                }
                if (calendarState == 1)
                    setDataToLineChartRenewTemperature()
                else
                    setDataToLineChartRenewHumidity1()
            }else{
                print("error~~~~~!!!!1")
            }
        }
    }

    // 실시간 그래프 갱신
    @RequiresApi(Build.VERSION_CODES.O)
    fun setGraph(data:Body) {
        val id = (activity as MainActivity).getSpinnerData()
        if(state == 0) {
            sensorList.removeAt(1)
            sensorList1.removeAt(1)
        }
        if(id == "ERROR")
            requireActivity().runOnUiThread { Toast.makeText(context, "장치 이름을 불러올 수 없습니다.", Toast.LENGTH_SHORT).show() }
        else{
            globalTime = data.timestamp
            globalTemperature = data.temperature
            globalHumidity = data.humidity

            val time1 = globalTime.substring(globalTime.length -8, globalTime.length)
            globalTime = time1.substring(0 until 2)
            Log.d("checkGlobal1 : " , globalTime)
            val compareName = sensorList[globalCount].name
            Log.d("checkGlobal2 : " , compareName)
            val compareTemperature = sensorList[globalCount].temp
            val compareHumidity = sensorList1[globalCount].humi
            if (globalTime == compareName) {
                if(globalTemperature >=0){
                    if (compareTemperature < globalTemperature) {
                        sensorList[globalCount] = sensor(globalTime, globalTemperature)
                    }
                    if(compareHumidity < globalHumidity){
                        sensorList1[globalCount] = sensor1(globalTime, globalHumidity)
                    }
                }else{
                    if (compareTemperature > globalTemperature) {
                        sensorList[globalCount] = sensor(globalTime, globalTemperature)
                    }
                }
            }
            else {
                globalCount += 1
                state = 1
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

    // 모든 그래프 초기화
    @RequiresApi(Build.VERSION_CODES.O)
    fun setGraph2(data:Body) {
        sensorList.clear()
        sensorList1.clear()
        sensorList2.clear()
        sensorList3.clear()
        getSensorList2()
        getSensorList3()
        sensorList.add(sensor("", 0))
        sensorList1.add(sensor1("", 0))

        setDataToLineChartRenewTemperature()
        setDataToLineChartRenewHumidity1()

        globalTime = data.timestamp
        globalTemperature = data.temperature
        globalHumidity = data.humidity
        val time1 = globalTime.substring(globalTime.length -5, globalTime.length)
        globalTime = time1.substring(0 until 2)
        sensorList.add(sensor(globalTime, globalTemperature))
        sensorList1.add(sensor1(globalTime, globalHumidity))
        requireActivity().runOnUiThread {
            binding.temp.text = ""
            binding.humitext1.text = ""
            binding.textViewTemp.text = globalTemperature.toString()
            binding.textViewHumi.text = globalHumidity.toString()
            if (globalState == 1)
                setDataToLineChartRenew()
            else
                setDataToLineChartRenewHumidity()
        }

    }
}