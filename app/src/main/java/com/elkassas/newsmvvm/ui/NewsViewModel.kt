package com.elkassas.newsmvvm.ui

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.*
import android.net.NetworkCapabilities.*
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elkassas.newsmvvm.NewsApplication
import com.elkassas.newsmvvm.data.Article
import com.elkassas.newsmvvm.data.NewsResponse
import com.elkassas.newsmvvm.repository.NewsRepository
import com.elkassas.newsmvvm.utils.ApiResources
import kotlinx.coroutines.launch
import retrofit2.Response
import okio.IOException

class NewsViewModel(app: Application, val newsRepository: NewsRepository) : AndroidViewModel(app) {

    //breaking news mutable live data and page number
    val breakingNews: MutableLiveData<ApiResources<NewsResponse>> = MutableLiveData()
    var breakingNewsPage = 1
    var breakingNewsResponse: NewsResponse? = null

    //search for news mutable live data and page number
    val searchNews: MutableLiveData<ApiResources<NewsResponse>> = MutableLiveData()
    var searchNewsPage = 1
    var searchNewsResponse: NewsResponse? = null
    var newSearchQuery:String? = null
    var oldSearchQuery:String? = null

    init {
        getBreakingNews("eg")
    }

    fun getBreakingNews(countryCode: String) = viewModelScope.launch {
        breakingNews.postValue(ApiResources.Loading())
        val response = newsRepository.getBreakingNews(countryCode, breakingNewsPage)

        breakingNews.postValue(handleBreakingNewsResponse(response))
    }

    fun searchNews(searchQuery: String) = viewModelScope.launch {
        searchNews.postValue(ApiResources.Loading())
        val response = newsRepository.searchNews(searchQuery, searchNewsPage)

        searchNews.postValue(handleSearchNewsResponse(response))
    }


    private fun handleBreakingNewsResponse(response: Response<NewsResponse>): ApiResources<NewsResponse> {

        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                breakingNewsPage++
                if (breakingNewsResponse == null) {
                    breakingNewsResponse = resultResponse
                } else {
                    val oldArticles = breakingNewsResponse?.articles
                    val newArticles = resultResponse.articles
                    oldArticles?.addAll(newArticles)
                }

                return ApiResources.Success(breakingNewsResponse ?: resultResponse)
            }
        }
        return ApiResources.Error(response.message())

    }

    private fun handleSearchNewsResponse(response: Response<NewsResponse>): ApiResources<NewsResponse> {

        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->

                searchNewsPage++
                if (searchNewsResponse == null) {
                    oldSearchQuery = newSearchQuery
                    searchNewsResponse = resultResponse

                } else {
                    val oldArticles = searchNewsResponse?.articles
                    val newArticles = resultResponse.articles
                    oldArticles?.addAll(newArticles)
                }

                return ApiResources.Success(searchNewsResponse ?: resultResponse)
            }
        }
        return ApiResources.Error(response.message())

    }

    //Room functions

    fun saveArticle(article: Article) = viewModelScope.launch {
        newsRepository.insertSaved(article)
    }

    fun getSavedArticles() = newsRepository.getSavedArticles()

    fun deleteArticle(article: Article) = viewModelScope.launch {
        newsRepository.deleteSaved(article)
    }

    private suspend fun safeSearchNewsCall(searchQuery: String) {
        newSearchQuery = searchQuery
        searchNews.postValue(ApiResources.Loading())
        try {
            if(hasInternetConnection()) {
                val response = newsRepository.searchNews(searchQuery, searchNewsPage)
                searchNews.postValue(handleSearchNewsResponse(response))
            } else {
                searchNews.postValue(ApiResources.Error("No internet connection"))
            }
        } catch(t: Throwable) {
            when(t) {
                is IOException -> searchNews.postValue(ApiResources.Error("Network Failure"))
                else -> searchNews.postValue(ApiResources.Error("Conversion Error"))
            }
        }
    }
    private suspend fun safeBreakingNewsCall(countryCode: String) {
        breakingNews.postValue(ApiResources.Loading())
        try {
            if(hasInternetConnection()) {
                val response = newsRepository.getBreakingNews(countryCode, breakingNewsPage)
                breakingNews.postValue(handleBreakingNewsResponse(response))
            } else {
                breakingNews.postValue(ApiResources.Error("No internet connection"))
            }
        } catch(t: Throwable) {
            when(t) {
                is IOException -> breakingNews.postValue(ApiResources.Error("Network Failure"))
                else -> breakingNews.postValue(ApiResources.Error("Conversion Error"))
            }
        }
    }
    private fun hasInternetConnection(): Boolean {
        val connectivityManager = getApplication<NewsApplication>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            return when {
                capabilities.hasTransport(TRANSPORT_WIFI) -> true
                capabilities.hasTransport(TRANSPORT_CELLULAR) -> true
                capabilities.hasTransport(TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.activeNetworkInfo?.run {
                return when(type) {
                    TYPE_WIFI -> true
                    TYPE_MOBILE -> true
                    TYPE_ETHERNET -> true
                    else -> false
                }
            }
        }
        return false
    }

}