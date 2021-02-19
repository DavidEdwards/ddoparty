package dae.ddo.state

sealed class NavigationLocator

object LocatorTabParties : NavigationLocator()
object LocatorTabQuests : NavigationLocator()
object LocatorTabFilters : NavigationLocator()
data class LocatorEditFilter(val filterId: Long = 0) : NavigationLocator()
data class LocatorEditCondition(val filterId: Long, val conditionId: Long = 0L) :
    NavigationLocator()

object LocatorSettings : NavigationLocator()