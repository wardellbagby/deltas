package com.wardellbagby.deltas.android.trackers

import com.wardellbagby.deltas.android.networking.DefaultNetworkResult
import com.wardellbagby.deltas.android.networking.NetworkResult
import com.wardellbagby.deltas.models.trackers.CreateTrackerRequest
import com.wardellbagby.deltas.models.trackers.DeleteTrackerRequest
import com.wardellbagby.deltas.models.trackers.GetTrackerRequest
import com.wardellbagby.deltas.models.trackers.GetTrackerResponse
import com.wardellbagby.deltas.models.trackers.ListTrackersRequest
import com.wardellbagby.deltas.models.trackers.ListTrackersResponse
import com.wardellbagby.deltas.models.trackers.SubscribeTrackerRequest
import com.wardellbagby.deltas.models.trackers.UnsubscribeTrackerRequest
import com.wardellbagby.deltas.models.trackers.UpdateTrackerRequest
import com.wardellbagby.deltas.models.trackers.history.ListHistoryRequest
import com.wardellbagby.deltas.models.trackers.history.ListHistoryResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface TrackerService {
  @POST("tracker/list")
  suspend fun listTrackers(
    @Body request: ListTrackersRequest
  ): NetworkResult<ListTrackersResponse>

  @POST("tracker/get")
  suspend fun getTracker(
    @Body request: GetTrackerRequest
  ): NetworkResult<GetTrackerResponse>

  @POST("tracker/create")
  suspend fun createTracker(
    @Body request: CreateTrackerRequest
  ): DefaultNetworkResult

  @POST("tracker/update")
  suspend fun updateTracker(
    @Body request: UpdateTrackerRequest
  ): DefaultNetworkResult

  @POST("tracker/delete")
  suspend fun deleteTracker(
    @Body request: DeleteTrackerRequest
  ): DefaultNetworkResult

  @POST("tracker/subscribe")
  suspend fun subscribeTracker(
    @Body request: SubscribeTrackerRequest
  ): DefaultNetworkResult

  @POST("tracker/unsubscribe")
  suspend fun unsubscribeTracker(
    @Body request: UnsubscribeTrackerRequest
  ): DefaultNetworkResult

  @POST("tracker/history/list")
  suspend fun listTrackerHistory(
    @Body request: ListHistoryRequest
  ): NetworkResult<ListHistoryResponse>
}