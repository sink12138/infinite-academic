# Infinite Academic - 后端开发规范



## 1. 开发环境

主要部件版本如下：

> Java 语言版本：17（使用 Oracle 官网的 JDK 或 OpenJDK）
>
> Springboot 版本：2.5.6
>
> Spring Cloud 版本：2020.4
>
> Elasticsearch 依赖项版本：7.15.2
>
> Knife4j 依赖项版本：3.0.3

其余版本均已在统一（公共） pom.xml 中定义或计划在服务器上部署，使用时无需考虑，**请不要修改版本**。

开发工具：推荐 JetBrains IntelliJ IDEA，请升级到最新版。



## 2. 工程和模块

### 2.1 打开工程

将仓库 clone 到本地，切换到 backend 分支。工程根目录是 `/backend/academic/`。

使用 IDEA **打开（注意不是新建）**以上目录，在弹出的对话框中选择 **Trust maven project**。

### 2.2 新建模块

所有模块均位于 `/backend/academic/modules/`。若要新建自己负责的模块，在工程名上右键 new - module：

<img src="img/Infinite%20Academic%20-%20Development%20Specification/image-20211028152257041.png" alt="image-20211028152257041" style="zoom: 40%;" />

选择 Spring Initializr，**注意设置好模块目录**。Name 和 Artifact 为模块名，Group 为 com.buaa，Package name 为 com.buaa.academic.<模块名>。Java 选择 17。

<img src="img/Infinite%20Academic%20-%20Development%20Specification/image-20211028152902926.png" alt="image-20211028152902926" style="zoom:50%;" />

点击 Next，Springboot 版本选择 2.5.6（可以先不管，新建后从 pom 里直接继承父工程的版本）。

#### 2.2.1 pom.xml 依赖

根据需要选择依赖。父工程已经包括 lombok、fastjson 依赖，实体类模块已经包含 elasticsearch 依赖，可以不用添加。一般需要选择 Web - Spring Web 和 Spring Cloud Discovery - Eureka Discovery Client。

或者直接复制以下 pom，填入一些你的模块的具体信息：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.buaa</groupId>
        <artifactId>academic</artifactId>
        <version>1.0</version>
        <relativePath>../../pom.xml</relativePath>
    </parent>

    <artifactId>模块名</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>search</name>
    <description>模块描述，英文</description>
    <packaging>jar</packaging>

    <dependencies>
        <dependency>
            <groupId>com.buaa</groupId>
            <artifactId>document</artifactId>
            <version>1.0</version>
            <scope>system</scope>
            <systemPath>${project.basedir}/../document/target/document-1.0.jar</systemPath>
        </dependency>
        <dependency>
            <groupId>com.buaa</groupId>
            <artifactId>model</artifactId>
            <version>1.0</version>
            <scope>system</scope>
            <systemPath>${project.basedir}/../model/target/model-1.0.jar</systemPath>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
        <!-- 后面加其他自己需要的依赖 -->
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <includeSystemScope>true</includeSystemScope>
                </configuration>
            </plugin>
        </plugins>
    </build>

</project>
```

在整个工程根目录的父 pom.xml 的 modules 标签里加上你新建的模块：

```xml
<modules>
    <module>modules/document</module>
    <module>modules/model</module>
    <module>modules/eureka</module>
    <module>modules/gateway</module>
    <module>modules/search</module>
    <module>modules/你的模块名字</module>
</modules>
```

#### 2.2.2 配置文件 application.yml

配置文件统一使用 .yml 格式（删掉默认的 .properties），且使用`dev`和`prod`两套配置。

application.yml 指定了默认运行时使用的配置策略：

```yaml
spring:
  profiles:
    default: dev
```

application-dev.yml 指定了本地开发测试时使用的配置策略（注意 redis 是本地连接的，其余是远程连接服务器的）：

```yaml
server:
  port: 8091

spring:
  application:
    name: client-search
  jackson:
    default-property-inclusion: non_null
  redis:
    password: 114514
    timeout: 5000
    jedis:
      pool:
        max-idle: 10
        max-wait: -1ms
        min-idle: 2
  elasticsearch:
    rest:
      username: elastic
      password: elastic-academic-2021
      uris: http://120.46.154.200:9200

