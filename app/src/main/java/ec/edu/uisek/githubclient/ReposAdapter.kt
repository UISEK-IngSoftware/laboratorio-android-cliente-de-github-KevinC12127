package ec.edu.uisek.githubclient

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide // Importante añadir esta librería para cargar imágenes
import ec.edu.uisek.githubclient.databinding.FragmentRepoItemBinding // <- CORRECCIÓN IMPORTANTE
import ec.edu.uisek.githubclient.models.Repo

class ReposAdapter(
    private val onEditClick: (Repo) -> Unit,
    private val onDeleteClick: (Repo) -> Unit
) : RecyclerView.Adapter<ReposAdapter.RepoViewHolder>() {

    private var repos: List<Repo> = listOf()

    fun updateRepositories(newRepos: List<Repo>) {
        repos = newRepos
        notifyDataSetChanged()
    }

    // Aquí usamos FragmentRepoItemBinding en lugar de ItemRepoBinding
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RepoViewHolder {
        val binding = FragmentRepoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RepoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RepoViewHolder, position: Int) {
        val repo = repos[position]
        holder.bind(repo)
        // Asignar los clics a los botones
        holder.binding.editButton.setOnClickListener { onEditClick(repo) }
        holder.binding.deleteButton.setOnClickListener { onDeleteClick(repo) }
    }

    override fun getItemCount(): Int = repos.size

    // Y aquí también usamos FragmentRepoItemBinding
    class RepoViewHolder(val binding: FragmentRepoItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(repo: Repo) {
            binding.repoName.text = repo.name
            binding.repoDescription.text = repo.description ?: "Sin descripción"
            binding.repoLang.text = repo.language ?: "No especificado" // Ajustado al ID correcto del XML

            // Cargar la imagen del avatar del dueño del repositorio
            Glide.with(binding.root.context)
                .load(repo.owner.avatarUrl) // Asumiendo que el modelo RepoOwner tiene 'avatar_url'
                .placeholder(R.drawable.ic_launcher_background) // Imagen mientras carga
                .error(R.drawable.ic_launcher_background) // Imagen si hay error
                .into(binding.repoOwnerImage) // El ImageView en tu layout
        }
    }
}
