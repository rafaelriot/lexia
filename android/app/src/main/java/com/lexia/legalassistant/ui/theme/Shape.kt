package com.lexia.legalassistant.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val LexiaShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp), // Tags, chips, badges
    small = RoundedCornerShape(8.dp),      // Buttons, inputs, chips
    medium = RoundedCornerShape(12.dp),    // Context bars, cards
    large = RoundedCornerShape(16.dp),     // Article cards, modal sheets
    extraLarge = RoundedCornerShape(24.dp) // Major containers & dialogs
)
