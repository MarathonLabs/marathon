package marathon.reducers

import marathon.model.Run

data class AppState(
    val run: Run,
    val view: ViewState
)

fun rootReducer(
    state: AppState,
    action: Any
) = AppState(
    state.run,
    ViewState.Home
)

sealed class ViewState {
    object Home : ViewState()
    class Pool(val id: String) : ViewState()
    class Test(val poolId: String, val deviceId: String, val id: String) : ViewState()
}
