<template>
  <v-card>
    <FilterYearPicker v-on:handleYear="handleYear"  v-show="this.showType != '期刊' && this.showType != '机构'"></FilterYearPicker>
    <v-divider></v-divider>
    <v-select
      v-show="showType == '论文'"
      class="select"
      v-model="filter.paperType"
      :items="paperTypes"
      hide-details
      prepend-inner-icon="mdi-dots-grid"
      background-color="grey lighten-2"
      filled
      @change="emit()"
    ></v-select>
    <v-divider></v-divider>
    <v-select
      v-show="showType == '专利'"
      class="select"
      v-model="filter.patentType"
      :items="patentTypes"
      hide-details
      prepend-inner-icon="mdi-dots-grid"
      background-color="grey lighten-2"
      filled
      @change="emit()"
    ></v-select>
    <v-divider></v-divider>
    <v-select
      v-show="showType == '科研人员'"
      class="select"
      v-model="filter.researcherType"
      :items="researcherTypes"
      hide-details
      prepend-inner-icon="mdi-dots-grid"
      background-color="grey lighten-2"
      filled
      @change="emit()"
    ></v-select>
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
    <div class="checkbox"  v-show="this.showType != '期刊' && this.showType != '机构'">
      <header class="checkboxLabel">学科</header>
      <v-checkbox
        class="checkboxitem"
        v-model="filter.topics_selected"
        v-for="topic in topics"
        :key="topic"
        :label="`${topic}`"
        :value="topic"
        @change="emit()"
      ></v-checkbox>
    </div>
    <v-divider></v-divider>
    <div class="checkbox"  v-show="this.showType != '期刊' && this.showType != '机构'">
      <header class="checkboxLabel">作者</header>
      <v-checkbox
        class="checkboxItem"
        v-model="filter.authors_selected"
        v-for="author in authors"
        :key="author"
        :label="`${author}`"
        :value="author"
        @change="emit()"
      ></v-checkbox>
    </div>
    <v-divider></v-divider>
    <div class="checkbox"  v-show="this.showType != '期刊' && this.showType != '机构'">
      <header class="checkboxLabel">期刊</header>
      <v-checkbox
        class="checkboxItem"
        v-model="filter.journals_selected"
        v-for="journal in journals"
        :key="journal"
        :label="`${journal}`"
        :value="journal"
        @change="emit()"
      ></v-checkbox>
    </div>
    <v-divider></v-divider>
    <div class="checkbox"  v-show="this.showType != '期刊' && this.showType != '机构'">
      <header class="checkboxLabel">机构</header>
      <v-checkbox
        class="checkboxItem"
        v-model="filter.institutions_selected"
        v-for="institution in institutions"
        :key="institution"
        :label="`${institution}`"
        :value="institution"
        @change="emit()"
      ></v-checkbox>
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
      menu: false,
      showType: "全部",
      paperTypes: [
        "标题/摘要/关键词/学科",
        "论文类别",
        "作者姓名",
        "机构名称",
        "期刊名称",
      ],
      patentTypes: ["标题/摘要","类型","申请人","发明人"],
      researcherTypes: ["姓名","研究方向","相关机构"],
      queryTypes: ["doi", "issn", "patentNum"],
      filter: {
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
      topics: ["a", "b", "c", "d", "e"],
      authors: ["a1", "b2", "c3", "d4", "e5"],
      journals: ["a0", "b0", "c0", "d0", "e0", "f0"],
      institutions: ["a01", "b02", "c03", "d04"],
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