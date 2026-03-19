package com.example.wheatherapp

import android.net.Uri
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import com.example.wheatherapp.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    // b2d4c42835a5c5b54ef451b6589926a4
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        fetchWeatherData("Karachi")
        searchCity()

    }

    private fun searchCity() {
        val searchView = binding.searchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    fetchWeatherData(query)
                    binding.searchView.clearFocus()
                }
                return true
            }

            override fun onQueryTextChange(newText : String?): Boolean {
                return true
            }
        })

    }

    private fun fetchWeatherData(cityName : String) {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .build().create(ApiInterface::class.java)

        val response = retrofit.getWeatherData(cityName,"b2d4c42835a5c5b54ef451b6589926a4","metric")
        response.enqueue(object  : Callback<WeatherApp> {
            override fun onResponse(call: Call<WeatherApp>, response: Response<WeatherApp>) {
               val responseBody = response.body()
                if(response.isSuccessful && responseBody!=null){

                    val temperature = responseBody.main.temp.toString()
                    val humidity = responseBody.main.humidity
                    val windSpeed = responseBody.wind.speed
                    val sunRise = responseBody.sys.sunrise.toLong()
                    val sunSet = responseBody.sys.sunset.toLong()
                    val seaLevel = responseBody.main.pressure
                    val condition = responseBody.weather.firstOrNull()?.main?:"unknown"
                    val maxTemp = responseBody.main.temp_max
                    val minTemp = responseBody.main.temp_min


                    binding.temp.text = "$temperature °C"
                    binding.weather.text = condition
                    binding.maxTem.text = "Max Temp: $maxTemp °C"
                    binding.minTem.text = "Min Temp: $minTemp °C"
                    binding.humidity.text = "$humidity %"
                    binding.windSpeed.text = "$windSpeed m/s"
                    binding.sunRise.text = "${time(sunRise)}"
                    binding.sunSet.text = "${time(sunSet)}"
                    binding.sea.text = "$seaLevel hPa"
                    binding.condition.text = condition
                    binding.day.text = dayName(System.currentTimeMillis())
                    binding.date.text = date()
                    binding.cityName.text = "$cityName"

                    changeBackground(condition)
                }
            }
            override fun onFailure(p0: Call<WeatherApp?>?, p1: Throwable?) {
            }
        })
        }

    private fun changeBackground(conditions: String) {

        val (videoRes, lottieRes) = when(conditions.lowercase()) {

            "clear Sky", "sunny", "clear" ->
                Pair(R.raw.sunny_weather01, R.raw.sun)

            "partly clouds", "clouds" ->
                Pair(R.raw.partly_clouds_weather01, R.raw.cloud)

            "haze", "mist", "foggy" , "smoke" , "dust" ->
                Pair(R.raw.foggy_weather01, R.raw.cloud)

            "overcast" ->
                Pair(R.raw.overcast_weather01, R.raw.cloud)

            "light rain", "drizzle", "moderate rain", "showers" ,"rain"->
                Pair(R.raw.light_rain_weather01, R.raw.rain)

            "heavy rain" , "thunderstorm" ->
                Pair(R.raw.heavy_rain_weather01, R.raw.rain)

            "light snow", "moderate snow", "blizzard" ->
                Pair(R.raw.light_snow_weather01, R.raw.snow)

            "heavy snow" ->
                Pair(R.raw.heavy_snow_weather01, R.raw.snow)

            else ->
                Pair(R.raw.sunny_weather01, R.raw.sun)
        }

        binding.lottieAnimationView.setAnimation(lottieRes)
        binding.lottieAnimationView.playAnimation()

        playVideo(videoRes)
    }

    private fun playVideo(videoResId: Int) {
        val path = "android.resource://$packageName/$videoResId"
        binding.videoBackground.apply {
            setVideoURI(Uri.parse(path))
            setOnPreparedListener { mediaPlayer ->
                mediaPlayer.isLooping = true
                mediaPlayer.setVolume(0f, 0f)
                start()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.videoBackground.start()
    }

    override fun onPause() {
        super.onPause()
        binding.videoBackground.pause()
    }

    fun dayName(timestamp : Long) : String{
            val sdf = SimpleDateFormat("EEEE",Locale.getDefault())
            return sdf.format((Date()))
    }
    fun time(timestamp : Long) : String{
        val sdf = SimpleDateFormat("HH:mm",Locale.getDefault())
        return sdf.format((timestamp*1000))
    }

    private fun date() : String{
        val sdf = SimpleDateFormat("dd MMMM yyyy",Locale.getDefault())
        return sdf.format((Date()))
    }
}