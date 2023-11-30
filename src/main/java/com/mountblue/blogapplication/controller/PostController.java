package com.mountblue.blogapplication.controller;

import com.mountblue.blogapplication.entity.Comment;
import com.mountblue.blogapplication.entity.Post;
import com.mountblue.blogapplication.entity.Tag;
import com.mountblue.blogapplication.entity.Users;
import com.mountblue.blogapplication.repository.TagRepository;
import com.mountblue.blogapplication.repository.UserRepository;
import com.mountblue.blogapplication.service.CommentService;
import com.mountblue.blogapplication.service.PostService;
import com.mountblue.blogapplication.service.TagService;
import com.mountblue.blogapplication.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Controller
public class PostController {


    private PostService postService;

    private CommentService commentService;
    private TagService tagService;

    private TagRepository tagRepository;

    private UserService userService;
    private UserRepository userRepository;

    @Autowired
    public PostController(PostService postService, CommentService commentService, TagService tagService,
                          TagRepository tagRepository,UserService userService,UserRepository userRepository) {
        this.postService = postService;
        this.commentService = commentService;
        this.tagService = tagService;
        this.tagRepository = tagRepository;
        this.userService=userService;
        this.userRepository=userRepository;
    }

    @PostMapping("/register")
    public String saveUserData(@ModelAttribute Users user){
        user.setRole("ROLE_author");
        userService.saveUser(user);
        return "login";
    }

private boolean isCurrentUserAuthor(Post post) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
        return false;
    }

    String currentPrincipalName = authentication.getName();
    Users currentUser = userService.getUserByEmail(currentPrincipalName);
    if (currentUser == null) {
        return false;
    }
    if (currentUser.getName() == null && currentUser.getRole()!="ADMIN") {
        return false;
    }

    return currentUser.getName().equals(post.getAuthor());
}
    private boolean isCurrentUserAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() &&
                authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_admin"));
    }


    private void handleAuthentication(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("user", authentication.getPrincipal());
        }
    }

    @GetMapping("/")
    public String getAllPost(@RequestParam(name = "searchTerm", required = false) String searchTerm,
                             Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        Users currentUser = userService.getUserByEmail(currentPrincipalName);
        return getSortedPageWithPagginationWithField(1, "title", "asc", searchTerm,
                model,currentUser);
    }

    @GetMapping("/page/{pageNo}")
    public String getSortedPageWithPagginationWithField(
            @PathVariable(name = "pageNo") Integer pageNo,
            @RequestParam("sortField") String sortField,
            @RequestParam("sortDir") String sortDir,
            @RequestParam(name = "searchTerm", required = false) String searchTerm,
            Model model,Users user) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        user=userService.getUserByEmail(currentPrincipalName);

        int pageSize = 10;
        List<Post> allPost;

        if (searchTerm != null && !searchTerm.isEmpty()) {
            Page<Post> searchResults = postService.searchAndSortByPaginationField(searchTerm,
                    pageNo, pageSize, sortField, sortDir);
            allPost = searchResults.getContent();
            model.addAttribute("totalPages", searchResults.getTotalPages());
            model.addAttribute("totalItems", searchResults.getTotalElements());
        } else {
            Page<Post> sortPost = postService.sortByPaginationField(pageNo, pageSize, sortField, sortDir);
            allPost = sortPost.getContent();
            model.addAttribute("totalPages", sortPost.getTotalPages());
            model.addAttribute("totalItems", sortPost.getTotalElements());
        }

        model.addAttribute("currentPage", pageNo);
        model.addAttribute("post", allPost);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reserveSortDir", sortDir.equals("asc") ? "desc" : "asc");
        model.addAttribute("searchTerm", searchTerm);
        model.addAttribute("user",user);

        return "post";
    }


    @GetMapping("/filter")
    public String getfilteredPosts(@RequestParam(required = false) Set<String> authors,
                                   @RequestParam(required = false) Set<Long> tags,
                                   @RequestParam(name = "page", defaultValue = "0") Integer pageNo,
                                   Model model) {
        int pageSize = 10;

        Page<Post> filteredPosts = postService.filteredPageWithPaggination(authors, tags, pageNo, pageSize);
        if (filteredPosts.isEmpty()) {
            return "404";
        }
        String sortDir = "asc";
        String sortField = "publishedAt";
        model.addAttribute("currentPage", pageNo);
        model.addAttribute("totalPages", filteredPosts.getTotalPages());
        model.addAttribute("totalItems", filteredPosts.getTotalElements());
        model.addAttribute("post", filteredPosts.getContent());
        model.addAttribute("authors", authors);
        model.addAttribute("tags", tags);

        return "filteredPost";
    }


    @GetMapping("/option")
    public String authorAndTagsMenu(Model model) {
        model.addAttribute("authors", postService.getAllAuthors());
        List<Tag> getAlltags = tagService.getAllTags();
        model.addAttribute("tags", getAlltags);
        return "filterOption";
    }

    @GetMapping("/post")
    public String createNewPost(Model model) {
        Post newPost = new Post();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();

        Users currentUser = userService.getUserByEmail(currentPrincipalName);
        newPost.setCreatedAt(LocalDateTime.now());
        newPost.setAuthor(currentUser.getName());
        model.addAttribute("post", newPost);
        model.addAttribute("existingTags", tagService.getAllTags());
        return "post_new";
    }

    @PostMapping("/submission")
    public String showPost(Post post, @RequestParam("existingTags") String tag) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();

        Users currentUser = userService.getUserByEmail(currentPrincipalName);

        post.setAuthor(currentUser.getName());
        post.setPublishedAt(LocalDateTime.now());
        postService.createPostWithTags(post, tag);
        return "redirect:/";
    }


    @GetMapping("/readmore/{postId}")
    public String viewPost(@PathVariable("postId") Long postId, Model model) {

        Post post = postService.getPostById(postId);
        model.addAttribute("post", post);

        List<Comment> comments = commentService.getCommentByPostId(postId);
        model.addAttribute("comments", comments);
        model.addAttribute("newComment", new Comment());

        return "post-details";
    }

    @GetMapping("/showFormForUpdate/{postId}")

    public String showPostUpdate(@PathVariable Long postId, Model model) {
        Post existingPost = postService.getPostById(postId);


        if (!isCurrentUserAuthor(existingPost) && !isCurrentUserAdmin()) {
            return "redirect:/";
        }
        StringBuilder tagValueWithComma = new StringBuilder();
        for (Tag tag : existingPost.getTag()) {
            tagValueWithComma.append(tag.getName());
            tagValueWithComma.append(",");
        }
        if (tagValueWithComma.length() > 0) {
            tagValueWithComma.deleteCharAt(tagValueWithComma.length() - 1);
        }

        model.addAttribute("tagValue", tagValueWithComma.toString());
        model.addAttribute("post", existingPost);
        return "post_update";
    }

    @PostMapping("/updatePost/{postId}")
    public String updatePost(@ModelAttribute("post") Post updatedPost,
                             @RequestParam("updateTag") String tag,
                             @PathVariable("postId") Long id) {

        Post post = postService.getPostById(id);
        if (!isCurrentUserAuthor(post) && !isCurrentUserAdmin()) {
            return "redirect:/";
        }

        updatedPost.setPublishedAt(post.getPublishedAt());
        postService.updatePost(updatedPost, tag,id);
        return "redirect:/readmore/" + updatedPost.getId();
    }

    @GetMapping("/showFormForDelete")
    public String deletePost(@RequestParam("postId") Long id) {
        Post post = postService.getPostById(id);
        if (!isCurrentUserAuthor(post) && !isCurrentUserAdmin()) {
            return "redirect:/readmore/" + post.getId();
        }

        postService.deletePostById(id);
        return "redirect:/";
    }


}
