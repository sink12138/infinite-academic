<template>
  <v-btn 
    fab
    color="grey lighten-2"
    elevation="4"
    small
    @click="goBack()"
  >
    <v-icon>mdi-arrow-left</v-icon>
  </v-btn>
</template>

<script>
export default {
  methods: {
    goBack() {
      window.history.back();
    }
  }
}
</script>