package com.wardellbagby.tracks.android.paging

import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.Worker
import com.squareup.workflow1.Workflow
import com.squareup.workflow1.action
import com.squareup.workflow1.runningWorker
import com.wardellbagby.tracks.android.paging.PagingWorkflow.Output
import com.wardellbagby.tracks.android.paging.PagingWorkflow.PageLoadType.Initial
import com.wardellbagby.tracks.android.paging.PagingWorkflow.PageLoadType.Subsequent
import com.wardellbagby.tracks.android.paging.PagingWorkflow.Props
import com.wardellbagby.tracks.android.paging.PagingWorkflow.Rendering
import com.wardellbagby.tracks.android.paging.PagingWorkflow.State
import com.wardellbagby.tracks.android.paging.PagingWorkflow.State.Failure
import com.wardellbagby.tracks.android.paging.PagingWorkflow.State.Idle
import com.wardellbagby.tracks.android.paging.PagingWorkflow.State.Loading
import com.wardellbagby.tracks.android.strings.isNotNullOrBlank

sealed interface LoadResult<out T : Any> {
  data class Loaded<T : Any>(
    val items: List<T>,
    val cursor: String?
  ) : LoadResult<T>

  data class Failure(
    val message: String?
  ) : LoadResult<Nothing>
}

data class RequestArgs<T : Any>(
  val cursor: String?,
  val args: T
)

fun interface Loader<RequestT : Any, T : Any> {
  suspend fun load(args: RequestArgs<RequestT>): LoadResult<T>
}

object PagingWorkflowFactory {
  @JvmSuppressWildcards
  fun <T : Any, RequestT : Any> create(
    loader: Loader<RequestT, T>
  ): Workflow<Props<RequestT>, Output, Rendering<T>> {
    return PagingWorkflow(loader)
  }
}

class PagingWorkflow<T : Any, RequestT : Any>(
  private val loader: Loader<RequestT, T>
) : StatefulWorkflow<Props<RequestT>, State<T>, Output, Rendering<T>>() {
  enum class PageLoadType {
    Initial,
    Subsequent
  }

  data class Props<RequestT : Any>(
    val idempotencyToken: String,
    val loadType: PageLoadType,
    val request: RequestT
  )

  sealed interface Rendering<T : Any> {
    val items: List<T>

    data class Loaded<T : Any>(
      override val items: List<T>
    ) : Rendering<T>

    data class Loading<T : Any>(
      override val items: List<T>,
      val loadType: PageLoadType
    ) : Rendering<T>

    data class Failure<T : Any>(
      override val items: List<T>,
      val message: String?,
      val loadType: PageLoadType
    ) : Rendering<T>
  }

  sealed interface State<T : Any> {
    val items: List<T>

    data class Idle<T : Any>(
      override val items: List<T>,
      val nextCursor: String?
    ) : State<T> {
      val hasMoreItems: Boolean
        get() = nextCursor.isNotNullOrBlank()
    }

    data class Loading<T : Any>(
      override val items: List<T>,
      val cursor: String?
    ) : State<T>

    data class Failure<T : Any>(
      override val items: List<T>,
      val nextCursor: String?,
      val message: String?
    ) : State<T>
  }

  sealed interface Output {
    object Failed : Output
    data class LoadCompleted(
      val loadType: PageLoadType
    ) : Output
  }

  override fun initialState(props: Props<RequestT>, snapshot: Snapshot?): State<T> {
    return when (props.loadType) {
      Initial -> Loading(
        items = emptyList(),
        cursor = null
      )

      Subsequent -> error("Can't use Subsequent load type for initial state")
    }
  }

  override fun render(
    renderProps: Props<RequestT>,
    renderState: State<T>,
    context: RenderContext
  ): Rendering<T> {
    return when (renderState) {
      is Idle -> Rendering.Loaded(items = renderState.items)
      is Failure -> Rendering.Failure(
        items = renderState.items,
        message = renderState.message,
        loadType = renderProps.loadType
      )

      is Loading -> {
        context.runningWorker(
          key = renderProps.idempotencyToken,
          worker = Worker.from {
            loader.load(
              RequestArgs(
                cursor = renderState.cursor,
                args = renderProps.request
              )
            )
          },
        ) {
          when (it) {
            is LoadResult.Failure -> action {
              val currentState = state as? Loading ?: return@action
              state = Failure(
                items = state.items,
                nextCursor = currentState.cursor,
                message = it.message
              )
              setOutput(Output.Failed)
            }

            is LoadResult.Loaded -> action {
              state = Idle(
                items = renderState.items + it.items,
                nextCursor = it.cursor
              )
              setOutput(Output.LoadCompleted(loadType = props.loadType))
            }
          }
        }
        Rendering.Loading(
          items = renderState.items,
          loadType = renderProps.loadType
        )
      }
    }
  }

  override fun snapshotState(state: State<T>): Snapshot? {
    return null
  }

  private fun <T : Any> State<T>.getSubsequentState(): State<T> {
    return when (this) {
      is Failure -> Loading(
        items = items,
        cursor = nextCursor
      )

      is Idle -> if (hasMoreItems) Loading(
        items = items,
        cursor = nextCursor
      ) else this

      is Loading -> Loading(
        items = items,
        cursor = cursor
      )
    }
  }

  override fun onPropsChanged(
    old: Props<RequestT>,
    new: Props<RequestT>,
    state: State<T>
  ): State<T> {
    if (old.request != new.request &&
      (new.loadType != Initial || new.idempotencyToken == old.idempotencyToken)
    ) {
      error("Not allowed to change the request parameters without also resetting the idempotency token and changing the request type to Initial.")
    }

    return when (old.loadType) {
      Initial -> when (new.loadType) {
        Initial -> Loading(
          items = emptyList(),
          cursor = null
        )

        Subsequent -> state.getSubsequentState()
      }

      Subsequent -> when (new.loadType) {
        Initial -> Loading(
          items = emptyList(),
          cursor = null
        )

        Subsequent -> state.getSubsequentState()
      }
    }
  }
}