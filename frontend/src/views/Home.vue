<template>
  <v-main>
    <BaseNavigation :router="this.router"></BaseNavigation>
    <div class="big-font">Infinite Academic</div>
    <BaseSearchBar class="ma-auto"></BaseSearchBar>
    <v-divider></v-divider>
    <v-btn @click="echo"></v-btn>
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
    subjects: [
      { name: "物理", heat: 100},
      { name: "化学", heat: 94},
      { name: "生物", heat: 84},
      { name: "数学", heat: 76},
      { name: "物理1", heat: 70},
      { name: "化学1", heat: 62},
      { name: "生物1", heat: 60},
      { name: "数学1", heat: 59},
      { name: "物理2", heat: 55},
      { name: "化学2", heat: 52},
    ],
    topics: [
      { name: "物理", heat: 100},
      { name: "化学", heat: 94},
      { name: "生物", heat: 84},
      { name: "数学", heat: 76},
      { name: "物理1", heat: 70},
      { name: "化学1", heat: 62},
      { name: "生物1", heat: 60},
      { name: "数学1", heat: 59},
      { name: "物理2", heat: 55},
      { name: "化学2", heat: 52},
    ]
  }),
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
    }
  }
}
</script>