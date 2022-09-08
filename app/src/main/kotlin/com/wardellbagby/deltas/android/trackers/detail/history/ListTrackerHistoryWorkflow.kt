package com.wardellbagby.deltas.android.trackers.detail.history

import android.os.Parcelable
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.WorkflowAction.Companion.noAction
import com.squareup.workflow1.ui.toParcelable
import com.squareup.workflow1.ui.toSnapshot
import com.wardellbagby.deltas.android.networking.NetworkResult.Failure
import com.wardellbagby.deltas.android.networking.NetworkResult.Success
import com.wardellbagby.deltas.android.networking.generateUniqueToken
import com.wardellbagby.deltas.android.paging.LoadResult
import com.wardellbagby.deltas.android.paging.PagingWorkflow
import com.wardellbagby.deltas.android.paging.PagingWorkflow.PageLoadType
import com.wardellbagby.deltas.android.paging.PagingWorkflow.PageLoadType.Initial
import com.wardellbagby.deltas.android.paging.PagingWorkflow.PageLoadType.Subsequent
import com.wardellbagby.deltas.android.paging.PagingWorkflow.Rendering
import com.wardellbagby.deltas.android.paging.PagingWorkflow.Rendering.Loaded
import com.wardellbagby.deltas.android.paging.PagingWorkflow.Rendering.Loading
import com.wardellbagby.deltas.android.paging.PagingWorkflowFactory
import com.wardellbagby.deltas.android.trackers.TrackerService
import com.wardellbagby.deltas.android.trackers.detail.ContributesToLazyList
import com.wardellbagby.deltas.android.trackers.detail.history.ListTrackerHistoryWorkflow.State
import com.wardellbagby.deltas.models.trackers.history.HistoryDTO
import com.wardellbagby.deltas.models.trackers.history.ListHistoryRequest
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

class ListTrackerHistoryWorkflow
@Inject constructor(
  private val service: TrackerService,
) : StatefulWorkflow<String, State, Nothing, ContributesToLazyList>() {
  private val pagingWorkflow =
    PagingWorkflowFactory.create<HistoryDTO, String> { (cursor, trackerId) ->
      service.listTrackerHistory(
        ListHistoryRequest(
          trackerId = trackerId,
          cursor = cursor
        )
      ).let {
        when (it) {
          is Failure -> LoadResult.Failure(it.message)
          is Success -> LoadResult.Loaded(it.response.history, it.response.cursor)
        }
      }
    }

  @Parcelize
  data class State(
    val loadType: PageLoadType,
    val idempotencyToken: String
  ) : Parcelable

  override fun initialState(props: String, snapshot: Snapshot?): State {
    return snapshot?.toParcelable() ?: State(
      loadType = Initial,
      idempotencyToken = generateUniqueToken()
    )
  }

  override fun snapshotState(state: State): Snapshot {
    return state.toSnapshot()
  }

  override fun render(
    renderProps: String,
    renderState: State,
    context: RenderContext
  ): ContributesToLazyList {
    val pagingRendering = context.renderChild(
      child = pagingWorkflow,
      props = PagingWorkflow.Props(
        idempotencyToken = renderState.idempotencyToken,
        loadType = renderState.loadType,
        request = renderProps
      )
    ) {
      noAction()
    }

    return ListTrackerHistoryRendering(
      phase = when (pagingRendering) {
        is Rendering.Failure -> ListTrackerHistoryPhase.Failure(
          pagingRendering.message ?: "Failed to load history"
        )

        is Loaded, is Loading -> ListTrackerHistoryPhase.Loaded(
          histories = pagingRendering.items,
          isLoadingMore = pagingRendering is Loading,
          onCloseToEnd = context.eventHandler {
            state = State(
              loadType = Subsequent,
              idempotencyToken = generateUniqueToken()
            )
          }
        )
      },
    )
  }
}