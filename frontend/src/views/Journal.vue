<template>
  <div>
    <Banner :title="{text: 'Journal', icon: 'mdi-newspaper-variant'}"></Banner>
    <div class="whole">
      <div class="message">
        <v-row>
          <v-col cols="3">
            <div class="roundIMG" style="float:left;">
              <img :src=journalData.coverUrl>
            </div>
          </v-col>
          <v-col>
            <div>
              <h1>
                {{journalData.title}}
              </h1>
              <h3 v-if="journalData.sponsor">主办单位:{{journalData.sponsor}}</h3>
              <h3 v-if="journalData.issn">ISSN:{{journalData.issn}}</h3>
            </div>
          </v-col>
        </v-row>
      </div>
      <v-img
        id="journal"
        class="ma-auto"
        height="400"
        aspect-ratio="16/9"
      ></v-img>
      <PaperTabs :styles="styles"></PaperTabs>
    </div>
  </div>
</template>

<script>
import Banner from '../components/BaseBanner.vue'
import { publishChart } from '../components/mixins/mixin'
import PaperTabs from '../components/PaperTabs.vue';
export default {
  mixins: [publishChart],
  components: {
    Banner,
    PaperTabs
  },
  data() {
    return{
      id:{},
      journalData:{},
      styles:"journals",
      page:0
    }
  },
  watch: {
    $route() {
      this.$router.go(0);
    },
  },
  mounted() {
    this.id = this.$route.query.id
    this.getInfo()
    this.initChart();
  },
  methods:{
    getInfo(){
      this.$axios({
        method: "get",
        url: "/api/search/info/journal/"+this.id,
      }).then(response => {
        console.log(response.data)
        this.journalData=response.data.data
      }).catch(error => {
        console.log(error)
      })
    }
  }
}
</script>

<style>

</style>
