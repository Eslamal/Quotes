package com.example.qoutes.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import androidx.navigation.Navigation
import com.example.qoutes.R
import com.example.qoutes.databinding.ActivityQuotesBinding
import com.example.qoutes.store.Preference
import com.example.qoutes.ui.fragments.SettingsFragment
import com.example.qoutes.viewmodels.QuoteViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class QuotesActivity : AppCompatActivity() {

    var atHome = true
    private lateinit var binding: ActivityQuotesBinding
    private val viewModel by viewModels<QuoteViewModel>()
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val savedLang = viewModel.getSetting(Preference.APP_LANGUAGE)
        if (!savedLang.isNullOrEmpty()) {
            val appLocale = LocaleListCompat.forLanguageTags(savedLang)
            AppCompatDelegate.setApplicationLocales(appLocale)
        }

        val isDark = viewModel.getSetting(Preference.IS_DARK_MODE)
        val mode = if (isDark) AppCompatDelegate.MODE_NIGHT_YES
        else AppCompatDelegate.MODE_NIGHT_NO
        AppCompatDelegate.setDefaultNightMode(mode)

        binding = ActivityQuotesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.myBookmarksImgBtn.setOnClickListener {
            try {
                val navController = Navigation.findNavController(binding.quotesNavHostFragment)
                navController.navigate(R.id.action_global_to_bookmarkFragment)


                atHome = false
                updateToolbarState(isHome = false)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        binding.backToQuotePage.setOnClickListener {
            onBackPressed()
        }

        binding.settingsBtn.setOnClickListener {
            SettingsFragment().show(supportFragmentManager, "SettingsFragment")
        }

        setTheme(R.style.Theme_Quotes)

        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { isGranted: Boolean ->
            viewModel.saveSetting(Preference.ASK_NOTIF_PERM, false)
            val message = if (isGranted) "Notifications set up successfully!"
            else  "Daily quotes won't work! Please set up notifications in settings."
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && viewModel.getSetting(Preference.ASK_NOTIF_PERM)) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (!viewModel.getSetting(Preference.CHECK_FOR_UPDATES)) return
    }

    override fun onBackPressed() {
        if (!atHome) {
            super.onBackPressed()
            atHome = true
            updateToolbarState(isHome = true)
        } else {
            super.onBackPressed()
        }
    }

    private fun updateToolbarState(isHome: Boolean) {
        with(binding) {
            if (isHome) {
                settingsBtn.visibility = View.VISIBLE
                myBookmarksImgBtn.visibility = View.VISIBLE
                backToQuotePage.visibility = View.GONE
                activityTitle.text = resources.getText(R.string.app_name)
            } else {
                settingsBtn.visibility = View.GONE
                myBookmarksImgBtn.visibility = View.GONE
                backToQuotePage.visibility = View.VISIBLE
                activityTitle.text = resources.getText(R.string.myBookMarks)
            }
        }
    }
}