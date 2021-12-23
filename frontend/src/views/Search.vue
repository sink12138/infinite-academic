<template>
  <div>
    <Banner
      v-if="fromDoor == ''"
      :title="{ text: 'Search', icon: 'mdi-magnify-expand' }"
    ></Banner>

    <BaseSearchBar
      ref="bar"
      v-on:searchResult="searchResult"
      v-on:jumpPage="jump"
      v-on:filterChange="filterChange"
      v-on:searchFilter="searchFilter"
      v-on:searchType="searchType"
    ></BaseSearchBar>
    <v-card class="searchMain">
      <v-col cols="3">
        <BaseFilter
          class="filter"
          ref="filter"
          v-on:handleFilter="handleFilter"
          v-on:searchFilter="handleFilter2"
          v-show="filter != '机构' && filter != '期刊'"
        ></BaseFilter>
      </v-col>
      <v-col cols="36">
        <div>
          <div class="result">
            <div v-if="results.length != 0">
              <v-row></v-row>
              <v-row>
                <v-col>
                  当前 第 {{ page }} 页,共 {{ length }} 页,共
                  {{ itemNum }} 条,耗时 {{ timeCost }} ms
                </v-col>
              </v-row>
            </div>
            <div v-if="data.correction != null">
              <span
                >已为您推荐&nbsp;<i v-html="data.correction"></i
                >&nbsp;的结果</span
              >
            </div>
            <div v-if="data.detection != null">
              <v-card>
                <div v-if="data.detection == 'journal'">
                  <!-- 期刊 -->
                  <tr
                    v-for="(row, index) in sliceList(data.recommendation, 3)"
                    :key="index"
                  >
                    <td
                      v-for="item in row"
                      :key="item.id"
                      style="width: 33%; height: 50%; table-layout: fixed"
                    >
                      <JournalCard
                        :item="item"
                        :disabled="fromDoor"
                      ></JournalCard>
                      <v-btn v-if="fromDoor != ''" @click="toDoor(item)"
                        >选择</v-btn>
                    </td>
                  </tr>
                </div>
                <div v-else-if="data.detection == 'researcher'">
                  <!-- 科研人员 -->
                  <tr
                    v-for="(row, index) in sliceList(data.recommendation, 3)"
                    :key="index"
                  >
                    <td
                      v-for="item in row"
                      :key="item.id"
                      style="width: 33%; height: 50%; table-layout: fixed"
                    >
                      <AuthorCard
                        :item="item"
                        :disabled="fromDoor"
                      ></AuthorCard>
                      <v-btn v-if="fromDoor != ''" @click="toDoor(item)"
                        >选择</v-btn
                      >
                    </td>
                  </tr>
                </div>
                <div v-else-if="data.detection == 'institution'">
                  <!-- 机构 -->
                  <tr
                    v-for="(row, index) in sliceList(data.recommendation, 3)"
                    :key="index"
                  >
                    <td
                      v-for="item in row"
                      :key="item.id"
                      style="width: 33%; height: 50%; table-layout: fixed"
                    >
                      <InstitutionCard
                        :item="item"
                        :disabled="fromDoor"
                      ></InstitutionCard>
                      <v-btn v-if="fromDoor != ''" @click="toDoor(item)"
                        >选择</v-btn
                      >
                    </td>
                  </tr>
                </div>
              </v-card>
            </div>
            <div v-if="results.length != 0">
              <div v-if="searchType1 == '全部' || searchType1 == '论文'">
                <div v-for="item in results" :key="item.id">
                  <!-- 论文 -->
                  <PaperCard :item="item" :disabled="fromDoor"></PaperCard>
                  <v-btn v-if="fromDoor!=''" @click="toDoor(item)">选择</v-btn>
                </div>
              </div>
              <div v-else-if="searchType1 == '期刊'">
                <div v-for="item in results" :key="item.id">
                  <!-- 期刊 -->
                  <JournalCard :item="item" :disabled="fromDoor"></JournalCard>
                  <v-btn v-if="fromDoor!=''" @click="toDoor(item)">选择</v-btn>
                </div>
              </div>
              <div v-else-if="searchType1 == '专利'">
                <div v-for="item in results" :key="item.id">
                  <!-- 专利 -->
                  <v-card class="text-left my-2" max-width="650">
                    <v-card-title class="d-flex">
                      <v-icon class="mx-1">
                        mdi-text-box-multiple-outline
                      </v-icon>
                      <span
                        class="link"
                        @click="href('patent', item.id)"
                        v-html="item.title"
                      ></span>
                      <v-spacer></v-spacer>
                    </v-card-title>
                    <v-card-subtitle class="pb-0">
                      <span v-if="item.fillingDate">
                        申请日:{{ item.fillingDate }} </span
                      >&nbsp;
                      <span v-if="item.publicationDate">
                        公开日:{{ item.publicationDate }} </span
                      >&nbsp;
                      <span v-if="item.applicant"
                        >申请人:{{ item.applicant }}</span
                      >&nbsp;
                    </v-card-subtitle>
                    <v-card-text class="pb-0">
                      <span
                        v-for="(inventor, idx) in item.inventors"
                        :key="inventor.id"
                      >
                        <span
                          class="link"
                          v-if="idx == item.inventors.length - 1"
                          @click="href('author', inventor.id)"
                        >
                          {{ inventor.name }}
                        </span>
                        <span
                          class="link"
                          v-else
                          @click="href('author', inventor.id)"
                        >
                          {{ inventor.name + "," }}
                        </span>
                      </span>
                    </v-card-text>
                  </v-card>
                </div>
              </div>
              <div v-else-if="searchType1 == '科研人员'">
                <div v-for="item in results" :key="item.id">
                  <!-- 科研人员 -->
                  <AuthorCard :item="item" :disabled="fromDoor"></AuthorCard>
                  <v-btn v-if="fromDoor!=''" @click="toDoor(item)">选择</v-btn>
                </div>
              </div>
              <div v-else-if="searchType1 == '机构'">
                <tr v-for="(row, index) in sliceList(results, 2)" :key="index">
                  <td
                    v-for="item in row"
                    :key="item.id"
                    style="width: 33%; height: 50%; table-layout: fixed"
                  >
                    <!-- 机构 -->
                    <InstitutionCard
                      :item="item"
                      :disabled="fromDoor"
                    ></InstitutionCard>
                    <v-btn v-if="fromDoor != ''" @click="toDoor(item)"
                      >选择</v-btn
                    >
                  </td>
                </tr>
              </div>
            </div>
            <div v-if="results.length != 0">
              <v-row>
                <v-col cols="8">
                  当前 第 {{ page }} 页,共 {{ length }} 页,共 {{ itemNum }} 条
                </v-col>
              </v-row>
              <v-row>
                <el-pagination
                  @current-change="pageChange"
                  :current-page.sync="jumpPage"
                  layout="prev, pager, next, jumper"
                  :page-size="10"
                  :total="itemNum"
                >
                </el-pagination>
              </v-row>
            </div>
          </div>
        </div>
      </v-col>
    </v-card>
    <div v-if="fromDoor != ''" class="fixBut" style="right: 15%">
      <v-btn @click="close()">关闭</v-btn>
    </div>
  </div>
