<template>
  <v-card height="100%">
    <FilterYearPicker
      v-on:handleYear="handleYear"
      v-show="this.showType == '论文' || this.showType == '全部'"
    ></FilterYearPicker>
    <v-divider></v-divider>
    <div class="ml-4">
      <v-switch v-model="filter.translated" label="语种关联"></v-switch>
      <v-switch v-model="filter.fuzzy" label="模糊搜索"></v-switch>
    </div>
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
        <v-select
          label="类型"
          :items="paperType"
          v-model="filter.paperType.type"
          @change="emit()"
        ></v-select>
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
    </div>
    <div v-show="
      showType == '全部' || showType == '论文' || showType == '科研人员'
      "
    >
      <v-col>
        <v-text-field
          label="引用数量大于"
          v-model="filter.citationNum"
          :rules="numberRule"
          @change="emitFilter()"
        ></v-text-field>
      </v-col>
    </div>
    <div v-show="showType == '科研人员'">
      <v-col>
        <v-text-field
          label="专利数量大于"
          v-model="filter.patentNum"
          :rules="numberRule"
          @change="emitFilter()"
        ></v-text-field>
      </v-col>
    </div>
    <div v-show="showType == '科研人员'">
      <v-col>
        <v-text-field
          label="论文数量大于"
          v-model="filter.paperNum"
          :rules="numberRule"
          @change="emitFilter()"
        ></v-text-field>
      </v-col>
    </div>
    <div v-show="showType == '科研人员'">
      <v-col>
        <v-text-field
          label="h指数高于"
          v-model="filter.hIndex"
          :rules="numberRule"
          @change="emitFilter()"
        ></v-text-field>
      </v-col>
    </div>
    <div v-show="showType == '科研人员'">
      <v-col>
        <v-text-field
          label="g指数高于"
          v-model="filter.gIndex"
          :rules="numberRule"
          @change="emitFilter()"
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
      <el-collapse v-model="authorFilter">
        <el-collapse-item name="1">
          <template slot="title">
            <v-icon>mdi-account</v-icon>
            <span class="text-body-2">作者</span>
          </template>
          <v-checkbox
            class="checkboxItem"
            v-model="filter.authors_selected"
            v-for="(author, index) in authors"
            :key="index"
            :label="`${author.term}(${author.frequency})`"
            :value="author.term"
            @change="emitFilter()"
          ></v-checkbox>
        </el-collapse-item>
      </el-collapse>
    </div>
    <v-divider></v-divider>
    <div class="checkbox" v-if="this.journals.length != 0">
      <el-collapse v-model="journalFilter">
        <el-collapse-item name="1">
          <template slot="title">
            <v-icon>mdi-newspaper-variant-outline</v-icon>
            <span class="text-body-2">期刊</span>
          </template>
          <v-checkbox
            class="checkboxItem"
            v-model="filter.journals_selected"
            v-for="(journal, index) in journals"
            :key="index"
            :label="`${journal.term}(${journal.frequency})`"
            :value="journal.term"
            @change="emitFilter()"
          ></v-checkbox>
        </el-collapse-item>
      </el-collapse>
    </div>
    <v-divider></v-divider>
    <div class="checkbox" v-if="this.institutions.length != 0">
      <el-collapse v-model="institutionFilter">
        <el-collapse-item name="1">
          <template slot="title">
            <v-icon>mdi-home</v-icon>
            <span class="text-body-2">机构</span>
          </template>
          <v-checkbox
            class="checkboxItem"
            v-model="filter.institutions_selected"
            v-for="(institution, index) in institutions"
            :key="index"
            :label="`${institution.term}(${institution.frequency})`"
            :value="institution.term"
            @change="emitFilter()"
          ></v-checkbox>
        </el-collapse-item>
      </el-collapse>
    </div>
    <v-divider></v-divider>
    <div class="checkbox" v-if="this.subjects.length != 0">
      <el-collapse v-model="subjectFilter">
        <el-collapse-item name="1">
          <template slot="title">
            <v-icon>mdi-flask-empty</v-icon>
            <span class="text-body-2">学科</span>
          </template>
          <v-checkbox
            class="checkboxItem"
            v-model="filter.subjects_selected"
            v-for="(subject, index) in subjects"
            :key="index"
            :label="`${subject.term}(${subject.frequency})`"
            :value="subject.term"
            @change="emitFilter()"
          >
          </v-checkbox>
        </el-collapse-item>
      </el-collapse>
    </div>
    <v-divider></v-divider>
    <div class="checkbox" v-if="this.types.length != 0">
      <el-collapse v-model="typeFilter">
        <el-collapse-item name="1">
          <template slot="title">
            <v-icon>mdi-menu</v-icon>
            <span class="text-body-2">类型</span>
          </template>
          <v-checkbox
            class="checkboxItem"
            v-model="filter.types_selected"
            v-for="(type, index) in types"
            :key="index"
            :label="`${type.term}(${type.frequency})`"
            :value="type.term"
            @change="emitFilter()"
          >
          </v-checkbox>
        </el-collapse-item>
      </el-collapse>
    </div>
    <v-divider></v-divider>
    <div class="checkbox" v-if="this.keywords.length != 0">
      <el-collapse v-model="keywordFilter">
        <el-collapse-item name="1">
          <template slot="title">
            <v-icon>mdi-tag</v-icon>
            <span class="text-body-2">关键词</span>
          </template>
          <v-checkbox
            class="checkboxItem"
            v-model="filter.keywords_selected"
            v-for="(keyword, index) in keywords"
            :key="index"
            :label="`${keyword.term}(${keyword.frequency})`"
            :value="keyword.term"
            @change="emitFilter()"
          >
          </v-checkbox>
        </el-collapse-item>
      </el-collapse>
    </div>
    <v-divider></v-divider>
    <div class="checkbox" v-if="this.interests.length != 0">
      <el-collapse v-model="interestFilter">
        <el-collapse-item name="1">
          <template slot="title">
            <v-icon>mdi-directions-fork</v-icon>
            <span class="text-body-2">研究方向</span>
          </template>
          <v-checkbox
            class="checkboxItem"
            v-model="filter.interests_selected"
            v-for="(interest, index) in interests"
            :key="index"
            :label="`${interest.term}(${interest.frequency})`"
            :value="interest.term"
            @change="emitFilter()"
          >
          </v-checkbox>
        </el-collapse-item>
      </el-collapse>
    </div>
    <v-divider></v-divider>
    <div class="checkbox" v-if="this.inventors.length != 0">
      <el-collapse v-model="inventorFilter">
        <el-collapse-item name="1">
          <template slot="title">
            <v-icon>mdi-account</v-icon>
            <span class="text-body-2">发明人</span>
          </template>
          <v-checkbox
            class="checkboxItem"
            v-model="filter.inventors_selected"
            v-for="(inventor, index) in inventors"
            :key="index"
            :label="`${inventor.term}(${inventor.frequency})`"
            :value="inventor.term"
            @change="emitFilter()"
          >
          </v-checkbox>
        </el-collapse-item>
      </el-collapse>
    </div>
    <v-divider></v-divider>
    <div class="checkbox" v-if="this.applicants.length != 0">
      <el-collapse v-model="applicantFilter">
        <el-collapse-item name="1">
          <template slot="title">
            <v-icon>mdi-account</v-icon>
            <span class="text-body-2">申请人</span>
          </template>
          <v-checkbox
            class="checkboxItem"
            v-model="filter.applicants_selected"
            v-for="(applicant, index) in applicants"
            :key="index"
            :label="`${applicant.term}(${applicant.frequency})`"
            :value="applicant.term"
            @change="emitFilter()"
          >
          </v-checkbox>
        </el-collapse-item>
      </el-collapse>
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
      paperType: ["期刊论文","学位论文","会议论文",""],
      authorFilter: ["1"],
      journalFilter: ["1"],
      institutionFilter: ["1"],
      subjectFilter: ["1"],
      typeFilter: ["1"],
      keywordFilter: ["1"],
      interestFilter: ["1"],
      inventorFilter: ["1"],
      applicantFilter: ["1"],
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
        translated: true,
        fuzzy:true,
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
      this.$emit("searchFilter", this.filter);
    },
    emit: function () {
      this.$emit("handleFilter", this.filter);
    },
    emitFilter: function () {
      this.$emit("searchFilter", this.filter);
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
  margin: 2px;
}
.checkbox {
  margin-left: 15px;
}
.v-input--checkbox {
  font-weight: 800;
}
</style>