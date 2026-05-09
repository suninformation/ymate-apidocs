# YMATE-APIDOCS

[![Maven Central status](https://img.shields.io/maven-central/v/net.ymate.apidocs/ymate-apidocs-annotation.svg)](https://search.maven.org/artifact/net.ymate.apidocs/ymate-apidocs-annotation)
[![LICENSE](https://img.shields.io/github/license/suninformation/ymate-apidocs.svg)](https://gitee.com/suninformation/ymate-apidocs/blob/master/LICENSE)

为 YMP 框架开发提供的一套基于 Java 注解实现的接口开发文档自动生成工具，支持 HTML、Gitbook、Postman、JSON、 Markdown 等格式。

请参考 [YMATE-MAVEN-PLUGIN](https://gitee.com/suninformation/ymate-maven-plugin#apidocs) 文档了解如何使用文档生成器插件命令。



## Maven包依赖

```xml
<plugin>
    <groupId>net.ymate.apidocs</groupId>
    <artifactId>ymate-apidocs-annotation</artifactId>
    <version>2.0.1</version>
</plugin>
```



## 注解使用说明

### 一、接口文档注解 (@Apis)

声明包或类为API接口文档入口，用于配置文档的标题和版本等基本信息。

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| title | String | - | 文档标题（必填） |
| version | String | - | 版本信息（必填） |
| snakeCase | boolean | false | 是否使用蛇形命名法输出属性名称 |
| order | int | 0 | 自定义排序 |

**示例：**

```java
@Apis(title = "用户管理接口文档", version = "1.0.0")
package com.example.api;
```

---

### 二、接口类注解 (@Api)

声明一个类为API接口并支持文档自动生成。

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| value | String | - | API接口名称（必填） |
| mapping | String | "" | 请求URL地址映射 |
| group | String | "" | 所属分组名称 |
| order | int | 0 | 自定义排序 |
| description | String | "" | API接口描述 |
| scopes | String[] | {} | 授权范围集合 |
| hidden | boolean | false | 是否隐藏 |

**示例：**

```java
@Api(value = "用户接口", mapping = "/user", group = "用户管理", description = "用户相关接口")
public class UserApi {

    @ApiAction(value = "获取用户信息", description = "根据用户ID获取用户详细信息")
    public Result<User> getUser(String userId) {
        // ...
    }
}
```

---

### 三、接口方法注解 (@ApiAction)

声明一个API接口方法。

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| value | String | - | 接口方法显示名称（必填） |
| description | String | "" | 接口方法描述 |
| mapping | String | "" | 请求URL地址映射 |
| notes | String[] | {} | 接口方法提示内容 |
| group | String | "" | 接口方法所属分组 |
| httpStatus | int | 200 | HTTP请求响应状态值 |
| httpMethod | String[] | {} | HTTP请求方法，如：GET, POST, PUT, DELETE等 |
| requestType | String | "" | 请求ContentType类型，可选值：json\|xml |
| scopes | String[] | {} | 授权范围集合 |
| order | int | 0 | 自定义排序 |
| hidden | boolean | false | 是否隐藏 |

**示例：**

```java
@ApiAction(
    value = "创建用户",
    description = "注册一个新用户",
    httpMethod = {"POST"},
    requestType = "json"
)
public Result createUser(@ApiParam(value = "userName", description = "用户名", required = true) String userName) {
    // ...
}
```

---

### 四、接口参数注解 (@ApiParam)

接口方法参数配置。

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| value | String | "" | 参数名称 |
| description | String | "" | 参数说明 |
| defaultValue | String | "" | 参数默认值 |
| demoValue | String | "" | 自动生成示例时的参数值 |
| allowValues | String[] | {} | 参数可选值集合 |
| required | boolean | false | 参数是否必须 |
| type | Class<?> | String.class | 参数类型 |
| minLength | long | 0 | 最小长度 |
| maxLength | long | 0 | 最大长度 |
| model | boolean | false | 是否为模型对象 |
| multiple | boolean | false | 是否为数组集合 |
| multipart | boolean | false | 是否为文件上传 |
| pathVariable | boolean | false | 是否为路径变量 |
| snakeCase | boolean | false | 参数名称是否使用蛇形命名法 |
| example | String | "" | 简单参数示例 |
| examples | ApiExample[] | {} | 参数示例集合 |
| hidden | boolean | false | 是否隐藏 |

**示例：**

```java
@ApiParam(
    value = "pageNum",
    description = "页码",
    defaultValue = "1",
    required = true,
    type = int.class
)
```

---

### 五、开发者注解 (@ApiAuthor / @ApiAuthors)

接口开发者信息。

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| value | String | - | 开发者名称（必填） |
| url | String | "" | 开发者主页地址 |
| email | String | "" | 开发者联系邮箱地址 |

**示例：**

```java
@ApiAuthor(value = "张三", url = "https://example.com", email = "zhangsan@example.com")
@Apis(title = "接口文档", version = "1.0.0")
package com.example.api;
```

或者使用 @ApiAuthors 声明多个开发者：

```java
@ApiAuthors({
    @ApiAuthor(value = "张三", email = "zhangsan@example.com"),
    @ApiAuthor(value = "李四", email = "lisi@example.com")
})
@Apis(title = "接口文档", version = "1.0.0")
package com.example.api;
```

---

### 六、分组注解 (@ApiGroup / @ApiGroups)

定义接口分组。

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| value | String | - | 分组名称（必填） |
| description | String | "" | 分组描述信息 |

**示例：**

```java
@ApiGroup(value = "用户管理", description = "用户相关接口")
@ApiGroup(value = "订单管理", description = "订单相关接口")
public class UserApi {
    // ...
}
```

---

### 七、变更记录注解 (@ApiChangeLog / @ApiChangeLogs)

变更记录信息。

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| date | String | - | 日期，格式如：2018-04-15 01:37（必填） |
| description | String | - | 变更内容描述（必填） |
| action | Action | Action.UPDATE | 变更动作：CREATE, UPDATE, ADD, REMOVE, FIX |
| author | ApiAuthor | @ApiAuthor("") | 变更作者信息 |

**示例：**

```java
@ApiChangeLog(
    date = "2020-01-15 10:30",
    description = "新增用户注册接口",
    action = ApiChangeLog.Action.ADD,
    author = @ApiAuthor("张三")
)
public class UserApi {
    // ...
}
```

---

### 八、响应信息注解 (@ApiResponse / @ApiResponses)

接口方法响应信息。

**@ApiResponse 属性：**

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| httpStatus | int | 200 | HTTP响应状态值 |
| code | String | - | 业务响应码（必填） |
| message | String | - | 响应信息（必填） |

**@ApiResponses 属性：**

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| name | String | "" | 响应数据类型名称 |
| description | String | "" | 描述 |
| value | ApiResponse[] | {} | 响应信息集合 |
| multiple | boolean | false | 是否为数组集合 |
| type | Class<?> | Void.class | 响应数据类型 |
| properties | ApiProperty[] | {} | 响应数据属性集合 |

**示例：**

```java
@ApiResponses({
    @ApiResponse(code = "200", message = "请求成功"),
    @ApiResponse(code = "400", message = "参数错误"),
    @ApiResponse(code = "500", message = "服务器内部错误")
})
public Result getUser(String userId) {
    // ...
}
```

---

### 九、响应数据类型注解 (@ApiResponseType / @ApiResponseTypes)

用于注册一个自定义响应数据类型。

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| name | String | "" | 响应数据类型名称 |
| description | String | "" | 描述 |
| type | Class<?> | - | 响应数据类型（必填） |

**示例：**

```java
@ApiResponseType(
    name = "User",
    description = "用户信息",
    type = User.class
)
public class UserApi {
    // ...
}
```

---

### 十、响应示例注解 (@ApiExample / @ApiExamples)

示例配置。

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| name | String | "" | 示例名称 |
| description | String | "" | 示例描述 |
| type | String | "" | 类型，如：json, xml, java等 |
| value | String | - | 示例内容（必填） |

**示例：**

```java
@ApiExample(
    name = "用户信息示例",
    description = "返回的用户信息结构",
    type = "json",
    value = "{\"id\": 1, \"name\": \"张三\", \"email\": \"zhangsan@example.com\"}"
)
public Result getUser(String userId) {
    // ...
}
```

---

### 十一、自动生成响应示例注解 (@ApiGenerateResponseExample)

用于配合 @ApiResponses 注解生成接口响应示例代码。

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| name | String | "" | 示例名称 |
| description | String | "" | 示例描述 |
| paging | boolean | false | 是否使用分页 |

**示例：**

```java
@ApiResponses({
    @ApiResponse(code = "200", message = "请求成功", type = User.class)
})
@ApiGenerateResponseExample(description = "生成用户信息响应示例", paging = false)
public Result getUser(String userId) {
    // ...
}
```

---

### 十二、请求/响应头注解 (@ApiHeader / @ApiRequestHeaders / @ApiResponseHeaders)

**@ApiHeader 属性：**

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| name | String | - | 名称（必填） |
| value | String | "" | 值 |
| description | String | "" | 内容描述 |
| type | Class<?> | String.class | 数据类型 |

**示例：**

```java
@ApiRequestHeaders({
    @ApiHeader(name = "Authorization", description = "认证令牌", type = String.class),
    @ApiHeader(name = "Content-Type", description = "内容类型", type = String.class)
})
public Result getUser(String userId) {
    // ...
}
```

---

### 十三、授权验证注解 (@ApiAuthorization)

定义接口授权验证信息。

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| value | String | - | 授权类型名称（必填） |
| url | String | - | 授权服务URL地址（必填） |
| type | String | "" | 授权方式 |
| tokenName | String | "" | 令牌名称 |
| tokenStore | TokenStore | TokenStore.PARAMETER | 令牌存储方式：HEADER, PARAMETER |
| requestType | String | "POST" | 令牌HTTP请求类型 |
| requestParams | ApiParam[] | {} | 令牌请求参数集合 |
| scopes | String[] | {} | 授权范围集合 |
| description | String | "" | 描述 |

**示例：**

```java
@ApiAuthorization(
    value = "OAuth2",
    url = "https://auth.example.com/token",
    type = "Bearer",
    tokenName = "access_token",
    tokenStore = ApiAuthorization.TokenStore.HEADER
)
package com.example.api;
```

---

### 十四、授权范围注解 (@ApiScope)

用于自定义授权范围注解。

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| value | String | - | 名称（必填） |

**示例：**

```java
@ApiScope("user:read")
@interface CustomScopeAnnotation {
}
```

---

### 十五、访问权限注解 (@ApiSecurity)

接口访问权限控制。

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| roles | String[] | {} | 角色集合 |
| value | String[] | {} | 权限码集合 |
| logicalType | LogicalType | LogicalType.AND | 逻辑类型：AND, OR |
| description | String | "" | 描述 |

**示例：**

```java
@ApiSecurity(roles = {"ADMIN", "USER"}, logicalType = ApiSecurity.LogicalType.OR)
public Result deleteUser(String userId) {
    // ...
}
```

---

### 十六、服务器注解 (@ApiServer / @ApiServers)

定义API服务器信息。

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| schemes | String[] | {"https", "http"} | 模式，支持 https 和 http |
| host | String | - | 主机访问域名或IP地址（必填） |
| description | String | "" | 描述 |

**示例：**

```java
@ApiServers({
    @ApiServer(schemes = {"https"}, host = "api.example.com", description = "生产环境"),
    @ApiServer(schemes = {"http"}, host = "192.168.1.100:8080", description = "测试环境")
})
package com.example.api;
```

---

### 十七、授权协议注解 (@ApiLicense)

授权协议信息。

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| value | String | - | 协议名称（必填） |
| url | String | "" | 协议URL地址 |
| description | String | "" | 描述信息 |

**示例：**

```java
@ApiLicense(
    value = "Apache License 2.0",
    url = "https://www.apache.org/licenses/LICENSE-2.0",
    description = "本项目采用 Apache 2.0 许可证"
)
package com.example.api;
```

---

### 十八、扩展信息注解 (@ApiExtension / @ApiExtensions)

自定义扩展信息。

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| name | String | "" | 扩展名称 |
| description | String | "" | 扩展描述 |
| value | ApiProperty[] | {} | 扩展属性集合 |

**示例：**

```java
@ApiExtension(
    name = "扩展配置",
    description = "自定义扩展配置",
    value = {
        @ApiProperty(name = "customKey", value = "customValue", description = "自定义键值对")
    }
)
public class UserApi {
    // ...
}
```

---

### 十九、属性注解 (@ApiProperty)

自定义属性配置。

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| name | String | "" | 属性名称 |
| value | String | "" | 属性值 |
| valueClass | Class<?> | Void.class | 自定义值类型 |
| demoValue | String | "" | 自动生成示例时的属性值 |
| model | boolean | false | 是否为模型对象 |
| modelClass | Class<?> | Void.class | 自定义模型对象类型 |
| description | String | "" | 属性描述 |

**示例：**

```java
public class User {
    @ApiProperty(name = "userId", description = "用户ID")
    private Long id;

    @ApiProperty(name = "userName", description = "用户名", required = true)
    private String name;
}
```

---

### 二十、全局默认响应注解 (@ApiDefaultResponses)

注册全局默认接口方法响应信息。

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| standardType | Class<? extends Serializable> | Serializable.class | 自定义通用响应报文结构 |
| pagingType | Class<? extends Serializable> | Serializable.class | 自定义分页查询响应报文结构 |
| codeParamName | String | "" | 自定义响应码参数名称 |
| msgParamName | String | "" | 自定义响应消息描述参数名称 |
| dataParamName | String | "" | 自定义响应业务数据参数名称 |
| examples | ApiExample[] | {} | 响应报文示例 |

**示例：**

```java
@ApiDefaultResponses(
    standardType = StandardResult.class,
    pagingType = PagingResult.class,
    codeParamName = "code",
    msgParamName = "message",
    dataParamName = "data",
    examples = {
        @ApiExample(name = "成功示例", type = "json", value = "{\"code\": 200, \"message\": \"success\", \"data\": {}}"),
        @ApiExample(name = "失败示例", type = "json", value = "{\"code\": 500, \"message\": \"error\", \"data\": null}")
    }
)
package com.example.api;
```

---

## One More Thing

YMP 不仅提供便捷的 Web 及其它 Java 项目的快速开发体验，也将不断提供更多丰富的项目实践经验。

感兴趣的小伙伴儿们可以加入官方 QQ 群：[480374360](https://qm.qq.com/cgi-bin/qm/qr?k=3KSXbRoridGeFxTVA8HZzyhwU_btZQJ2)，一起交流学习，帮助 YMP 成长！

如果喜欢 YMP，希望得到你的支持和鼓励！

![Donation Code](https://ymate.net/img/donation_code.png)

了解更多有关 YMP 框架的内容，请访问官网：[https://ymate.net](https://ymate.net)