</template>

<script>
import BaseFilter from "../components/BaseFilter.vue";
import BaseSearchBar from "../components/BaseSearchBar.vue";
import Banner from "../components/BaseBanner.vue";

import PaperCard from "../components/card/CardPaper.vue";
import AuthorCard from "../components/card/CardAuthor.vue";
import InstitutionCard from "../components/card/CardInstitution.vue";
import JournalCard from "../components/card/CardJournal.vue";
export default {
  components: {
    PaperCard,
    AuthorCard,
    InstitutionCard,
    JournalCard,
    BaseFilter,
    Banner,
    BaseSearchBar,
  },
  props: {
    fromDoor: {
      type: String,
      default: "",
    },
    todo: {
      type: String,
      default: "全部",
    },
  },
  data() {
    return {
      filters: {
        year1: 1900,
        year2: 2021,
        translated: true,
        fuzzy: true,
        citationNum: null,
        paperNum: null,
        patentNum: null,
        hIndex: null,
        gIndex: null,
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
      timeCost: 0,
      page: 1,
      length: 1,
      itemNum: 0,
      filter: "全部",
      results: [],
      data: {},
      searchType1: "论文",
      router: [{ href: "/", icon: "mdi-arrow-left", title: "Back" }],
    };
  },
  computed: {
    sliceList() {
      return function (data, count) {
        if (data != undefined) {
          let index = 0;
          let arrTemp = [];
          for (let i = 0; i < data.length; i++) {
            index = parseInt(i / count);
            if (arrTemp.length <= index) {
              arrTemp.push([]);
            }
            arrTemp[index].push(data[i]);
          }
          return arrTemp;
        }
      };
    },
  },
  created() {
    setTimeout(() => {
      this.$refs.bar.high = true;
      console.log(this.$refs.bar.high);
      if (this.$route.query.text != null && this.$route.query.text != "") {
        this.$refs.bar.text = this.$route.query.text;
        switch (this.$route.query.filter) {
          case "论文":
            this.$refs.bar.paperSearch[0].text = this.$route.query.text;
            break;
          case "科研人员":
            this.$refs.bar.researcherSearch[0].text = this.$route.query.text;
            break;
          case "专利":
            this.$refs.bar.patentSearch[0].text = this.$route.query.text;
            break;
        }
      }
      console.log(this.$refs.bar.text);
      if (this.$route.query.filter != null && this.$route.query.filter != "") {
        this.filter = this.$route.query.filter;
      } else {
        this.filter = this.todo;
      }
      this.$refs.filter.showType = this.filter;
      this.$refs.bar.filter = this.filter;
      if (this.$route.query.text != null && this.$route.query.text != "")
        this.$refs.bar.search();
    }, 1);
  },
  methods: {
    searchType(filter) {
      this.searchType1 = filter;
    },
    href(type, id) {
      if (id == null) {
        this.$notify({
          title: '数据缺失',
          message: '信息暂未收录，给您带来不便敬请谅解。',
          type: 'warning'
        });
        return;
      }
      this.$router.push({
        path: type,
        query: { id: id },
      });
    },
    handleFilter(filter) {
      this.filters = filter;
    },
    handleFilter2(filter) {
      this.filters = filter;
      this.$refs.bar.searchFilters();
    },
    searchResult(data) {
      this.data = data;
      this.timeCost = this.data.timeCost;
      this.results = this.data.items;
      this.page = this.data.page + 1;
      this.length = this.data.totalPages;
      this.itemNum = this.data.totalHits;
    },
    searchFilter(filter) {
      if (filter.data.authors != null) {
        this.$refs.filter.authors = filter.data.authors;
        this.$refs.filter.filter.authors_selected = [];
      } else this.$refs.filter.authors = [];
      if (filter.data.subjects != null) {
        this.$refs.filter.filter.subjects_selected = [];
        this.$refs.filter.subjects = filter.data.subjects;
      } else this.$refs.filter.subjects = [];
      if (filter.data.journals != null) {
        this.$refs.filter.filter.journals_selected = [];
        this.$refs.filter.journals = filter.data.journals;
      } else this.$refs.filter.journals = [];
      if (filter.data.institutions != null) {
        this.$refs.filter.filter.institutions_selected = [];
        this.$refs.filter.institutions = filter.data.institutions;
      } else this.$refs.filter.institutions = [];
      if (filter.data.types != null) {
        this.$refs.filter.filter.types_selected = [];
        this.$refs.filter.types = filter.data.types;
      } else this.$refs.filter.types = [];
      if (filter.data.keywords != null) {
        this.$refs.filter.filter.keywords_selected = [];
        this.$refs.filter.keywords = filter.data.keywords;
      } else this.$refs.filter.keywords = [];
      if (filter.data.interests != null) {
        this.$refs.filter.filter.interests_selected = [];
        this.$refs.filter.interests = filter.data.interests;
      } else this.$refs.filter.interests = [];
      if (filter.data.inventors != null) {
        this.$refs.filter.filter.inventors_selected = [];
        this.$refs.filter.inventors = filter.data.inventors;
      } else this.$refs.filter.inventors = [];
      if (filter.data.applicants != null) {
        this.$refs.filter.filter.applicants_selected = [];
        this.$refs.filter.applicants = filter.data.applicants;
      } else this.$refs.filter.applicants = [];
    },
    jump(data) {
      this.data = data;
      this.results = this.data.items;
      this.page = this.jumpPage;
    },
    pageChange() {
      if (this.jumpPage > this.length) this.jumpPage = this.length;
      if (this.jumpPage < 1) this.jumpPage = 1;
      this.$refs.bar.page = this.jumpPage - 1;
      this.$refs.bar.jumpPage();
    },
    filterChange(filter) {
      this.filter = filter;
      this.$refs.filter.showType = filter;
    },
    close() {
      this.$emit("closeID", "close");
    },
    toDoor(item) {
      this.$emit("findResult", item);
    },
  },
};
</script>

<style>
.searchMain {
  margin-left: 15%;
  margin-right: 15%;
}
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
