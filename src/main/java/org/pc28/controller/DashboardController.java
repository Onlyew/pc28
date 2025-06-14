package org.pc28.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 仪表盘控制器
 * 负责处理仪表盘页面的请求
 */
@Controller
public class DashboardController {
    
    /**
     * 现金流量分析仪表盘页面
     * 
     * @return 重定向到静态HTML页面
     */
    @GetMapping("/cashflow")
    public String cashflowDashboard() {
        return "redirect:/cashflow-dashboard.html";
    }
}