eureka:
  instance:
    instance-id: ${spring.application.name}:${server.port}
    prefer-ip-address: true
  client:
    serviceUrl:
      defaultZone: http://eureka:eureka-academic-2021@121.36.98.60:8100/eureka/
    register-with-eureka: false

knife4j:
  basic:
    enable: true
    username: knife4j
    password: knife4j-academic-2021
  enable: true

logging:
  level:
    com.netflix.discovery.shared.resolver.aws.ConfigClusterResolver: warn
    com.netflix.discovery.DiscoveryClient: warn
```

application-prod.yml 指定了在服务器上部署时（生产环境中）使用的配置：

```yaml
server:
  port: 8091

spring:
  application:
    name: client-search
  jackson:
    default-property-inclusion: non_null
  redis:
    password: redis-academic-2021
    timeout: 5000
    cluster:
      nodes:
        - 192.168.0.228:7001
        - 192.168.0.228:7002
        - 192.168.0.228:7003
        - 192.168.0.239:7001
        - 192.168.0.239:7002
        - 192.168.0.239:7003
      max-redirects: 3
    jedis:
      pool:
        max-idle: 10
        max-wait: -1ms
        min-idle: 2
  elasticsearch:
    rest:
      username: elastic
      password: elastic-academic-2021
      uris: http://192.168.0.239:9200

eureka:
  instance:
    instance-id: ${spring.application.name}:${server.port}
    prefer-ip-address: true
  client:
    serviceUrl:
      defaultZone: http://eureka:eureka-academic-2021@192.168.0.228:8100/eureka/

knife4j:
  basic:
    enable: true
    username: knife4j
    password: knife4j-academic-2021
  enable: true

logging:
  level:
    com.netflix.discovery.shared.resolver.aws.ConfigClusterResolver: warn
    com.netflix.discovery.DiscoveryClient: warn
```

#### 2.2.3 knife4j 配置类

Knife4j 配置可参考 search 模块的 com.buaa.academic.search.config.SwaggerConfiguration：

```java
package com.buaa.academic.search.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
@Import(BeanValidatorPluginsConfiguration.class)
public class SwaggerConfiguration {

    @Bean
    @Order(value = 1)
    public Docket groupRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(groupApiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.buaa.academic.search.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo groupApiInfo() {
        return new ApiInfoBuilder()
                .title("搜索模块 - API文档")
                .description("<div style='font-size:14px;'>实体类信息显示、批量查询、关联搜索</div>")
                .contact(new Contact("yq", "", ""))
                .termsOfServiceUrl("http://http://120.46.154.200:8091/")
                .version("1.0")
                .build();
    }

}
```

每人自行负责维护自己模块 pom 里的版本号，初始默认为 0.0.1-SNAPSHOT。

### 2.3 公共和结构性模块

eureka 是微服务注册和发现中心，gateway 是统一网关，平时有专人维护，不需要修改。

document 是数据库实体模块，model 是通用模型，包括响应体、请求体、异常等。**修改这两个模块后需要提醒其他开发人员，并且这两个模块在出现修改后需要重新 package。**

此外，tool 是通用工具类模块，欢迎添加你自己编写的实用工具类。



## 编码和注释

### 3.1 命名规范和项目结构

类名：大写驼峰；变量名：小写驼峰；枚举变量名：全大写加下划线分割；包名：全小写不加下划线。

JSON 键名为小写驼峰格式，多值属性用复数命名。

除启动类以外，所有类均要置于 com.buaa.academic.<模块名> 的**子包**内：

| 层                 | 子包名       | 类名             |
| ------------------ | ------------ | ---------------- |
| 控制层             | controller   | XxxController    |
| 服务层             | service      | XxxService       |
| 服务实现类         | service.impl | XxxServiceImpl   |
| 模块调用层         | client       | XxxClient        |
| 数据层             | dao          | XxxRepository    |
| 模型包装类         | model        | Xxx              |
| 实体层（如果需要） | entity       | Xxx              |
| 配置层（如果需要） | config       | XxxConfiguration |
| 异常处理层         | handler      | XxxHandler       |

### 3.2 业务流程

每人自行维护自己模块的 knife4j 接口文档。**GET 方法使用 @RequestParam 注解而不是 @RequestBody。**控制层函数一般直接接收模型包装类作为请求体参数，直接返回模型包装类作为响应体内容，方便前端预览接口文档。若所需请求参数较少允许使用 @RequestParam。请求体模型有需要可以自己定义，响应体模型统一使用 model 模块中的 Result&lt;D&gt;。

建议使用统一异常处理。异常及其类型定义于 model 模块中，如果有需要可以自行添加枚举变量。

控制类需要进行所有的合法性检查（参数、权限、存在性等），不能让底层方法判断操作合法性。下层不调用上层，不跨层调用，一般情况下同层尽量不调用自己（当然可以自行添加私有方法）。

参数格式检查推荐使用 hibernate-validator 进行统一快速判断，仅将 复杂/复合/与实际业务逻辑有关 的参数格式检查留到控制层，能够减少很大一部分工作量并增强可读性。详见：[Spring boot 结合hibernate-validate校验数据_Jankin丶Chen的博客-CSDN博客](https://blog.csdn.net/qq_45152095/article/details/120922171) 。使用时控制类上注解`@Validate`，参数前注解`@Valid`（几个用于字符串的检查似乎只有这样才能生效）。

完整的控制层和异常处理业务流程可以参考：

```java
// Controller
@RestController
@Api(tags = "演示控制类")
public class DemoController {

