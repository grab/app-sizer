/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.tools.apk.analyzer;

import com.android.annotations.NonNull;
import com.android.tools.apk.analyzer.internal.GzipSizeCalculator;
import java.nio.file.Path;
import java.util.Map;

public interface ApkSizeCalculator {
    long getFullApkDownloadSize(@NonNull Path apk);

    long getFullApkRawSize(@NonNull Path apk);

    @NonNull
    Map<String, Long> getDownloadSizePerFile(@NonNull Path apk);

    @NonNull
    Map<String, Long> getRawSizePerFile(@NonNull Path apk);

    @NonNull
    static ApkSizeCalculator getDefault() {
        return new GzipSizeCalculator();
    }
}
