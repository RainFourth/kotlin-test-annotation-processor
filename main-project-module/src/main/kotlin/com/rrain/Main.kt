package com.rrain

import com.rrain.annotations.AllArgsConstructor
import com.rrain.utils.Data3

fun main(){
    val d0 = Data3()
    val d1 = Data3(1)
    val d5 = Data3(1, "dl")
    val d3 = Data3(1,"2",true)
    println(d3)

    println(Data4(0,1,"2",true))
}

@AllArgsConstructor
class Data3{
    var a: Int = 0
    var b: String? = null
    var c: Boolean = false

    override fun toString(): String {
        return "Data3(a=$a, b=$b, c=$c)"
    }
}

@AllArgsConstructor
class Data4(var z: Int? = null, tmp: String? = null){
    var a: Int = 0
    var b: String? = null
    var c: Boolean = false

    override fun toString(): String {
        return "Data4(z=$z, a=$a, b=$b, c=$c)"
    }
}


fun Data3(a: Int, b: String) = Data3().apply {
        this.a = a
        this.b = b
}
