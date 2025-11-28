package ec.edu.uisek.githubclient

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
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
    private val apiService: GithubApiService by lazy {
        RetrofitClient.getApiService()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        binding.newRepoFab.setOnClickListener {
            displayNewRepoForm()
        }
        fetchRepositories()
    }

    override fun onResume() {
        super.onResume()
        fetchRepositories()
    }

    private fun setupRecyclerView() {
        reposAdapter = ReposAdapter(::onEditClick, ::onDeleteClick)
        binding.reposRecyclerView.adapter = reposAdapter
    }

    private fun fetchRepositories() {
        apiService.getRepos().enqueue(object : Callback<List<Repo>> {
            override fun onResponse(call: Call<List<Repo>>, response: Response<List<Repo>>) {
                if (response.isSuccessful) {
                    val repos = response.body()
                    if (repos != null && repos.isNotEmpty()) {
                        reposAdapter.updateRepositories(repos)
                    } else {
                        showMessage("No se encontraron repositorios")
                    }
                } else {
                    handleApiError(response.code())
                }
            }

            override fun onFailure(call: Call<List<Repo>>, t: Throwable) {
                showMessage("No se pudieron cargar los repositorio")
            }
        })
    }

    private fun onEditClick(repo: Repo) {
        Intent(this, RepoForm::class.java).apply {
            putExtra("repo", repo)
            startActivity(this)
        }
    }

    private fun onDeleteClick(repo: Repo) {
        apiService.deleteRepo(repo.owner.login, repo.name).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    showMessage("Repositorio eliminado con éxito.")
                    fetchRepositories() // Refresh the list
                } else {
                    handleApiError(response.code())
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                showMessage("Error al eliminar el repositorio")
            }
        })
    }

    private fun handleApiError(code: Int) {
        val errorMessage = when (code) {
            401 -> "No Autorizado"
            403 -> "Prohibido"
            404 -> "No Encontrado"
            else -> "Error $code"
        }
        showMessage("Error: $errorMessage")
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