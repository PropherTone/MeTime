package com.protone.database.sp

import kotlin.reflect.KProperty

class DataTool(val dataDelegate: DataDelegate){

    fun int(key: String,defValue: Int): Delegate<Int> {
        return object : Delegate<Int>{
            override fun getValue(thisRef: Any?, property: KProperty<*>): Int {
                return dataDelegate.getInt(key, defValue)
            }

            override fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
                dataDelegate.setInt(key, value)
            }
        }
    }

    fun long(key: String,defValue: Long): Delegate<Long> {
        return object : Delegate<Long>{
            override fun getValue(thisRef: Any?, property: KProperty<*>): Long {
                return dataDelegate.getLong(key, defValue)
            }

            override fun setValue(thisRef: Any?, property: KProperty<*>, value: Long) {
                dataDelegate.setLong(key, value)
            }
        }
    }

    fun string(key: String,defValue: String): Delegate<String> {
        return object : Delegate<String>{
            override fun getValue(thisRef: Any?, property: KProperty<*>): String {
                return dataDelegate.getString(key, defValue)
            }

            override fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
                dataDelegate.setString(key,value)
            }
        }
    }

    fun boolean(key: String,defValue: Boolean):Delegate<Boolean>{
        return object : Delegate<Boolean>{
            override fun getValue(thisRef: Any?, property: KProperty<*>): Boolean {
                return dataDelegate.getBoolean(key, defValue)
            }

            override fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) {
                return dataDelegate.setBoolean(key, value)
            }
        }
    }

}