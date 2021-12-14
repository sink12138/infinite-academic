<template>
  <div class="search_bar d-inline-flex">
    <v-select
      class="select"
      v-model="filter"
      :items="items"
      hide-details
      prepend-inner-icon="mdi-dots-grid"
      background-color="grey lighten-2"
      filled
      @change="emitFilter"
    ></v-select>
    <v-divider vertical></v-divider>
    <v-text-field
      class="input"
      v-model="text"
      clearable
      filled
      full-width
      background-color="grey lighten-5"
      counter="30"
      hide-details
      append-icon="mdi-magnify"
      @click:append="search"
    >
    </v-text-field>
  </div>
</template>

<script>
import qs from "qs";
export default {
  data() {
    return {
      items: ["全部", "论文", "期刊", "专利", "机构", "科研人员", "精确"],
      filter: "全部",
      text: null,
      filters: {},
      data: {},
      request: {},
      page: 0,
    };
  },
  methods: {
    emitFilter() {
      this.$emit("filterChange", this.filter);
    },
    search() {
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
            fuzzy: true,
            keyword: "",
            languages: ["zh"],
            logic: "and",
            translated: true,
          },
        ],
        filters: [],
        page: this.page,
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
                fuzzy: true,
                keyword: keywords[i],
                languages: ["zh"],
                logic: "and",
                scope: scope,
                subConditions: [condition],
                translated: true,
              };
              condition = condition1;
            }
            this.request.conditions[0] = condition;
          }
          if (this.filters.paperType.type != "") {
            keywords = this.filters.patentType.type.trim().split(/\s+/);
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
              this.request.conditions.push(condition);
            }
          }
          if (this.filters.paperType.authors != "") {
            keywords = this.filters.paperType.authors.trim().split(/\s+/);
            condition = {
              compound: false,
              fuzzy: false,
              keyword: keywords[0],
              logic: "and",
              scope: ["authors.name"],
              translated: false,
            };
            if (keywords.length != 1) {
              for (i = 1; i < keywords.length; i++) {
                condition1 = {
                  compound: true,
                  fuzzy: false,
                  keyword: keywords[i],
                  logic: "and",
                  scope: ["authors.name"],
                  subConditions: [condition],
                  translated: false,
                };
                condition = condition1;
              }
              this.request.conditions.push(condition);
            }
          }
          if (this.filters.paperType.institutions != "") {
            keywords = this.filters.paperType.institutions.trim().split(/\s+/);
            condition = {
              compound: false,
              fuzzy: false,
              keyword: keywords[0],
              logic: "and",
              scope: ["institutions.name"],
              translated: false,
            };
            if (keywords.length != 1) {
              for (i = 1; i < keywords.length; i++) {
                condition1 = {
                  compound: true,
                  fuzzy: false,
                  keyword: keywords[i],
                  logic: "and",
                  scope: ["institutions.name"],
                  subConditions: [condition],
                  translated: false,
                };
                condition = condition1;
              }
              this.request.conditions.push(condition);
            }
          }
          if (this.filters.paperType.journal != "") {
            keywords = this.filters.paperType.journal.trim().split(/\s+/);
            condition = {
              compound: false,
              fuzzy: false,
              keyword: keywords[0],
              logic: "and",
              scope: ["journal.title"],
              translated: false,
            };
            if (keywords.length != 1) {
              for (i = 1; i < keywords.length; i++) {
                condition1 = {
                  compound: true,
                  fuzzy: false,
                  keyword: keywords[i],
                  logic: "and",
                  scope: ["journal.title"],
                  subConditions: [condition],
                  translated: false,
                };
                condition = condition1;
              }
              this.request.conditions.push(condition);
            }
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
                languages: ["zh"],
                logic: "and",
                scope: ["title"],
                subConditions: [condition],
                translated: true,
              };
              condition = condition1;
            }
            this.request.conditions[0] = condition;
          }
          break;
        }
        case "机构": {
          url = url + "/institution";
          this.request.conditions[0].scope = ["name"];
          this.request.conditions[0].keyword = keywords[0];
          if (keywords.length != 1) {
            condition = this.request.conditions[0];
            for (i = 1; i < keywords.length; i++) {
              condition1 = {
                compound: true,
                fuzzy: true,
                keyword: keywords[i],
                languages: ["zh"],
                logic: "and",
                scope: ["name"],
                subConditions: [condition],
                translated: true,
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
                fuzzy: true,
                keyword: keywords[i],
                languages: ["zh"],
                logic: "and",
                scope: scope,
                subConditions: [condition],
                translated: true,
              };
              condition = condition1;
            }
            this.request.conditions[0] = condition;
          }
          if (this.filters.patentType.type != "") {
            keywords = this.filters.patentType.type.trim().split(/\s+/);
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
              this.request.conditions.push(condition);
            }
          }
          if (this.filters.patentType.applicant != "") {
            keywords = this.filters.patentType.applicant.trim().split(/\s+/);
            condition = {
              compound: false,
              fuzzy: true,
              keyword: keywords[0],
              logic: "and",
              scope: ["applicant"],
              translated: false,
            };
            if (keywords.length != 1) {
              for (i = 1; i < keywords.length; i++) {
                condition1 = {
                  compound: true,
                  fuzzy: false,
                  keyword: keywords[i],
                  logic: "and",
                  scope: ["applicant"],
                  subConditions: [condition],
                  translated: false,
                };
                condition = condition1;
              }
              this.request.conditions.push(condition);
            }
          }
          if (this.filters.patentType.inventors != "") {
            keywords = this.filters.patentType.inventors.trim().split(/\s+/);
            condition = {
              compound: false,
              fuzzy: false,
              keyword: keywords[0],
              logic: "and",
              scope: ["inventors.name"],
              translated: false,
            };
            if (keywords.length != 1) {
              for (i = 1; i < keywords.length; i++) {
                condition1 = {
                  compound: true,
                  fuzzy: false,
                  keyword: keywords[i],
                  logic: "and",
                  scope: ["inventors.name"],
                  subConditions: [condition],
                  translated: false,
                };
                condition = condition1;
              }
              this.request.conditions.push(condition);
            }
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
                fuzzy: true,
                keyword: keywords[i],
                languages: ["zh"],
                logic: "and",
                scope: scope,
                subConditions: [condition],
                translated: true,
              };
              condition = condition1;
            }
            this.request.conditions[0] = condition;
          }
          if (this.filters.researcherType.interests != "") {
            keywords = this.filters.researcherType.interests
              .trim()
              .split(/\s+/);
            condition = {
              compound: false,
              fuzzy: true,
              keyword: keywords[0],
              logic: "and",
              scope: ["interests"],
              translated: false,
            };
            if (keywords.length != 1) {
              for (i = 1; i < keywords.length; i++) {
                condition1 = {
                  compound: true,
                  fuzzy: false,
                  keyword: keywords[i],
                  logic: "and",
                  scope: ["interests"],
                  subConditions: [condition],
                  translated: false,
                };
                condition = condition1;
              }
              this.request.conditions.push(condition);
            }
          }
          if (this.filters.researcherType.currentInst != "") {
            keywords = this.filters.researcherType.currentInst
              .trim()
              .split(/\s+/);
            condition = {
              compound: false,
              fuzzy: true,
              keyword: keywords[0],
              logic: "and",
              scope: ["currentInst.name"],
              translated: false,
            };
            if (keywords.length != 1) {
              for (i = 1; i < keywords.length; i++) {
                condition1 = {
                  compound: true,
                  fuzzy: false,
                  keyword: keywords[i],
                  logic: "and",
                  scope: ["interests"],
                  subConditions: [condition],
                  translated: false,
                };
                condition = condition1;
              }
              this.request.conditions.push(condition);
            }
          }
          if (this.filters.researcherType.institutions != "") {
            keywords = this.filters.researcherType.institutions
              .trim()
              .split(/\s+/);
            condition = {
              compound: false,
              fuzzy: true,
              keyword: keywords[0],
              logic: "and",
              scope: ["institutions.name"],
              translated: false,
            };
            if (keywords.length != 1) {
              for (i = 1; i < keywords.length; i++) {
                condition1 = {
                  compound: true,
                  fuzzy: false,
                  keyword: keywords[i],
                  logic: "and",
                  scope: ["institutions.name"],
                  subConditions: [condition],
                  translated: false,
                };
                condition = condition1;
              }
              this.request.conditions.push(condition);
            }
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
          this.request = {
            key: this.text,
            type: this.type,
          };
          break;
        }
      }
      console.log(JSON.stringify(this.request));
      console.log(url);
      if (this.filter != "精确") {
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
          url: url,
          data: qs.stringify(this.request),
        })
          .then((response) => {
            console.log(response.data);
            this.data = response.data;
            this.$emit("searchResult", this.data.data);
          })
          .catch((error) => {
            console.log(error);
          });
      }
    },
  },
};
</script>

<style scoped>
.search_bar {
  width: 66%;
}
.select {
  height: 40px;
  width: 12%;
}
.input {
  width: 88%;
}
</style>