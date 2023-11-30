package com.mountblue.blogapplication.controller;

import com.mountblue.blogapplication.entity.Comment;
import com.mountblue.blogapplication.entity.Post;
import com.mountblue.blogapplication.entity.Users;
import com.mountblue.blogapplication.service.CommentService;
import com.mountblue.blogapplication.service.PostService;
import com.mountblue.blogapplication.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class CommentController {

    private CommentService commentService;
    private PostService postService;

    private UserService userService;

    @Autowired
    public CommentController (CommentService commentService, PostService postService,UserService userService){
        this.commentService=commentService;
        this.postService=postService;
        this.userService=userService;
    }

    @GetMapping("/read/{postId}")
    public String addComment(@PathVariable Long postId, @ModelAttribute("newComment") Comment newComment) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();

        Post post = postService.getPostById(postId);
        newComment.setPost(post);
        newComment.setName(currentPrincipalName);

        commentService.writeComment(newComment);

        return "redirect:/readmore/{postId}";
    }

    @GetMapping("/comment")
    public String updateCommentOnPost(@RequestParam("postId") Long id, Model model) {
        Comment existingComment = commentService.getCommentById(id);
        Post post = existingComment.getPost();

        if (!isCurrentUserAuthor(post) && !isCurrentUserAdmin()) {
            System.out.println(isCurrentUserAuthor(post));
            return "redirect:/";
        }

        model.addAttribute("existingComment", existingComment);
        return "comment_update";
    }


    @PostMapping("/comments/{postId}")
    public String updateComment(@PathVariable Long postId, @ModelAttribute("existingComment") Comment comment) {

        String updated = comment.getComment();
        commentService.updateComment(postId, updated);
        Comment commentUpdate = commentService.getCommentById(postId);
        Long id = commentUpdate.getPost().getId();
        return "redirect:/readmore/" + id;
    }

    @GetMapping("/delete")
    public String deleteComment(@RequestParam("commentId") Long commentId) {
        Comment commentUpdate = commentService.getCommentById(commentId);
        Post post=commentUpdate.getPost();
        if (!isCurrentUserAuthor(post) && !isCurrentUserAdmin()) {
            return "redirect:/";
        }
        Long id = commentUpdate.getPost().getId();
        commentService.deleteComment(commentId);
        return "redirect:/readmore/" + id;
    }

    private boolean isCurrentUserAuthor(Post post) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication ==null || !authentication.isAuthenticated()) {
            return false;
        }
        String currentPrincipalName = authentication.getName();
        Users currentUser = userService.getUserByEmail(currentPrincipalName);
        if (currentUser == null) {
            return false;
        }
        if (currentUser.getName() == null) {
            return false;
        }
        return currentUser.getName().equals(post.getAuthor());
    }
    private boolean isCurrentUserAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() &&
                authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_admin"));
    }


}
