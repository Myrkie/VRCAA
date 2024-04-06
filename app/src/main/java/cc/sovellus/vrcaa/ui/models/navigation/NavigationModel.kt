package cc.sovellus.vrcaa.ui.models.navigation

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cc.sovellus.vrcaa.R
import cc.sovellus.vrcaa.api.vrchat.http.models.LimitedUser
import cc.sovellus.vrcaa.api.updater.AutoUpdater
import cc.sovellus.vrcaa.helper.enableUpdates
import cc.sovellus.vrcaa.helper.searchFeaturedWorlds
import cc.sovellus.vrcaa.helper.sortWorlds
import cc.sovellus.vrcaa.helper.usersAmount
import cc.sovellus.vrcaa.helper.worldsAmount
import cc.sovellus.vrcaa.manager.ApiManager.api
import cc.sovellus.vrcaa.manager.FriendManager
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class NavigationScreenModel(
    private val context: Context
) : ScreenModel {

    private val preferences: SharedPreferences = context.getSharedPreferences("vrcaa_prefs", Context.MODE_PRIVATE)

    var isSearchActive = mutableStateOf(false)
    var searchText = mutableStateOf("")
    var tonalElevation = mutableStateOf(16.dp)
    var searchHistory = mutableListOf<String>()

    var featuredWorlds = mutableStateOf(preferences.searchFeaturedWorlds)
    var sortWorlds = mutableStateOf(preferences.sortWorlds)
    var worldsAmount = mutableIntStateOf(preferences.worldsAmount)
    var usersAmount = mutableIntStateOf(preferences.usersAmount)


    var hasUpdate = mutableStateOf(false)

    init {
        screenModelScope.launch {

            if (preferences.enableUpdates) {
                hasUpdate.value = AutoUpdater.checkForUpdates()
            }

            val friends: MutableList<LimitedUser> = ArrayList()
            api.getFriends()?.let { friends += it }
            api.getFriends(true)?.let { friends += it }
            FriendManager.setFriends(friends)
        }
    }

    fun enterSearchMode() {
        screenModelScope.launch {
            tonalElevation.value = 0.dp
            delay(100)
            isSearchActive.value = true
        }
    }

    fun existSearchMode() {
        screenModelScope.launch {
            isSearchActive.value = false
            delay(160)
            tonalElevation.value = 16.dp

            if (searchText.value.isNotEmpty())
                searchHistory.add(searchText.value)
            clearSearchText()
        }
    }

    fun clearSearchText() {
        searchText.value = ""
    }

    fun update(context: Context) {
        val update = File(context.filesDir, "temp.apk")
        screenModelScope.launch {
            if (AutoUpdater.downloadUpdate(update)) {
                AutoUpdater.installUpdate(context, update)
            } else {
                Toast.makeText(
                    context,
                    context.getString(R.string.update_toast_failed_update),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun resetSettings() {
        preferences.searchFeaturedWorlds = false
        featuredWorlds.value = false
        preferences.sortWorlds = "relevance"
        sortWorlds.value = "relevance"
        preferences.worldsAmount = 50
        worldsAmount.intValue = 50
        preferences.usersAmount = 50
        usersAmount.intValue = 50

        Toast.makeText(
            context,
            "Reset settings.",
            Toast.LENGTH_SHORT
        ).show()
    }

    fun applySettings() {
        preferences.searchFeaturedWorlds = featuredWorlds.value
        preferences.sortWorlds = sortWorlds.value
        preferences.worldsAmount = worldsAmount.intValue
        preferences.usersAmount = usersAmount.intValue

        Toast.makeText(
            context,
            "Applied settings.",
            Toast.LENGTH_SHORT
        ).show()
    }
}