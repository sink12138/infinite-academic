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
    >{{title.icon}}
    </v-icon>
    <div class="my-font">
      {{title.text}}
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
  },
  props: {
    title: {
      type: Object,
      default:() => {}
    }
  },
}
</script>

<style>

</style>