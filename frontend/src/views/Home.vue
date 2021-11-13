<template>
  <div class="d-flex flex-column">
    <home-navigation></home-navigation>
    <div class="big_font">Infinite Acadmic</div>
    <search-bar class="ma-auto"></search-bar>
  </div>
</template>

<script>
import SearchBar from '../components/SearchBar.vue'
import Navigation from '../components/Navigation.vue'
export default {
  components: {
    'search-bar': SearchBar,
    'home-navigation': Navigation
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