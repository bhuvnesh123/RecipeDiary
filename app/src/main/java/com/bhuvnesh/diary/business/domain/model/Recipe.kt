package com.bhuvnesh.diary.business.domain.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Recipe(
    val id: String,
    val title: String,
    val ingredients: String,
    val steps: String,
    val imageUrl: String,
    val updated_at: String,
    val created_at: String
) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Recipe

        if (id != other.id) return false
        if (title != other.title) return false
        if (ingredients != other.ingredients) return false
        if (steps != other.steps) return false
        if (imageUrl != other.imageUrl) return false
        if (created_at != other.created_at) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + ingredients.hashCode()
        result = 31 * result + steps.hashCode()
        result = 31 * result + imageUrl.hashCode()
        result = 31 * result + created_at.hashCode()
        return result
    }
}