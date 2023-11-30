package com.mountblue.blogapplication.service;

import com.mountblue.blogapplication.entity.Post;
import com.mountblue.blogapplication.entity.Tag;
import com.mountblue.blogapplication.repository.PostRepository;
import com.mountblue.blogapplication.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class PostService {

    private final PostRepository postRepository;

    private final TagRepository tagRepository;

    private final TagService tagService;

    @Autowired
    public PostService(PostRepository postRepository,TagRepository tagRepository,TagService tagService){
        this.postRepository=postRepository;
        this.tagRepository=tagRepository;
        this.tagService=tagService;
    }

    public Post getPostById(Long id){
        return  postRepository.findById(id).get();
    }

    public void deletePostById(Long id){
        postRepository.deleteById(id);
    }
    public void updatePost(Post updatedPost,String tagNames,Long id) {

        updatedPost.getTag().clear();
        String[] tagArray = tagNames.split(",");

        for (String tagName : tagArray) {
            tagName = tagName.trim();
            Tag tags = tagRepository.findByName(tagName).orElse(new Tag());

            if (tags.getName()!=null && !tags.getName().isEmpty()) {
                updatedPost.getTag().add(tags);
            } else {
                tags.setName(tagName);
                tagRepository.save(tags);
                updatedPost.getTag().add(tags);
            }
        }

        updatedPost.setUpdatedAt(LocalDateTime.now());
        updatedPost.setId(id);
        postRepository.save(updatedPost);
    }

    public void createPostWithTags(Post post, String userTag) {
        post.setCreatedAt(LocalDateTime.now());
        String[] tags = userTag.split(",");
        Set<Tag> uniqueTags = new HashSet<>();

        for (String tag : tags) {
            tag = tag.trim();
            Tag existingTag = tagService.getTagByName(tag);
            if (existingTag != null) {
                uniqueTags.add(existingTag);
            } else {
                Tag newTag = new Tag();
                newTag.setName(tag);
                tagService.saveTag(newTag);
                uniqueTags.add(newTag);
            }
        }

        post.setTag(uniqueTags);
        postRepository.save(post);
    }
    public Page<Post> filteredPageWithPaggination(Set<String> authors, Set<Long> tags, int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);

        if (authors != null && !authors.isEmpty() && tags != null && !tags.isEmpty()) {
            return postRepository.findByAuthorInAndTagIdIn(authors, tags, pageable);
        } else if (authors != null && !authors.isEmpty()) {
            return postRepository.findByAuthorIn(authors, pageable);
        } else if (tags != null && !tags.isEmpty()) {
            return postRepository.findByTagIdIn(tags, pageable);
        } else {
            return postRepository.findAll(pageable);
        }
    }

    public Page<Post> sortByPaginationField(Integer pageNumber, Integer pagesize,String field,String sortDirection){
        Sort sort=sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name())?
                Sort.by(field).ascending(): Sort.by(field).descending();
        Pageable pageable=PageRequest.of(pageNumber-1,pagesize,sort);
        return postRepository.findAll(pageable);
    }

    public Page<Post> searchAndSortByPaginationField(String searchTerm, Integer pageNo, int pageSize, String sortField,
                                                     String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortField);
        PageRequest pageRequest = PageRequest.of(pageNo-1, pageSize, sort);

        return postRepository.searchByMultipleFields(searchTerm, pageRequest);
    }

    public Set<String> getAllAuthors() {
        return postRepository.findAllAuthors();
    }

    public void savePost(Post post){
        postRepository.save(post);
    }

}
