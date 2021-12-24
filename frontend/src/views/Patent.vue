<template>
  <v-main>
    <Banner :title="{text: '专利', icon: 'mdi-book-open-blank-variant'}"></Banner>
    <BasePatent v-bind:patentData="this.patentData"></BasePatent>
  </v-main>
</template>

<script>
import Banner from '../components/BaseBanner.vue'
import BasePatent from '../components/BasePatent.vue'
export default {
  components: {
    Banner,BasePatent
  },
  data(){
    return{
      id:{},
      patentData: {},
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
        this.patentData=res.data.data;
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
