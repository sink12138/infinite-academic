<template>
  <div class="d-flex flex-column">
    <BaseNavigation></BaseNavigation>
    <div class="big-font">Infinite Acadmic</div>
    <BaseSearchBar class="ma-auto"></BaseSearchBar>
    <v-btn @click="echo()">echo</v-btn>
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
    expand: false
  }),
  methods: {
    echo() {
      this.$axios({
        method: "get",
        url: "/api/account/echo",
        params: {
          str: "echo hello"
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