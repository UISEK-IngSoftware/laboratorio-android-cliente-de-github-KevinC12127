package ec.edu.uisek.githubclient

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import ec.edu.uisek.githubclient.databinding.ActivityMainBinding
import ec.edu.uisek.githubclient.models.Repo
import ec.edu.uisek.githubclient.services.GithubApiService
import ec.edu.uisek.githubclient.services.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var reposAdapter: ReposAdapter
    private val apiService: GithubApiService = RetrofitClient.gitHubApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()

        binding.newRepoFab.setOnClickListener {
            displayNewRepoForm()
        }
    }

    override fun onResume() {
        super.onResume()
        fetchRepositories()
    }

    private fun setupRecyclerView() {
        reposAdapter = ReposAdapter(
            onEditClick = { repo ->
                val intent = Intent(this, RepoForm::class.java).apply {
                    putExtra("IS_EDIT_MODE", true)
                    putExtra("REPO_NAME", repo.name)
                    putExtra("REPO_DESCRIPTION", repo.description)
                    putExtra("REPO_OWNER", repo.owner.login)
                }
                startActivity(intent)
            },
            onDeleteClick = { repo ->
                showDeleteConfirmationDialog(repo)
            }
        )
        binding.reposRecyclerView.adapter = reposAdapter
    }

    private fun fetchRepositories() {
        val call = apiService.getRepos()

        call.enqueue(object : Callback<List<Repo>> {
            override fun onResponse(call: Call<List<Repo>>, response: Response<List<Repo>>) {
                if (response.isSuccessful) {
                    val repos = response.body()
                    if (repos != null && repos.isNotEmpty()) {
                        reposAdapter.updateRepositories(repos)
                    } else {
                        showMessage("No se encontraron repositorios")
                    }
                } else {
                    val errorMessage = when (response.code()) {
                        401 -> "No autorizado. Revisa tu token de acceso."
                        403 -> "Prohibido. ¿Tienes permisos para eliminar?"
                        404 -> "No encontrado"
                        else -> "Error: ${response.code()}"
                    }
                    showMessage(errorMessage)
                }
            }

            override fun onFailure(call: Call<List<Repo>>, t: Throwable) {
                showMessage("No se pudieron cargar los repositorios. Error: ${t.message}")
            }
        })
    }

    private fun deleteRepository(repo: Repo) {
        val owner = repo.owner.login
        val repoName = repo.name

        apiService.deleteRepo(owner, repoName).enqueue(object: Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    showMessage("Repositorio eliminado con éxito")
                    fetchRepositories() // Refresca la lista
                } else {
                    showMessage("Error al eliminar: ${response.code()}. Asegúrate de tener los permisos correctos.")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                showMessage("Fallo al eliminar el repositorio: ${t.message}")
            }
        })
    }

    private fun showDeleteConfirmationDialog(repo: Repo) {
        AlertDialog.Builder(this)
            .setTitle("Confirmar Eliminación")
            .setMessage("¿Estás seguro de que quieres eliminar el repositorio '${repo.name}'?")
            .setPositiveButton("Eliminar") { _, _ ->
                deleteRepository(repo)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun displayNewRepoForm() {
        Intent(this, RepoForm::class.java).apply {
            startActivity(this)
        }
    }
}