    @PostMapping("/test")
    @ApiOperation(value = "测试接口", notes = "返回传入的相同字符串")
    public Result<String> test(@RequestBody RequestModel<String> model) throws AcademicException {
        Result<String> result = new Result<>();
        // Param checks
        if (model == null)
            return result.withFailure(ExceptionType.ILLEGAL_FORMAT);
        String param = model.getData();
        if (param == null)
            return result.withFailure(ExceptionType.ILLEGAL_FORMAT);
        
        if (param.equals("Throw an exception")) {
            throw new AcademicException(ExceptionType.INTERNAL_SERVER_ERROR);
        }
        else if (param.equals("Return a failure")) {
            return result.withFailure(ExceptionType.INTERNAL_SERVER_ERROR);
        }
        return result.withData(param);
    }

}

// GlobalExceptionHandler
// 这一部分可以直接参考search模块的handler包
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AcademicException.class)
    @ResponseBody
    public Result<Void> handleCustomException(AcademicException exception) {
        return new Result<Void>().withFailure(exception.getMessage());
    }
    
    @ExceptionHandler({ HttpMessageConversionException.class, MethodArgumentNotValidException.class })
    @ResponseBody
    public Result<Void> handleJsonException(Exception exception) {
        return new Result<Void>().withFailure(ExceptionType.INVALID_PARAM);
    }
    
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result<Void> handleOtherException(Exception exception) {
        exception.printStackTrace();
        return new Result<Void>().withFailure(ExceptionType.INTERNAL_SERVER_ERROR);
    }

}
```

如果返回值没有 data 键，使用 Result&lt;Void&gt; 作为返回类型。

底层业务逻辑自行设计。

### 3.3 接口设计

新增接口时，**首先和前端开发人员对接商议确定业务流程**，随后在《接口声明》表格中添加对应接口的简要描述。编写控制类和模型类时必须使用对应的 knife4j 注解，不能只放着一个参数名字不加描述给前端看。请求方法（POST/GET）、内容类型（json/form-data/url）、参数数据类型（尤其是非字符串类型）必须事先和前端开发人员明确；**返回参数的存在性**（是否可能为 null，或在何条件下为 null）也务必明确。

接口开发完成后，其规格一般不再作修改（因为你修改了前端也得跟着改，可能造成前后端版本不一致）。如果确实要更改，应首先与前端开发人员商议，修改完成并部署后提醒前端人员。

对于一些请求或返回数据结构较复杂、数据量较大的接口，尤其是涉及界面中分级表单、图表展示等的接口，应与前端商议最佳的组织形式。很多前端组件的输入数据结构是定义好的，且后端进行针对性适配比前端转换数据结构容易得多。**千万不要想当然直接使用自己认为合适的结构。**

### 3.4 说明和注释

接口文档相关的说明注解使用中文，尽可能告诉前端这个接口是用来干什么的，以及每个参数的含义、类型、必要性、示例；尽可能告诉前端每个返回值的含义、类型、是否一定存在、示例。

注释一般用英文（书写方便）。

控制类方法中的各种合法性判断建议写单行注释标注（标注一下这部分在检查什么），不用写 JavaDoc。

工具类和业务逻辑比较复杂的服务类方法建议写一下 JavaDoc（除非保证你自己永远能懂且不需要给别人用）。数据层、实体层等一般不需要注释。

其他地方没有很严格的注释要求，只要保证代码风格基本都能很快看懂。

### 3.5 代码风格

~~这部分就不用多说了吧~~

运算符、等号前后加空格，逗号后加空格。同时建议根据 IDEA 的提示消除所有 warning（除非无法消除）。**尤其要注意 typo，最容易坑人坑己。**



## 4. Git 版本控制

一般情况下在 backend 分支完成所有开发工作（避免分支合并造成麻烦）。除 src、pom.xml、Dockerfile 和 .gitignore 以外所有文件和文件夹不得 push 到远程仓库，如果被自动 add 了，需要右键 Git - Rollback，然后 Git - Ignore - 选择添加到模块目录下的 .gitignore 文件。每个模块根目录都建一个 .gitignore，可以参考：

```gitignore
HELP.md
target/
!.mvn/wrapper/maven-wrapper.jar
!**/src/main/**/target/
!**/src/test/**/target/

