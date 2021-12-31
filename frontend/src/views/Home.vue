<template>
  <v-main class="home">
    <BaseNavigation :router="this.router"></BaseNavigation>
    <div class="big-font">Infinite Academic</div>
    <BaseSearchBar class="ma-auto bar"></BaseSearchBar>
    <v-container class="d-flex pa-0 align-center">
      <v-card outlined height="479px" width="800px">
        <v-divider class="mb-16 pb-16"></v-divider>
        <div class="d-flex align-center justify-center">
          <v-sheet>
            <v-icon size="64">mdi-book-open-blank-variant</v-icon>
            <v-card-text> 论文</v-card-text>
            <v-card-text>{{ this.total.papers }} </v-card-text>
          </v-sheet>
          <v-sheet>
            <v-icon size="64">mdi-clipboard-account</v-icon>
            <v-card-text> 科研人员</v-card-text>
            <v-card-text>{{ this.total.researchers }} </v-card-text>
          </v-sheet>
          <v-sheet>
            <v-icon size="64">mdi-home</v-icon>
            <v-card-text> 机构</v-card-text>
            <v-card-text>{{ this.total.institutions }} </v-card-text>
          </v-sheet>
          <v-sheet>
            <v-icon size="64">mdi-newspaper-variant</v-icon>
            <v-card-text> 期刊</v-card-text>
            <v-card-text>{{ this.total.journals }} </v-card-text>
          </v-sheet>
          <v-sheet>
            <v-icon size="64">mdi-lightbulb-on</v-icon>
            <v-card-text> 专利</v-card-text>
            <v-card-text>{{ this.total.patents }} </v-card-text>
          </v-sheet>
        </div>
      </v-card>
      <Hots :items="this.topics" type="话题"></Hots>
      <Hots :items="this.subjects" type="学科"></Hots>
    </v-container>
  </v-main>
</template>

<script>
import BaseSearchBar from "../components/BaseSearchBar.vue";
import BaseNavigation from "../components/BaseNavigation.vue";
import Hots from "../components/HomeHot.vue";
export default {
  components: {
    BaseSearchBar,
    BaseNavigation,
    Hots,
  },
  data: () => ({
    expand: false,
    router: [
      /*{ href: "/paper", icon: "mdi-book-open-blank-variant", title: "Paper" },
        { href: "/author", icon: "mdi-clipboard-account", title: "Author" },
        { href: "/institution", icon: "mdi-home", title: "Institution" },
        { href: "/journal", icon: "mdi-newspaper-variant", title: "Journals" },*/
      { href: "/search", icon: "mdi-magnify-expand", title: "高级检索" },
      /*{ href: "/topic", icon: "mdi-tooltip-check", title: "Topic" },
        { href: "/subject", icon: "mdi-flask-empty", title: "Subject" },
        { href: "/admin", icon: "mdi-shield-lock", title: "Admin" },
        { href: "/about", icon: "mdi-account", title: "About" },*/
    ],
    subjects: [],
    topics: [],
    total: {
      institutions: "",
      journals: "",
      papers: "",
      patents: "",
      researchers: "",
    },
  }),
  mounted() {
    this.getHots();
    this.getTotal();
  },
  methods: {
    echo() {
      this.$axios({
        method: "get",
        url: "/api/account/echo",
      })
        .then((response) => {
          console.log(response.data);
        })
        .catch((error) => {
          console.log(error);
        });
    },
    getHots() {
      this.$axios({
        method: "get",
        url: "/api/analysis/hotspots",
        params: {
          num: 10,
        },
      })
        .then((res) => {
          if (res.data.success) {
            this.subjects = res.data.data.subjects;
            this.topics = res.data.data.topics;
            this.standardize();
          } else {
            console.log(res.data.message);
          }
        })
        .catch((error) => {
          console.log(error);
        });
    },
    standardize() {
      var x = 100;
      x = this.subjects[0].heat;
      this.subjects.forEach(function (item) {
        item.percent = (item.heat * 100) / x;
      });
      x = this.topics[0].heat;
      this.topics.forEach(function (item) {
        item.percent = (item.heat * 100) / x;
      });
    },
    getTotal() {
      this.$axios({
        method: "get",
        url: "/api/analysis/total",
      })
        .then((res) => {
          if (res.data.success) {
            console.log(res.data.data);
            this.total = res.data.data;
          } else {
            console.log(res.data.message);
          }
        })
        .catch((error) => {
          console.log(error);
        });
    },
  },
};
</script>

<style scoped>
.home {
  background-image: url("../assets/Home_bg.jpg");
  background-size: 100%;
  background-repeat: no-repeat;
  height: 100%;
}
.home-font {
  font-size: 42px;
}
.bar {
  width: 1185px;
  background-color: rgba(255, 255, 255, 0.753);
}
</style>