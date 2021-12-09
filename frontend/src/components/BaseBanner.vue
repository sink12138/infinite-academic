<template>
  <v-toolbar
    dark
    dense
    elevation="0"
  >
    <BaseGoBack></BaseGoBack>
    <v-spacer></v-spacer>
    <v-icon 
      class="text-h4"
    >mdi-tooltip-check
    </v-icon>
    <div class="my-font">
      Topic
    </div>
    <v-spacer></v-spacer>
    <BaseCitation></BaseCitation>
  </v-toolbar>
</template>

<script>
import BaseGoBack from "../components/BaseGoBack.vue"
import BaseCitation from "../components/BaseCitation.vue";
export default {
  components: {
    BaseCitation,
    BaseGoBack
  }
}
</script>

<style>

</style>