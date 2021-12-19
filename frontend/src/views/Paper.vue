<template>
  <v-main>
    <Banner :title="{text: 'Paper', icon: 'mdi-book-open-blank-variant'}"></Banner>
    <BasePaper v-bind:paperdata="this.paperdata" v-bind:references="this.references" :citations="this.citations"></BasePaper>
  </v-main>
</template>

<script>
import Banner from '../components/BaseBanner.vue'
import BasePaper from '../components/BasePaper.vue'
export default {
  components: {
    Banner,BasePaper
  },
  props:{
    id:{}
  },
  data(){
    return{
      paperdata: {},
      references:{},
      citations:{},
    }
  },
  mounted() {
    //本地测试
    // this.$axios.get('../../paper.json').then(res=>{
    //   this.paperdata=res.data.data;
    // }),
    //与后端对接
    this.$axios.get('api/search/info/paper/'+this.id).then(res=>{
      if(!res.data.success){
        alert(res.data.message);
      }else{
        this.paperdata=res.data.data;
      }
    })
    //本地
    // this.$axios.get('../../references.json').then(res=>{
    //   this.references=res.data.data;
    // }),
    //后端对接
    this.$axios.get('/api/search/relation/references/'+this.paperdata.id+'/'+0).then(res=>{
     if(!res.data.success){
        alert(res.data.message);
      }else{
        this.references=res.data.data;
       }
    }),
    //本地
    // this.$axios.get('../../citations.json').then(res=>{
    //   this.citations=res.data.data;
    // })
    //后端对接
    this.$axios.get('/api/search/relation/citations/'+this.paperdata.id+'/'+0).then(res=>{
     if(!res.data.success){
        alert(res.data.message);
      }else{
        this.citations=res.data.data;
       }
    })
  },
}
</script>
<style scoped>
div{
  padding: 30px;
}
</style>