### STS ###
.apt_generated
.classpath
.factorypath
.project
.settings
.springBeans
.sts4-cache

### IntelliJ IDEA ###
.idea
*.iws
*.iml
*.ipr

### NetBeans ###
/nbproject/private/
/nbbuild/
/dist/
/nbdist/
/.nb-gradle/
build/
!**/src/main/**/build/
!**/src/test/**/build/

### VS Code ###
.vscode/
/.mvn/
/mvnw
/mvnw.cmd
```

Commit 时填写一下这次提交干了什么，不要只写一两个词。程序没有测试过最好不要 push，否则需要告知一下其他人。共用模块或重要内容修改也需要说一声。

如果要开自己的分支，**千万注意分支的合并方向。**



## 5. 测试和部署

### 5.1 本地测试

**首先确保 application.yml 的第一行设置为 test。** Elasticsearch 数据库、Redis 缓存、Eureka 注册中心直接使用远端服务器，但一般会绕过 Gateway 网关入口进行测试（因为你本地测试的服务没有注册，网关不知道你在哪，所以只能直接给自己模块的端口号发请求）。稍微注意一下实际 URL 路径和 Header 参数（可能和有网关时不太一样）。

如果你的程序要在 Docker 以外的环境下以生产环境配置运行，你需要手动在命令行添加`--spring.profiles.active=prod`。默认情况下程序以开发测试环境配置运行。

**如果涉及重大试验项目，可能对远端数据库造成污染，可以在本地部署运行一整套架构进行测试。**

### 5.2 部署

**部署前请先 pull、完成自己的本地测试、push 到仓库。**

Dockerfile 放在模块根目录，注意最后指定了使用生产环境的配置：

```dockerfile
FROM openjdk:17
WORKDIR /app/
ADD target/*.jar app.jar
ENV TZ=PRC
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
EXPOSE <你的模块端口号>
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
```

以名为 search 的微服务为例，**严格按照以下命名规范**设置部署启动项（为了别人也能临时帮你部署）：

<img src="img/Infinite%20Academic%20-%20Development%20Specification/image-20211028173905576.png" alt="image-20211028173905576" style="zoom: 33%;" />

Server 和 Bind-mounts 根据后续服务器情况修改，暂时不用管。主要是镜像、容器名和网络模式选项。

