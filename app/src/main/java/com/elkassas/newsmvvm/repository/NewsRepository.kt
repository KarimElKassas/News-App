package com.elkassas.newsmvvm.repository

import com.elkassas.newsmvvm.api.RetrofitInstance
import com.elkassas.newsmvvm.data.Article
import com.elkassas.newsmvvm.room.ArticlesDatabase

class NewsRepository(val db: ArticlesDatabase) {

    suspend fun getBreakingNews(countryCode: String, pageNumber: Int) =
        RetrofitInstance.api.getBreakingNews(countryCode, pageNumber)

    suspend fun searchNews(searchQuery: String, pageNumber: Int) =
        RetrofitInstance.api.searchForNews(searchQuery, pageNumber)


    suspend fun insertSaved(article: Article) = db.getArticleDao().insert(article)

    fun getSavedArticles() = db.getArticleDao().getAllArticles()

    suspend fun deleteSaved(article: Article) = db.getArticleDao().deleteArticle(article)


}