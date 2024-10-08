package com.spring.blog.repository;

import com.spring.blog.domain.Article;
import com.spring.blog.domain.ArticleImages;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ArticleImagesRepository extends JpaRepository<ArticleImages, Long> {

    @Modifying
    @Query("DELETE FROM ArticleImages a WHERE a.article.id = :articleId")
    void deleteByArticleId(@Param("articleId") Long articleId);

    @Modifying
    @Query("DELETE FROM ArticleImages a WHERE a.article IN :articles")
    void deleteByArticles(@Param("articles") List<Article> articles);

    List<ArticleImages> getArticleImagesByArticle(Article article);

    @Query("SELECT a FROM ArticleImages a WHERE a.article IN :articles")
    List<ArticleImages> findAllByArticles(@Param("articles") List<Article> articles);
}