package com.elkassas.newsmvvm.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.elkassas.newsmvvm.R
import com.elkassas.newsmvvm.databinding.ActivityNewsBinding
import com.elkassas.newsmvvm.repository.NewsRepository
import com.elkassas.newsmvvm.room.ArticlesDatabase

class NewsActivity : AppCompatActivity() {

    private lateinit var binding : ActivityNewsBinding
    private lateinit var view : View

    private lateinit var navController : NavController

    lateinit var viewModel: NewsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Binding View
        binding = ActivityNewsBinding.inflate(layoutInflater)
        view = binding.root
        setContentView(view)

        //get nav controller
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.newsNavHostFragment) as NavHostFragment
        navController = navHostFragment.navController

        //attach bottom navigation view with navigation component to navigate into fragments
        binding.bottomNav.setupWithNavController(navController)

        //view model
        val newsRepository = NewsRepository(ArticlesDatabase(this))
        val viewModelProviderFactory = NewsViewModelProviderFactory(application,newsRepository)
        viewModel = ViewModelProvider(this,viewModelProviderFactory).get(NewsViewModel::class.java)


    }
}