<template>
  <div>
    <h1>Search</h1>
    <go-back></go-back>
    <div>
        <filter></filter>
    </div>
  </div>
</template>

<script>
import GoBack from '../components/GoBack.vue'
import Filter from '../components/Filter.vue'

export default {
  components: { GoBack,Filter },
}
</script>

<style>

</style>