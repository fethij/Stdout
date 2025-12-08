package com.tewelde.stdout.common

sealed interface LoadState<Data> {
  val dataOrNull: Data? get() = (this as? Loaded<Data>)?.data
  val errorOrNull: Throwable? get() = (this as? Error)?.error

  data object Loading : LoadState<Nothing>
  class Loaded<Data>(val data: Data) : LoadState<Data>
  data class Error(val error: Throwable) : LoadState<Nothing>
}

@Suppress("UNCHECKED_CAST")
inline fun <Data, Result> LoadState<Data>.map(mapper: (Data) -> Result): LoadState<Result> {
  return when (this) {
    is LoadState.Loaded -> LoadState.Loaded(mapper(data))
    is LoadState.Error -> this as LoadState<Result>
    LoadState.Loading -> LoadState.Loading as LoadState<Result>
  }
}

inline fun <Data> LoadState<Data>.onLoaded(action: (Data) -> Unit): LoadState<Data> {
  if (this is LoadState.Loaded<Data>) {
    action(data)
  }
  return this
}

inline fun <Data> LoadState<Data>.onLoading(action: () -> Unit): LoadState<Data> {
  if (this is LoadState.Loading) {
    action()
  }
  return this
}
