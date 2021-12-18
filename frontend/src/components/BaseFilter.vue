<template>
  <v-card>
    <FilterYearPicker
      v-on:handleYear="handleYear"
      v-show="this.showType == '论文' || this.showType == '全部'"
    ></FilterYearPicker>
    <v-divider></v-divider>
    <v-select
      v-show="showType == '论文' || showType == '全部'"
      class="select"
      v-model="filter.paperSort"
      :items="paperSorts"
      hide-details
      prepend-inner-icon="mdi-dots-grid"
      background-color="grey lighten-2"
      filled
      @change="emit()"
    ></v-select>
    <v-divider></v-divider>
    <div v-show="showType == '论文'">
      <v-col>
        <v-text-field
          label="类型"
          v-model="filter.paperType.type"
          @change="emit()"
        ></v-text-field>
      </v-col>
      <v-col>
        <v-text-field
          label="作者"
          v-model="filter.paperType.authors"
          @change="emit()"
        ></v-text-field>
      </v-col>
      <v-col>
        <v-text-field
          label="发表机构"
          v-model="filter.paperType.institutions"
          @change="emit()"
        ></v-text-field>
      </v-col>
      <v-col>
        <v-text-field
          label="刊登期刊"
          v-model="filter.paperType.journal"
          @change="emit()"
        ></v-text-field>
      </v-col>
    </div>
    <div v-show="showType == '专利'">
      <v-select
        class="select"
        v-model="filter.patentSort"
        :items="patentSorts"
        hide-details
        prepend-inner-icon="mdi-dots-grid"
        background-color="grey lighten-2"
        filled
        @change="emit()"
      ></v-select>
      <v-divider></v-divider>
      <v-col>
        <v-text-field
          label="类型"
          v-model="filter.patentType.type"
          @change="emit()"
        ></v-text-field>
      </v-col>
      <v-col>
        <v-text-field
          label="申请人"
          v-model="filter.patentType.applicant"
          @change="emit()"
        ></v-text-field>
      </v-col>
      <v-col>
        <v-text-field
          label="发明人"
          v-model="filter.patentType.inventors"
          @change="emit()"
        ></v-text-field>
      </v-col>
    </div>
    <v-divider></v-divider>
    <div v-show="showType == '科研人员'">
      <v-select
        class="select"
        v-model="filter.researcherSort"
        :items="researcherSorts"
        hide-details
        prepend-inner-icon="mdi-dots-grid"
        background-color="grey lighten-2"
        filled
        @change="emit()"
      ></v-select>
      <v-divider></v-divider>
      <v-col>
        <v-text-field
          label="研究方向"
          v-model="filter.researcherType.interests"
          @change="emit()"
        ></v-text-field>
      </v-col>
      <v-col>
        <v-text-field
          label="所属机构"
          v-model="filter.researcherType.currentInst"
          @change="emit()"
        ></v-text-field>
      </v-col>
      <v-col>
        <v-text-field
          label="合作机构"
          v-model="filter.researcherType.institutions"
          @change="emit()"
        ></v-text-field>
      </v-col>
    </div>

    <div v-show="showType == '全部' || showType == '论文' || showType == '科研人员'">
      <v-col>
        <v-text-field
          label="引用数量大于"
          v-model="filter.citationNum"
          :rules="numberRule"
          @change="emit()"
        ></v-text-field>
      </v-col>
    </div>
    <div v-show="showType == '科研人员'">
      <v-col>
        <v-text-field
          label="专利数量大于"
          v-model="filter.patentNum"
          :rules="numberRule"
          @change="emit()"
        ></v-text-field>
      </v-col>
    </div>
    <div v-show="showType == '科研人员'">
      <v-col>
        <v-text-field
          label="论文数量大于"
          v-model="filter.paperNum"
          :rules="numberRule"
          @change="emit()"
        ></v-text-field>
      </v-col>
    </div>
    <div v-show="showType == '科研人员'">
      <v-col>
        <v-text-field
          label="h指数高于"
          v-model="filter.hIndex"
          :rules="numberRule"
          @change="emit()"
        ></v-text-field>
      </v-col>
    </div>
    <div v-show="showType == '科研人员'">
      <v-col>
        <v-text-field
          label="g指数高于"
          v-model="filter.gIndex"
          :rules="numberRule"
          @change="emit()"
        ></v-text-field>
      </v-col>
    </div>
    <v-divider></v-divider>
    <v-select
      v-show="showType == '精确'"
      class="select"
      v-model="filter.queryType"
      :items="queryTypes"
      hide-details
      prepend-inner-icon="mdi-dots-grid"
      background-color="grey lighten-2"
      filled
      @change="emit()"
    ></v-select>
    <v-divider></v-divider>
    <div class="checkbox" v-if="this.authors.length != 0">
      <header class="checkboxLabel">作者</header>
      <v-checkbox
        class="checkboxItem"
        v-model="filter.authors_selected"
        v-for="(author, index) in authors"
        :key="index"
        :label="`${author.term}`"
        :value="author.term"
        @change="emit()"
      ></v-checkbox>
    </div>
    <v-divider></v-divider>
    <div class="checkbox" v-if="this.journals.length != 0">
      <header class="checkboxLabel">期刊</header>
      <v-checkbox
        class="checkboxItem"
        v-model="filter.journals_selected"
        v-for="(journal, index) in journals"
        :key="index"
        :label="`${journal.term}`"
        :value="journal.term"
        @change="emit()"
      ></v-checkbox>
    </div>
    <v-divider></v-divider>
    <div class="checkbox" v-if="this.institutions.length != 0">
      <header class="checkboxLabel">机构</header>
      <v-checkbox
        class="checkboxItem"
        v-model="filter.institutions_selected"
        v-for="(institution, index) in institutions"
        :key="index"
        :label="`${institution.term}`"
        :value="institution.term"
        @change="emit()"
      ></v-checkbox>
    </div>
    <v-divider></v-divider>
    <div class="checkbox" v-if="this.subjects.length != 0">
      <header class="checkboxLabel">学科</header>
      <v-checkbox
        class="checkboxItem"
        v-model="filter.subjects_selected"
        v-for="(subject, index) in subjects"
        :key="index"
        :label="`${subject.term}`"
        :value="subject.term"
        @change="emit()"
      >
      </v-checkbox>
    </div>
    <v-divider></v-divider>
    <div class="checkbox" v-if="this.types.length != 0">
      <header class="checkboxLabel">类型</header>
      <v-checkbox
        class="checkboxItem"
        v-model="filter.types_selected"
        v-for="(type, index) in types"
        :key="index"
        :label="`${type.term}`"
        :value="type.term"
        @change="emit()"
      >
      </v-checkbox>
    </div>
    <v-divider></v-divider>
    <div class="checkbox" v-if="this.keywords.length != 0">
      <header class="checkboxLabel">关键词</header>
      <v-checkbox
        class="checkboxItem"
        v-model="filter.keywords_selected"
        v-for="(keyword, index) in keywords"
        :key="index"
        :label="`${keyword.term}`"
        :value="keyword.term"
        @change="emit()"
      >
      </v-checkbox>
    </div>
    <v-divider></v-divider>
    <div class="checkbox" v-if="this.interests.length != 0">
      <header class="checkboxLabel">研究方向</header>
      <v-checkbox
        class="checkboxItem"
        v-model="filter.interests_selected"
        v-for="(interest, index) in interests"
        :key="index"
        :label="`${interest.term}`"
        :value="interest.term"
        @change="emit()"
      >
      </v-checkbox>
    </div>
    <v-divider></v-divider>
    <div class="checkbox" v-if="this.inventors.length != 0">
      <header class="checkboxLabel">发明人</header>
      <v-checkbox
        class="checkboxItem"
        v-model="filter.inventors_selected"
        v-for="(inventor, index) in inventor"
        :key="index"
        :label="`${inventor.term}`"
        :value="inventor.term"
        @change="emit()"
      >
      </v-checkbox>
    </div>
    <v-divider></v-divider>
    <div class="checkbox" v-if="this.applicants.length != 0">
      <header class="checkboxLabel">申请人</header>
      <v-checkbox
        class="checkboxItem"
        v-model="filter.applicants_selected"
        v-for="(applicant, index) in applicants"
        :key="index"
        :label="`${applicant.term}`"
        :value="applicant.term"
        @change="emit()"
      >
      </v-checkbox>
    </div>
  </v-card>
