<template>
  <div class="d-flex flex-column">
    <navigation></navigation>
    <div class="big_font">Infinite Acadmic</div>
    <search-bar class="ma-auto"></search-bar>
  </div>
</template>

<script>
import SearchBar from '../components/SearchBar.vue'
import Navigation from '../components/Navigation.vue'
export default {
  components: {
    SearchBar,
    Navigation
  },
  data:() =>  ({
    expand: false
  }),
  methods: {
    notify() {
      this.$notify({
        title: 'hello',
        message: 'test message',
        type: 'success'
      });
    }
  }
}
</script>