<template>
  <v-app-bar
    dark
    dense
    elevation="0"
    style="position: sticky; top: 0; z-index: 999"
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
  </v-app-bar>
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