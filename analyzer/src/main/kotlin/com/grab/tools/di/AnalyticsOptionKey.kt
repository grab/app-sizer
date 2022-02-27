package com.grab.tools.di

import com.grab.tools.AnalyticsOption
import dagger.MapKey

/** A [MapKey] annotation for maps with [AnalyticsOption] keys.  */

@MustBeDocumented
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.FIELD
)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class AnalyticsOptionKey(val value: AnalyticsOption)