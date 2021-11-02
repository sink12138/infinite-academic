<template>
  <v-card class="d-inline-flex align-center">
    <go-back class="ml-6"></go-back>
    <div class="ma-auto d-inline-flex">
      <v-icon 
        class="ml-5 text-h2"
        color="indigo darken-4"
      >
        mdi-home
      </v-icon>
      <div class="myfont my-5">
        Institution
      </div>
    </div>
  </v-card>
</template>

<script>
import GoBack from '../components/GoBack.vue'

export default {
  components: { GoBack },
}
</script>

<style>
.myfont {
  font-family: Fira Code;
  font-size: 30px;
  font:800;
}
</style>