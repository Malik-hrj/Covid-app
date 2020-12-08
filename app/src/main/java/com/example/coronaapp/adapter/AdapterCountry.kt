package com.example.coronaapp.adapter

import android.icu.text.DecimalFormat
import android.icu.text.NumberFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.coronaapp.R
import com.example.coronaapp.model.CountriesItem
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.list_corona.view.*
import java.util.*
import kotlin.collections.ArrayList

class AdapterCountry(private val country: java.util.ArrayList<CountriesItem>, private val clickListener: (CountriesItem) -> Unit) :
    RecyclerView.Adapter<CountryViewHolder>(), Filterable {

    var countryfirstList = ArrayList<CountriesItem>()
    init {
        countryfirstList = country
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_corona, parent, false)
        return CountryViewHolder(view)
    }

    override fun getItemCount(): Int {
        return countryfirstList.size
    }

    override fun onBindViewHolder(holder: CountryViewHolder, position: Int) {
        val data = countryfirstList[position]
        holder.bind(data, clickListener)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(p0: CharSequence?): FilterResults {
                val charSearch = p0.toString()
                countryfirstList = if (charSearch.isEmpty()){
                    country
                }else{
                    val resultList = ArrayList<CountriesItem>()
                    for (row in country){
                        val search = row.country!!.toLowerCase(Locale.ROOT)
                        if (search.contains(charSearch.toLowerCase(Locale.ROOT))) {
                            resultList.add(row)
                        }
                    }
                    resultList
                }
                val filterResult = FilterResults()
                filterResult.values = countryfirstList
                return filterResult
            }

            override fun publishResults(p0: CharSequence?, p1: FilterResults?) {
                countryfirstList = p1?.values as ArrayList<CountriesItem>
                notifyDataSetChanged()
            }
        }
    }
}

class CountryViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
    fun bind(negara: CountriesItem, clickListener: (CountriesItem) -> Unit) {
        val name_country : TextView = itemView.tv_countryName
        val flag_negara : CircleImageView = itemView.img_flag_circle
        val country_totalcase : TextView = itemView.tv_countryTotalCase
        val country_totalRecovered : TextView = itemView.tv_countryTotalRecovered
        val country_totalDeath : TextView = itemView.tv_countryTotalDeath

        val formatter: java.text.NumberFormat = java.text.DecimalFormat("#,###")

        name_country.tv_countryName.text = negara.country
        country_totalcase.tv_countryTotalCase.text =
            formatter.format(negara.totalConfirmed?.toDouble())
        country_totalRecovered.tv_countryTotalRecovered.text =
            formatter.format(negara.totalRecovered?.toDouble())
        country_totalDeath.tv_countryTotalDeath.text =
            formatter.format(negara.totalDeaths?.toDouble())


        Glide.with(itemView)
            .load("https://www.countryflags.io/" + negara.countryCode + "/flat/64.png")
            .into(flag_negara)

        name_country.setOnClickListener{clickListener(negara)}
        flag_negara.setOnClickListener{clickListener(negara)}
        country_totalcase.setOnClickListener{clickListener(negara)}
        country_totalDeath.setOnClickListener{clickListener(negara)}
        country_totalRecovered.setOnClickListener{clickListener(negara)}


    }
}