<template>
  <v-main>
    <BaseNavigation :router="this.router"></BaseNavigation>
    <div class="big-font">Infinite Academic</div>
    <BaseSearchBar class="ma-auto"></BaseSearchBar>
    <v-row no-gutters>
      <v-col cols="2">
        <Hots :items="this.subjects" type="学科"></Hots>
      </v-col>
      <v-col cols="2">
        <Hots :items="this.topics" type="话题"></Hots>
      </v-col>
    </v-row>
  </v-main>
</template>

<script>
import BaseSearchBar from '../components/BaseSearchBar.vue'
import BaseNavigation from '../components/BaseNavigation.vue'
import Hots from '../components/HomeHot.vue'
export default {
  components: {
    BaseSearchBar,
    BaseNavigation,
    Hots
  },
  data:() =>  ({
    expand: false,
    router: [
        { href: "/paper", icon: "mdi-book-open-blank-variant", title: "Paper" },
        { href: "/author", icon: "mdi-clipboard-account", title: "Author" },
        { href: "/institution", icon: "mdi-home", title: "Institution" },
        { href: "/journal", icon: "mdi-newspaper-variant", title: "Journals" },
        { href: "/search", icon: "mdi-magnify-expand", title: "Search" },
        { href: "/topic", icon: "mdi-tooltip-check", title: "Topic" },
        { href: "/subject", icon: "mdi-flask-empty", title: "Subject" },
        { href: "/admin", icon: "mdi-shield-lock", title: "Admin" },
        { href: "/about", icon: "mdi-account", title: "About" },
    ],
    subjects: [],
    topics: []
  }),
  mounted() {
    this.getHots();
  },
  methods: {
    echo() {
      this.$axios({
        method: "get",
        url: "/api/account/echo",
      }).then(response => {
        console.log(response.data)
      }).catch(error => {
        console.log(error)
      })
    },
    getHots() {
      this.$axios({
        method: "get",
        url: "/api/analysis/hotspots",
        params: {
          num: 10
        }
      }).then(res => {
        if (res.data.success) {
          this.subjects = res.data.data.subjects;
          this.topics = res.data.data.topics;
          this.standardize();
        } else {
          console.log(res.data.message)
        }
      }).catch(error => {
        console.log(error)
      })
    },
    standardize() {
      var x = 100;
      x = this.subjects[0].heat;
      this.subjects.forEach(function(item) {
        item.percent  = item.heat*100/x;
      })
      x = this.topics[0].heat;
      this.topics.forEach(function(item) {
        item.percent  = item.heat*100/x;
      })
    }
  },
  
}
</script>