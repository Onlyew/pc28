package org.pc28.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pc28.model.WebDownloadResult;
import org.pc28.service.WebDownloadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 网页下载控制器
 */
@RestController
@RequestMapping("/api/download")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "网页下载", description = "提供网页下载相关功能的API")
public class WebDownloadController {
    
    private final WebDownloadService webDownloadService;
    
    /**
     * 下载网页
     * 
     * @return 下载结果
     */
    @GetMapping("/webpage")
    @Operation(
            summary = "下载网页",
            description = "从红淘淘网站下载网页内容并保存到本地，返回下载结果信息"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "下载成功",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "下载失败",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "服务器内部错误",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<Map<String, Object>> downloadWebpage() {
        log.info("收到下载网页请求");
        
        WebDownloadResult result = webDownloadService.downloadAndSaveDefault();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", result.isSuccess());
        
        if (result.isSuccess()) {
            response.put("message", "网页下载成功");
            response.put("filePath", result.getFilePath());
            response.put("downloadTimeMillis", result.getDownloadTimeMillis());
            response.put("contentLength", result.getContent().length());
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "网页下载失败");
            response.put("error", result.getErrorMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}