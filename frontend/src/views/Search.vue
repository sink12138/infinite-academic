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
                  <v-btn v-if="fromDoor != ''" @click="toDoor(item)"
                    >选择</v-btn
                  >
                </div>
              </div>
              <div v-else-if="searchType1 == '期刊'">
                <div v-for="item in results" :key="item.id">
                  <!-- 期刊 -->
                  <JournalCard :item="item" :disabled="fromDoor"></JournalCard>
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
                      <v-spacer> </v-spacer>
                      <v-dialog v-model="dialog" persistent max-width="600px">
                        <template v-slot:activator="{ on, attrs }">
                          <v-btn
                            depressed
                            height="100%"
                            v-bind="attrs"
                            v-on="on"
                            icon
                            @click="convert(item)"
                          >
                            <v-icon>mdi-delete</v-icon>
                          </v-btn>
                        </template>
                        <v-card>
                          <v-card-title>
                            <span class="headline">专利转移</span>
                          </v-card-title>
                          <v-card-text>
                            <v-container>
                              <div class="whole">
                                <v-row>
                                  <v-col cols="12">
                                    <v-text-field
                                      v-model="showItem.address"
                                      label="转让后的地址"
                                      required
                                    ></v-text-field>
                                  </v-col>
                                </v-row>
                                <v-row>
                                  <v-col cols="12">
                                    <v-text-field
                                      v-model="showItem.agency"
                                      label="转让后的代理机构"
                                      required
                                    ></v-text-field>
                                  </v-col>
                                </v-row>
                                <v-row>
                                  <v-col cols="12">
                                    <v-text-field
                                      v-model="showItem.agent"
                                      label="转让后的代理人"
                                      required
                                    ></v-text-field>
                                  </v-col>
                                </v-row>
                                <v-row>
                                  <v-col cols="12">
                                    <v-text-field
                                      v-model="showItem.applicant"
                                      label="转让后的申请人"
                                      required
                                    ></v-text-field>
                                  </v-col>
                                </v-row>
                                <v-row>
                                  <v-col cols="12">
                                    <v-text-field
                                      v-model="showItem.transferee"
                                      label="受让方（可选填）"
                                    ></v-text-field>
                                  </v-col>
                                </v-row>
                                <v-row>
                                  <v-col cols="12">
                                    <v-text-field
                                      v-model="showItem.transferor"
                                      label="转让方(可选填）"
                                    ></v-text-field>
                                  </v-col>
                                </v-row>
                                <v-row>
                                  <v-col cols="12">
                                    <v-text-field
                                      v-model="email"
                                      label="邮箱（可选填）"
                                    ></v-text-field>
                                  </v-col>
                                </v-row>
                                <v-row>
                                  <v-col cols="12">
                                    <v-form
                                      ref="uploadFileForm"
                                      v-model="uploadFile"
                                    >
                                      <v-file-input
                                        v-model="fileInfo"
                                        show-size
                                        accept=".xls,.xlsx"
                                        @change="uploadFile"
                                        label="点击选择文件，文件格后缀为：.xls、.xlsx"
                                      ></v-file-input>
                                    </v-form>
                                  </v-col>
                                </v-row>
                                <v-row>
                                  <v-col cols="12">
                                    <v-text-field
                                      v-model="websiteLink"
                                      label="相关网站链接（可选填）"
                                    ></v-text-field>
                                  </v-col>
                                </v-row>
                              </div>
                              <v-btn
                                color="primary"
                                large
                                @click="dialog = false"
                                width="160px"
                              >
                                返回
                              </v-btn>
                              &emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;
                              <v-btn
                                color="primary"
                                large
                                @click="submitApplication()"
                                width="160px"
                              >
                                提交申请
                              </v-btn>
                            </v-container>
                          </v-card-text>
                        </v-card>
                      </v-dialog>
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
                  <v-btn v-if="fromDoor != ''" @click="toDoor(item)"
                    >选择</v-btn
                  >
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
  mounted() {
    this.$refs.bar.filter = this.todo;
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
      uploadFile: true,
      fileInfo: [],
      showItem: {
        address: "",
        agency: "",
        agent: "",
        applicant: "",
        transferee: "",
        transferor: "",
      },
      returnItem: {
        address: "",
        agency: "",
        agent: "",
        applicant: "",
        patentId: "",
      },
      baseItem: {},
      email: "",
      fileToken: "",
      websiteLink: "",
      dialog: false,
      patentId: "",
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
      this.filter = this.$route.query.filter;
      this.$refs.filter.showType = this.filter;
      this.$refs.bar.filter = this.filter;
      if (this.$route.query.text != null && this.$route.query.text != "")
        this.$refs.bar.search();
    }, 1);
  },
  methods: {
    convert(item) {
      this.baseItem = Object.assign({}, item);
      this.dialog = true;
      console.log(this.baseItem);
      this.showItem.id = this.baseItem.id;
      console.log(this.showItem.id);
      this.$axios({
        method: "get",
        url: "/api/search/info/patent/" + this.showItem.id,
      }).then((response) => {
        console.log(response.data);
        if (response.data.success) {
          this.showItem.address = response.data.data.address;
          this.showItem.agency = response.data.data.agency;
          this.showItem.agent = response.data.data.agent;
          this.showItem.applicant = response.data.data.applicant;
        }
      });
    },
    submitApplication() {
      console.log(this.fileInfo);
      this.$axios({
        method: "post",
        url: "/api/resourse/upload",
        data: {
          file: this.fileInfo,
        },
      }).then((response) => {
        console.log(response.data);
        this.fileToken = response.data.data;
      });
      this.$axios({
        method: "post",
        url: "/api/account/patent/transfer",
        data: {
          content: {
            address: this.showItem.address,
            agency: this.showItem.agency,
            agent: this.showItem.agent,
            applicant: this.showItem.applicant,
            patentId: this.baseItem.id,
            transferee: this.showItem.transferee,
            transferor: this.showItem.transferor,
          },
          email: this.email,
          fileToken: this.fileToken,
          websiteLink: this.websiteLink,
        },
      }).then((response) => {
        console.log(this.showItem.address);
        console.log(this.showItem.agency);
        console.log(this.showItem.agent);
        console.log(this.showItem.applicant);
        console.log(this.showItem.transferee);
        console.log(this.showItem.transferor);
        console.log(this.baseItem.id);
        console.log(this.email);
        console.log(this.websiteLink);
        console.log(this.fileToken);
        console.log(response.data);
        if (response.data.success) {
          this.$notify({
            title: "成功",
            message: "发送申请成功，请等待工作人员审核",
            type: "success",
          });
          this.dialog = false;
        } else {
          this.$notify({
            title: "失败",
            message: "发送申请失败，请检查表单信息",
            type: "error",
          });
        }
      });
    },
    searchType(filter) {
      this.searchType1 = filter;
    },
    href(type, id) {
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
