package ec.edu.uisek.githubclient

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ec.edu.uisek.githubclient.databinding.ActivityRepoFormBinding
import ec.edu.uisek.githubclient.models.Repo
import ec.edu.uisek.githubclient.models.RepoRequest
import ec.edu.uisek.githubclient.services.GithubApiService
import ec.edu.uisek.githubclient.services.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RepoForm : AppCompatActivity() {
    private lateinit var binding: ActivityRepoFormBinding
    private val apiService: GithubApiService? = RetrofitClient.getApiService()

    private var isEditMode = false
    private var originalRepoName: String? = null
    private var owner: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRepoFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isEditMode = intent.getBooleanExtra("IS_EDIT_MODE", false)

        if (isEditMode) {
            setupEditMode()
        } else {
            binding.formTitle.text = "Crear Nuevo Repositorio"
        }

        binding.saveButton.setOnClickListener {
            saveRepository()
        }

        binding.cancelButton.setOnClickListener { // Añadido
            finish() // Cierra la actividad actual
        }
    }

    private fun setupEditMode() {
        binding.formTitle.text = "Editar Repositorio"
        originalRepoName = intent.getStringExtra("REPO_NAME")
        val description = intent.getStringExtra("REPO_DESCRIPTION")
        owner = intent.getStringExtra("REPO_OWNER")

        binding.repoNameInput.setText(originalRepoName)
        binding.repoNameInput.isEnabled = false // El nombre no se puede editar con PATCH
        binding.repoDescriptionInput.setText(description)
    }

    private fun saveRepository() {
        val name = binding.repoNameInput.text.toString()
        val description = binding.repoDescriptionInput.text.toString()

        if (name.isBlank()) {
            showMessage("El nombre del repositorio no puede estar vacío")
            return
        }

        val repoRequest = RepoRequest(name = name, description = description)

        if (isEditMode) {
            updateRepository(repoRequest)
        } else {
            createRepository(repoRequest)
        }
    }

    private fun createRepository(repoRequest: RepoRequest) {
        apiService?.createRepo(repoRequest)?.enqueue(object : Callback<Repo> {
            override fun onResponse(call: Call<Repo>, response: Response<Repo>) {
                if (response.isSuccessful) {
                    showMessage("Repositorio creado con éxito")
                    finish() // Cierra el formulario y vuelve a la lista
                } else {
                    showMessage("Error al crear: ${response.code()} - ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<Repo>, t: Throwable) {
                showMessage("Fallo al crear el repositorio: ${t.message}")
            }
        })
    }

    private fun updateRepository(repoRequest: RepoRequest) {
        if (owner == null || originalRepoName == null) {
            showMessage("Error: No se pudo identificar el repositorio a editar.")
            return
        }
        apiService?.updateRepo(owner!!, originalRepoName!!, repoRequest)?.enqueue(object: Callback<Repo>{
            override fun onResponse(call: Call<Repo>, response: Response<Repo>) {
                if(response.isSuccessful) {
                    showMessage("Repositorio actualizado con éxito")
                    finish()
                } else {
                    showMessage("Error al actualizar: ${response.code()} - ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<Repo>, t: Throwable) {
                showMessage("Fallo al actualizar el repositorio: ${t.message}")
            }
        })
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
