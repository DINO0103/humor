package com.example.demo.news;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "categories", schema = "public")
@Getter @Setter @NoArgsConstructor
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    private String icon; // FontAwesome 아이콘 클래스명 (예: fa-star)

    @Column(nullable = false)
    private int orders = 0; // 출력 순서 (낮은 번호가 먼저 출력)

    private boolean isHidden = false;

    private boolean isDeleted = false; // 논리적 삭제

    public Category(String name) {
        this.name = name;
    }
}
