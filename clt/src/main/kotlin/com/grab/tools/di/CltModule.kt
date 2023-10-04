package com.grab.tools.di

import com.grab.tools.CltInputFileProvider
import com.grab.tools.report.NAMED_DEVICE_NAME
import com.grab.tools.report.NAMED_EXTRA_TAG
import com.grab.tools.report.NAMED_PROJECT_NAME
import com.grab.tools.report.ProjectInfoProvider
import com.grab.tools.utils.DefaultFileQuery
import com.grab.tools.utils.FileQuery
import com.grab.tools.utils.InputFileProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module
object CltModule {
    @Provides
    fun provideProjectInfoFactory(
        @Named(NAMED_DEVICE_NAME) deviceName: String,
        @Named(NAMED_PROJECT_NAME) projectName: String,
        @Named(NAMED_EXTRA_TAG) pipelineId: String,
    ) = ProjectInfoProvider(deviceName = deviceName, projectName = projectName, pipelineId = pipelineId)
}

@Module
interface CltBinder {
    @Binds
    fun DefaultFileQuery.bindDefaultFileQuery(): FileQuery

    @Binds
    fun CltInputFileProvider.bindInputFileProvider(): InputFileProvider
}