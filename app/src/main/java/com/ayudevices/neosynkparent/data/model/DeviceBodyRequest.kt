package com.ayudevices.neosynkparent.data.model

import com.google.gson.annotations.SerializedName

data class DeviceBodyRequest(
    @SerializedName("parent_id") val parentId: String,
    @SerializedName("child_id") val childId: String,
    @SerializedName("requested_device") val reqDevice: List<String>
)
