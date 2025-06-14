package org.pc28.repository;

import org.pc28.entity.Sf6HistoryRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * SF6服务器历史记录存储库
 */
@Repository
public interface Sf6HistoryRepository extends JpaRepository<Sf6HistoryRecord, Long> {
    
    /**
     * 根据期号查找记录
     */
    Optional<Sf6HistoryRecord> findByPeriod(Integer period);
    
    /**
     * 检查指定期号的记录是否存在
     */
    @Query("SELECT COUNT(s) > 0 FROM Sf6HistoryRecord s WHERE s.period = :period")
    boolean existsByPeriod(@Param("period") Integer period);
}
