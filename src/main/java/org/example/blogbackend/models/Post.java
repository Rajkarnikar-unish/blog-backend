package org.example.blogbackend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@With
@AllArgsConstructor
@Builder
@NoArgsConstructor
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @NotNull
    @Column(name = "title", nullable=false)
    private String title;

    @NotBlank
    @NotNull
    @Column(name = "content", columnDefinition = "CLOB")
    private String content;

    @ManyToOne//(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private User author;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private EPostStatus status = EPostStatus.DRAFT;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated= LocalDateTime.now();

    @Column(name = "is_published")
    private Boolean isPublished = false;

    public Post(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public void publish() {
        this.status = EPostStatus.PUBLISHED;
        this.isPublished = true;
        this.lastUpdated = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", author=" + author +
                ", createdAt=" + createdAt +
                ", status=" + status +
                ", lastUpdated=" + lastUpdated +
                ", isPublished=" + isPublished +
                '}';
    }
}
