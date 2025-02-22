package fr.gradignan.rpgmaps.core.ui.error

import fr.gradignan.rpgmaps.core.model.DataError
import rpg_maps.core.ui.generated.resources.Res
import rpg_maps.core.ui.generated.resources.error_forbidden
import rpg_maps.core.ui.generated.resources.error_http_unknown
import rpg_maps.core.ui.generated.resources.error_internal_server
import rpg_maps.core.ui.generated.resources.error_local_unknown
import rpg_maps.core.ui.generated.resources.error_no_data
import rpg_maps.core.ui.generated.resources.error_no_internet
import rpg_maps.core.ui.generated.resources.error_not_found
import rpg_maps.core.ui.generated.resources.error_serialization
import rpg_maps.core.ui.generated.resources.error_unauthorized
import rpg_maps.core.ui.generated.resources.error_websocket_unknown
import rpg_maps.core.ui.generated.resources.error_wrong_credentials

fun DataError.toUiText(): UiText {
    val stringRes = when(this) {
        DataError.Http.NO_INTERNET -> Res.string.error_no_internet
        DataError.Http.NOT_FOUND -> Res.string.error_not_found
        DataError.Http.SERIALIZATION -> Res.string.error_serialization
        DataError.Http.SERVER_ERROR -> Res.string.error_internal_server
        DataError.Http.FORBIDDEN -> Res.string.error_forbidden
        DataError.Http.UNKNOWN -> Res.string.error_http_unknown
        DataError.Http.UNAUTHORIZED -> Res.string.error_unauthorized
        DataError.Local.NO_DATA -> Res.string.error_no_data
        DataError.Local.UNKNOWN -> Res.string.error_local_unknown
        DataError.WebSocket.UNKNOWN -> Res.string.error_websocket_unknown
        DataError.Http.WRONG_CREDENTIALS -> Res.string.error_wrong_credentials
        DataError.WebSocket.SERIALIZATION -> Res.string.error_serialization
    }

    return UiText.StringResourceId(stringRes)
}
