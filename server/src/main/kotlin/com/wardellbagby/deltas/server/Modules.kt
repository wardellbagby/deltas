package com.wardellbagby.deltas.server

import com.wardellbagby.deltas.server.routes.trackers.TrackersRepository
import com.wardellbagby.deltas.server.routes.users.UserDataRepository
import kotlinx.coroutines.CoroutineScope
import org.koin.dsl.module

fun createMainModule(scope: CoroutineScope) = module {
  single { scope }
  single { TrackersRepository(get()) }
  single { UserDataRepository(get()) }
}