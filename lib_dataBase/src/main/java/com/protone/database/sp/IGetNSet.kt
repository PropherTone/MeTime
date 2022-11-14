package com.protone.database.sp

interface IGetNSet {
    fun setInt(key:String,value:Int)
    fun getInt(key:String,defValue:Int): Int
    fun setLong(key:String,value:Long)
    fun getLong(key:String,defValue:Long): Long
    fun setString(key:String,value:String)
    fun getString(key:String,defValue:String): String
    fun setBoolean(key:String, value:Boolean)
    fun getBoolean(key:String,defValue:Boolean): Boolean
}