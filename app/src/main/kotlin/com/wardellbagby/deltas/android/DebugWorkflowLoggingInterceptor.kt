package com.wardellbagby.deltas.android

import android.util.Log
import com.squareup.workflow1.SimpleLoggingWorkflowInterceptor

/**
 * Logs Workflow transitions and rendering on debug builds of the app.
 */
object DebugWorkflowLoggingInterceptor : SimpleLoggingWorkflowInterceptor() {
  override fun log(text: String) {
    if (BuildConfig.DEBUG) {
      Log.v("Workflow Breadcrumb", text)
    }
  }
}
