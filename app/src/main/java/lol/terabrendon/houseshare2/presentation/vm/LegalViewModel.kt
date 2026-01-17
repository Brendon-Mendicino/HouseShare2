package lol.terabrendon.houseshare2.presentation.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import lol.terabrendon.houseshare2.R
import lol.terabrendon.houseshare2.data.repository.UserDataRepository
import lol.terabrendon.houseshare2.data.repository.UserDataRepository.Update.SendAnalytics
import lol.terabrendon.houseshare2.data.repository.UserDataRepository.Update.TermsConditions
import lol.terabrendon.houseshare2.presentation.util.SnackbarController
import lol.terabrendon.houseshare2.presentation.util.SnackbarEvent
import lol.terabrendon.houseshare2.presentation.util.UiText
import javax.inject.Inject

@HiltViewModel
class LegalViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository,
) : ViewModel() {
    sealed interface Event {
        data object TermsVisited : Event
        data object TermsAccepted : Event
        data object AnalyticsToggled : Event
        data object Finish : Event
    }

    data class UiState(
        val termsVisited: Boolean = false,
        val termsAccepted: Boolean = false,
        val analyticsAccepted: Boolean = false,
    )

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()

    init {
        // Update the state of the analytics consent with the saved one by the user
        viewModelScope.launch {
            val savedState = userDataRepository.sendAnalytics.first()
            _state.update { it.copy(analyticsAccepted = savedState) }
        }
    }

    fun onEvent(event: Event) = viewModelScope.launch {
        when (event) {
            Event.TermsVisited -> _state.update { it.copy(termsVisited = true) }
            Event.TermsAccepted -> {
                if (!_state.value.termsVisited) {
                    SnackbarController.sendEvent(
                        SnackbarEvent(
                            message = UiText.Res(R.string.you_must_read_the_terms_first),
                            withDismissAction = true,
                        )
                    )
                    return@launch
                }

                _state.update { it.copy(termsAccepted = !it.termsAccepted) }
            }

            Event.AnalyticsToggled -> _state.update { it.copy(analyticsAccepted = !it.analyticsAccepted) }
            Event.Finish -> {
                if (!state.value.termsAccepted) {
                    return@launch
                }

                userDataRepository.update(TermsConditions(true))
                userDataRepository.update(SendAnalytics(state.value.analyticsAccepted))
            }
        }
    }
}