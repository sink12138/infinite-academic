<template>
  <base-citation></base-citation>
</template>

<script>
import BaseCitation from '../components/BaseCitation.vue';

export default {
  components: {
    BaseCitation
  },
}
</script>


<style scoped>
.v-card--reveal {
  bottom: 0;
  opacity: 1 !important;
  position: absolute;
  width: 100%;
}
</style>