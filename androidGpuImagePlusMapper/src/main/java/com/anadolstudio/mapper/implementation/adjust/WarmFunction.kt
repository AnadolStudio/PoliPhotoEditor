package com.anadolstudio.mapper.implementation.adjust

import com.anadolstudio.mapper.Function

class WarmFunction(temperature: Float, function: Function = Function.Empty) :
    WhiteBalanceFunction(temperature = temperature, tint = TINT, function = function) {

        private companion object{
            const val TINT = 1F
        }
}
