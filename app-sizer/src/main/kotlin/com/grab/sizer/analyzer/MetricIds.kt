package com.grab.sizer.analyzer

import com.grab.sizer.report.*
import com.grab.sizer.report.FIELD_KEY_CONTRIBUTOR
import com.grab.sizer.report.FIELD_KEY_OWNER
import com.grab.sizer.report.FIELD_KEY_SIZE
import com.grab.sizer.report.FIELD_KEY_TAG

internal const val LIBRARY_METRICS_ID = "library"
internal const val METRICS_ID_APK = "apk"
internal const val METRICS_ID_BASIC = "apk_basic"
internal const val METRICS_ID_CODEBASE = "team"
internal const val METRICS_ID_LARGE_FILES = "large_file"
internal const val LIB_CONTENT_METRICS_ID = "library_content"
internal const val METRICS_ID_MODULES = "module"
internal const val NOT_AVAILABLE_VALUE = "NA"


internal fun createRow(name: String, value: Long, owner: String = NOT_AVAILABLE_VALUE, tag: String = NOT_AVAILABLE_VALUE, rowName: String? = null): Row = Row(
    fields = listOf(
        TagField(
            name = FIELD_KEY_CONTRIBUTOR,
            value = name
        ),
        TagField(
            name = FIELD_KEY_OWNER,
            value = owner
        ),
        TagField(
            name = FIELD_KEY_TAG,
            value = tag
        ),
        DefaultField(
            name = FIELD_KEY_SIZE,
            value = value
        )
    ),
    name = rowName ?: name
)