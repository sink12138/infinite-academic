<template>
  <v-card
    outlined
  >
    <v-card-title>
      <v-icon large>mdi-calendar-range</v-icon>
      时间选择
      <v-spacer></v-spacer>
      <v-btn
        @click="expand = !expand"
      >
        {{year[0]}}-{{year[1]}}
      </v-btn>
    </v-card-title>
    
    <v-expand-x-transition>
      <v-range-slider
        class="mt-6 px-4"
        v-show="expand"
        :max="end"
        :min="start"
        v-model="year"
        thumb-label="always"
      ></v-range-slider>
    </v-expand-x-transition>
  </v-card>
</template>

<script>
export default {
  data:() => ({
    expand: false,
    start: 1900,
    end: 2021,
    year: [2019,2020]
  })
}
</script>

<style>

</style>