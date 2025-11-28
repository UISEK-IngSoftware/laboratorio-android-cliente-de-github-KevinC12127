package ec.edu.uisek.githubclient.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Repo(
    val id: Long,
    val name: String,
    val description: String?,
    val language: String?,
    @SerializedName("private")
    val isPrivate: Boolean,
    val owner: RepoOwner // Contiene al dueño
) : Serializable
