<template>
  <div>
    <h1>Search</h1>
    <BaseGoBack></BaseGoBack>
    <div>
      <BaseFilter></BaseFilter>
    </div>
  </div>
</template>

<script>
import BaseFilter from '../components/BaseFilter.vue'
import BaseGoBack from '../components/BaseGoBack.vue'

export default {
  components: {
    BaseGoBack,
    BaseFilter 
  },
}
</script>

<style>

</style>