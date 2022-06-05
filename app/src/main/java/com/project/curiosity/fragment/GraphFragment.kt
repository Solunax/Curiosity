package com.project.curiosity.fragment
import android.accounts.AccountManager.get
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
import java.lang.reflect.Array.get
import java.nio.file.Paths.get
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.timer

private var sensorList = ArrayList<sensor>()
private var sensorList1 = ArrayList<sensor1>()
private var sensorList2 = ArrayList<sensor>() // 특정 날짜 temp
private var sensorList3 = ArrayList<sensor1>() // 특정 날짜 humi
private var globalstring :String = ""
private var globaltime :String = ""
private var globaltemp :Int = 0
private var globalhumi :Int = 0
var globalcount = 1
var global_state = 1
var calendar_state = 1
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


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = GraphFragmentBinding.inflate(inflater, container, false)
        val temp = binding.textViewTemp
        val humi = binding.textViewHumi
        val temp_text = binding.temp
        val humi_text = binding.humitext1
        val imageButton_temp = binding.imageButtonTemp
        val imageButton_humi = binding.imageButtonHumi
        val imageButton_temp_serach = binding.imageButton7
        val imageButton_humi_serach = binding.imageButton6

        lineChart = binding.lineChart
        lineChart2 = binding.lineChart2
        lineChart3 = binding.lineChart3

        val timer = timer(period = 10000) {
            getData1("curiosity", "")
        }

        //var a = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) //"yyyy-MM-dd HH:mm:ss"

        imageButton_temp.setOnClickListener {
            global_state = 1
            setDataToLineChart_renew()

        }

        imageButton_humi.setOnClickListener {
            global_state = 2
            setDataToLineChart_renew_humi()
        }

        imageButton_temp_serach.setOnClickListener {
            calendar_state = 1
            val cal = Calendar.getInstance()    //캘린더뷰 만들기
            val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                if(month + 1 <= 9)
                    dateString = "${year}-${"0" + (month+1)}-${dayOfMonth}"
                else
                    dateString = "${year}-${month+1}-${dayOfMonth}"

                getData1("curiosity", dateString)
                temp_text.setText(dateString)
            }
            DatePickerDialog(requireActivity(), dateSetListener, cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH)).show()
        }



        imageButton_humi_serach.setOnClickListener {
            calendar_state = 2
            val cal = Calendar.getInstance()    //캘린더뷰 만들기
            val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                if(month + 1 <= 9)
                    dateString = "${year}-${"0" + (month+1)}-${dayOfMonth}"
                else
                    dateString = "${year}-${month+1}-${dayOfMonth}"

                getData1("curiosity", dateString)
                humi_text.setText(dateString)
            }
            DatePickerDialog(requireActivity(), dateSetListener, cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        initLineChart()
        setDataToLineChart()
        setDataToLineChart_humi()
        initLineChart2()
        setDataToLineChart2()
        initLineChart3()
        setDataToLineChart3()

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

        lineChart2.setDragXEnabled(true);

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

        lineChart3.setDragXEnabled(true);

        // to draw label on xAxis
        xAxis.setDrawAxisLine(true)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.valueFormatter = MyAxisFormatter3()
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
    //humi1
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setDataToLineChart2() {
        //now draw bar chart with dynamic data
        val entries: ArrayList<Entry> = ArrayList()

        sensorList2 = getsensorList2()

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
        lineDataSet.setColor(Color.parseColor("#6441A5"))
        lineDataSet.setCircleColor(Color.DKGRAY);

        lineChart2.invalidate()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setDataToLineChart3() {
        //now draw bar chart with dynamic data
        val entries: ArrayList<Entry> = ArrayList()

        sensorList3 = getsensorList3()

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
        lineDataSet.setColor(Color.parseColor("#6441A5"))
        lineDataSet.setCircleColor(Color.DKGRAY);

        lineChart3.invalidate()
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
    // temp1
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setDataToLineChart_renew_temp1() {
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
        lineDataSet.setColor(Color.parseColor("#6441A5"))
        lineDataSet.setCircleColor(Color.DKGRAY);

        lineChart2.invalidate()
    }

    // humi1
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setDataToLineChart_renew_humi1() {
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
        lineDataSet.setColor(Color.parseColor("#6441A5"))
        lineDataSet.setCircleColor(Color.DKGRAY);

        lineChart3.invalidate()
    }

    // temp
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getsensorList(): ArrayList<sensor> {
        sensorList.add(sensor("", 0))
        sensorList.add(sensor("", 0))

        return sensorList
    }

    // humi
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getsensorList1(): ArrayList<sensor1> {
        sensorList1.add(sensor1("", 0))
        sensorList1.add(sensor1("", 0))

        return sensorList1
    }
    // temp1
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getsensorList2(): ArrayList<sensor> {
        sensorList2.add(sensor("", 0))
        sensorList2.add(sensor("", 0))
        return sensorList2
    }
    // humi1
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getsensorList3(): ArrayList<sensor1> {
        sensorList3.add(sensor1("", 0))
        sensorList3.add(sensor1("", 0))
        return sensorList3
    }


    private fun getData1(nameValue: String, timeValue: String) {
        job = CoroutineScope(Dispatchers.IO).launch {
            val request = Request2(nameValue, timeValue)
            val response = ApiClient1.getApiClient1().getData1(request)
            if (response.isSuccessful && response.body()!!.statusCode == 200) {

                globaltime = response.body()!!.body[0].timestamp
                globaltemp = response.body()!!.body[0].temperature
                globalhumi = response.body()!!.body[0].humidity

                var time1 = globaltime.substring(globaltime.length -5, globaltime.length)
                globaltime = time1.substring(0 until 2)
                var compare_name = sensorList.get(globalcount).name
                var compare_temp = sensorList.get(globalcount).temp
                var compare_humi = sensorList1.get(globalcount).humi
                if (globaltime == compare_name) {
                    if (compare_temp < globaltemp) {
                        sensorList.set(globalcount, sensor(globaltime, globaltemp))}
                    if(compare_humi < globalhumi){
                        sensorList1.set(globalcount, sensor1(globaltime, globalhumi))
                    }
                }
                else {
                    globalcount += 1
                    sensorList.add(sensor(globaltime, globaltemp))
                    sensorList1.add(sensor1(globaltime, globalhumi))
                }
            }
            else if (response.isSuccessful && response.body()!!.statusCode == 300) {
                var i = 0
                var count = response.body()!!.length
                sensorList2.clear()
                sensorList3.clear()
                sensorList2.add(sensor("", 0))
                sensorList3.add(sensor1("", 0))
                while(i < count) {
                    var a = response.body()!!.body[i].timestamp
                    var b = response.body()!!.body[i].temperature
                    var c = response.body()!!.body[i].humidity
                    var time2 = a.substring(a.length - 5, a.length)
                    a = time2.substring(0 until 2)
                    sensorList2.add(sensor(a, b))
                    sensorList3.add(sensor1(a, c))
                    i += 1
                }
                if(calendar_state == 1)
                    setDataToLineChart_renew_temp1()
                else
                    setDataToLineChart_renew_humi1()
            }
            else if (response.isSuccessful && response.body()!!.statusCode == 100) {
                Toast.makeText(getActivity(), "myText", Toast.LENGTH_SHORT).show();
            }

            requireActivity().runOnUiThread {
                binding.textViewTemp.setText(globaltemp.toString())
                binding.textViewHumi.setText(globalhumi.toString())
                if (global_state == 1)
                    setDataToLineChart_renew()
                else
                    setDataToLineChart_renew_humi()


            }
        }
    }
}







//        initLineChart2()
//        setDataToLineChart2()
//        initLineChart3()
//        setDataToLineChart3()

