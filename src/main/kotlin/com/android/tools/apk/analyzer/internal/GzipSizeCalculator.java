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

package com.android.tools.apk.analyzer.internal;

import com.android.SdkConstants;
import com.android.annotations.NonNull;
import com.android.tools.apk.analyzer.ApkSizeCalculator;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteStreams;
import com.google.common.io.CountingOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.Map;
import java.util.logging.Logger;
import java.util.zip.*;

public class GzipSizeCalculator implements ApkSizeCalculator {

    public static final String VIRTUAL_ENTRY_NAME = "";

    public GzipSizeCalculator() {}

    private static void verify(@NonNull Path apk) {
        try (ZipFile zf = new ZipFile(apk.toFile())) {
            // just verifying that this is a valid zip file
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot open apk: ", e);
        }
    }

    @Override
    public long getFullApkDownloadSize(@NonNull Path apk) {
        verify(apk);
        CountingOutputStream out = new CountingOutputStream(ByteStreams.nullOutputStream());

        // There is a difference between uncompressing the apk, and then compressing again using
        // "gzip -9", versus just compressing the apk itself using "gzip -9". But the difference
        // seems to be negligible, and we are only aiming at an estimate of what Play provides, so
        // this should suffice. This also seems to be the same approach taken by
        // https://github.com/googlesamples/apk-patch-size-estimator

        try (GZIPOutputStream zos = new MaxGzipOutputStream(out)) {
            Files.copy(apk, zos);
            zos.flush();
        } catch (IOException e) {
            return -1;
        }

        return out.getCount();
    }

    @Override
    public long getFullApkRawSize(@NonNull Path apk) {
        verify(apk);
        try {
            return Files.size(apk);
        } catch (IOException e) {
            Logger.getLogger(GzipSizeCalculator.class.getName())
                    .severe("Error obtaining size of the APK: " + e.toString());
            return -1;
        }
    }

    @NonNull
    @Override
    public Map<String, Long> getDownloadSizePerFile(@NonNull Path apk) {
        verify(apk);
        try {
            Path rezippedApk = Files.createTempFile("analyzer", SdkConstants.DOT_ZIP);
            reCompressWithZip(apk, rezippedApk);
            Map<String, Long> compressedSizePerFile = getCompressedSizePerFile(rezippedApk);
            Files.delete(rezippedApk);
            return compressedSizePerFile;
        } catch (IOException e) {
            String msg =
                    "Error while re-compressing apk to determine file by file download sizes: "
                            + e.toString();
            Logger.getLogger(GzipSizeCalculator.class.getName()).severe(msg);
            return ImmutableMap.of();
        }
    }

    @NonNull
    @Override
    public Map<String, Long> getRawSizePerFile(@NonNull Path apk) {
        verify(apk);
        return getCompressedSizePerFile(apk);
    }

    private static Map<String, Long> getCompressedSizePerFile(Path apk) {
        ImmutableMap.Builder<String, Long> sizes = new ImmutableMap.Builder<>();

        try (ZipFile zf = new ZipFile(apk.toFile())) {
            Enumeration<? extends ZipEntry> entries = zf.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                // In dev mode, zipflinger may generate virtual entries which must be
                // ignored.
                if (isVirtualEntry(zipEntry)) {
                    continue;
                }
                if (!zipEntry.isDirectory()) {
                    sizes.put("/" + zipEntry.getName(), zipEntry.getCompressedSize());
                }
            }
        } catch (IOException ignored) {
        }

        return sizes.build();
    }

    /**
     * Provides a zip archive that is compressed at level 9, but still maintains archive
     * information. This implies that it will be slightly larger than compressing using gzip (which
     * only compresses a single file, not an archive). But having compression information per file
     * is useful to get an approximate idea of how well each file compresses.
     */
    private static void reCompressWithZip(@NonNull Path from, @NonNull Path to) throws IOException {
        // copy entire contents of one zip file to another, where the destination zip is written to
        // with the maximum compression level
        try (ZipInputStream zis =
                        new ZipInputStream(new BufferedInputStream(Files.newInputStream(from)));
                ZipOutputStream zos =
                        new MaxZipOutputStream(
                                new BufferedOutputStream(Files.newOutputStream(to)))) {
            ZipEntry ze;
            while ((ze = zis.getNextEntry()) != null) {
                // In dev mode, zipflinger may generate virtual entries which must be
                // ignored.
                if (isVirtualEntry(ze)) {
                    continue;
                }
                ZipEntry compressedZe = new ZipEntry(ze.getName());
                compressedZe.setMethod(ZipEntry.DEFLATED);
                compressedZe.setTime(ze.getTime());
                zos.putNextEntry(compressedZe);
                ByteStreams.copy(zis, zos);
            }
        }
    }

    private static final class MaxGzipOutputStream extends GZIPOutputStream {
        public MaxGzipOutputStream(OutputStream out) throws IOException {
            super(out);
            // Google Play serves an APK that is compressed using gzip -9
            def.setLevel(Deflater.BEST_COMPRESSION);
        }
    }

    private static final class MaxZipOutputStream extends ZipOutputStream {
        public MaxZipOutputStream(OutputStream out) throws IOException {
            super(out);
            def.setLevel(Deflater.BEST_COMPRESSION);
        }
    }

    public static boolean isVirtualEntry(ZipEntry e) {
        return VIRTUAL_ENTRY_NAME.equals(e.getName());
    }
}
