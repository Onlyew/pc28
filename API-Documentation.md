
# 红淘淘网页下载API文档

## 概述

本API用于从红淘淘网站(http://www.redtaotao.com/jnd28_sf1_mszh.html)下载网页内容并保存到本地。

## 基本信息

- **版本**：1.0.0
- **基础URL**：`http://localhost:8080`
- **联系人**：开发团队
- **联系邮箱**：support@example.com
- **许可证**：Apache 2.0

## API端点

### 下载网页

从红淘淘网站下载网页内容并保存到本地，返回下载结果信息。

#### 请求

- **URL**：`/api/download/webpage`
- **方法**：`GET`
- **请求参数**：无

#### 响应

##### 成功响应 (200 OK)

```json
{
  "success": true,
  "message": "网页下载成功",
  "filePath": "E:/pc28/download/redtaotao.html",
  "downloadTimeMillis": 1245,
  "contentLength": 12500
}
```

##### 失败响应 (400 Bad Request)

```json
{
  "success": false,
  "message": "网页下载失败",
  "error": "连接超时"
}
```

##### 服务器错误 (500 Internal Server Error)

```json
{
  "success": false,
  "message": "服务器内部错误",
  "error": "处理请求时发生错误"
}
```

## 模型定义

### WebDownloadResult

网页下载结果对象。

| 属性名             | 类型    | 描述                     | 示例                                         |
| ------------------ | ------- | ------------------------ | -------------------------------------------- |
| success            | boolean | 下载是否成功             | true                                         |
| content            | string  | 下载的网页内容           | "`<html><body>`网页内容`</body></html>`" |
| filePath           | string  | 保存的文件路径           | "E:/pc28/download/redtaotao.html"            |
| errorMessage       | string  | 错误信息（如果下载失败） | "连接超时"                                   |
| downloadTimeMillis | long    | 下载耗时（毫秒）         | 1245                                         |

## 配置说明

### 常量配置

所有常量配置都在 `WebConstants`类中定义：

| 常量名             | 值                                                                                                                    | 描述                     |
| ------------------ | --------------------------------------------------------------------------------------------------------------------- | ------------------------ |
| TARGET_URL         | "http://www.redtaotao.com/jnd28_sf1_mszh.html"                                                                        | 目标网页URL              |
| SAVE_PATH          | "download"                                                                                                            | 下载文件保存路径         |
| FILE_NAME          | "redtaotao.html"                                                                                                      | 下载文件名               |
| CONNECTION_TIMEOUT | 10000                                                                                                                 | 请求超时时间（毫秒）     |
| READ_TIMEOUT       | 10000                                                                                                                 | 请求读取超时时间（毫秒） |
| USER_AGENT         | "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36" | 用户代理头信息           |

## 使用示例

### curl

```bash
curl -X GET http://localhost:8080/api/download/webpage
```

### Java

```java
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ApiClient {
    public static void main(String[] args) {
        try {
            URL url = new URL("http://localhost:8080/api/download/webpage");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
          
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
          
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
          
            System.out.println(response.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

## 错误处理

API使用标准的HTTP状态码来表示请求的状态：

- **200 OK**：请求成功
- **400 Bad Request**：请求参数错误或处理请求时出现业务逻辑错误
- **500 Internal Server Error**：服务器内部错误

## 注意事项

1. 该API仅用于下载红淘淘网站的网页内容，请勿用于非法用途
2. 下载的内容会保存在项目根目录的download文件夹下
3. 如需使用不同的保存路径，可以修改WebConstants类中的常量配置
