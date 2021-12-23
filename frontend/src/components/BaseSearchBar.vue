<template>
  <div>
    <v-container
      class="d-flex"
      width="66%"
      v-if="
        !high ||
        filter == '全部' ||
        filter == '期刊' ||
        filter == '机构' ||
        filter == '精确'
      "
      :key="1"
    >
      <v-select
        class="select"
        v-model="filter"
        :items="items"
        :menu-props="{ maxHeight: 360 }"
        hide-details
        prepend-inner-icon="mdi-dots-grid"
        background-color="grey lighten-2"
        filled
        @change="emitFilter"
      ></v-select>
      <v-combobox
        class="input"
        outlined
        hide-spin-buttons="true"
        label="搜索"
        :value="text"
        :items="suggest"
        :search-input.sync="text"
        @update:search-input="getSuggest"
        append-icon="mdi-magnify"
        @click:append="search"
        @keyup.enter="search"
      >
      </v-combobox>
      <el-button
        icon="el-icon-search"
        round
        @click="searchInResult"
        style="max-height: 56px"
        class="btn"
        v-show="!this.normal && this.filter != '全部' && this.filter != '精确'"
      >
        在结果中检索
      </el-button>
    </v-container>
    <v-container width="66%" v-else :key="2">
      <tr class="d-flex">
        <v-select
          v-model="filter"
          :items="items"
          :menu-props="{ maxHeight: 360 }"
          hide-details
          prepend-inner-icon="mdi-dots-grid"
          background-color="grey lighten-2"
          filled
          @change="emitFilter"
        ></v-select>
        <el-button icon="el-icon-search" round @click="searchInResult">
          在结果中检索
        </el-button>
      </tr>
      <div v-if="filter == '论文'">
        <tr
          v-for="(item, index) in paperSearch"
          :key="index"
          class="highSearch d-flex"
        >
          <v-select
            v-model="item.scope"
            :items="paperT"
            :menu-props="{ maxHeight: 360 }"
            hide-details
            prepend-inner-icon="mdi-dots-grid"
            background-color="grey lighten-2"
            filled
          ></v-select>
          <v-select
            v-model="item.logic"
            :items="logics"
            :menu-props="{ maxHeight: 360 }"
            hide-details
            prepend-inner-icon="mdi-dots-grid"
            background-color="grey lighten-2"
            filled
          ></v-select>
          <v-combobox
            class="input"
            outlined
            hide-spin-buttons="true"
            label="搜索"
            :value="item.text"
            :items="item.suggest"
            :search-input.sync="item.text"
            @update:search-input="getSuggest1(item.text, index)"
            append-icon="mdi-magnify"
            @click:append="search1"
            @keyup.enter="search1"
          >
          </v-combobox>
          <el-button
            class="btn"
            style="max-height: 56px"
            @click="rm('paper', index)"
            >-</el-button
          >
          <el-button class="btn" style="max-height: 56px" @click="add('paper')"
            >+</el-button
          >
        </tr>
      </div>
      <div v-else-if="filter == '专利'">
        <tr
          v-for="(item, index) in patentSearch"
          :key="index"
          class="highSearch d-flex"
        >
          <v-select
            v-model="item.scope"
            :items="patentT"
            :menu-props="{ maxHeight: 360 }"
            hide-details
            prepend-inner-icon="mdi-dots-grid"
            background-color="grey lighten-2"
            filled
          ></v-select>
          <v-select
            v-model="item.logic"
            :items="logics"
            :menu-props="{ maxHeight: 360 }"
            hide-details
            prepend-inner-icon="mdi-dots-grid"
            background-color="grey lighten-2"
            filled
          ></v-select>
          <v-combobox
            class="input"
            outlined
            hide-spin-buttons="true"
            label="搜索"
            :value="item.text"
            :items="item.suggest"
            :search-input.sync="item.text"
            @update:search-input="getSuggest1(item.text, index)"
            append-icon="mdi-magnify"
            @click:append="search1"
            @keyup.enter="search1"
          >
          </v-combobox>
          <el-button
            class="btn"
            style="max-height: 56px"
            @click="rm('patent', index)"
            >-</el-button
          >
          <el-button class="btn" style="max-height: 56px" @click="add('patent')"
            >+</el-button
          >
        </tr>
      </div>
      <div v-else-if="filter == '科研人员'">
        <tr
          v-for="(item, index) in researcherSearch"
          :key="index"
          class="highSearch d-flex"
        >
          <v-select
            v-model="item.scope"
            :items="researcherT"
            :menu-props="{ maxHeight: 360 }"
            hide-details
            prepend-inner-icon="mdi-dots-grid"
            background-color="grey lighten-2"
            filled
          ></v-select>
          <v-select
            v-model="item.logic"
            :items="logics"
            :menu-props="{ maxHeight: 360 }"
            hide-details
            prepend-inner-icon="mdi-dots-grid"
            background-color="grey lighten-2"
            filled
          ></v-select>
          <v-combobox
            class="input"
            outlined
            hide-spin-buttons="true"
            label="搜索"
            :value="item.text"
            :items="item.suggest"
            :search-input.sync="item.text"
            @update:search-input="getSuggest1(item.text, index)"
            append-icon="mdi-magnify"
            @click:append="search1"
            @keyup.enter="search1"
          >
          </v-combobox>
          <el-button
            class="btn"
            style="max-height: 56px"
            @click="rm('researcher', index)"
            >-</el-button
          >
          <el-button
            class="btn"
            style="max-height: 56px"
            @click="add('researcher')"
            >+</el-button
          >
        </tr>
      </div>
      <div v-else>
        <v-combobox
          v-if="!high"
          class="input"
          outlined
          hide-spin-buttons="true"
          label="搜索"
          :value="text"
          :items="suggest"
          :search-input.sync="text"
          @update:search-input="getSuggest"
          append-icon="mdi-magnify"
          @click:append="search"
          @keyup.enter="search"
        >
        </v-combobox>
      </div>
    </v-container>
  </div>
</template>

