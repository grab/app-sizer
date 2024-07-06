package com.grab.sizer.report.json

import com.google.gson.annotations.SerializedName

data class Metrics(
    @SerializedName("name")
    val name: String,
    @SerializedName("fields")
    val fields: List<Field>,
    @SerializedName("tags")
    val tags: List<Tag>,
    @SerializedName("timestamp")
    val timestamp: Long
)

data class Field(
    @SerializedName("name")
    val name: String,
    @SerializedName("value")
    val value: String,
    /**
     * Values: "float", "string", "integer", "boolean"
     */
    @SerializedName("value_type")
    val valueType: String
)

data class Tag(
    @SerializedName("name")
    val name: String,
    @SerializedName("value")
    val value: String,
    @SerializedName("value_type")
    val valueType: String
)