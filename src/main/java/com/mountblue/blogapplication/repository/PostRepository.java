package com.mountblue.blogapplication.repository;

import com.mountblue.blogapplication.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;

public interface PostRepository extends JpaRepository<Post,Long> {
    Page<Post> findByAuthorInAndTagIdIn(Set<String> authorNames, Set<Long> tagIds, Pageable pageable);
    Page<Post> findByAuthorIn(Set<String> authors, Pageable pageable);
    Page<Post> findByTagIdIn(Set<Long> tags, Pageable pageable);

    @Query("SELECT DISTINCT p.author FROM Post p")
    Set<String> findAllAuthors();

    @Query("SELECT p FROM Post p " +
            "LEFT JOIN FETCH p.tag t " +
            "WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(p.excerpt) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(p.author) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(p.content) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(t.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) ")
    Page<Post> searchByMultipleFields(String searchTerm, PageRequest pageRequest);

//    Post findPostByCommentId(Long id);
}
