package com.wardellbagby.tracks.android.trackers.creation

import android.os.Parcelable
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.ui.ParcelableTextController
import com.squareup.workflow1.ui.Screen
import com.squareup.workflow1.ui.toParcelable
import com.squareup.workflow1.ui.toSnapshot
import com.wardellbagby.tracks.android.trackers.creation.CreateTrackerWorkflow.Output
import com.wardellbagby.tracks.android.trackers.creation.CreateTrackerWorkflow.Output.Cancelled
import com.wardellbagby.tracks.android.trackers.creation.CreateTrackerWorkflow.Output.Created
import com.wardellbagby.tracks.android.trackers.creation.CreateTrackerWorkflow.State
import com.wardellbagby.tracks.models.trackers.TrackerType
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

class CreateTrackerWorkflow
@Inject constructor() : StatefulWorkflow<Unit, State, Output, Screen>() {
  @Parcelize
  data class State(
    val type: TrackerType = TrackerType.Elapsed,
    val labelTextController: ParcelableTextController = ParcelableTextController()
  ) : Parcelable

  sealed interface Output {
    data class Created(val type: TrackerType, val label: String) : Output
    object Cancelled : Output
  }

  override fun initialState(props: Unit, snapshot: Snapshot?): State {
    return snapshot?.toParcelable() ?: State()
  }

  override fun render(
    renderProps: Unit,
    renderState: State,
    context: RenderContext
  ): Screen {
    return CreateTrackerScreen(
      type = renderState.type,
      labelTextController = renderState.labelTextController,
      onTypeSelected = context.eventHandler { type: TrackerType ->
        state = state.copy(type = type)
      },
      onBack = context.eventHandler {
        setOutput(Cancelled)
      },
      onSave = context.eventHandler {
        setOutput(
          Created(
            type = state.type,
            label = state.labelTextController.textValue
          )
        )
      }
    )
  }

  override fun snapshotState(state: State) = state.toSnapshot()
}