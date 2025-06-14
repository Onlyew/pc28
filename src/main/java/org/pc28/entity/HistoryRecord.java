package org.pc28.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 历史记录基础实体类
 */
@Data
@NoArgsConstructor
@MappedSuperclass
public abstract class HistoryRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "period", nullable = false, unique = true)
    private Integer period;
    
    @Column(name = "numbers", nullable = false)
    private String numbers;
    
    @Column(name = "prediction")
    private String prediction;
    
    @Column(name = "outcome")
    private String outcome;
    
    @Column(name = "total_number")
    private Integer totalNumber;
    
    @Column(name = "kill_number")
    private String killNumber;
    
    @Column(name = "betting_result")
    private String bettingResult;
    
    @Column(name = "open_result")
    private String openResult;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
