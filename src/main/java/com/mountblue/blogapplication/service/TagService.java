package com.mountblue.blogapplication.service;

import com.mountblue.blogapplication.entity.Tag;
import com.mountblue.blogapplication.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TagService {
    private TagRepository tagRepository;

    @Autowired
    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }


    public void saveTag(Tag tag) {
        tagRepository.save(tag);
    }

    public Tag getTagByName(String tagName) {
        return tagRepository.findByName(tagName).orElse(null);
    }
}
