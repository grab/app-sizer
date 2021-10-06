package com.grab.tools.utils

import dagger.Binds
import dagger.Module

@Module
interface FileProviderModule {
    @Binds
    fun DefaultFileQuery.bindDefaultFileQuery(): FileQuery
}