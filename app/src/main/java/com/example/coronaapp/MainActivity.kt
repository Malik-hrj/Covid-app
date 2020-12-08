package com.example.coronaapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coronaapp.adapter.AdapterCountry
import com.example.coronaapp.model.CountriesItem
import com.example.coronaapp.model.ResponseCountry
import com.example.coronaapp.network.ApiService
import com.example.coronaapp.network.RetrofitBuilder.retrofit
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.ArrayList

class MainActivity : AppCompatActivity() {

    private var ascending = true
    companion object {
        private lateinit var adapterNegara: AdapterCountry
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        search_view.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p5: String?): Boolean {
                return false
            }
            override fun onQueryTextChange(p0: String?): Boolean {
                adapterNegara.filter.filter(p0)
                return false
            }

        })

        swipe_refresh.setOnRefreshListener {
            getNegara()
            swipe_refresh.isRefreshing = false
        }
        initializedView()
        getNegara()
    }

    private fun initializedView() {
        btnSequence.setOnClickListener {
            sequenceWithoutInternet(ascending)
        }
    }

    private fun sequenceWithoutInternet(ascending: Boolean) {
        rv_country.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@MainActivity)
            if (ascending) {
                (layoutManager as LinearLayoutManager).reverseLayout = true
                (layoutManager as LinearLayoutManager).stackFromEnd  = true
                Toast.makeText(this@MainActivity, "Z-A", Toast.LENGTH_SHORT).show()
            }
            adapter = adapterNegara
        }
    }

    private fun getNegara() {
        val api = retrofit.create(ApiService::class.java)
        api.getAllNegara().enqueue(object : Callback<ResponseCountry> {
            override fun onFailure(call: Call<ResponseCountry>, t: Throwable) {
                progress_bar.visibility = View.GONE
            }
            override fun onResponse(
                call: Call<ResponseCountry>,
                response: Response<ResponseCountry> ) {
                if (response.isSuccessful) {
                    val getListDataCorona = response.body()!!.global
                    val formatter: NumberFormat = DecimalFormat("#,###")
                    txt_confirmed_globe.text = formatter.format(getListDataCorona?.totalConfirmed?.toDouble())
                    txt_recovered_globe.text = formatter.format(getListDataCorona?.totalRecovered?.toDouble())
                    txt_death_globe.text = formatter.format(getListDataCorona?.totalDeaths?.toDouble())
                    rv_country.apply {
                        setHasFixedSize(true)
                        layoutManager = LinearLayoutManager(this@MainActivity)
                        progress_bar?.visibility = View.GONE
                        adapterNegara = AdapterCountry(response.body()!!.countries as ArrayList<CountriesItem>)
                        {Negara -> itemClicked(Negara) }
                        adapter = adapterNegara

                    }
                }else{
                    progress_bar?.visibility = View.GONE
                }
            }
        })
    }

    private fun itemClicked(negara: CountriesItem) {
        val moveWithData = Intent(this@MainActivity, DetailChartCountry::class.java)
        moveWithData.putExtra(DetailChartCountry.EXTRA_COUNTRY, negara)
        startActivity(moveWithData)


    }
}


