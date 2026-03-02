package com.example.demo.news.model;

import com.example.demo.user.model.SiteUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "posts", schema = "public")
@Getter @Setter @NoArgsConstructor
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String category;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String author;

    private LocalDateTime createDate;

    private int views;

    private int recommends;

    private String ipAddress;

    private boolean isHidden = false;

    @ManyToOne
    private SiteUser user;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    private List<Comment> commentList = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    private List<PostFile> fileList = new ArrayList<>();

    public Post(String category, String title, String content, String author, LocalDateTime createDate, int views, int recommends) {
        this.category = category;
        this.title = title;
        this.content = content;
        this.author = author;
        this.createDate = createDate;
        this.views = views;
        this.recommends = recommends;
    }
}
