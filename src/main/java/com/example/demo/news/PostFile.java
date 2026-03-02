package com.example.demo.news;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "post_files", schema = "public")
@Getter @Setter @NoArgsConstructor
public class PostFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Post post;

    private String originalName;
    private String storedName;
    private String filePath;

    public PostFile(Post post, String originalName, String storedName, String filePath) {
        this.post = post;
        this.originalName = originalName;
        this.storedName = storedName;
        this.filePath = filePath;
    }
}
