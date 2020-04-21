package com.smartpocket.cuantoteroban.graphic

sealed class GraphicStatus {

    class Loading : GraphicStatus()

    class ShowingData : GraphicStatus()

    class Error(val errorMsg: String, val showPeriodButtons: Boolean = false) : GraphicStatus()

}