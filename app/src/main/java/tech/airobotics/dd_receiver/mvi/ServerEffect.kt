package tech.airobotics.dd_receiver.mvi

sealed class ServerEffect {
    data class ShowToast(val text: String) : ServerEffect()
    data class NotificationUpdate(val text: String) : ServerEffect()
}

