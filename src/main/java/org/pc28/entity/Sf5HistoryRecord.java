package org.pc28.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * SF5服务器历史记录实体类
 */
@Entity
@Table(name = "sf5_history")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Sf5HistoryRecord extends HistoryRecord {
    
}
