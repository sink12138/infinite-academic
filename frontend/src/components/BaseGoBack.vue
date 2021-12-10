<template>
  <v-btn
    small
    height="100%"
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