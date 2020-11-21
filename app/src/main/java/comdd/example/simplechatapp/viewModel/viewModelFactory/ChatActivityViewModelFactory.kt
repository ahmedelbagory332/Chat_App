package comdd.example.simplechatapp.viewModel.viewModelFactory

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import comdd.example.simplechatapp.viewModel.ChatActivityViewModel
import comdd.example.simplechatapp.viewModel.LastMessageActivityViewModel
import comdd.example.simplechatapp.viewModel.SignUpActivityViewModel
import comdd.example.simplechatapp.viewModel.UsersActivityViewModel

@Suppress("UNCHECKED_CAST")
class ChatActivityViewModelFactory(private val application: Application) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatActivityViewModel::class.java)) {
            return ChatActivityViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }


}