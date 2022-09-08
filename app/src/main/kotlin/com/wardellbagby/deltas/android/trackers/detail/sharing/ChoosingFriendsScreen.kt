package com.wardellbagby.deltas.android.trackers.detail.sharing

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.squareup.workflow1.ui.ViewEnvironment
import com.squareup.workflow1.ui.compose.ComposeScreen
import com.wardellbagby.deltas.android.R
import com.wardellbagby.deltas.android.friends.Friend

data class ChoosingFriendsScreen(
  val allFriends: List<Friend>,
  val selectedFriends: List<Friend>,
  val onFriendSelected: (Friend) -> Unit,
  val onAddFriendClicked: () -> Unit,
  val onShareClicked: () -> Unit
) : ComposeScreen {

  @Composable
  override fun Content(viewEnvironment: ViewEnvironment) {
    val selectedFriendsById = remember(selectedFriends) {
      selectedFriends.map { it.id }
    }

    if (allFriends.isEmpty()) {
      Column(
        modifier = Modifier.padding(16.dp).fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(stringResource(R.string.friends_null_state_title))
        Spacer(Modifier.height(16.dp))
        Button(
          onClick = { onAddFriendClicked() },
        ) {
          Text(stringResource(R.string.add_new_friend))
        }
      }
    } else {
      Column(Modifier.fillMaxSize().padding(bottom = 16.dp)) {
        LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp)) {
          items(allFriends) {
            CheckableRow(
              modifier = Modifier.fillMaxWidth(),
              label = it.label,
              enabled = selectedFriends.size < 10,
              checked = selectedFriendsById.contains(it.id),
              onCheckedChanged = { _ ->
                onFriendSelected(it)
              }
            )
          }
        }

        Spacer(Modifier.weight(1f))

        Button(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
          onClick = { onShareClicked() }) {
          Text(stringResource(R.string.share))
        }
      }
    }
  }

  @Composable
  private fun CheckableRow(
    modifier: Modifier = Modifier,
    label: String,
    checked: Boolean,
    onCheckedChanged: (Boolean) -> Unit,
    enabled: Boolean = true
  ) {
    Row(
      modifier = modifier.clickable {
        if (enabled) {
          onCheckedChanged(!checked)
        }
      },
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Checkbox(checked = checked, onCheckedChange = onCheckedChanged, enabled = enabled)
      Text(label)
    }
  }
}
