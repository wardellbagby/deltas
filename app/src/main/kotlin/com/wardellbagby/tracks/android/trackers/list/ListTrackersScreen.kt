package com.wardellbagby.tracks.android.trackers.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.squareup.workflow1.ui.ViewEnvironment
import com.squareup.workflow1.ui.compose.ComposeScreen
import com.squareup.workflow1.ui.compose.WorkflowRendering
import com.wardellbagby.tracks.android.R
import com.wardellbagby.tracks.android.core_ui.ContentPadding
import com.wardellbagby.tracks.android.core_ui.DataRow
import com.wardellbagby.tracks.android.core_ui.LoadingRowRendering
import com.wardellbagby.tracks.android.core_ui.LoadingScreen
import com.wardellbagby.tracks.android.core_ui.StickyHeaderText
import com.wardellbagby.tracks.android.times.format
import com.wardellbagby.tracks.android.trackers.models.Tracker
import com.wardellbagby.tracks.android.trackers.models.Tracker.ElapsedTimeTracker
import com.wardellbagby.tracks.android.trackers.models.Tracker.IncrementalTracker
import com.wardellbagby.tracks.utils.lastIndex
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.Instant
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toKotlinDuration

data class ListTrackersScreen(
  val allTrackers: List<Tracker>,
  val onTrackerClicked: (Tracker) -> Unit,
  val isLoading: Boolean,
  val onCloseToEnd: () -> Unit
) : ComposeScreen {

  @Composable
  override fun Content(viewEnvironment: ViewEnvironment) = when {
    allTrackers.isEmpty() && !isLoading -> NullState(viewEnvironment)
    allTrackers.isEmpty() && isLoading -> WorkflowRendering(LoadingScreen, viewEnvironment)
    else -> TrackerList(viewEnvironment)
  }

  @Composable
  private fun NullState(viewEnvironment: ViewEnvironment) {
    Column(
      modifier = Modifier
        .padding(viewEnvironment[ContentPadding])
        .fillMaxSize(),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        stringResource(R.string.trackers_null_state_title),
        style = MaterialTheme.typography.titleMedium
      )
      Text(stringResource(R.string.trackers_null_state_message))
    }
  }

  @OptIn(ExperimentalFoundationApi::class)
  @Composable
  private fun TrackerList(viewEnvironment: ViewEnvironment) {
    val grouped = remember(allTrackers) {
      allTrackers.groupBy { it.ownerLabel }
    }

    LazyColumn(contentPadding = viewEnvironment[ContentPadding]) {
      grouped.entries.forEachIndexed { groupIndex, (header, trackers) ->
        val isCurrentUsersTrackers = trackers.first().canEdit

        stickyHeader(key = header) {
          StickyHeaderText(
            text = if (isCurrentUsersTrackers) {
              stringResource(R.string.mine)
            } else {
              header
            },
            color = if (isCurrentUsersTrackers) {
              MaterialTheme.colorScheme.onPrimaryContainer
            } else {
              MaterialTheme.colorScheme.onSecondaryContainer
            },
            backgroundColor = if (isCurrentUsersTrackers) {
              MaterialTheme.colorScheme.primaryContainer
            } else {
              MaterialTheme.colorScheme.secondaryContainer
            }
          )
        }
        itemsIndexed(trackers) { index, tracker ->
          if (groupIndex == grouped.lastIndex && index == trackers.lastIndex) {
            LaunchedEffect(tracker) {
              onCloseToEnd()
            }
          }

          TrackerRow(onClick = { onTrackerClicked(tracker) }) {
            when (tracker) {
              is ElapsedTimeTracker -> tracker.show()
              is IncrementalTracker -> tracker.show()
            }
          }
        }
      }
      if (isLoading) {
        item {
          WorkflowRendering(LoadingRowRendering, viewEnvironment)
        }
      }
    }
  }

  @Composable
  fun TrackerRow(onClick: () -> Unit, content: @Composable () -> Unit) {
    Column(Modifier.clickable { onClick() }) {
      Row(
        Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(Modifier.weight(1f)) { content() }
        Spacer(Modifier.width(4.dp))
        Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Details")
      }
      Divider()
    }
  }

  @Composable
  fun IncrementalTracker.show() {
    DataRow(label = label, value = count.toString())
  }

  @Composable
  fun ElapsedTimeTracker.show() {
    var elapsedTime by remember {
      mutableStateOf(Duration.between(resetTime, Instant.now()))
    }

    LaunchedEffect(elapsedTime) {
      delay(30.seconds)
      elapsedTime = Duration.between(resetTime, Instant.now())
    }

    DataRow(label = label, value = elapsedTime.toKotlinDuration().format().get())
  }
}