<template>
  <v-main>
    <Banner :title="{text: 'Patent', icon: 'mdi-book-open-blank-variant'}"></Banner>
    <BasePatent v-bind:paperdata="this.patentdata"></BasePatent>
    <PatentTabs :styles="styles"></PatentTabs>
  </v-main>
</template>

<script>
import Banner from '../components/BaseBanner.vue'
import BasePatent from '../components/BasePatent.vue'
import PatentTabs from '../components/PatentTabs.vue'
export default {
  components: {
    Banner,BasePatent,PatentTabs
  },
  data(){
    return{
      id:{},
      patentdata: {},
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
    this.$axios.get('/api/search/info/patent/'+this.id).then(res=>{
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
