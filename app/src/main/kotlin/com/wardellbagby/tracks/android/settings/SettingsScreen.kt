package com.wardellbagby.tracks.android.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.squareup.workflow1.ui.ViewEnvironment
import com.squareup.workflow1.ui.compose.ComposeScreen
import com.wardellbagby.tracks.android.BuildConfig
import com.wardellbagby.tracks.android.R
import com.wardellbagby.tracks.android.core_ui.ContentPadding
import com.wardellbagby.tracks.android.core_ui.plus
import com.wardellbagby.tracks.android.networking.Endpoint.Endpoints
import com.wardellbagby.tracks.android.networking.Endpoint.Endpoints.Local
import com.wardellbagby.tracks.android.networking.Endpoint.Endpoints.Production

private val Endpoints.next: String
  get() = when (this) {
    Production -> Local
    Local -> Production
  }.name.lowercase()

data class SettingsScreen(
  val endpoint: Endpoints,
  val onEndpointChanged: (Endpoints) -> Unit,
  val onLogoutClicked: () -> Unit
) : ComposeScreen {
  @Composable
  override fun Content(viewEnvironment: ViewEnvironment) {
    Column(
      modifier = Modifier.fillMaxSize()
        .padding(
          viewEnvironment[ContentPadding] + PaddingValues(
            start = 16.dp,
            end = 16.dp,
            bottom = 16.dp
          )
        ),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Spacer(Modifier.weight(1f))
      if (BuildConfig.DEBUG) {
        Button(
          modifier = Modifier.fillMaxWidth(),
          onClick = {
            onEndpointChanged(if (endpoint == Production) Local else Production)
          }
        ) {
          Text(stringResource(R.string.switch_endpoint, endpoint.next))
        }
      }
      Button(
        modifier = Modifier.fillMaxWidth(),
        onClick = onLogoutClicked
      ) {
        Text(stringResource(R.string.logout))
      }
    }
  }
}
