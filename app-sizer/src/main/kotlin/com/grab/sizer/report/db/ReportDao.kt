package com.grab.sizer.report.db

import com.grab.sizer.report.Report

interface ReportDao {
    fun addReport(report: Report)
    fun getReportById(id : String)
}

