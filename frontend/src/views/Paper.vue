<template>
  <v-main>
    <Banner :title="{text: 'Paper', icon: 'mdi-book-open-blank-variant'}"></Banner>
    <BasePaper v-bind:paperdata="this.paperdata"></BasePaper>
  </v-main>
</template>

<script>
import Banner from '../components/BaseBanner.vue'
import BasePaper from '../components/BasePaper.vue'
export default {
  components: {
    Banner,BasePaper
  },
  data(){
    return{
      paperdata: {}
    }
  },
  mounted() {
    this.$axios.get('../../paper.json').then(res=>{
      this.paperdata=res.data.data;
    })
  },
}
</script>
<style scoped>
div{
  padding: 30px;
}
</style>