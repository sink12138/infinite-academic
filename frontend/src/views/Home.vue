<template>
  <div class="d-flex flex-column">
    <BaseNavigation></BaseNavigation>
    <div class="big-font">Infinite Acadmic</div>
    <BaseSearchBar class="ma-auto"></BaseSearchBar>
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
    notify() {
      this.$notify({
        title: "hello",
        message: "test message",
        type: "success"
      });
    }
  }
}
</script>