</template>

<script>
import FilterYearPicker from "../components/FilterYearPicker.vue";

export default {
  components: {
    FilterYearPicker,
  },
  data() {
    return {
      numberRule: [(v) => /^[0-9]*$/.test(v) || "请输入数字"],
      menu: false,
      showType: "全部",
      paperSorts: ["相关度排序", "出版日期正序", "出版日期倒序", "引用数量"],
      patentSorts: ["相关度排序", "申请日期正序", "申请日期倒序"],
      researcherSorts: [
        "相关度排序",
        "论文数量",
        "专利数量",
        "引用数量",
        "h指数",
        "g指数",
      ],
      queryTypes: ["doi", "issn", "patentNum"],
      filter: {
        year1: 1900,
        year2: 2021,
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
      authors: [],
      subjects: [],
      journals: [],
      institutions: [],
      types: [],
      keywords: [],
      interests: [],
      inventors: [],
      applicants: [],
    };
  },
  methods: {
    handleYear(year) {
      this.filter.year1 = year[0];
      this.filter.year2 = year[1];
    },
    emit: function () {
      this.$emit("handleFilter", this.filter);
    },
  },
};
</script>

<style scoped>
.checkboxLabel {
  margin-top: 10px;
  margin-bottom: 10px;
}
.checkboxItem {
  margin: 3px;
}
.checkbox {
  margin-left: 15px;
}
</style>