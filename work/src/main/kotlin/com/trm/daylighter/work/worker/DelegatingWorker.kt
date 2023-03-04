package com.trm.daylighter.work.worker

import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlin.reflect.KClass

@EntryPoint
@InstallIn(SingletonComponent::class)
interface HiltWorkerFactoryEntryPoint {
  fun hiltWorkerFactory(): HiltWorkerFactory
}

private const val WORKER_CLASS_NAME = "RouterWorkerDelegateClassName"

fun KClass<out CoroutineWorker>.delegatedData(
  putArgs: Data.Builder.() -> Data.Builder = { this }
): Data = Data.Builder().putString(WORKER_CLASS_NAME, qualifiedName).putArgs().build()

class DelegatingWorker(
  appContext: Context,
  workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {
  private val workerClassName = workerParams.inputData.getString(WORKER_CLASS_NAME) ?: ""

  private val delegateWorker =
    EntryPointAccessors.fromApplication<HiltWorkerFactoryEntryPoint>(appContext)
      .hiltWorkerFactory()
      .createWorker(appContext, workerClassName, workerParams) as? CoroutineWorker
      ?: throw IllegalArgumentException("Unable to find appropriate worker")

  override suspend fun getForegroundInfo(): ForegroundInfo = delegateWorker.getForegroundInfo()

  override suspend fun doWork(): Result = delegateWorker.doWork()
}
