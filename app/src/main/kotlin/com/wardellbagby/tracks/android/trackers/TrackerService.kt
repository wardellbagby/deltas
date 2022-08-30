package com.wardellbagby.tracks.android.trackers

import com.wardellbagby.tracks.android.networking.DefaultNetworkResult
import com.wardellbagby.tracks.android.networking.NetworkResult
import com.wardellbagby.tracks.models.trackers.CreateTrackerRequest
import com.wardellbagby.tracks.models.trackers.DeleteTrackerRequest
import com.wardellbagby.tracks.models.trackers.ListTrackersRequest
import com.wardellbagby.tracks.models.trackers.ListTrackersResponse
import com.wardellbagby.tracks.models.trackers.UnsubscribeTrackerRequest
import com.wardellbagby.tracks.models.trackers.UpdateTrackerRequest
import com.wardellbagby.tracks.models.trackers.history.ListHistoryRequest
import com.wardellbagby.tracks.models.trackers.history.ListHistoryResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface TrackerService {
  @POST("tracker/list")
  suspend fun listTrackers(
    @Body request: ListTrackersRequest
  ): NetworkResult<ListTrackersResponse>

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

  @POST("tracker/unsubscribe")
  suspend fun unsubscribeTracker(
    @Body request: UnsubscribeTrackerRequest
  ): DefaultNetworkResult

  @POST("tracker/history/list")
  suspend fun listTrackerHistory(
    @Body request: ListHistoryRequest
  ): NetworkResult<ListHistoryResponse>
}