package com.example.pictureoftheday

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import coil.api.load
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class MainActivity : AppCompatActivity() {

    private val retrofitImpl: RetrofitImpl = RetrofitImpl()
    private val callback = object :
        Callback<DataModel> {

        override fun onResponse(
            call: Call<DataModel>,
            response: Response<DataModel>
        ) {
            if (response.isSuccessful && response.body() != null) {
                renderData(response.body(), null)
            } else {
                renderData(null, Throwable("Ответ от сервера пустой"))
            }
        }

        override fun onFailure(call: Call<DataModel>, t: Throwable) {
            renderData(null, t)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sendServerRequest()
    }

    private fun sendServerRequest() {
        retrofitImpl.getRequest().getPictureOfTheDay("DEMO_KEY").enqueue(callback)
    }

    private fun renderData(dataModel: DataModel?, error: Throwable?) {
        if (dataModel == null || error != null) {
            Toast.makeText(this, error?.message, Toast.LENGTH_LONG).show()//Ошибка
        } else {
            val url = dataModel.url
            if (url.isNullOrEmpty()) {
                //"Ссылка на фото пустая")
            } else if (url.contains("www.youtube.com")) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            } else {
                image_view.load(url)
            }
            val explanation = dataModel.explanation
            if (explanation.isNullOrEmpty()) {
                //"Описание пустое"
            } else {
                text_view.text = explanation
            }
        }
    }
}

data class DataModel(
    val explanation: String?,
    val url: String?
)

interface PictureOfTheDayAPI {
    @GET("planetary/apod")
    fun getPictureOfTheDay(@Query("api_key") apiKey: String): Call<DataModel>
}

class RetrofitImpl {

    fun getRequest(): PictureOfTheDayAPI {
        val podRetrofit = Retrofit.Builder()
            .baseUrl("https://api.nasa.gov/")
            .addConverterFactory(
                GsonConverterFactory.create(
                    GsonBuilder().setLenient().create()
                )
            )
            .build()
        return podRetrofit.create(PictureOfTheDayAPI::class.java)
    }
}
