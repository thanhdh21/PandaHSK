package com.example.hoctiengtrung2.data.remote.response

import com.google.gson.JsonElement

data class ThivienResponse(
    val mode: String? = null,
    val lang: String? = null,
    val message: String? = null,
    val result: List<ThivienResult>? = null
)

data class ThivienResult(
    val t: Int? = null,
    val i: String? = null, // input character
    val o: JsonElement? = null, // output reading(s), can be list or string
    val c: Boolean? = null
) {
    fun getFirstReading(): String? {
        return when {
            o == null -> null
            o.isJsonArray -> {
                val array = o.asJsonArray
                if (array.size() > 0) array.get(0).asString else null
            }
            o.isJsonPrimitive -> o.asString
            else -> null
        }
    }

    fun getAllReadings(): List<String> {
        return when {
            o == null -> emptyList()
            o.isJsonArray -> o.asJsonArray.map { it.asString }
            o.isJsonPrimitive -> listOf(o.asString)
            else -> emptyList()
        }
    }
}
