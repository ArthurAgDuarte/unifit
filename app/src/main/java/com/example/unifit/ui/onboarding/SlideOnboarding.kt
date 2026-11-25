package com.example.unifit.ui.onboarding

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

// Conteúdo de um slide do onboarding
data class SlideOnboarding(
    @DrawableRes val icone: Int,
    @StringRes val titulo: Int,
    @StringRes val descricao: Int
)
