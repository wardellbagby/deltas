package com.wardellbagby.tracks.android.trackers.detail.history

import android.os.Parcelable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.squareup.workflow1.ui.ViewEnvironment
import com.squareup.workflow1.ui.compose.WorkflowRendering
import com.wardellbagby.tracks.android.R
import com.wardellbagby.tracks.android.core_ui.FailureScreen
import com.wardellbagby.tracks.android.core_ui.LoadingRowRendering
import com.wardellbagby.tracks.android.strings.TextData
import com.wardellbagby.tracks.android.strings.asTextData
import com.wardellbagby.tracks.android.strings.isNotNullOrBlank
import com.wardellbagby.tracks.android.times.format
import com.wardellbagby.tracks.android.trackers.detail.ContributesToLazyList
import com.wardellbagby.tracks.android.trackers.detail.history.ListTrackerHistoryPhase.Failure
import com.wardellbagby.tracks.android.trackers.detail.history.ListTrackerHistoryPhase.Loaded
import com.wardellbagby.tracks.models.trackers.history.HistoryDTO
import com.wardellbagby.tracks.utils.nullIfBlank
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import kotlinx.parcelize.Parcelize
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle.SHORT

@Parcelize
data class HistoryKey(val id: String) : Parcelable

sealed interface ListTrackerHistoryPhase {
  data class Failure(val message: String) : ListTrackerHistoryPhase
  data class Loaded(
    val histories: List<HistoryDTO>,
    val onCloseToEnd: () -> Unit,
    val isLoadingMore: Boolean
  ) : ListTrackerHistoryPhase
}

data class ListTrackerHistoryRendering(
  val phase: ListTrackerHistoryPhase
) : ContributesToLazyList {

  override fun LazyListScope.contributeItems(viewEnvironment: ViewEnvironment) {
    when (phase) {
      is Failure -> item {
        FailureScreen(message = phase.message.asTextData())
      }

      is Loaded -> {
        if (phase.histories.isEmpty() && !phase.isLoadingMore) {
          item {
            Box(
              modifier = Modifier.padding(16.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                stringResource(R.string.history_null_state),
                style = MaterialTheme.typography.titleMedium
              )
            }
          }
        } else {
          itemsIndexed(
            phase.histories,
            key = { _, history -> HistoryKey(history.id) }) { index, history: HistoryDTO ->
            history.Row()
            Divider()
            if (phase.histories.lastIndex == index) {
              LaunchedEffect(index) {
                phase.onCloseToEnd()
              }
            }
          }
          if (phase.isLoadingMore) {
            item {
              WorkflowRendering(LoadingRowRendering, viewEnvironment)
            }
          }
        }
      }

    }
  }

  private fun Int.formatAsDifference(): TextData {
    return when {
      this > 0 -> "+$this"
      this < 0 -> "-$this"
      else -> "0"
    }.asTextData()
  }

  @Composable
  private fun HistoryDTO.Row() {
    val hasLabel = label.isNotNullOrBlank()
    val title = label?.nullIfBlank() ?: time.format()
    val value = if (newCount != null) {
      (newCount!! - oldCount!!).formatAsDifference()
    } else {
      (time - oldResetTime!!).format()
    }
    Row(
      modifier = Modifier
        .padding(16.dp)
        .fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(Modifier.weight(1f)) {
        Text(
          title,
          modifier = Modifier.fillMaxWidth(),
          style = MaterialTheme.typography.labelLarge
        )
        if (hasLabel) {
          Text(
            time.format(),
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.labelSmall
          )
        }
      }
      Column(Modifier.weight(1f)) {
        Text(
          value.get(),
          modifier = Modifier.fillMaxWidth(),
          textAlign = TextAlign.Right,
          style = MaterialTheme.typography.bodyLarge
        )
      }
    }
  }

  private fun Instant.format(): String {
    return DateTimeFormatter.ofLocalizedDateTime(SHORT)
      .format(toLocalDateTime(TimeZone.currentSystemDefault()).toJavaLocalDateTime())
  }
}