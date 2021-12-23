<template>
  <v-main>
    <Banner :title="{text: 'Paper', icon: 'mdi-book-open-blank-variant'}"></Banner>
    <BasePaper v-bind:paperdata="this.paperdata"></BasePaper>
    <PaperTabs :styles="styles"></PaperTabs>
  </v-main>
</template>

<script>
import Banner from '../components/BaseBanner.vue'
import BasePaper from '../components/BasePaper.vue'
import PaperTabs from '../components/PaperTabs.vue'
export default {
  components: {
    Banner,BasePaper,PaperTabs
  },
  data(){
    return{
      id:{},
      paperdata: {},
      styles:'paper'
    }
  },
  watch: {
    $route() {
      this.$router.go(0);
    },
  },
  mounted() {
    this.id = this.$route.query.id
    console.log(this.id)
    this.$axios.get('/api/search/info/paper/'+this.id).then(res=>{
      if(!res.data.success){
        console.log(res.data.message);
      }else{
        this.paperdata=res.data.data;
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