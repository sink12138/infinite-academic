<template>
  <div>
    <Banner :title="{ text: 'Search', icon: 'mdi-magnify-expand' }"></Banner>

    <BaseSearchBar
      ref="bar"
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
          <div class="result">
            <div v-if="data.correction != null">
              <span
                >已为您推荐&nbsp;<i v-html="data.correction"></i
                >&nbsp;的结果</span
              >
            </div>
            <div v-if="data.detection != null">
              <div v-if="data.detection == 'journal'">
                <div v-for="item in recommendation" :key="item.id">
                  <!-- 期刊 -->
                  <v-card class="text-left my-2" max-width="650">
                    <v-card-title class="d-flex">
                      <v-icon class="mx-1">
                        mdi-text-box-multiple-outline
                      </v-icon>
                      <a
                        @click="href('journal', item.id)"
                        v-html="item.title"
                      ></a>
                      <v-spacer></v-spacer>
                    </v-card-title>
                    <v-card-text>
                      <span> {{ item.sponsor }} </span>
                    </v-card-text>
                  </v-card>
                </div>
              </div>
              <div v-else-if="data.detection == 'researcher'">
                <div v-for="item in recommendation" :key="item.id">
                  <!-- 科研人员 -->
                  <v-card class="text-left my-2" max-width="650">
                    <v-card-title class="d-flex">
                      <v-icon class="mx-1">
                        mdi-text-box-multiple-outline
                      </v-icon>
                      <a
                        @click="href('author', item.id)"
                        v-html="item.name"
                      ></a>
                      <v-spacer></v-spacer>
                    </v-card-title>
                    <v-card-subtitle class="pb-0">
                      <span> 研究方向:{{ item.interests }} </span>
                      &nbsp;
                      <a
                        v-if="item.institution"
                        @click="href('institution', item.institution.id)"
                      >
                        {{ item.institution.name }} </a
                      >&nbsp;
                      <span v-if="item.citationNum">
                        被引量:{{ item.citationNum }}
                      </span>
                      <span v-if="item.paperNum">
                        已发表文章:{{ item.paperNum }}
                      </span>
                      <span v-if="item.patentNum">
                        专利数量:{{ item.patentNum }}
                      </span>
                    </v-card-subtitle>
                  </v-card>
                </div>
              </div>
              <div v-else-if="data.detection == 'institution'">
                <div v-for="item in recommendation" :key="item.id">
                  <!-- 机构 -->
                  <v-card class="text-left my-2" max-width="650">
                    <v-card-title class="d-flex">
                      <v-icon class="mx-1">
                        mdi-text-box-multiple-outline
                      </v-icon>
                      <a
                        @click="href('institution', item.id)"
                        v-html="item.name"
                      ></a>
                      <v-spacer></v-spacer>
                    </v-card-title>
                  </v-card>
                </div>
              </div>
            </div>
            <div v-for="item in results" :key="item.id">
              <div v-if="item.authors != null">
                <!-- 论文 -->
                <v-card class="text-left my-2" max-width="650">
                  <v-card-title class="d-flex">
                    <v-icon class="mx-1">
                      mdi-text-box-multiple-outline
                    </v-icon>
                    <a @click="href('paper', item.id)" v-html="item.title"></a>
                    <v-spacer></v-spacer>
                    <v-btn icon>
                      <v-icon>mdi-comma-box</v-icon>
                    </v-btn>
                  </v-card-title>
                  <v-card-subtitle class="pb-0">
                    <span
                      v-if="item.date"
                      v-text="item.date.substr(0, 4)"
                    ></span
                    >&nbsp;
                    <a
                      v-if="item.journal"
                      @click="href('journal', item.journal.id)"
                    >
                      {{ item.journal.title }} </a
                    >&nbsp;
                    <span>被引量:{{ item.citationNum }}</span>
                  </v-card-subtitle>
                  <v-card-text class="pb-0">
                    <span
                      v-for="(author, idx) in item.authors"
                      :key="author.id"
                    >
                      <a
                        v-if="idx == item.authors.length - 1"
                        @click="href('author', author.id)"
                        >{{ author.name }}</a
                      >
                      <a v-else @click="href('author', author.id)">{{
                        author.name + ","
                      }}</a>
                    </span>
                  </v-card-text>
                  <v-card-text class="pt-2 pb-0">
                    <span
                      class="mx-1"
                      v-for="keyword in item.keywords"
                      :key="keyword"
                      v-html="keyword"
                    >
                      <v-btn small outlined>
                        <v-icon small> mdi-tag-outline </v-icon>
                      </v-btn>
                    </span>
                  </v-card-text>
                  <v-card-text>
                    <span v-if="item.abstract"> {{ item.abstract }} </span>
                  </v-card-text>
                </v-card>
              </div>
              <div v-else-if="item.sponsor != null">
                <!-- 期刊 -->
                <v-card class="text-left my-2" max-width="650">
                  <v-card-title class="d-flex">
                    <v-icon class="mx-1">
                      mdi-text-box-multiple-outline
                    </v-icon>
                    <a
                      @click="href('journal', item.id)"
                      v-html="item.title"
                    ></a>
                    <v-spacer></v-spacer>
                  </v-card-title>
                  <v-card-text>
                    <span> {{ item.sponsor }} </span>
                  </v-card-text>
                </v-card>
              </div>
              <div v-else-if="item.applicant != null">
                <!-- 专利 -->
                <v-card class="text-left my-2" max-width="650">
                  <v-card-title class="d-flex">
                    <v-icon class="mx-1">
                      mdi-text-box-multiple-outline
                    </v-icon>
                    <a @click="href('paper', item.id)" v-html="item.title"></a>
                    <v-spacer></v-spacer>
                  </v-card-title>
                  <v-card-subtitle class="pb-0">
                    <span v-if="item.fillingDate">
                      申请日:{{ item.fillingDate.substr(0, 4) }} </span
                    >&nbsp;
                    <span v-if="item.publicationDate">
                      公开日:{{ item.publicationDate.substr(0, 4) }} </span
                    >&nbsp; <span>申请人:{{ item.applicant }}</span
                    >&nbsp;
                  </v-card-subtitle>
                  <v-card-text class="pb-0">
                    <span
                      v-for="(inventor, idx) in item.inventors"
                      :key="inventor.id"
                    >
                      <a
                        v-if="idx == item.inventors.length - 1"
                        @click="href('author', inventor.id)"
                        >{{ inventor.name }}</a
                      >
                      <a v-else @click="href('author', inventor.id)">{{
                        inventor.name + ","
                      }}</a>
                    </span>
                  </v-card-text>
                </v-card>
              </div>
              <div v-else-if="item.interests != null">
                <!-- 科研人员 -->
                <v-card class="text-left my-2" max-width="650">
                  <v-card-title class="d-flex">
                    <v-icon class="mx-1">
                      mdi-text-box-multiple-outline
                    </v-icon>
                    <a @click="href('author', item.id)" v-html="item.name"></a>
                    <v-spacer></v-spacer>
                  </v-card-title>
                  <v-card-subtitle class="pb-0">
                    <span> 研究方向:{{ item.interests }} </span>
                    &nbsp;
                    <a
                      v-if="item.institution"
                      @click="href('institution', item.institution.id)"
                    >
                      {{ item.institution.name }} </a
                    >&nbsp;
                    <span v-if="item.citationNum">
                      被引量:{{ item.citationNum }}
                    </span>
                    <span v-if="item.paperNum">
                      已发表文章:{{ item.paperNum }}
                    </span>
                    <span v-if="item.patentNum">
                      专利数量:{{ item.patentNum }}
                    </span>
                  </v-card-subtitle>
                </v-card>
              </div>
              <div v-else>
                <!-- 机构 -->
                <v-card class="text-left my-2" max-width="650">
                  <v-card-title class="d-flex">
                    <v-icon class="mx-1">
                      mdi-text-box-multiple-outline
                    </v-icon>
                    <a
                      @click="href('institution', item.id)"
                      v-html="item.name"
                    ></a>
                    <v-spacer></v-spacer>
                  </v-card-title>
                </v-card>
              </div>
            </div>
            <div v-if="results.length != 0">
              <v-row></v-row>
              <v-row>
                <v-col>
                  当前 第 {{ page }} 页,共 {{ length }} 页,共 {{ itemNum }} 条
                </v-col>
                <v-col>
                  <v-text-field
                    label="跳转至"
                    v-model="jumpPage"
                    append-icon="mdi-magnify"
                    @click:append="pageChange"
                  ></v-text-field>
                </v-col>
              </v-row>
            </div>
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
        patentType: {
          type: "",
          applicant: "",
          inventors: "",
        },
        patentSort: "相关度排序",
        researcherType: {
          interests: "",
          currentInst: "",
          institutions: "",
        },
        researcherSort: "相关度排序",
        queryType: "doi",
      },
      jumpPage: 1,
      page: 1,
      length: 1,
      itemNum: 0,
      filter: "全部",
      results: [],
      data: {},
      router: [{ href: "/", icon: "mdi-arrow-left", title: "Back" }],
    };
  },
  methods: {
    href(type, id) {
      this.$router.push({
        path: type,
        query: { id: id },
      });
    },
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
      this.data = data;
      this.results = this.data.items;
      this.page = this.data.page + 1;
      this.length = this.data.totalPages;
      this.itemNum = this.data.totalHits;
    },
    pageChange() {
      if (this.jumpPage > this.totalPages) this.jumpPage = this.totalPages;
      this.$refs.bar.page = this.jumpPage - 1;
      this.$refs.bar.search();
    },
    filterChange(filter) {
      this.filter = filter;
      this.$refs.filter.showType = filter;
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
  float: left;
  width: 50%;
}
.itemTitle {
  font-size: 20px;
}
.itemInfo {
  text-align: left;
  margin-left: 20px;
}
</style>