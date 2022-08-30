package com.wardellbagby.tracks.android.friends.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.squareup.workflow1.ui.ViewEnvironment
import com.squareup.workflow1.ui.compose.ComposeScreen
import com.wardellbagby.tracks.android.R
import com.wardellbagby.tracks.android.core_ui.ContentPadding
import com.wardellbagby.tracks.android.core_ui.plus
import com.wardellbagby.tracks.android.friends.Friend

data class ListFriendsScreen(
  val friends: List<Friend>,
) : ComposeScreen {

  @Composable
  override fun Content(viewEnvironment: ViewEnvironment) {
    if (friends.isEmpty()) {
      Column(
        modifier = Modifier
          .padding(viewEnvironment[ContentPadding])
          .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          stringResource(R.string.friends_null_state_title),
          style = MaterialTheme.typography.titleMedium
        )
        Text(stringResource(R.string.friends_null_state_message))
      }
    } else {
      LazyColumn(
        contentPadding = viewEnvironment[ContentPadding] + PaddingValues(horizontal = 16.dp)
      ) {
        items(friends) { friend ->
          Box(
            Modifier.padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              friend.label,
              style = MaterialTheme.typography.bodyLarge
            )
          }
          Divider()

        }
      }
    }
  }
}