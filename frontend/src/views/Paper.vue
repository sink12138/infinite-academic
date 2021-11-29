<template>
  <div>
    <BasePaper v-bind:paperdata="this.paperdata"></BasePaper>
    <BaseGoBack></BaseGoBack>
  </div>
</template>
<script>
import BaseGoBack from '../components/BaseGoBack.vue'
import BasePaper from '../components/BasePaper.vue'
export default {
  data(){
    return{
      paperdata:{}
    }
  },
  components: {
    BaseGoBack,BasePaper
  },
  mounted(){
    this.$axios.get('../../paper.json').then(res=>{
      this.paperdata=res.data.data;
    })
  }
}
</script>
<style scoped>
div{
  padding: 30px;
}
</style>