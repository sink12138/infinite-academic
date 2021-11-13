<template>
  <div>
    <h1>Search</h1>
    <go-back></go-back>
    <div>
      <search-filter></search-filter>
    </div>
  </div>
</template>

<script>
import GoBack from '../components/GoBack.vue'
import Filter from '../components/Filter.vue'

export default {
  components: {
    'go-back': GoBack,
    'search-filter': Filter 
  },
}
</script>

<style>

</style>