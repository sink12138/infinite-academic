<template>
  <div>
    <Banner :title="{ text: 'Search', icon: 'mdi-magnify-expand' }"></Banner>

    <BaseSearchBar
      ref="bar"
      v-on:searchResult="searchResult"
      v-on:jumpPage="jump"
      v-on:filterChange="filterChange"
      v-on:searchFilter="searchFilter"
    ></BaseSearchBar>
    <v-card>
      <v-col cols="3">
        <BaseFilter
          class="filter"
          ref="filter"
          v-on:handleFilter="handleFilter"
          v-show="filter != '机构' && filter != '期刊'"
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
            <div v-if="results.length != 0">
              <div v-for="item in results" :key="item.id">
                <div v-if="item.authors != null">
                  <!-- 论文 -->
                  <v-card class="text-left my-2" max-width="650">
                    <v-card-title class="d-flex">
                      <v-icon class="mx-1">
                        mdi-text-box-multiple-outline
                      </v-icon>
                      <a
                        @click="href('paper', item.id)"
                        v-html="item.title"
                      ></a>
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
                      <a
                        @click="href('patent', item.id)"
                        v-html="item.title"
                      ></a>
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
                <div v-else>
                  <!-- 机构或期刊 -->
                  <v-card class="text-left my-2" max-width="650">
                    <v-card-title class="d-flex">
                      <v-icon class="mx-1">
                        mdi-text-box-multiple-outline
                      </v-icon>
                      <a
                        v-if="item.name != null"
                        @click="href('institution', item.id)"
                        v-html="item.name"
                      ></a>
                      <a
                        v-if="item.title != null"
                        @click="href('journal', item.id)"
                        v-html="item.title"
                      ></a>
                      <v-spacer></v-spacer>
                    </v-card-title>
                  </v-card>
                </div>
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
        authors_selected: [],
        subjects_selected: [],
        journals_selected: [],
        institutions_selected: [],
        types_selected: [],
        keywords_selected: [],
        interests_selected: [],
        inventors_selected: [],
        applicants_selected: [],
        paperType: {
          type: "",
          authors: "",
          institutions: "",
          journal: "",
        },
        paperSort: "相关度排序",
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
  created() {
    setTimeout(() => {
      if (this.$route.query.text != null && this.$route.query.text != "") {
        this.$refs.bar.text = this.$route.query.text;
        console.log(this.$refs.bar.text);
        this.filter = this.$route.query.filter;
        this.$refs.filter.showType = this.filter;
        this.$refs.bar.filter = this.filter;
        this.$refs.bar.search();
      }
    }, 5);
  },
  methods: {
    href(type, id) {
      this.$router.push({
        path: type,
        query: { id: id },
      });
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
    searchFilter(filter) {
      if (filter.data.authors != null)
        this.$refs.filter.authors = filter.data.authors;
      else this.$refs.filter.authors = [];
      if (filter.data.subjects != null)
        this.$refs.filter.subjects = filter.data.subjects;
      else this.$refs.filter.subjects = [];
      if (filter.data.journals != null)
        this.$refs.filter.journals = filter.data.journals;
      else this.$refs.filter.journals = [];
      if (filter.data.institutions != null)
        this.$refs.filter.institutions = filter.data.institutions;
      else this.$refs.filter.institutions = [];
      if (filter.data.types != null)
        this.$refs.filter.types = filter.data.types;
      else this.$refs.filter.types = [];
      if (filter.data.keywords != null)
        this.$refs.filter.keywords = filter.data.keywords;
      else this.$refs.filter.keywords = [];
      if (filter.data.interests != null)
        this.$refs.filter.interests = filter.data.interests;
      else this.$refs.filter.interests = [];
      if (filter.data.inventors != null)
        this.$refs.filter.inventors = filter.data.inventors;
      else this.$refs.filter.inventors = [];
      if (filter.data.applicants != null)
        this.$refs.filter.applicants = filter.data.applicants;
      else this.$refs.filter.applicants = [];
      this.$refs.filter.filter.authors_selected = [];
      this.$refs.filter.filter.subjects_selected = [];
      this.$refs.filter.filter.journals_selected = [];
      this.$refs.filter.filter.institutions_selected = [];
      this.$refs.filter.filter.types_selected = [];
      this.$refs.filter.filter.keywords_selected = [];
      this.$refs.filter.filter.interests_selected = [];
      this.$refs.filter.filter.inventors_selected = [];
      this.$refs.filter.filter.applicants_selected = [];
    },
    jump(data) {
      this.data = data;
      this.results = this.data.items;
      this.page = this.jumpPage;
    },
    pageChange() {
      if (this.jumpPage > this.length) this.jumpPage = this.length;
      this.$refs.bar.page = this.jumpPage - 1;
      this.$refs.bar.jumpPage();
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