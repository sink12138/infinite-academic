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
      items: ['全部','主题','论文','期刊','机构','作者','精确(ISBN、ISSN、DOI、专利号)'],
      filter: '全部',
      text: null,
      filters:{},
      data:{},
    }
  },
  methods: {
    search() {
      this.$parent.echo();
      this.filters=this.$parent.filters;
      console.log("搜索过滤器");
      console.log(this.filters);
      var url="http://120.46.154.200:8091/apis/search"
      switch(this.filter){
        case "全部":url=url+"/smart";break;
        case "主题":url=url+"/smart";break;
        case "论文":url=url+"/paper";break;
        case "期刊":url=url+"/journal";break;
        case "机构":url=url+"/institution";break;
        case "作者":url=url+"/researcher";break;
        case "精确(ISBN、ISSN、DOI、专利号)":url=url+"/query";break;
      }
      this.$axios({
        method: "post",
        url: url,
        data: {
          
        }
      }).then(response => {
        console.log(response.data);
        this.data=response.data;
      }).catch(error => {
        console.log(error)
      })
      this.$notify({
        title: '搜索',
        message: this.filter+'\n'+this.text,
        type: 'success'
      });
      this.$parent.searchResult();
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