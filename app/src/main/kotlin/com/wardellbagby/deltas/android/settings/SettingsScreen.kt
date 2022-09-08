package com.wardellbagby.deltas.android.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.squareup.workflow1.ui.ViewEnvironment
import com.squareup.workflow1.ui.compose.ComposeScreen
import com.wardellbagby.deltas.android.BuildConfig
import com.wardellbagby.deltas.android.R
import com.wardellbagby.deltas.android.core_ui.ContentPadding
import com.wardellbagby.deltas.android.core_ui.plus
import com.wardellbagby.deltas.android.networking.Endpoint.Endpoints
import com.wardellbagby.deltas.android.networking.Endpoint.Endpoints.Local
import com.wardellbagby.deltas.android.networking.Endpoint.Endpoints.Production
import com.wardellbagby.deltas.android.strings.TextData

private val Endpoints.next: String
  get() = when (this) {
    Production -> Local
    Local -> Production
  }.name.lowercase()

data class SettingsScreen(
  val endpoint: Endpoints,
  val displayName: TextData,
  val onDisplayNameClicked: () -> Unit,
  val onEndpointChanged: (Endpoints) -> Unit,
  val onLogoutClicked: () -> Unit
) : ComposeScreen {
  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  override fun Content(viewEnvironment: ViewEnvironment) {
    Scaffold(
      modifier = Modifier.padding(viewEnvironment[ContentPadding]),
      topBar = {
        CenterAlignedTopAppBar(title = { Text(stringResource(R.string.settings)) })
      },
      content = {
        Column(
          modifier = Modifier.fillMaxSize()
            .padding(
              it + PaddingValues(
                start = 16.dp,
                end = 16.dp,
                bottom = 16.dp
              )
            ),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          FilledTonalButton(
            onClick = onDisplayNameClicked,
            modifier = Modifier.fillMaxWidth()
          ) {
            Text("Change display name")
          }

          Spacer(Modifier.weight(1f))

          if (BuildConfig.DEBUG) {
            OutlinedButton(
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
    )
  }
}
