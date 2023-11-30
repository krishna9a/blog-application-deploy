package com.mountblue.blogapplication.service;

import com.mountblue.blogapplication.entity.Comment;
import com.mountblue.blogapplication.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {

    private CommentRepository commentRepository;

    @Autowired
    public CommentService(CommentRepository commentRepository){
        this.commentRepository=commentRepository;
    }

    public List<Comment> getCommentByPostId(Long postId){
        return commentRepository.findByPostId(postId);
    }
    public Comment writeComment(Comment comment){
        return  commentRepository.save(comment);
    }

    public void updateComment(Long id,String updateComment){
        Comment existingComment=commentRepository.findById(id).orElse(null);
        if(existingComment !=null){
            existingComment.setComment(updateComment);
            commentRepository.save(existingComment);
        }
    }

    public void deleteComment(Long id){
        commentRepository.deleteById(id);
    }

    public Comment getCommentById(Long id){
        return commentRepository.findCommentById(id);
    }

}
