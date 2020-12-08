package com.example.coronaapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.bumptech.glide.Glide
import com.example.coronaapp.model.CountriesItem
import com.example.coronaapp.model.InfoNegara
import com.example.coronaapp.network.ApiService
import com.example.coronaapp.network.InfoService
import com.example.coronaapp.network.RetrofitBuilder
import kotlinx.android.synthetic.main.activity_detail_chart_country.*
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*
import java.util.concurrent.TimeUnit
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.sql.Date
import java.text.SimpleDateFormat
import kotlin.collections.ArrayList

class DetailChartCountry : AppCompatActivity() {

    companion object {
        const val EXTRA_COUNTRY = "EXTRA_COUNTRY"
        lateinit var simpanDataNegara: String
        lateinit var simpanDataFlag: String
    }

    private val sharedPrefFile = "kotlinsharedpreferences" //untuk membuat nama/variable file menyimpan data
    private lateinit var sharedPreferences : SharedPreferences //digunakan ntuk memproseskan data
    private var dayCases = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_chart_country)

        sharedPreferences = this.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE) // untuk membatasi activity
        val editor : SharedPreferences.Editor = sharedPreferences.edit()

        //dapatkan data tidak null
        val data = intent.getParcelableExtra<CountriesItem>(EXTRA_COUNTRY)
        val formatter: NumberFormat = DecimalFormat("#,###")

        // Jika data tidak null
        data?.let {
            txt_countryName.text = data.country
            latest_update.text = data.date
            txt_totalCurrentConfirmed.text = formatter.format(data.totalConfirmed?.toDouble())
            txt_newConfirmed.text = formatter.format(data.totalConfirmed?.toDouble())
            txt_totalCurrentDeaths.text = formatter.format(data.totalDeaths?.toDouble())
            txt_newDeaths.text = formatter.format(data.newDeaths?.toDouble())
            txt_totalCurrentRecovered.text = formatter.format(data.totalRecovered?.toDouble())
            txt_newRecovered.text = formatter.format(data.newRecovered?.toDouble())



            editor.putString(data.country, data.country)// untuk menyimpan
            editor.apply()
            editor.commit()

            val simpanNegara = sharedPreferences.getString(data.country, data.country)
            val simpanFlag = sharedPreferences.getString(data.countryCode, data.countryCode)
            simpanDataNegara = simpanNegara.toString()
            simpanDataFlag = simpanFlag.toString() + "/flat/64.png"

            if (simpanFlag != null ) {
                Glide.with(this).load ("https://www.countryflags.io/$simpanDataFlag" )
                    .into(img_countryFlag)
            }else {
                Toast.makeText(this, "Gambar Tidak Ketemu", Toast.LENGTH_SHORT).show()
            }
            getChart()
        }


    }

    private fun getChart() {
        val okhttp = OkHttpClient().newBuilder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.covid19api.com/dayone/country/")
            .client(okhttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(InfoService::class.java)
        api.getInfoService(simpanDataNegara).enqueue(object : Callback<List<InfoNegara>> {
            override fun onFailure(call: Call<List<InfoNegara>>, t: Throwable) {
                Toast.makeText(this@DetailChartCountry, "Error", Toast.LENGTH_SHORT).show()

            }

            @SuppressLint("SimpleDateFormat")
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(
                call: Call<List<InfoNegara>>, response: Response<List<InfoNegara>>) {
                val getListDataCorona : List<InfoNegara> = response.body()!!
                if (response.isSuccessful) {
                    val barEnteries : ArrayList<BarEntry> = ArrayList()
                    val barEnteries2 : ArrayList<BarEntry> = ArrayList()
                    val barEnteries3 : ArrayList<BarEntry> = ArrayList()
                    val barEnteries4 : ArrayList<BarEntry> = ArrayList()
                    var i = 0

                    while (i < getListDataCorona.size) {
                        for (s in getListDataCorona) {
                            val barEntry = BarEntry(i.toFloat(), s.Confirmed?.toFloat() ?: 0f)
                            val barEntry2 = BarEntry(i.toFloat(), s.Deaths?.toFloat() ?: 0f)
                            val barEntry3 = BarEntry(i.toFloat(), s.Recovered?.toFloat() ?: 0f)
                            val barEntry4 = BarEntry(i.toFloat(), s.Active?.toFloat() ?: 0f)

                            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:SS'Z'")
                            val outputFormat = SimpleDateFormat("dd-MM-yyyy")
                            @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS") val date: java.util.Date? = inputFormat.parse(s.Date!!)
                            val formattedDate: String = outputFormat.format(date!!)
                            dayCases.add(formattedDate)

                            barEnteries.add(barEntry)
                            barEnteries2.add(barEntry2)
                            barEnteries3.add(barEntry3)
                            barEnteries4.add(barEntry4)
                            i++
                        }
                        val xAxis: XAxis = chart_data1.xAxis
                        xAxis.valueFormatter = IndexAxisValueFormatter(dayCases)
                        chart_data1.axisLeft.axisMinimum = 0f
                        xAxis.position = XAxis.XAxisPosition.BOTTOM
                        xAxis.granularity = 1f
                        xAxis.setCenterAxisLabels(true)
                        xAxis.isGranularityEnabled = true

                        val barDataSet = BarDataSet(barEnteries, "Confirmed")
                        val barDataSet2 = BarDataSet(barEnteries2, "Deaths")
                        val barDataSet3 = BarDataSet(barEnteries3, "Recovered")
                        val barDataSet4 = BarDataSet(barEnteries4, "Active")

                        barDataSet.setColors(Color.parseColor("#F44336"))
                        barDataSet2.setColors(Color.parseColor("#FFEB3B"))
                        barDataSet3.setColors(Color.parseColor("#03DAC5"))
                        barDataSet4.setColors(Color.parseColor("#2196F3"))

                        val data = BarData(barDataSet, barDataSet2, barDataSet3, barDataSet4)
                        chart_data1.data = data

                        val barSpace = 0.02f
                        val groupSpace = 0.3f
                        val groupCount = 4f

                        data.barWidth = 0.15f
                        chart_data1.invalidate()
                        chart_data1.setNoDataTextColor(R.color.cardview_dark_background)
                        chart_data1.setTouchEnabled(true)
                        chart_data1.description.isEnabled = false
                        chart_data1.xAxis.axisMinimum = 0f
                        chart_data1.setVisibleXRangeMaximum(
                            0f + chart_data1.barData.getGroupWidth(
                                groupSpace,
                                barSpace
                            ) * groupCount
                        )
                        chart_data1.groupBars(0f,groupSpace, barSpace)

                    }
                }
            }


        })

    }
}