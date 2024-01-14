package com.grab.sizer.report

import com.grab.sizer.report.db.ReportDao
import dagger.Lazy

class DatabaseReportWriter(private val reportDaoSet: Lazy<Set<ReportDao>>) : ReportWriter {
    override fun write(reportId: String, report: Report) {
        reportDaoSet.get().forEach {
            it.addReport(report)
        }
    }
}