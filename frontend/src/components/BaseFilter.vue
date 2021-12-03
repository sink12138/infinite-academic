<template>
  <v-card>
    <FilterYearPicker></FilterYearPicker>
    <div>
      <el-date-picker
        v-model="filter.year1"
        type="year"
        placeholder="起始日期"
        value-format="yyyy"
        @change="handleChange"
      >
      </el-date-picker>
      <el-date-picker
        v-model="filter.year2"
        type="year"
        placeholder="结束日期"
        value-format="yyyy"
        @change="handleChange"
      >
      </el-date-picker>
    </div>
    <v-divider></v-divider>
    <div class="checkbox">
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
    <div class="checkbox">
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
    <div class="checkbox">
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
    <div class="checkbox">
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
import FilterYearPicker from '../components/FilterYearPicker.vue'

export default {
  components: {
    FilterYearPicker
  },
  data() {
    return {
      menu: false,
      filter:{
        year1: "",
        Year2: "",
        topics_selected: [],  
        authors_selected: [],
        journals_selected: [],
        institutions_selected: [],
      },
      topics: ["a", "b", "c", "d", "e"],
      authors: ["a1", "b2", "c3", "d4", "e5"],
      journals: ["a0", "b0", "c0", "d0", "e0", "f0"],
      institutions: ["a01", "b02", "c03", "d04"],
    };
  },
  methods: {
    emit:function(){
      this.$emit('handleFilter', 'filter');
    },
    handleChange: function () {
      if(this.filter.year1 != "" && this.filter.Year2 != "" ){
        var d2 = new Date(Date.parse(this.filter.Year2)); //取今天的日期  
        var d1 = new Date(Date.parse(this.filter.year1));
        if (d1 > d2) {
            this.$notify({
              title: "起止日期",
              message: "起始日期大于截至日期",
              type: "warning",
            });
        }else{
          this.$emit('handleFilter', 'filter');
        }  
      }else{
        this.$emit('handleFilter', 'filter');
      }
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