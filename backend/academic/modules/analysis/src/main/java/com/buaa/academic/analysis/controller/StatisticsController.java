package com.buaa.academic.analysis.controller;

import com.buaa.academic.analysis.model.Bucket;
import com.buaa.academic.analysis.model.HotWord;
import com.buaa.academic.analysis.model.TopRanks;
import com.buaa.academic.analysis.service.AnalysisShowService;
import com.buaa.academic.document.entity.Institution;
import com.buaa.academic.document.entity.Journal;
import com.buaa.academic.document.entity.Researcher;
import com.buaa.academic.document.statistic.NumPerYear;
import com.buaa.academic.model.exception.ExceptionType;
import com.buaa.academic.model.web.Result;
import com.buaa.academic.tool.validator.AllowValues;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.elasticsearch.index.query.WrapperQueryBuilder;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;
import java.util.List;
import java.util.Map;

@RestController
@Validated
@Api(tags = "数据统计和排名")
public class StatisticsController {

    @Autowired
    private AnalysisShowService analysisShowService;

    @GetMapping("/hotspots")
    @ApiOperation(value = "热点学科和话题", notes = "获取当前热度最高的若干个学科或话题")
    @ApiImplicitParam(name = "num", value = "\"热门Top N\"中的\"N\"，最大为20。")
    public Result<TopRanks<HotWord>> hotspots(@RequestParam("num") @Positive @Max(20) int num) {
        Result<TopRanks<HotWord>> result = new Result<>();
        TopRanks<HotWord> ranks = new TopRanks<>();
        ranks.setSubjects(analysisShowService.getHotWords("subjects", num));
        ranks.setTopics(analysisShowService.getHotWords("topics", num));
        return result.withData(ranks);
    }

    @GetMapping("/aggregations")
    @ApiOperation(
            value = "搜索结果聚合",
            notes = "展示搜索结果的聚合，例如年份统计、领域统计、作者统计等。</br>" +
                    "本接口依赖于搜索模块的缓存，因此不需要传递任何额外参数，但需先请求搜索模块的对应接口后再请求本接口。")
    public Result<Map<String, List<Bucket>>> searchAggregation(HttpServletRequest request) {
        Result<Map<String, List<Bucket>>> result = new Result<>();

        HttpSession session = request.getSession();
        String indexName = (String) session.getAttribute("index");
        String queryDSL = (String) session.getAttribute("query");
        String filterDSL = (String) session.getAttribute("filter");

        if (indexName == null || queryDSL == null)
            return result.withFailure("该搜索不支持结果过滤或尚未进行搜索");

        WrapperQueryBuilder wrapperQuery = new WrapperQueryBuilder(queryDSL);
        WrapperQueryBuilder wrapperFilter = filterDSL == null ? null : new WrapperQueryBuilder(filterDSL);
        Map<String, List<Bucket>> searchAggregations = analysisShowService.searchAggregate(indexName, wrapperQuery, wrapperFilter);

        return result.withData(searchAggregations);
    }

    @GetMapping("/statistics/{entity}/{id}")
    @ApiOperation(value = "每年发文统计", notes = "用于机构、学者、期刊的发文统计图")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entity", value = "要进行统计的实体的类别（researcher/journal/institution）"),
            @ApiImplicitParam(name = "id", value = "实体id", example = "GF_4ynwBF-Mu8unTG1hc")})
    public Result<NumPerYear> statistics(@PathVariable("entity") @AllowValues({"researcher", "journal", "institution"}) String entity,
                                         @PathVariable("id") @NotEmpty @Length(min = 20, max = 20) String id) {
        Result<NumPerYear> result = new Result<>();
        Class<?> target;
        switch (entity) {
            case "researcher" -> target = Researcher.class;
            case "journal" -> target = Journal.class;
            case "institution" -> target = Institution.class;
            default -> {
                return result.withFailure(ExceptionType.INVALID_PARAM);
            }
        }
        if (!analysisShowService.existsTarget(target, id))
            return result.withFailure(ExceptionType.NOT_FOUND);
        NumPerYear data = analysisShowService.getPublicationStatic(entity, id);
        return result.withData(data);
    }

}