<script>
//import qs from 'qs'
export default {
  data() {
    return {
      normal: true,
      items: ["全部", "论文", "期刊", "专利", "机构", "科研人员", "精确"],
      logics: ["and", "or", "not"],
      filter: "全部",
      text: "",
      suggest: [],
      filters: {},
      data: {},
      request: {},
      page: 0,
      aggregations: {},
      paperT: ["篇关摘", "作者", "发表机构", "刊登期刊"],
      patentT: ["标题摘要", "类型", "申请人", "发明人"],
      researcherT: ["姓名", "研究方向", "合作机构", "所属机构"],
      paperSearch: [{ scope: "篇关摘", text: "", suggest: [], logic: "and" }],
      patentSearch: [
        { scope: "标题摘要", text: "", suggest: [], logic: "and" },
      ],
      researcherSearch: [
        { scope: "姓名", text: "", suggest: [], logic: "and" },
      ],
      high: false,
    };
  },
  created() {
    setTimeout(() => {
      if (this.$route.path != "/") this.normal = false;
    }, 1);
  },
  methods: {
    add(type) {
      switch (type) {
        case "paper":
          if (this.paperSearch.length < 10)
            this.paperSearch.push({
              scope: "篇关摘",
              text: "",
              suggest: [],
              logic: "and",
            });
          break;
        case "patent":
          if (this.patentSearch.length < 10)
            this.patentSearch.push({
              scope: "标题摘要",
              text: "",
              suggest: [],
              logic: "and",
            });
          break;
        case "researcher":
          if (this.researcherSearch.length < 10)
            this.researcherSearch.push({
              scope: "姓名",
              text: "",
              suggest: [],
              logic: "and",
            });
          break;
      }
    },
    rm(type, index) {
      switch (type) {
        case "paper":
          if (this.paperSearch.length > 1) this.paperSearch.splice(index, 1);
          break;
        case "patent":
          if (this.patentSearch.length > 1) this.patentSearch.splice(index, 1);
          break;
        case "researcher":
          if (this.researcherSearch.length > 1)
            this.researcherSearch.splice(index, 1);
          break;
      }
    },
    emitFilter() {
      this.$emit("filterChange", this.filter);
    },
    getSuggest(val) {
      var url = "";
      switch (this.filter) {
        case "全部":
        case "论文":
          url = "/api/search/suggest/paper";
          break;
        case "期刊": {
          url = "/api/search/suggest/paper";
          break;
        }
        case "专利": {
          url = "/api/search/suggest/paper";
          break;
        }
        case "机构": {
          url = "/api/search/suggest/paper";
          break;
        }
        default:
          break;
      }
      if (url != "" && val != "" && val != null) {
        this.$axios({
          method: "post",
          url: url,
          params: {
            text: val,
          },
        })
          .then((response) => {
            this.suggest = [];
            console.log(response);
            if (response.data.success == true) {
              console.log(response.data.data);
              if (response.data.data.correction.length != 0)
                this.suggest = this.suggest.concat(
                  response.data.data.correction
                );
              if (response.data.data.completion.length != 0)
                this.suggest = this.suggest.concat(
                  response.data.data.completion
                );
              for (var i = 0; i < this.suggest.length; i++) {
                this.suggest[i] = this.suggest[i]
                  .replaceAll("<b>", "")
                  .replaceAll("</b>", "");
              }
            } else {
              this.suggest = [];
            }
          })
          .catch((error) => {
            console.log(error);
          });
      }
    },
    getSuggest1(val, index) {
      var url = "";
      var i;
      switch (this.filter) {
        case "论文":
          url = "/api/search/suggest/paper";
          break;
        case "专利": {
          url = "/api/search/suggest/paper";
          break;
        }
        default:
          break;
      }
      if (url != "" && val != "" && val != null) {
        this.$axios({
          method: "post",
          url: url,
          params: {
            text: val,
          },
        })
          .then((response) => {
            switch (this.filter) {
              case "论文":
                this.paperSearch[index].suggest = [];
                console.log(response);
                if (response.data.success == true) {
                  console.log(response.data.data);
                  if (response.data.data.correction.length != 0)
                    this.paperSearch[index].suggest = this.paperSearch[
                      index
                    ].suggest.concat(response.data.data.correction);
                  if (response.data.data.completion.length != 0)
                    this.paperSearch[index].suggest = this.paperSearch[
                      index
                    ].suggest.concat(response.data.data.completion);
                  for (i = 0; i < this.paperSearch[index].suggest.length; i++) {
                    this.paperSearch[index].suggest[i] = this.paperSearch[
                      index
                    ].suggest[i]
                      .replaceAll("<b>", "")
                      .replaceAll("</b>", "");
                  }
                } else {
                  this.paperSearch[index].suggest = [];
                }
                break;
              case "科研人员":
                this.researcherSearch[index].suggest = [];
                console.log(response);
                if (response.data.success == true) {
                  console.log(response.data.data);
                  if (response.data.data.correction.length != 0)
                    this.researcherSearch[index].suggest =
                      this.researcherSearch[index].suggest.concat(
                        response.data.data.correction
                      );
                  if (response.data.data.completion.length != 0)
                    this.researcherSearch[index].suggest =
                      this.researcherSearch[index].suggest.concat(
                        response.data.data.completion
                      );
                  for (
                    i = 0;
                    i < this.researcherSearch[index].suggest.length;
                    i++
                  ) {
                    this.researcherSearch[index].suggest[i] =
                      this.researcherSearch[index].suggest[i]
                        .replaceAll("<b>", "")
                        .replaceAll("</b>", "");
                  }
                } else {
                  this.researcherSearch[index].suggest = [];
                }
                break;
            }
          })
          .catch((error) => {
            console.log(error);
          });
      }
    },
    search() {
      if (this.$route.path != "/") {
        if (this.text == null || this.text == "") return;
        this.filters = this.$parent.filters;
        console.log("搜索过滤器");
        console.log(this.filters);
        var i;
        var url = "/api/search";
        var keyword = this.text.trim();
        var keywords = keyword.split(/\s+/);
        var condition;
        var condition1;
        var fuzzy = true;
        var translated = true;
        console.log(keywords);
        this.request = {
          conditions: [
            {
              compound: false,
              fuzzy: this.filters.fuzzy,
              keyword: "",
              languages: ["zh", "en"],
              logic: "and",
              translated: true,
            },
          ],
          filters: [],
          page: 0,
          size: 10,
        };
        switch (this.filter) {
          case "全部": {
            url = url + "/smart";
            this.request = {
              filters: [],
              keyword: keyword,
              page: this.page,
              size: 10,
              translated: true,
            };
            if (this.filters.year1 != 1900 || this.filters.year2 != 2021) {
              this.request.filters.push({
                attr: "year",
                format: "numeric",
                intParams: [this.filters.year1, this.filters.year2],
                type: "range",
              });
            }
            if (
              this.filters.citationNum != null &&
              this.filters.citationNum != ""
            ) {
              this.request.filters.push({
                attr: "citationNum",
                format: "numeric",
                intParams: [Number(this.filters.citationNum)],
                type: "above",
              });
            }
            if (this.filters.paperSort == "出版日期倒序") {
              this.request.sort = "date.desc";
            } else if (this.filters.paperSort == "出版日期正序") {
              this.request.sort = "date.asc";
            } else if (this.filters.paperSort == "引用数量") {
              this.request.sort = "citationNum.desc";
            }
            break;
          }
          case "论文": {
            url = url + "/paper";
            var scope = ["title", "abstract", "keywords", "subjects"];
            this.request.conditions[0].scope = scope;
            this.request.conditions[0].keyword = keywords[0];
            if (keywords.length != 1) {
              condition = this.request.conditions[0];
              for (i = 1; i < keywords.length; i++) {
                condition1 = {
                  compound: true,
                  fuzzy: this.filters.fuzzy,
                  keyword: keywords[i],
                  languages: ["zh", "en"],
                  logic: "and",
                  scope: scope,
                  subConditions: [condition],
                  translated: this.filters.translated,
                };
                condition = condition1;
              }
              this.request.conditions[0] = condition;
            }
            if (this.filters.paperType.type != "") {
              keywords = this.filters.paperType.type.trim().split(/\s+/);
              console.log(keywords);
              condition = {
                compound: false,
                fuzzy: false,
                keyword: keywords[0],
                logic: "and",
                scope: ["type"],
                translated: false,
              };
              if (keywords.length != 1) {
                for (i = 1; i < keywords.length; i++) {
                  condition1 = {
                    compound: true,
                    fuzzy: false,
                    keyword: keywords[i],
                    logic: "and",
                    scope: ["type"],
                    subConditions: [condition],
                    translated: false,
                  };
                  condition = condition1;
                }
              }
              this.request.conditions.push(condition);
            }
            if (this.filters.year1 != 1900 || this.filters.year2 != 2021) {
              this.request.filters.push({
                attr: "year",
                format: "numeric",
                intParams: [this.filters.year1, this.filters.year2],
                type: "range",
              });
            }
            if (
              this.filters.citationNum != null &&
              this.filters.citationNum != ""
            ) {
              this.request.filters.push({
                attr: "citationNum",
                format: "numeric",
                intParams: [Number(this.filters.citationNum)],
                type: "above",
              });
            }
            if (this.filters.paperSort == "出版日期倒序") {
              this.request.sort = "date.desc";
            } else if (this.filters.paperSort == "出版日期正序") {
              this.request.sort = "date.asc";
            } else if (this.filters.paperSort == "引用数量") {
              this.request.sort = "citationNum.desc";
            }
            break;
          }
          case "期刊": {
            url = url + "/journal";
            this.request.conditions[0].scope = ["title"];
            this.request.conditions[0].keyword = keywords[0];
            if (keywords.length != 1) {
              condition = this.request.conditions[0];
              for (i = 1; i < keywords.length; i++) {
                condition1 = {
                  compound: true,
                  fuzzy: true,
                  keyword: keywords[i],
                  languages: ["zh", "en"],
                  logic: "and",
                  scope: ["title"],
                  subConditions: [condition],
                  translated: this.filters.translated,
                };
                condition = condition1;
              }
              this.request.conditions[0] = condition;
            }
            break;
          }
          case "机构": {
            url = url + "/institution";
            this.request.size = 20;
            this.request.conditions[0].scope = ["name"];
            this.request.conditions[0].keyword = keywords[0];
            if (keywords.length != 1) {
              condition = this.request.conditions[0];
              for (i = 1; i < keywords.length; i++) {
                condition1 = {
                  compound: true,
                  fuzzy: true,
                  keyword: keywords[i],
                  languages: ["zh", "en"],
                  logic: "and",
                  scope: ["name"],
                  subConditions: [condition],
                  translated: this.filters.translated,
                };
                condition = condition1;
              }
              this.request.conditions[0] = condition;
            }
            break;
          }
          case "专利": {
            url = url + "/patent";
            scope = ["title", "abstract"];
            this.request.conditions[0].scope = scope;
            this.request.conditions[0].keyword = keywords[0];
            if (keywords.length != 1) {
              condition = this.request.conditions[0];
              for (i = 1; i < keywords.length; i++) {
                condition1 = {
                  compound: true,
                  fuzzy: this.filters.fuzzy,
                  keyword: keywords[i],
                  languages: ["zh", "en"],
                  logic: "and",
                  scope: scope,
                  subConditions: [condition],
                  translated: this.filters.translated,
                };
                condition = condition1;
              }
              this.request.conditions[0] = condition;
            }
            if (this.filters.patentSort == "申请日期倒序") {
              this.request.sort = "fillingDate.desc";
            } else if (this.filters.patentSort == "申请日期正序") {
              this.request.sort = "fillingDate.asc";
            }
            break;
          }
          case "科研人员": {
            url = url + "/researcher";
            scope = ["name"];
            fuzzy = false;
            translated = false;
            this.request.conditions[0].scope = scope;
            this.request.conditions[0].fuzzy = fuzzy;
            this.request.conditions[0].translated = translated;
            this.request.conditions[0].keyword = keywords[0];
            if (keywords.length != 1) {
              condition = this.request.conditions[0];
              for (i = 1; i < keywords.length; i++) {
                condition1 = {
                  compound: true,
                  fuzzy: false,
                  keyword: keywords[i],
                  languages: ["zh", "en"],
                  logic: "and",
                  scope: scope,
                  subConditions: [condition],
                  translated: false,
                };
                condition = condition1;
              }
              this.request.conditions[0] = condition;
            }
            if (
              this.filters.citationNum != null &&
              this.filters.citationNum != ""
            ) {
              this.request.filters.push({
                attr: "citationNum",
                format: "numeric",
                intParams: [Number(this.filters.citationNum)],
                type: "above",
              });
            }
            if (this.filters.paperNum != null && this.filters.paperNum != "") {
              this.request.filters.push({
                attr: "paperNum",
                format: "numeric",
                intParams: [Number(this.filters.paperNum)],
                type: "above",
              });
            }
            if (
              this.filters.patentNum != null &&
              this.filters.patentNum != ""
            ) {
              this.request.filters.push({
                attr: "patentNum",
                format: "numeric",
                intParams: [Number(this.filters.patentNum)],
                type: "above",
              });
            }
            if (this.filters.hIndex != null && this.filters.hIndex != "") {
              this.request.filters.push({
                attr: "hIndex",
                format: "numeric",
                intParams: [Number(this.filters.hIndex)],
                type: "above",
              });
            }
            if (this.filters.gIndex != null && this.filters.gIndex != "") {
              this.request.filters.push({
                attr: "gIndex",
                format: "numeric",
                intParams: [Number(this.filters.gIndex)],
                type: "above",
              });
            }
            if (this.filters.researcherSort == "论文数量") {
              this.request.sort = "paperNum.desc";
            } else if (this.filters.researcherSort == "专利数量") {
              this.request.sort = "patentNum.desc";
            } else if (this.filters.researcherSort == "引用数量") {
              this.request.sort = "citationNum.desc";
            } else if (this.filters.researcherSort == "h指数") {
              this.request.sort = "hIndex.desc";
            } else if (this.filters.researcherSort == "g指数") {
              this.request.sort = "gIndex.desc";
            }
            break;
          }
          case "精确": {
            url = url + "/query";
            break;
          }
        }
        console.log(url);
        if (this.filter != "精确") {
          console.log(JSON.stringify(this.request));
          this.$axios({
            method: "post",
            url: url,
            data: this.request,
          })
            .then((response) => {
              console.log(response.data);
              this.data = response.data;
              this.$emit("searchResult", this.data.data);
              this.$emit("searchType", this.filter);
              if (
                this.filter == "全部" ||
                this.filter == "论文" ||
                this.filter == "科研人员" ||
                this.filter == "专利"
              ) {
                this.$axios({
                  method: "get",
                  url: "/api/analysis/aggregations",
                })
                  .then((response) => {
                    this.aggregations = response.data;
                    this.$emit("searchFilter", this.aggregations);
                    console.log(response.data);
                  })
                  .catch((error) => {
                    console.log(error);
                  });
              }
            })
            .catch((error) => {
              console.log(error);
            });
        } else {
          this.$axios({
            method: "post",
            url: "/api/search/query/",
            params: {
              key: this.text,
              type: this.filters.queryType,
            },
          })
            .then((response) => {
              console.log(response.data);
              this.data = response.data;
              if (this.data.success == false) {
                this.$notify({
                  title: "搜索失败",
                  message: this.data.message,
                  type: "warning",
                });
              } else {
                this.$router.push({ path: this.data.data });
              }
            })
            .catch((error) => {
              console.log(error);
            });
        }
      } else {
        this.$router.push({
          path: "/search",
          query: { text: this.text, filter: this.filter },
        });
      }
    },
    search1() {
      this.filters = this.$parent.filters;
      console.log("搜索过滤器");
      console.log(this.filters);
      var i, j;
      var url = "/api/search";
      var keywords;
      var scope;
      var condition;
      var condition1;
      var fuzzy = true;
      var translated = true;
      console.log(keywords);
      this.request = {
        conditions: [],
        filters: [],
        page: 0,
        size: 10,
      };
      switch (this.filter) {
        case "论文": {
          url = url + "/paper";
          for (i = 0; i < this.paperSearch.length; i++) {
            if (
              this.paperSearch[i].text == null ||
              this.paperSearch[i].text.match(/\s+/)
            )
              continue;
            switch (this.paperSearch[i].scope) {
              case "篇关摘":
                scope = ["title", "abstract", "keywords", "subjects"];
                fuzzy = this.filters.fuzzy;
                translated = this.filters.translated;
                break;
              case "作者":
                scope = ["authors.name"];
                fuzzy = false;
                translated = false;
                break;
              case "发表机构":
                scope = ["institutions.name"];
                fuzzy = this.filters.fuzzy;
                translated = this.filters.translated;
                break;
              case "刊登期刊":
                scope = ["journal.title"];
                fuzzy = this.filters.fuzzy;
                translated = this.filters.translated;
                break;
            }
            keywords = this.paperSearch[i].text.trim().split(/\s+/);
            console.log(keywords);
            condition = {
              compound: false,
              fuzzy: fuzzy,
              keyword: keywords[0],
              logic: this.paperSearch[i].logic,
              scope: scope,
              languages: ["zh", "en"],
              translated: translated,
            };
            if (keywords.length != 1) {
              for (j = 1; j < keywords.length; j++) {
                condition1 = {
                  compound: true,
                  fuzzy: fuzzy,
                  keyword: keywords[j],
                  logic: this.paperSearch[i].logic,
                  scope: scope,
                  subConditions: [condition],
                  languages: ["zh", "en"],
                  translated: translated,
                };
                condition = condition1;
              }
            }
            this.request.conditions.push(condition);
          }
          if (this.filters.paperType.type != "") {
            keywords = this.filters.paperType.type.trim().split(/\s+/);
            console.log(keywords);
            condition = {
              compound: false,
              fuzzy: false,
              keyword: keywords[0],
              logic: "and",
              scope: ["type"],
              translated: false,
            };
            if (keywords.length != 1) {
              for (i = 1; i < keywords.length; i++) {
                condition1 = {
                  compound: true,
                  fuzzy: false,
                  keyword: keywords[i],
                  logic: "and",
                  scope: ["type"],
                  subConditions: [condition],
                  translated: false,
                };
                condition = condition1;
              }
            }
            this.request.conditions.push(condition);
          }
          if (this.filters.year1 != 1900 || this.filters.year2 != 2021) {
            this.request.filters.push({
              attr: "year",
              format: "numeric",
              intParams: [this.filters.year1, this.filters.year2],
              type: "range",
            });
          }
          if (
            this.filters.citationNum != null &&
            this.filters.citationNum != ""
          ) {
            this.request.filters.push({
              attr: "citationNum",
              format: "numeric",
              intParams: [Number(this.filters.citationNum)],
              type: "above",
            });
          }
          if (this.filters.paperSort == "出版日期倒序") {
            this.request.sort = "date.desc";
          } else if (this.filters.paperSort == "出版日期正序") {
            this.request.sort = "date.asc";
          } else if (this.filters.paperSort == "引用数量") {
            this.request.sort = "citationNum.desc";
          }
          break;
        }
        case "专利": {
          url = url + "/patent";
          for (i = 0; i < this.patentSearch.length; i++) {
            if (
              this.patentSearch[i].text == null ||
              this.patentSearch[i].text.match(/\s+/)
            )
              continue;
            switch (this.patentSearch[i].scope) {
              case "标题摘要":
                scope = ["title", "abstract"];
                fuzzy = this.filters.fuzzy;
                translated = this.filters.translated;
                break;
              case "类型":
                scope = ["type"];
                fuzzy = false;
                translated = false;
                break;
              case "申请人":
                scope = ["applicant"];
                fuzzy = this.filters.fuzzy;
                translated = this.filters.translated;
                break;
              case "发明人":
                scope = ["inventors.name"];
                fuzzy = false;
                translated = false;
                break;
            }
            keywords = this.patentSearch[i].text.trim().split(/\s+/);
            console.log(keywords);
            condition = {
              compound: false,
              fuzzy: fuzzy,
              keyword: keywords[0],
              logic: this.patentSearch[i].logic,
              scope: scope,
              languages: ["zh", "en"],
              translated: translated,
            };
            if (keywords.length != 1) {
              for (j = 1; j < keywords.length; j++) {
                condition1 = {
                  compound: true,
                  fuzzy: fuzzy,
                  keyword: keywords[j],
                  logic: this.patentSearch[i].logic,
                  scope: scope,
                  subConditions: [condition],
                  languages: ["zh", "en"],
                  translated: translated,
                };
                condition = condition1;
              }
            }
            this.request.conditions.push(condition);
          }
          if (this.filters.patentSort == "申请日期倒序") {
            this.request.sort = "fillingDate.desc";
          } else if (this.filters.patentSort == "申请日期正序") {
            this.request.sort = "fillingDate.asc";
          }
          break;
        }
        case "科研人员": {
          url = url + "/researcher";
          for (i = 0; i < this.researcherSearch.length; i++) {
            if (
              this.researcherSearch[i].text == null ||
              this.researcherSearch[i].text.match(/\s+/)
            )
              continue;
            switch (this.researcherSearch[i].scope) {
              case "姓名":
                scope = ["name"];
                fuzzy = false;
                translated = false;
                break;
              case "研究方向":
                scope = ["interests"];
                fuzzy = this.filters.fuzzy;
                translated = this.filters.translated;
                break;
              case "合作机构":
                scope = ["institutions.name"];
                fuzzy = this.filters.fuzzy;
                translated = this.filters.translated;
                break;
              case "所属机构":
                scope = ["currentInst.name"];
                fuzzy = this.filters.fuzzy;
                translated = this.filters.translated;
                break;
            }
            keywords = this.researcherSearch[i].text.trim().split(/\s+/);
            console.log(keywords);
            condition = {
              compound: false,
              fuzzy: fuzzy,
              keyword: keywords[0],
              logic: this.researcherSearch[i].logic,
              scope: scope,
              languages: ["zh", "en"],
              translated: translated,
            };
            if (keywords.length != 1) {
              for (j = 1; j < keywords.length; j++) {
                condition1 = {
                  compound: true,
                  fuzzy: fuzzy,
                  keyword: keywords[j],
                  logic: this.researcherSearch[i].logic,
                  scope: scope,
                  subConditions: [condition],
                  languages: ["zh", "en"],
                  translated: translated,
                };
                condition = condition1;
              }
            }
            this.request.conditions.push(condition);
          }
          if (
            this.filters.citationNum != null &&
            this.filters.citationNum != ""
          ) {
            this.request.filters.push({
              attr: "citationNum",
              format: "numeric",
              intParams: [Number(this.filters.citationNum)],
              type: "above",
            });
          }
          if (this.filters.paperNum != null && this.filters.paperNum != "") {
            this.request.filters.push({
              attr: "paperNum",
              format: "numeric",
              intParams: [Number(this.filters.paperNum)],
              type: "above",
            });
          }
          if (this.filters.patentNum != null && this.filters.patentNum != "") {
            this.request.filters.push({
              attr: "patentNum",
              format: "numeric",
              intParams: [Number(this.filters.patentNum)],
              type: "above",
            });
          }
          if (this.filters.hIndex != null && this.filters.hIndex != "") {
            this.request.filters.push({
              attr: "hIndex",
              format: "numeric",
              intParams: [Number(this.filters.hIndex)],
              type: "above",
            });
          }
          if (this.filters.gIndex != null && this.filters.gIndex != "") {
            this.request.filters.push({
              attr: "gIndex",
              format: "numeric",
              intParams: [Number(this.filters.gIndex)],
              type: "above",
            });
          }
          if (this.filters.researcherSort == "论文数量") {
            this.request.sort = "paperNum.desc";
          } else if (this.filters.researcherSort == "专利数量") {
            this.request.sort = "patentNum.desc";
          } else if (this.filters.researcherSort == "引用数量") {
            this.request.sort = "citationNum.desc";
          } else if (this.filters.researcherSort == "h指数") {
            this.request.sort = "hIndex.desc";
          } else if (this.filters.researcherSort == "g指数") {
            this.request.sort = "gIndex.desc";
          }
          break;
        }
      }
      console.log(url);
      console.log(JSON.stringify(this.request));
      if (this.request.conditions.length == 0) return;
      this.$axios({
        method: "post",
        url: url,
        data: this.request,
      })
        .then((response) => {
          console.log(response.data);
          this.data = response.data;
          this.$emit("searchResult", this.data.data);
          this.$emit("searchType", this.filter);
          if (
            this.filter == "全部" ||
            this.filter == "论文" ||
            this.filter == "科研人员" ||
            this.filter == "专利"
          ) {
            this.$axios({
              method: "get",
              url: "/api/analysis/aggregations",
            })
              .then((response) => {
                this.aggregations = response.data;
                this.$emit("searchFilter", this.aggregations);
                console.log(response.data);
              })
              .catch((error) => {
                console.log(error);
              });
          }
        })
        .catch((error) => {
          console.log(error);
        });
    },
    searchFilters() {
      if (this.$route.path != "/") {
        this.filters = this.$parent.filters;
        console.log("搜索过滤器");
        console.log(this.filters);
        var url = "/api/search";
        var keyword;
        var keywords;
        if (this.text != null && this.text != "") {
          keyword = this.text.trim();
          keywords = keyword.split(/\s+/);
        }
        console.log(keywords);
        this.request.filters = [];
        switch (this.filter) {
          case "全部": {
            url = url + "/smart";
            this.request = {
              filters: [],
              keyword: keyword,
              page: this.page,
              size: 10,
              translated: true,
            };
            this.request.filters.push({
              attr: "year",
              format: "numeric",
              intParams: [this.filters.year1, this.filters.year2],
              type: "range",
            });
            if (
              this.filters.citationNum != null &&
              this.filters.citationNum != ""
            ) {
              this.request.filters.push({
                attr: "citationNum",
                format: "numeric",
                intParams: [Number(this.filters.citationNum)],
                type: "above",
              });
            }
            if (this.filters.subjects_selected.length != 0) {
              this.request.filters.push({
                attr: "subjects",
                format: "discrete",
                keyParams: this.filters.subjects_selected,
                type: "equal",
              });
            }
            if (this.filters.types_selected.length != 0) {
              this.request.filters.push({
                attr: "type",
                format: "discrete",
                keyParams: this.filters.types_selected,
                type: "equal",
              });
            }
            if (this.filters.authors_selected.length != 0) {
              this.request.filters.push({
                attr: "authors.name",
                format: "discrete",
                keyParams: this.filters.authors_selected,
                type: "equal",
              });
            }
            if (this.filters.institutions_selected.length != 0) {
              this.request.filters.push({
                attr: "institutions.name",
                format: "discrete",
                keyParams: this.filters.institutions_selected,
                type: "equal",
              });
            }
            if (this.filters.journals_selected.length != 0) {
              this.request.filters.push({
                attr: "journal.title",
                format: "discrete",
                keyParams: this.filters.journals_selected,
                type: "equal",
              });
            }
            if (this.filters.paperSort == "出版日期倒序") {
              this.request.sort = "date.desc";
            } else if (this.filters.paperSort == "出版日期正序") {
              this.request.sort = "date.asc";
            } else if (this.filters.paperSort == "引用数量") {
              this.request.sort = "citationNum.desc";
            }
            break;
          }
          case "论文": {
            url = url + "/paper";
            if (this.filters.year1 != 1900 || this.filters.year2 != 2021) {
              this.request.filters.push({
                attr: "year",
                format: "numeric",
                intParams: [this.filters.year1, this.filters.year2],
                type: "range",
              });
            }
            if (
              this.filters.citationNum != null &&
              this.filters.citationNum != ""
            ) {
              this.request.filters.push({
                attr: "citationNum",
                format: "numeric",
                intParams: [Number(this.filters.citationNum)],
                type: "above",
              });
            }
            if (this.filters.subjects_selected.length != 0) {
              this.request.filters.push({
                attr: "subjects",
                format: "discrete",
                keyParams: this.filters.subjects_selected,
                type: "equal",
              });
            }
            if (this.filters.types_selected.length != 0) {
              this.request.filters.push({
                attr: "type",
                format: "discrete",
                keyParams: this.filters.types_selected,
                type: "equal",
              });
            }
            if (this.filters.authors_selected.length != 0) {
              this.request.filters.push({
                attr: "authors.name",
                format: "discrete",
                keyParams: this.filters.authors_selected,
                type: "equal",
              });
            }
            if (this.filters.institutions_selected.length != 0) {
              this.request.filters.push({
                attr: "institutions.name",
                format: "discrete",
                keyParams: this.filters.institutions_selected,
                type: "equal",
              });
            }
            if (this.filters.journals_selected.length != 0) {
              this.request.filters.push({
                attr: "journal.title",
                format: "discrete",
                keyParams: this.filters.journals_selected,
                type: "equal",
              });
            }
            if (this.filters.paperSort == "出版日期倒序") {
              this.request.sort = "date.desc";
            } else if (this.filters.paperSort == "出版日期正序") {
              this.request.sort = "date.asc";
            } else if (this.filters.paperSort == "引用数量") {
              this.request.sort = "citationNum.desc";
            }
            break;
          }
          case "专利": {
            url = url + "/patent";
            if (this.filters.types_selected.length != 0) {
              this.request.filters.push({
                attr: "type",
                format: "discrete",
                keyParams: this.filters.types_selected,
                type: "equal",
              });
            }
            if (this.filters.inventors_selected.length != 0) {
              this.request.filters.push({
                attr: "inventors.name",
                format: "discrete",
                keyParams: this.filters.inventors_selected,
                type: "equal",
              });
            }
            if (this.filters.applicants_selected.length != 0) {
              this.request.filters.push({
                attr: "applicant",
                format: "discrete",
                keyParams: this.filters.applicants_selected,
                type: "equal",
              });
            }
            if (this.filters.patentSort == "申请日期倒序") {
              this.request.sort = "fillingDate.desc";
            } else if (this.filters.patentSort == "申请日期正序") {
              this.request.sort = "fillingDate.asc";
            }
            break;
          }
          case "科研人员": {
            url = url + "/researcher";
            if (this.filters.interests_selected.length != 0) {
              this.request.filters.push({
                attr: "interests",
                format: "discrete",
                keyParams: this.filters.interests_selected,
                type: "equal",
              });
            }
            if (this.filters.institutions_selected.length != 0) {
              this.request.filters.push({
                attr: "currentInst.name",
                format: "discrete",
                keyParams: this.filters.institutions_selected,
                type: "equal",
              });
            }
            if (this.filters.researcherSort == "论文数量") {
              this.request.sort = "paperNum.desc";
            } else if (this.filters.researcherSort == "专利数量") {
              this.request.sort = "patentNum.desc";
            } else if (this.filters.researcherSort == "引用数量") {
              this.request.sort = "citationNum.desc";
            } else if (this.filters.researcherSort == "h指数") {
              this.request.sort = "hIndex.desc";
            } else if (this.filters.researcherSort == "g指数") {
              this.request.sort = "gIndex.desc";
            }
            break;
          }
          case "精确": {
            url = url + "/query";
            break;
          }
        }
        console.log(url);
        if (this.filter != "全部" && this.request.conditions.length == 0)
          return;
        if (this.filter != "精确") {
          console.log(JSON.stringify(this.request));
          this.$axios({
            method: "post",
            url: url,
            data: this.request,
          })
            .then((response) => {
              console.log(response.data);
              this.data = response.data;
              this.$emit("searchResult", this.data.data);
            })
            .catch((error) => {
              console.log(error);
            });
        } else {
          this.$axios({
            method: "post",
            url: "/api/search/query/",
            params: {
              key: this.text,
              type: this.filters.queryType,
            },
          })
            .then((response) => {
              console.log(response.data);
              this.data = response.data;
              if (this.data.success == false) {
                this.$notify({
                  title: "搜索失败",
                  message: this.data.message,
                  type: "warning",
                });
              } else {
                this.$router.push({ path: "/search/info/" + this.data.data });
              }
            })
            .catch((error) => {
              console.log(error);
            });
        }
      } else {
        this.$router.push({
          path: "/search",
          query: { text: this.text, filter: this.filter },
        });
      }
    },
    searchInResult() {
      this.filters = this.$parent.filters;
      console.log("搜索过滤器");
      console.log(this.filters);
      var i, j, scope;
      var url = "/api/search";
      var keyword = this.text.trim();
      var keywords = keyword.split(/\s+/);
      var condition;
      var condition1;
      var fuzzy = true;
      var translated = true;
      console.log(keywords);
      this.request.filters = [];
      switch (this.filter) {
        case "论文": {
          url = url + "/paper";
          for (i = 0; i < this.paperSearch.length; i++) {
            if (
              this.paperSearch[i].text == null ||
              this.paperSearch[i].text.match(/\s+/)
            )
              continue;
            switch (this.paperSearch[i].scope) {
              case "篇关摘":
                scope = ["title", "abstract", "keywords", "subjects"];
                fuzzy = this.filters.fuzzy;
                translated = this.filters.translated;
                break;
              case "作者":
                scope = ["authors.name"];
                fuzzy = false;
                translated = false;
                break;
              case "发表机构":
                scope = ["institutions.name"];
                fuzzy = this.filters.fuzzy;
                translated = this.filters.translated;
                break;
              case "刊登期刊":
                scope = ["journal.title"];
                fuzzy = this.filters.fuzzy;
                translated = this.filters.translated;
                break;
            }
            keywords = this.paperSearch[i].text.trim().split(/\s+/);
            console.log(keywords);
            condition = {
              compound: false,
              fuzzy: fuzzy,
              keyword: keywords[0],
              logic: this.paperSearch[i].logic,
              scope: scope,
              languages: ["zh", "en"],
              translated: translated,
            };
            if (keywords.length != 1) {
              for (j = 1; j < keywords.length; j++) {
                condition1 = {
                  compound: true,
                  fuzzy: fuzzy,
                  keyword: keywords[j],
                  logic: this.paperSearch[i].logic,
                  scope: scope,
                  subConditions: [condition],
                  languages: ["zh", "en"],
                  translated: translated,
                };
                condition = condition1;
              }
            }
            this.request.conditions.push(condition);
          }
          if (this.filters.paperType.type != "") {
            keywords = this.filters.paperType.type.trim().split(/\s+/);
            console.log(keywords);
            condition = {
              compound: false,
              fuzzy: false,
              keyword: keywords[0],
              logic: "and",
              scope: ["type"],
              translated: false,
            };
            if (keywords.length != 1) {
              for (i = 1; i < keywords.length; i++) {
                condition1 = {
                  compound: true,
                  fuzzy: false,
                  keyword: keywords[i],
                  logic: "and",
                  scope: ["type"],
                  subConditions: [condition],
                  translated: false,
                };
                condition = condition1;
              }
            }
            this.request.conditions.push(condition);
          }
          if (this.filters.year1 != 1900 || this.filters.year2 != 2021) {
            this.request.filters.push({
              attr: "year",
              format: "numeric",
              intParams: [this.filters.year1, this.filters.year2],
              type: "range",
            });
          }
          if (
            this.filters.citationNum != null &&
            this.filters.citationNum != ""
          ) {
            this.request.filters.push({
              attr: "citationNum",
              format: "numeric",
              intParams: [Number(this.filters.citationNum)],
              type: "above",
            });
          }
          if (this.filters.paperSort == "出版日期倒序") {
            this.request.sort = "date.desc";
          } else if (this.filters.paperSort == "出版日期正序") {
            this.request.sort = "date.asc";
          } else if (this.filters.paperSort == "引用数量") {
            this.request.sort = "citationNum.desc";
          }
          break;
        }
        case "专利": {
          url = url + "/patent";
          for (i = 0; i < this.patentSearch.length; i++) {
            if (
              this.patentSearch[i].text == null ||
              this.patentSearch[i].text.match(/\s+/)
            )
              continue;
            switch (this.patentSearch[i].scope) {
              case "标题摘要":
                scope = ["title", "abstract"];
                fuzzy = this.filters.fuzzy;
                translated = this.filters.translated;
                break;
              case "类型":
                scope = ["type"];
                fuzzy = false;
                translated = false;
                break;
              case "申请人":
                scope = ["applicant"];
                fuzzy = this.filters.fuzzy;
                translated = this.filters.translated;
                break;
              case "发明人":
                scope = ["inventors.name"];
                fuzzy = false;
                translated = false;
                break;
            }
            keywords = this.patentSearch[i].text.trim().split(/\s+/);
            console.log(keywords);
            condition = {
              compound: false,
              fuzzy: fuzzy,
              keyword: keywords[0],
              logic: this.patentSearch[i].logic,
              scope: scope,
              languages: ["zh", "en"],
              translated: translated,
            };
            if (keywords.length != 1) {
              for (j = 1; j < keywords.length; j++) {
                condition1 = {
                  compound: true,
                  fuzzy: fuzzy,
                  keyword: keywords[j],
                  logic: this.patentSearch[i].logic,
                  scope: scope,
                  subConditions: [condition],
                  languages: ["zh", "en"],
                  translated: translated,
                };
                condition = condition1;
              }
            }
            this.request.conditions.push(condition);
          }
          if (this.filters.patentSort == "申请日期倒序") {
            this.request.sort = "fillingDate.desc";
          } else if (this.filters.patentSort == "申请日期正序") {
            this.request.sort = "fillingDate.asc";
          }
          break;
        }
        case "科研人员": {
          url = url + "/researcher";
          for (i = 0; i < this.researcherSearch.length; i++) {
            if (
              this.researcherSearch[i].text == null ||
              this.researcherSearch[i].text.match(/\s+/)
            )
              continue;
            switch (this.researcherSearch[i].scope) {
              case "姓名":
                scope = ["name"];
                fuzzy = false;
                translated = false;
                break;
              case "研究方向":
                scope = ["interests"];
                fuzzy = this.filters.fuzzy;
                translated = this.filters.translated;
                break;
              case "合作机构":
                scope = ["institutions.name"];
                fuzzy = this.filters.fuzzy;
                translated = this.filters.translated;
                break;
              case "所属机构":
                scope = ["currentInst.name"];
                fuzzy = this.filters.fuzzy;
                translated = this.filters.translated;
                break;
            }
            keywords = this.researcherSearch[i].text.trim().split(/\s+/);
            console.log(keywords);
            condition = {
              compound: false,
              fuzzy: fuzzy,
              keyword: keywords[0],
              logic: this.researcherSearch[i].logic,
              scope: scope,
              languages: ["zh", "en"],
              translated: translated,
            };
            if (keywords.length != 1) {
              for (j = 1; j < keywords.length; j++) {
                condition1 = {
                  compound: true,
                  fuzzy: fuzzy,
                  keyword: keywords[j],
                  logic: this.researcherSearch[i].logic,
                  scope: scope,
                  subConditions: [condition],
                  languages: ["zh", "en"],
                  translated: translated,
                };
                condition = condition1;
              }
            }
            this.request.conditions.push(condition);
          }
          if (
            this.filters.citationNum != null &&
            this.filters.citationNum != ""
          ) {
            this.request.filters.push({
              attr: "citationNum",
              format: "numeric",
              intParams: [Number(this.filters.citationNum)],
              type: "above",
            });
          }
          if (this.filters.paperNum != null && this.filters.paperNum != "") {
            this.request.filters.push({
              attr: "paperNum",
              format: "numeric",
              intParams: [Number(this.filters.paperNum)],
              type: "above",
            });
          }
          if (this.filters.patentNum != null && this.filters.patentNum != "") {
            this.request.filters.push({
              attr: "patentNum",
              format: "numeric",
              intParams: [Number(this.filters.patentNum)],
              type: "above",
            });
          }
          if (this.filters.hIndex != null && this.filters.hIndex != "") {
            this.request.filters.push({
              attr: "hIndex",
              format: "numeric",
              intParams: [Number(this.filters.hIndex)],
              type: "above",
            });
          }
          if (this.filters.gIndex != null && this.filters.gIndex != "") {
            this.request.filters.push({
              attr: "gIndex",
              format: "numeric",
              intParams: [Number(this.filters.gIndex)],
              type: "above",
            });
          }
          if (this.filters.researcherSort == "论文数量") {
            this.request.sort = "paperNum.desc";
          } else if (this.filters.researcherSort == "专利数量") {
            this.request.sort = "patentNum.desc";
          } else if (this.filters.researcherSort == "引用数量") {
            this.request.sort = "citationNum.desc";
          } else if (this.filters.researcherSort == "h指数") {
            this.request.sort = "hIndex.desc";
          } else if (this.filters.researcherSort == "g指数") {
            this.request.sort = "gIndex.desc";
          }
          break;
        }
        case "期刊": {
          if (this.text == null || this.text == "") return;
          url = url + "/journal";
          condition = {
            compound: false,
            fuzzy: true,
            keyword: keywords[0],
            languages: ["zh", "en"],
            logic: "and",
            scope: ["title"],
            translated: this.filters.translated,
          };
          if (keywords.length != 1) {
            for (i = 1; i < keywords.length; i++) {
              condition1 = {
                compound: true,
                fuzzy: true,
                keyword: keywords[i],
                languages: ["zh", "en"],
                logic: "and",
                scope: ["title"],
                subConditions: [condition],
                translated: this.filters.translated,
              };
              condition = condition1;
            }
          }
          this.request.conditions.push(condition);
          break;
        }
        case "机构": {
          if (this.text == null || this.text == "") return;
          url = url + "/institution";
          this.request.size = 20;
          condition = {
            compound: false,
            fuzzy: true,
            keyword: keywords[0],
            languages: ["zh", "en"],
            logic: "and",
            scope: ["name"],
            translated: this.filters.translated,
          };
          if (keywords.length != 1) {
            for (i = 1; i < keywords.length; i++) {
              condition1 = {
                compound: true,
                fuzzy: true,
                keyword: keywords[i],
                languages: ["zh", "en"],
                logic: "and",
                scope: ["name"],
                subConditions: [condition],
                translated: this.filters.translated,
              };
              condition = condition1;
            }
          }
          this.request.conditions.push(condition);
          break;
        }
      }
      console.log(url);
      console.log(JSON.stringify(this.request));
      this.$axios({
        method: "post",
        url: url,
        data: this.request,
      })
        .then((response) => {
          console.log(response.data);
          this.data = response.data;
          this.$emit("searchResult", this.data.data);
          this.$emit("searchType", this.filter);
          if (
            this.filter == "论文" ||
            this.filter == "科研人员" ||
            this.filter == "专利"
          ) {
            this.$axios({
              method: "get",
              url: "/api/analysis/aggregations",
            })
              .then((response) => {
                this.aggregations = response.data;
                this.$emit("searchFilter", this.aggregations);
                console.log(response.data);
              })
              .catch((error) => {
                console.log(error);
              });
          }
        })
        .catch((error) => {
          console.log(error);
        });
    },
    jumpPage() {
      var url;
      switch (this.filter) {
        case "全部":
          url = "/api/search/smart";
          break;
        case "论文":
          url = "/api/search/paper";
          break;
        case "科研人员":
          url = "/api/search/researcher";
          break;
        case "专利":
          url = "/api/search/patent";
          break;
        case "期刊":
          url = "/api/search/journal";
          break;
        case "机构":
          url = "/api/search/institution";
          break;
      }
      this.request.page = this.page;
      if (this.filter != "全部" && this.request.conditions.length == 0) return;
      console.log(JSON.stringify(this.request));
      this.$axios({
        method: "post",
        url: url,
        data: this.request,
      })
        .then((response) => {
          console.log(response.data);
          this.data = response.data;
          this.$emit("jumpPage", this.data.data);
        })
        .catch((error) => {
          console.log(error);
        });
    },
  },
};
</script>

<style scoped>
.highSearch {
  margin-top: 10px;
}
.search_bar {
  width: 66%;
  margin-top: 20px;
}
.select {
  width: 12%;
}
.input {
  width: 85%;
}
.btn {
  margin-left: 10px;
}
</style>