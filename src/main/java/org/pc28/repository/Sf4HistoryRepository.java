package org.pc28.repository;

import org.pc28.entity.Sf4HistoryRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * SF4服务器历史记录存储库
 */
@Repository
public interface Sf4HistoryRepository extends JpaRepository<Sf4HistoryRecord, Long> {
    
    /**
     * 根据期号查找记录
     */
    Optional<Sf4HistoryRecord> findByPeriod(Integer period);
    
    /**
     * 检查指定期号的记录是否存在
     */
    @Query("SELECT COUNT(s) > 0 FROM Sf4HistoryRecord s WHERE s.period = :period")
    boolean existsByPeriod(@Param("period") Integer period);
}
