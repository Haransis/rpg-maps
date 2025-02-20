package fr.gradignan.rpgmaps.feature.game.ui

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import fr.gradignan.rpgmaps.core.common.Resource
import fr.gradignan.rpgmaps.core.data.mapAction.MapActionRepository
import fr.gradignan.rpgmaps.core.model.Character
import fr.gradignan.rpgmaps.core.model.MapEffect
import fr.gradignan.rpgmaps.core.model.MapUpdate
import fr.gradignan.rpgmaps.feature.game.model.HUDState
import fr.gradignan.rpgmaps.feature.game.model.MapEffectsState
import fr.gradignan.rpgmaps.feature.game.model.MapOverlayState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameViewModel(private val mapActionRepository: MapActionRepository): ViewModel() {

    private var username = ""
    private val mapResourceUpdates: Flow<Resource<MapUpdate>> = mapActionRepository.getMapUpdatesFlow()
    private val mapEffects: Flow<MapEffect> = mapActionRepository.getMapEffectsFlow()

    private val _animations = MutableStateFlow(MapEffectsState())
    val effectsState = _animations.asStateFlow()
    init {
        mapEffects
            .onEach { effect ->
                when (effect) {
                    is MapEffect.Ping -> {
                        val currentPings = _animations.value.pings
                        _animations.value = _animations.value.copy(pings = currentPings + effect)

                        viewModelScope.launch {
                            delay(3000)
                            _animations.value = _animations.value.copy(
                                pings = _animations.value.pings.filterNot { it.x == effect.x && it.y == effect.y }
                            )
                        }
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private val initialState: MapOverlayState = MapOverlayState.Loading
    val overlayState: StateFlow<MapOverlayState> = mapResourceUpdates
        .scan(initialState) { previousState, payloadResource ->
            val state = (previousState as? MapOverlayState.UiStateMap) ?: MapOverlayState.UiStateMap()
            when (payloadResource) {
                is Resource.Success -> {
                    state.update(payloadResource.data)
                }
                is Resource.Error -> state.copy(errorMessage = payloadResource.exception.message)
                Resource.Loading -> MapOverlayState.Loading
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = initialState
        )

    private fun MapOverlayState.UiStateMap.update(mapUpdate: MapUpdate): MapOverlayState.UiStateMap =
        when(mapUpdate) {
            is MapUpdate.Connect -> this.copy(logs = logs + "- ${mapUpdate.user} connected")
            is MapUpdate.Initiate -> this.copy(
                logs = logs + "- Starting game",
                characters = mapUpdate.characters
            )
            is MapUpdate.LoadMap -> this.copy(
                logs = logs + "- Loading map: ${mapUpdate.map}",
                map = mapUpdate.map
            )
            is MapUpdate.Move -> {
                val updatedCharacters = this.characters.toMutableList().apply {
                    indexOfFirst { it.id == mapUpdate.id }.takeIf { it != -1 }?.let { index ->
                        val updatedCharacter = this[index].copy(x = mapUpdate.x, y = mapUpdate.y)
                        set(index, updatedCharacter)
                    }
                }

                this.copy(characters = updatedCharacters)
            }
            MapUpdate.NewTurn -> this.copy(logs = logs + "- New Turn")
            is MapUpdate.Next -> {
                val characterPlaying = characters.find { it.cmId == mapUpdate.id }
                if (characterPlaying != null) {
                    this.copy(
                        logs = logs + "- ${characterPlaying.name} is playing",
                        isPlayerTurn = characterPlaying.owner == username
                    )
                } else this
            }
        }

    private var _hudState = MutableStateFlow(HUDState())
    val hudState = _hudState.asStateFlow()

    fun onEndTurn() {
        mapActionRepository.endTurn()
    }

    fun onSprintChecked(checked: Boolean) {
        _hudState.update { it.copy(sprintEnabled = checked) }
    }

    fun onMapClick(click: Offset) {
        when (val state = overlayState.value) {
            MapOverlayState.Loading -> Logger.e { "click when loading map" }
            is MapOverlayState.UiStateMap -> {
                val clickedCharacter = state.characters.firstOrNull { character ->
                    val center = Offset(character.x.toFloat(), character.y.toFloat())
                    center.getDistanceTo(click) <= CHARACTER_RADIUS
                }
                _hudState.update {
                    when {
                        clickedCharacter == null && _hudState.value.selectedCharacter != null ->
                            it.copy(previewPath = it.previewPath + click)
                        clickedCharacter != null -> it.copy(selectedCharacter = clickedCharacter, previewPath = listOf(Offset(
                            clickedCharacter.x.toFloat(), clickedCharacter.y.toFloat()
                        )))
                        else -> it.copy(selectedCharacter = null, previewPath = emptyList())
                    }
                }
            }
        }
    }

    fun onUnselect() {
        _hudState.update {
            it.copy(selectedCharacter = null, previewPath = emptyList())
        }
    }

    fun setName(username: String) {
        this.username = username
    }
}
