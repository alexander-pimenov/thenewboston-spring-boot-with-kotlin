package tv.codealong.tutorials.various.dao.dictionary

import com.google.gson.Gson
import java.io.Serializable

class Reference(val list: MutableList<String>): Serializable  {
    fun json(): String = Gson().toJson(this)
}