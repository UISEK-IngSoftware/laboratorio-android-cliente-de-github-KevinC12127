package ec.edu.uisek.githubclient.models

// Modelo para enviar datos a la API (crear/actualizar repos)
data class RepoRequest(
    val name: String,
    val description: String?,
    val private: Boolean = false // Por defecto, los repositorios son públicos
)
