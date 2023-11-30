package com.mountblue.blogapplication.restapicontroller;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/user")
public class PostRestApiController {
    private PostService postService;

    private CommentService commentService;


    private UserService userService;

    private TagService tagService;

    private TagRepository tagRepository;
    private UserRepository userRepository;

    @Autowired
    public PostRestApiController(PostService postService, CommentService commentService, TagService tagService,
                          TagRepository tagRepository,UserService userService,UserRepository userRepository)
    {
        this.postService = postService;
        this.commentService = commentService;
        this.tagService = tagService;
        this.tagRepository = tagRepository;
        this.userService=userService;
        this.userRepository=userRepository;
    }

    @PostMapping("/register")
    public String saveUserData(@RequestBody Users user){
        user.setRole("author");
        userService.saveUser(user);
        return "register successful";
    }

    private boolean isCurrentUserAuthor(Post post) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        Users currentUser = userService.getUserByEmail(currentPrincipalName);
        //System.out.println(currentUser.getName());
        return currentUser.getName().equals(post.getAuthor());
    }

    private void handleAuthentication(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("user", authentication.getPrincipal());
        }
    }

    @GetMapping("/home")
    public ResponseEntity<List<Post>> getAllPost(@RequestParam(name = "searchTerm", required = false) String searchTerm) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        Users currentUser = userService.getUserByEmail(currentPrincipalName);
        return getSortedPageWithPagginationWithField(1, "title", "asc", searchTerm, currentUser);
    }

    @GetMapping("/page/{pageNo}")
    public ResponseEntity<List<Post>>getSortedPageWithPagginationWithField(
            @PathVariable(name = "pageNo") Integer pageNo,
            @RequestParam("sortField") String sortField,
            @RequestParam("sortDir") String sortDir,
            @RequestParam(name = "searchTerm", required = false) String searchTerm, Users user) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        user=userService.getUserByEmail(currentPrincipalName);
        //user.getRole().equals("admin");
        int pageSize = 10;
        List<Post> allPost;

        if (searchTerm != null && !searchTerm.isEmpty()) {
            Page<Post> searchResults = postService.searchAndSortByPaginationField(searchTerm,
                    pageNo, pageSize, sortField, sortDir);
            allPost = searchResults.getContent();
        } else {
            Page<Post> sortPost = postService.sortByPaginationField(pageNo, pageSize, sortField, sortDir);
            allPost = sortPost.getContent();
        }

        return new ResponseEntity<List<Post>>(allPost, HttpStatus.OK);
    }


    @GetMapping("/filter")
    public ResponseEntity<Page<Post>> getfilteredPosts(@RequestParam(required = false) Set<String> authors,
                                   @RequestParam(required = false) Set<Long> tags,
                                   @RequestParam(name = "page", defaultValue = "0") Integer pageNo) {
        int pageSize = 10;

        Page<Post> filteredPosts = postService.filteredPageWithPaggination(authors, tags, pageNo, pageSize);

        return new  ResponseEntity<Page<Post>>(filteredPosts,HttpStatus.OK) ;
    }


    @GetMapping("/option")
    public String authorAndTagsMenu(Model model) {
        model.addAttribute("authors", postService.getAllAuthors());
        List<Tag> getAlltags = tagService.getAllTags();
        model.addAttribute("tags", getAlltags);
        return "filterOption";
    }

    @GetMapping("/post")
    public String createNewPost(@RequestBody Post newPost) {
        //Post newPost = new Post();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();

        Users currentUser = userService.getUserByEmail(currentPrincipalName);
        newPost.setCreatedAt(LocalDateTime.now());
        newPost.setAuthor(currentUser.getName());
        postService.savePost(newPost);
       // model.addAttribute("existingTags", tagService.getAllTags());
        return "new post created";
    }

    @PostMapping("/post/submission")
    public String showPost(@RequestBody Post post, @RequestParam("existingTags") String tag) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();

        Users currentUser = userService.getUserByEmail(currentPrincipalName);

        post.setAuthor(currentUser.getName());
        post.setPublishedAt(LocalDateTime.now());
        postService.createPostWithTags(post, tag);
        return "redirect:/";
    }


    @GetMapping("/readmore/{postId}")
    public Post viewPost(@PathVariable("postId") Long postId) {
        Post post = postService.getPostById(postId);
        List<Comment> comments = commentService.getCommentByPostId(postId);
        post.setComments(comments);
        return post;
    }

    @GetMapping("/showFormForUpdate/{postId}")
    public String showPostUpdate(@PathVariable Long postId, Model model) {
        Post existingPost = postService.getPostById(postId);


        if (!isCurrentUserAuthor(existingPost)) {
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
    public Post updatePost(@RequestBody Post updatedPost,
                             @RequestParam("updateTag") String tag,
                             @PathVariable("postId") Long id) {

        Post post = postService.getPostById(id);
//        if (!isCurrentUserAuthor(post)) {
//            return "redirect:/";
//        }

        updatedPost.setPublishedAt(post.getPublishedAt());
        postService.updatePost(updatedPost, tag, id);
        return updatedPost;
    }

    @GetMapping("/showFormForDelete")
    public String deletePost(@RequestParam("postId") Long id) {
        Post post = postService.getPostById(id);
//        if (!isCurrentUserAuthor(post)) {
//            return "redirect:/readmore/" + post.getId();
//        }

        postService.deletePostById(id);
        return "post deleted successfully";
    }

    @GetMapping("/read{postId}")
    public String addComment(@RequestParam Long postId, @RequestBody Comment newComment) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();

        Post post = postService.getPostById(postId);
        newComment.setPost(post);
        newComment.setName(currentPrincipalName);

        commentService.writeComment(newComment);

        return "comment updated succesfully";
    }

    @GetMapping("/comment")
    public Post showUpdatePost(@RequestParam("postId") Long id, Model model) {
        Comment existingComment = commentService.getCommentById(id);
        Post post = existingComment.getPost();

//        if (!isCurrentUserAuthor(post)) {
//            System.out.println(isCurrentUserAuthor(post));
//            return "redirect:/";
//        }

        model.addAttribute("existingComment", existingComment);
        return post;
    }


    @PostMapping("/comments/{postId}")
    public Post updateComment(@PathVariable Long postId, @ModelAttribute("existingComment") Comment comment) {

        String updated = comment.getComment();
        commentService.updateComment(postId, updated);
        Comment commentUpdate = commentService.getCommentById(postId);
        Long id = commentUpdate.getPost().getId();
        Post post=postService.getPostById(id);
        return post;
    }

    @GetMapping("/delete")
    public String deleteComment(@RequestParam("commentId") Long commentId) {
        Comment commentUpdate = commentService.getCommentById(commentId);
        Post post=commentUpdate.getPost();
        if (!isCurrentUserAuthor(post)) {
            return "redirect:/";
        }
        Long id = commentUpdate.getPost().getId();
        commentService.deleteComment(commentId);
        return "comment deleted successfully";
    }

}
