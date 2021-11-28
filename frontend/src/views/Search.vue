<template>
  <div>
    <h1>
      <v-icon class="mdi-magnify text-h3">
        mdi-home
      </v-icon>
      Search
    </h1>
    <BaseGoBack class="back"></BaseGoBack>
    <BaseSearchBar class="search"></BaseSearchBar>
    <div>
      <div>
        <BaseFilter class="filter"></BaseFilter>
      </div>
      <div class="result">
        <v-row v-for="item in results" :key="item">
          <v-card class="card" max-width="400">
            <v-card-title>{{ item.title }}</v-card-title>
            <v-card-subtitle>{{ item.journal }}</v-card-subtitle>
            <v-card-text>
              <div>摘要：</div>
              <div>{{ item.abstract }}</div>
            </v-card-text>
            <v-card-actions>
              <v-btn color="blue" text> 转发 </v-btn>
              <v-btn color="blue" text> 引用 </v-btn>
            </v-card-actions>
          </v-card>
          <v-spacer></v-spacer>
        </v-row>
      </div>
    </div>
  </div>
</template>

<script>
import BaseFilter from "../components/BaseFilter.vue";
import BaseGoBack from "../components/BaseGoBack.vue";
import BaseSearchBar from "../components/BaseSearchBar.vue";

export default {
  components: {
    BaseGoBack,
    BaseFilter,
    BaseSearchBar,
  },
  data() {
    return {
      results: [
        { title: "aaa", journal: "b", abstract: "114514" },
        { title: "aaa", journal: "b", abstract: "114514" },
        { title: "aaa", journal: "b", abstract: "114514" },
      ],
    };
  },
};
</script>

<style>
.filter {
  float: left;
  left: 0;
}
.back {
  position: fixed;
  left: 20px;
}
.search {
  margin-bottom: 30px;
}
.result {
  float: right;
  width: 50%;
}
.card {
  width: 100%;
  margin-bottom: 10px;
}
</style>