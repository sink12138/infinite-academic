<template>
  <v-btn
    class="ml-1"
    small
    dark
    outlined
    fab
    elevation="0"
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