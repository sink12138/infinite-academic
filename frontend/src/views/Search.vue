<template>
  <div>
    <Banner :title="{ text: 'Search', icon: 'mdi-magnify-expand' }"></Banner>
    
      <BaseSearchBar
        ref="searchBar"
        v-on:searchResult="searchResult"
        v-on:filterChange="filterChange"
      ></BaseSearchBar>
      <v-card>
        <v-col>
      <BaseFilter
        class="filter"
        ref="filter"
        v-on:handleFilter="handleFilter"
      ></BaseFilter>
    </v-col>
    <v-col>
    <div>
      <div :class="(filter==='期刊'||filter==='机构') ? result : result1">
        <v-expansion-panels>
          <v-expansion-panel
            v-for="(item, index) in results"
            :key="index"
            expandable
          >
            <div v-if="item.authors != null">
              <v-expansion-panel-header>
                {{ index + 1 }}
              </v-expansion-panel-header>
              <div>
                <span class="itemTitle">
                  <p v-html="item.title"></p>
                </span>
              </div>
              <div>
                <h4>
                  <span v-if="item.journal != null"
                    ><a @click="goToJournal(item.journal)">{{
                      item.journal.title
                    }}</a></span
                  >
                </h4>
              </div>
              <div>
                <h4>
                  <span v-if="item.type != null">类别:{{ item.type }}</span>
                </h4>
              </div>
              <div>
                <span>
                  作者:
                  <i v-for="(author, idx) in item.authors" :key="idx">
                    &nbsp;<a @click="goToAuthor(author)">{{ author.name }}</a>
                  </i>
                </span>
              </div>
              <div v-if="item.keywords != null" class="itemInfo">
                <span>
                  关键词:&nbsp;
                  <i
                    v-for="(keyword, idx) in item.keywords"
                    :key="idx"
                    v-html="keyword + ' '"
                  ></i>
                </span>
              </div>
              <div v-if="item.abstract != null" class="itemInfo">
                <span>摘要：{{ item.abstract }}</span>
              </div>
              <div v-if="item.date != null" class="itemInfo">
                <span>发表日期:{{ item.date }}</span>
              </div>
              <div v-if="item.citationNum != null" class="itemInfo">
                <span>引用:{{ item.citationNum }}</span>
              </div>
              <v-btn color="blue" text @click="goToPaper(item)">
                查看全文
              </v-btn>
              <v-btn color="blue" text> 转发 </v-btn>
              <v-btn color="blue" text> 引用 </v-btn>

              <v-spacer></v-spacer>
            </div>
            <div v-else-if="item.sponsor != null">
              <v-expansion-panel-header>
                {{ index + 1 }}
              </v-expansion-panel-header>
              <div>
                <span class="itemTitle">
                  <p v-html="item.title"></p>
                </span>
              </div>
              <div>
                <h4>
                  <span>主办单位:{{ item.sponsor }}</span>
                </h4>
              </div>
              <v-spacer></v-spacer>
            </div>
            <div v-else-if="item.interests != null">
              <v-spacer></v-spacer>
            </div>
            <div v-else-if="item.applicant != null">
              <v-spacer></v-spacer>
            </div>
            <div v-else>
              <v-spacer></v-spacer>
            </div>
            <v-expansion-panel-content> </v-expansion-panel-content>
          </v-expansion-panel>
        </v-expansion-panels>
      </div>
    </div>
    </v-col>
    </v-card>
  </div>
</template>

<script>
import BaseFilter from "../components/BaseFilter.vue";
import BaseSearchBar from "../components/BaseSearchBar.vue";
import Banner from "../components/BaseBanner.vue";

export default {
  components: {
    BaseFilter,
    Banner,
    BaseSearchBar,
  },
  data() {
    return {
      filters: {
        year1: 1900,
        year2: 2021,
        topics_selected: [],
        authors_selected: [],
        journals_selected: [],
        institutions_selected: [],
        paperType: "标题/摘要/关键词/学科",
        patentType: "标题/摘要",
        researcherType: "姓名",
        queryType: "doi",
      },
      filter: "全部",
      results: [],
      data: {},
      router: [{ href: "/", icon: "mdi-arrow-left", title: "Back" }],
    };
  },
  methods: {
    goToPaper(paper) {
      console.log(paper.id);
    },
    goToJournal(journal) {
      console.log(journal.id);
    },
    goToAuthor(author) {
      console.log(author.id);
    },
    handleFilter(filter) {
      this.filters = filter;
    },
    searchResult(data) {
      console.log("搜索结果");
      this.data = data;
      console.log(this.data);
      this.results = this.data.items;
    },
    filterChange(filter) {
      this.filter = filter;
      this.$refs.filter.showType = filter;
      console.log(this.$refs.filter.showType);
    },
  },
};
</script>

<style>
.filter {
  float: left;
}
.result {
  margin-top: 30px;
  margin-left: 30px;
  margin-bottom: 50px;
  float:left;
  width: 50%;
}
.result1{
  margin-left: 10%;
  margin-right: 10%;
}
.itemTitle {
  font-size: 20px;
}
.itemInfo {
  text-align: left;
  margin-left: 20px;
}
</style>