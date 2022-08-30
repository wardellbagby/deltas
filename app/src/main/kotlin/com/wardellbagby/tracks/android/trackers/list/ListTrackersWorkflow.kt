package com.wardellbagby.tracks.android.trackers.list

import android.os.Parcelable
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.WorkflowAction
import com.squareup.workflow1.ui.Screen
import com.squareup.workflow1.ui.toParcelable
import com.squareup.workflow1.ui.toSnapshot
import com.wardellbagby.tracks.android.R
import com.wardellbagby.tracks.android.core_ui.FailureScreen
import com.wardellbagby.tracks.android.networking.NetworkResult
import com.wardellbagby.tracks.android.networking.generateUniqueToken
import com.wardellbagby.tracks.android.paging.LoadResult
import com.wardellbagby.tracks.android.paging.PagingWorkflow
import com.wardellbagby.tracks.android.paging.PagingWorkflow.PageLoadType
import com.wardellbagby.tracks.android.paging.PagingWorkflow.PageLoadType.Initial
import com.wardellbagby.tracks.android.paging.PagingWorkflow.PageLoadType.Subsequent
import com.wardellbagby.tracks.android.paging.PagingWorkflow.Rendering.Failure
import com.wardellbagby.tracks.android.paging.PagingWorkflow.Rendering.Loaded
import com.wardellbagby.tracks.android.paging.PagingWorkflow.Rendering.Loading
import com.wardellbagby.tracks.android.paging.PagingWorkflowFactory
import com.wardellbagby.tracks.android.strings.asTextData
import com.wardellbagby.tracks.android.trackers.TrackerService
import com.wardellbagby.tracks.android.trackers.list.ListTrackersWorkflow.Output
import com.wardellbagby.tracks.android.trackers.list.ListTrackersWorkflow.Output.TrackerClicked
import com.wardellbagby.tracks.android.trackers.list.ListTrackersWorkflow.State
import com.wardellbagby.tracks.android.trackers.models.Tracker
import com.wardellbagby.tracks.android.trackers.models.asModel
import com.wardellbagby.tracks.models.trackers.ListTrackersRequest
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

class ListTrackersWorkflow
@Inject constructor(
  private val service: TrackerService
) : StatefulWorkflow<Unit, State, Output, Screen>() {
  private val pagingWorkflow =
    PagingWorkflowFactory.create<Tracker, Unit> { (cursor) ->
      service.listTrackers(ListTrackersRequest(cursor = cursor)).let {
        when (it) {
          is NetworkResult.Failure -> LoadResult.Failure(it.message)
          is NetworkResult.Success -> LoadResult.Loaded(
            it.response.trackers.asModel(),
            it.response.cursor
          )
        }
      }
    }

  @Parcelize
  data class State(
    val loadType: PageLoadType,
    val idempotencyToken: String
  ) : Parcelable

  sealed interface Output {
    data class TrackerClicked(val tracker: Tracker) : Output
  }

  override fun initialState(props: Unit, snapshot: Snapshot?): State {
    return snapshot?.toParcelable() ?: State(
      loadType = Initial,
      idempotencyToken = generateUniqueToken()
    )
  }

  override fun render(renderProps: Unit, renderState: State, context: RenderContext): Screen {
    val pagingRendering = context.renderChild(
      child = pagingWorkflow,
      props = PagingWorkflow.Props(
        idempotencyToken = renderState.idempotencyToken,
        loadType = renderState.loadType,
        request = renderProps
      )
    ) {
      WorkflowAction.noAction()
    }

    return when (pagingRendering) {
      is Loaded, is Loading -> ListTrackersScreen(
        allTrackers = pagingRendering.items,
        isLoading = pagingRendering is Loading,
        onTrackerClicked = context.eventHandler { counter ->
          setOutput(TrackerClicked(counter))
        },
        onCloseToEnd = context.eventHandler {
          state = State(
            loadType = Subsequent,
            idempotencyToken = generateUniqueToken()
          )
        }
      )

      is Failure -> FailureScreen(
        title = R.string.unexpected_error.asTextData(),
        message = pagingRendering.message?.asTextData()
          ?: R.string.failed_to_load_trackers.asTextData(),
        onRetry = context.eventHandler {
          state = State(loadType = Initial, idempotencyToken = generateUniqueToken())
        }
      )
    }
  }

  override fun snapshotState(state: State) = state.toSnapshot()
}