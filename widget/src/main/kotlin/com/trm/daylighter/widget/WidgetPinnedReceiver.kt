package com.trm.daylighter.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class WidgetPinnedReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    Toast.makeText(
        context,
        context.getString(R.string.widget_pinned_go_to_homescreen),
        Toast.LENGTH_SHORT
      )
      .show()
  }
}
