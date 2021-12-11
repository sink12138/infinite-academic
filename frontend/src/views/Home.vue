<template>
  <div>
    <BaseNavigation :router="this.router"></BaseNavigation>
    <div class="big-font">Infinite Acadmic</div>
    <BaseSearchBar class="ma-auto"></BaseSearchBar>
    <div>
      <v-btn @click="echo()">echo</v-btn>
    </div>
  </div>
</template>

<script>
import BaseSearchBar from '../components/BaseSearchBar.vue'
import BaseNavigation from '../components/BaseNavigation.vue'
export default {
  components: {
    BaseSearchBar,
    BaseNavigation
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
  }),
  methods: {
    echo() {
      this.$axios({
        method: "get",
        url: "/api/account/echo",
        params: {
          str: "echo success"
        }
      }).then(response => {
        console.log(response.data)
      }).catch(error => {
        console.log(error)
      })
    }
  }
}
</script>