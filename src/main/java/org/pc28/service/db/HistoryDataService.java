package org.pc28.service.db;

import java.util.List;
import java.util.Map;

/**
 * 历史数据数据库服务接口
 */
public interface HistoryDataService {
    
    /**
     * 保存单个服务器的历史数据
     * 
     * @param serverPrefix 服务器前缀（如sf1, sf3等）
     * @param data 解析结果数据
     * @return 保存的记录数量
     */
    int saveServerData(String serverPrefix, Map<String, Object> data);
    
    /**
     * 保存所有服务器的历史数据
     * 
     * @param allResults 所有服务器的解析结果
     * @return 各服务器保存的记录数量
     */
    Map<String, Integer> saveAllServersData(List<Map<String, Object>> allResults);
}
