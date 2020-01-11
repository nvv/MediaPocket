package com.mediapocket.android.mapper

interface Mapper<T, R> {

    fun map(item: T) : R

}