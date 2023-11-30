package com.mountblue.blogapplication.repository;

import com.mountblue.blogapplication.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag,Long> {
    Optional<Tag> findByName(String tagName);

}
