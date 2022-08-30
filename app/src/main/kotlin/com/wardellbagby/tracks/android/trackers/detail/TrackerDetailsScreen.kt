package com.wardellbagby.tracks.android.trackers.detail

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.squareup.workflow1.ui.ViewEnvironment
import com.squareup.workflow1.ui.compose.ComposeScreen
import com.wardellbagby.tracks.android.R
import com.wardellbagby.tracks.android.core_ui.ConfirmButton
import com.wardellbagby.tracks.android.core_ui.LabeledValue
import com.wardellbagby.tracks.android.core_ui.StickyHeaderText
import com.wardellbagby.tracks.android.strings.asTextData
import com.wardellbagby.tracks.android.times.format
import com.wardellbagby.tracks.android.trackers.ContributesToTopBar
import com.wardellbagby.tracks.android.trackers.models.Tracker
import com.wardellbagby.tracks.android.trackers.models.Tracker.ElapsedTimeTracker
import com.wardellbagby.tracks.android.trackers.models.Tracker.IncrementalTracker
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.toKotlinInstant
import kotlin.time.Duration.Companion.seconds

interface ContributesToLazyList {
  @Composable fun contributeState(state: LazyListState) {}
  fun LazyListScope.contributeItems(viewEnvironment: ViewEnvironment)
}

data class TrackerDetailsScreen(
  val tracker: Tracker,
  val historyRendering: ContributesToLazyList,
  val onShareClicked: () -> Unit,
  val onIncrementClicked: () -> Unit,
  val onResetTimeClicked: () -> Unit,
  val onDeleteClicked: () -> Unit,
  val onUnsubscribeClicked: () -> Unit,
  val onBack: () -> Unit
) : ComposeScreen, ContributesToTopBar {

  override val actions: @Composable RowScope.() -> Unit
    get() = {
      if (tracker.canEdit) {
        IconButton(onClick = { onShareClicked() }) {
          Icon(Icons.Default.Share, "Share")
        }
      }
    }

  @OptIn(ExperimentalFoundationApi::class)
  @Composable
  override fun Content(viewEnvironment: ViewEnvironment) {
    BackHandler {
      onBack()
    }
    val lazyListState = rememberLazyListState()
    historyRendering.contributeState(lazyListState)

    Column(Modifier.padding(bottom = 16.dp)) {
      LazyColumn(
        Modifier.weight(1f),
        state = lazyListState,
        contentPadding = PaddingValues(vertical = 16.dp)
      ) {
        stickyHeader {
          StickyHeaderText(stringResource(R.string.details))
        }
        item {
          when (tracker) {
            is ElapsedTimeTracker -> tracker.show(Modifier.padding(16.dp))
            is IncrementalTracker -> tracker.show(Modifier.padding(16.dp))
          }
        }
        stickyHeader("history-items") {
          StickyHeaderText(stringResource(R.string.history))
        }
        with(historyRendering) {
          this@LazyColumn.contributeItems(viewEnvironment)
        }
      }
      Column(Modifier.padding(16.dp)) {
        if (tracker.canEdit) {
          DeleteButton()
          tracker.ChangeButton()
        } else {
          UnsubscribeButton()
        }
      }
    }
  }

  @Composable
  private fun ElapsedTimeTracker.show(modifier: Modifier) {
    var elapsedTime by remember {
      mutableStateOf(Clock.System.now() - resetTime.toKotlinInstant())
    }

    LaunchedEffect(elapsedTime) {
      delay(30.seconds)
      elapsedTime = Clock.System.now() - resetTime.toKotlinInstant()
    }

    Column(modifier) {
      LabeledValue(
        label = "Time since last reset",
        value = elapsedTime.format().get()
      )

      Spacer(Modifier.height(16.dp))

      LabeledValue(label = "Creator", value = tracker.ownerLabel)
    }
  }

  @Composable
  private fun IncrementalTracker.show(modifier: Modifier) {
    Column(modifier) {
      LabeledValue(
        label = "Total count",
        value = count.toString()
      )

      Spacer(Modifier.height(16.dp))

      LabeledValue(label = "Creator", value = tracker.ownerLabel)

      Spacer(Modifier.height(16.dp))
    }
  }

  @Composable
  fun Tracker.ChangeButton() {
    val modifier = Modifier.fillMaxWidth()

    when (this) {
      is ElapsedTimeTracker -> Button(
        modifier = modifier,
        onClick = { onResetTimeClicked() }) {
        Text(stringResource(R.string.reset_time))
      }

      is IncrementalTracker -> Button(
        modifier = modifier,
        onClick = { onIncrementClicked() }) {
        Text(stringResource(R.string.increment))
      }
    }
  }

  @Composable
  fun DeleteButton() {
    ConfirmButton(
      label = R.string.delete_tracker.asTextData(),
      confirmLabel = R.string.confirm_delete_tracker.asTextData(),
      onClick = onDeleteClicked
    )
  }

  @Composable
  fun UnsubscribeButton() {
    ConfirmButton(
      label = R.string.remove_tracker.asTextData(),
      confirmLabel = R.string.confirm_remove_tracker.asTextData(),
      onClick = onUnsubscribeClicked
    )
  }
}