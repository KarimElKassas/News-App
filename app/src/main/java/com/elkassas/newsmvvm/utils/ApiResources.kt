package com.elkassas.newsmvvm.utils

sealed class ApiResources<T>(
    val data : T? = null,
    val message : String? = null
){

    class Success<T>(data: T) : ApiResources<T>(data)
    class Error<T>(message: String, data: T? = null) : ApiResources<T>(data, message)
    class Loading<T> : ApiResources<T>()

}
