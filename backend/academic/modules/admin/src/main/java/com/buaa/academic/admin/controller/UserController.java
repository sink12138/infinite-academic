package com.buaa.academic.admin.controller;

import com.buaa.academic.admin.model.ListPage;
import com.buaa.academic.admin.service.AccountService;
import com.buaa.academic.admin.service.AuthValidator;
import com.buaa.academic.document.entity.User;
import com.buaa.academic.model.exception.ExceptionType;
import com.buaa.academic.model.web.Result;
import com.buaa.academic.tool.validator.AllowValues;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.hibernate.validator.constraints.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.*;
import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
@RequestMapping("/users")
@Validated
@Api(tags = "用户管理相关")
public class UserController {

    @Autowired
    private AuthValidator authValidator;

    @Autowired
    private ElasticsearchRestTemplate template;

    @Autowired
    private AccountService accountService;

    @GetMapping("/query")
    @ApiOperation(value = "查看和查询用户", notes = "根据给定的筛选条件查找用户，若不加条件则代表查看所有用户")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "页码，从0开始", required = true),
            @ApiImplicitParam(name = "size", value = "每页显示数量，最多30", required = true),
            @ApiImplicitParam(
                    name = "sort",
                    value = "排序方式，可用值：</br>" +
                            "<b>date-asc</b> - 注册时间升序</br>" +
                            "<b>date-desc</b> - 注册时间降序</br>" +
                            "<b>username-asc</b> - 用户名字典序升序</br>" +
                            "<b>username-desc</b> - 用户名字典序降序</br>" +
                            "若不传此字段，默认为注册时间降序。"),
            @ApiImplicitParam(name = "email", value = "邮箱"),
            @ApiImplicitParam(name = "username", value = "用户名"),
            @ApiImplicitParam(name = "scholar", value = "是否仅显示已认证学者", required = true)})
    public Result<ListPage<User>> list(@RequestHeader(name = "Auth") String auth,
                                       @RequestParam(name = "page") @PositiveOrZero int page,
                                       @RequestParam(name = "size") @Range(min = 1, max = 30) int size,
                                       @RequestParam(name = "sort", required = false, defaultValue = "date-desc")
                                           @AllowValues({"date-asc", "date-desc", "username-asc", "username-desc"}) String sort,
                                       @RequestParam(name = "email", required = false) @Email String email,
                                       @RequestParam(name = "username", required = false) String username,
                                       @RequestParam(name = "scholar") boolean scholar) {
        Result<ListPage<User>> result = new Result<>();
        if (!authValidator.headerCheck(auth))
            return result.withFailure(ExceptionType.UNAUTHORIZED);

        String[] split = sort.split("-");
        Sort pageSort;
        if (split[1].equals("asc")) {
            pageSort = Sort.by(Sort.Order.asc(split[0]));
        }
        else {
            pageSort = Sort.by(Sort.Order.desc(split[0]));
        }
        Pageable pageable = PageRequest.of(page, size, pageSort);
        BoolQueryBuilder filter = null;
        if (email != null || username != null || scholar) {
            filter = QueryBuilders.boolQuery();
        }
        if (email != null)
            filter.must(QueryBuilders.termQuery("email", email));
        if (username != null)
            filter.must(QueryBuilders.termQuery("username", username));
        if (scholar)
            filter.must(QueryBuilders.existsQuery("researcherId"));
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.matchAllQuery())
                .withPageable(pageable)
                .withTrackTotalHits(true);
        if (filter != null)
            queryBuilder.withFilter(filter);

        SearchHits<User> searchHits = template.search(queryBuilder.build(), User.class);
        ListPage<User> userPage = new ListPage<>();
        searchHits.forEach(hit -> userPage.add(hit.getContent()));
        userPage.setPageCount(Math.max(1, (int) (searchHits.getTotalHits()) + size - 1) / size);
        return result.withData(userPage);
    }

    @PostMapping("/save")
    @ApiOperation(value = "添加或修改用户")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "用户ID，若不传此字段或ID不存在代表新建，否则代表修改"),
            @ApiImplicitParam(name = "email", value = "邮箱", required = true),
            @ApiImplicitParam(name = "username", value = "用户名", required = true),
            @ApiImplicitParam(name = "password", value = "密码（SHA256Hex后），不传代表不修改密码")})
    public Result<Void> save(@RequestHeader(name = "Auth") String auth,
                             @RequestParam(name = "id", required = false) @Pattern(regexp = "^[0-9A-Za-z_-]{20}$") String id,
                             @RequestParam(name = "email") @Email String email,
                             @RequestParam(name = "username") @NotBlank String username,
                             @RequestParam(name = "password", required = false) String password) {
        Result<Void> result = new Result<>();
        if (!authValidator.headerCheck(auth))
            return result.withFailure(ExceptionType.UNAUTHORIZED);

        User user = null;
        if (id != null)
            user = template.get(id, User.class);
        if (user == null) {
            user = new User(email, username, password);
            user.setDate(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        }
        else {
            user.setEmail(email);
            user.setUsername(username);
            if (password != null)
                user.setPassword(password);
        }
        template.save(user);
        return result;
    }

    @PostMapping("/remove")
    @ApiOperation(value = "移除用户")
    public Result<Void> remove(@RequestHeader(name = "Auth") String auth,
                               @RequestParam(name = "id") @Pattern(regexp = "^[0-9A-Za-z_-]{20}$") String id) {
        Result<Void> result = new Result<>();
        if (!authValidator.headerCheck(auth))
            return result.withFailure(ExceptionType.UNAUTHORIZED);
        accountService.removeUser(id);
        return result;
    }

}
