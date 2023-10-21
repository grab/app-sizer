package com.grab.android.sample.level1

import android.app.Activity
import android.os.Bundle
import android.os.PersistableBundle
import com.grab.sample.dummy.DummyClass1

class Level1Activity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        DummyClass1().method1()
    }
}