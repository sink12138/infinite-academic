<template>
  <v-card
    class="mx-auto"
    max-width="80%"
  >
  <BasePaperCard :item="this.paperdata" style="margin:0 auto;left:0;right:0;width:100%;"></BasePaperCard>
    <v-tabs v-model="tab">
      <v-tab key="citations">引证文献</v-tab>
      <v-tab key="references">参考文献</v-tab>
    </v-tabs>
    <v-tabs-items v-model="tab">
        <v-tab-item key="citations">
          <v-card-text v-for="item in citations.items" v-bind:key="item.index">
              <BasePaperCard :item="item" style="margin:0 auto;left:0;right:0;width:100%;"></BasePaperCard>
          </v-card-text>
        </v-tab-item>
        <v-tab-item key="references">
          <v-card-text v-for="item in references.items" v-bind:key="item.index">
            <BasePaperCard :item="item" style="margin:0 auto;left:0;right:0;width:100%;"></BasePaperCard>
          </v-card-text>
        </v-tab-item>
    </v-tabs-items>
  </v-card>
</template>
<script>
import BasePaperCard from './card/CardPaper.vue'
export default{
  components: {
    BasePaperCard
  },
  props:{
    paperdata: {
      type: Object,
      default:() => {}
    },
    references:{
      type:Object,
      default:()=>{}
    },
    citations:{
      type:Object,
      default:()=>{}
    }
  },
  data(){
    return{
      tab:null
    }
  },
  mounted() {

  }
}
</script>
<style scoped>
a{
  text-decoration: none;
}
</style>