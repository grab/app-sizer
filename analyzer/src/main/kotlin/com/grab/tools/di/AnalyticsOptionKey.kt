package com.grab.tools.di

import com.grab.tools.AnalyticsOption
import dagger.MapKey
import java.lang.annotation.Documented
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

/** A [MapKey] annotation for maps with [AnalyticsOption] keys.  */

@Documented
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.FIELD
)
@Retention(
    RetentionPolicy.RUNTIME
)
@MapKey
annotation class AnalyticsOptionKey(val value: AnalyticsOption)