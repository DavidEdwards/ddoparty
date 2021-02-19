package dae.ddo.state

sealed class NetworkState {
    object None : NetworkState()
    object Loading : NetworkState()
    data class NotAvailable(val text: String) : NetworkState()
}