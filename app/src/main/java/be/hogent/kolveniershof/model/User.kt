package be.hogent.kolveniershof.model

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize
import java.util.*

/**
 * User entity
 *
 * @property id
 * @property firstName
 * @property lastName
 * @property email
 * @property isAdmin
 * @property birthday
 * @property absentDates
 * @property imgUrl
 * @property token
 */
@Parcelize
data class User(
    @field:Json(name = "_id")
    val id: String,
    @field:Json(name = "firstName")
    val firstName: String,
    @field:Json(name = "lastName")
    val lastName: String,
    @field:Json(name = "email")
    val email: String? = null,
    @field:Json(name = "admin")
    val isAdmin: Boolean = false,
    @field:Json(name = "picture")
    val imgUrl: String? = null,
    @field:Json(name = "token")
    val token: String? = null
) : Parcelable {
    override fun toString(): String {
        return "$firstName $lastName"
    }
}