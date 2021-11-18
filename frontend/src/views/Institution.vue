<template>
  <v-card class="d-inline-flex align-center">
    <BaseGoBack class="ml-6"></BaseGoBack>
    <div class="ma-auto d-inline-flex">
      <v-icon 
        class="ml-5 text-h2"
        color="indigo darken-4"
      >
        mdi-home
      </v-icon>
      <div class="my-font my-5">
        Institution
      </div>
    </div>
  </v-card>
</template>

<script>
import BaseGoBack from '../components/BaseGoBack.vue'

export default {
  components: {
    BaseGoBack,
  },
}
</script>

<style>
</style>
