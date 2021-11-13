<template>
  <div class="search_bar d-inline-flex">
    <v-select
      class="select"
      v-model="filter"
      :items="items"
      hide-details
      prepend-inner-icon="mdi-dots-grid"
      background-color="grey lighten-2"
      filled
    ></v-select>
    <v-divider
      vertical
    ></v-divider>
    <v-text-field
      class="input"
      v-model="text"
      clearable
      filled
      full-width
      background-color="grey lighten-5"
      counter="30"
      hide-details
      append-icon="mdi-magnify"
      @click:append="search"
    >
    </v-text-field>
  </div>
</template>

<script>
export default {
  data() {
    return {
      items: ['全部','主题','论文','期刊','机构','作者'],
      filter: '全部',
      text: null,
    }
  },
  methods: {
    search() {
      this.$notify({
        title: '搜索',
        message: this.filter+'\n'+this.text,
        type: 'success'
      });
    }
  }
}
</script>

<style scoped>
.search_bar {
  width: 66%;
}
.select {
  height: 40px;
  width: 12%;
}
.input {
  width: 88%;
}
</style>