<template>
  <div>
    <h1>
      <v-icon class="mdi-magnify text-h3">
        mdi-home
      </v-icon>
      Search
    </h1>
    <BaseGoBack class="back"></BaseGoBack>
    <BaseSearchBar class="search" ref="searchBar"></BaseSearchBar>
    <div>
      <div>
        <BaseFilter class="filter" ref="filter"></BaseFilter>
      </div>
      <div class="result">
        <v-row v-for="item in results" :key="item">
          <div v-if="item.authors!=null">
            <v-card class="card" max-width="400">
              <v-card-title>{{ item.title }}</v-card-title>
              <v-card-subtitle>{{ item.journal }}</v-card-subtitle>
              <v-card-text>
                <div>作者：</div>
                <div v-for="author in item.authors" :key="author">{{ author.name }}</div>
              </v-card-text>
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
          </div>
          <div v-else-if="item.interests!=null">
            <v-card class="card" max-width="400">
              <v-card-title>{{ item.title }}</v-card-title>
              <v-card-subtitle>{{ item.journal }}</v-card-subtitle>
              <v-card-text>
                <div>摘要：</div>
                <div>{{ item.abstract }}</div>
              </v-card-text>
              <v-card-text>
                <div>研究方向:</div>
                <div>{{ item.interests }}</div>
              </v-card-text>
              <v-card-actions>
                <v-btn color="blue" text> 转发 </v-btn>
                <v-btn color="blue" text> 引用 </v-btn>
              </v-card-actions>
            </v-card>
            <v-spacer></v-spacer>
          </div>
          <div v-else-if="item.applicant!=null">
            <v-card class="card" max-width="400">
              <v-card-title>{{ item.title }}</v-card-title>
              <v-card-subtitle>{{ item.journal }}</v-card-subtitle>
              <v-card-subtitle>申请人：{{ item.applicant }}</v-card-subtitle>
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
          </div>
          <div v-else>
            <v-card class="card" max-width="400">
              <v-card-title>{{ item.title }}</v-card-title>
              <v-card-subtitle>{{ item.journal }}</v-card-subtitle>
              <v-card-subtitle>name|:{{ item.name }}</v-card-subtitle>
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
          </div>
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
      filters:{},
      results: [
        { title: "aaa", journal: "b", abstract: "114514" , authors: [{name:"name1"},{name:"name2"}]},
        { title: "aaa", journal: "b", abstract: "114514" , interests: "软件工程"},
        { title: "aaa", journal: "b", abstract: "114514" , applicant: "谭火彬"},
        { title: "aaa", journal: "b", abstract: "114514" , name: "name"},
      ],
      data:{},
    };
  },
  methods:{
    echo(){
      this.filters=this.$refs.filter.filter; 
    },
    searchResult(){
      this.data=this.$refs.searchBar.data;
      console.log(this.data+"data");
      this.results=this.data.items;
    },
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