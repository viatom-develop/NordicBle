package com.lepu.anxin.retrofit.post

import com.google.gson.Gson

class GetDepartmentList(doctorId: String) {

    val id = doctorId

    override fun toString(): String {
        return Gson().toJson(this)
    }
}