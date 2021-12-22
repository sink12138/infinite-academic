<template>
  <div>
    <Banner :title="{text: 'Institution', icon: 'mdi-home'}"></Banner>
    <div class="whole">
      <div class="message">
        <v-row>
          <v-col cols="3">
            <div class="roundIMG" style="float:left;">
              <img :src=logoUrl>
            </div>
          </v-col>
          <v-col>
            <div>
              <h1>
                {{name}}
              </h1>
            </div>
          </v-col>
        </v-row>
      </div>
      <v-container class="d-flex">
        <v-img
          id="institution"
          class="ma-auto"
          height="400"
          width="600"
        ></v-img>
        <v-img
          id="net"
          class="ma-auto"
          height="400"
          width="600"
        ></v-img>
      </v-container>
      <PaperTabs :styles="styles"></PaperTabs>
    </div>
  </div>
</template>

<script>
import Banner from '../components/BaseBanner.vue'
import { publishChart,relationshipNetworkChart } from '../components/mixins/mixin'
import PaperTabs from '../components/PaperTabs.vue'
export default {
  mixins: [publishChart,relationshipNetworkChart],
  components: {
    Banner,
    PaperTabs
  },
  data(){
    return{
      id:{},
      styles:"institutions",
      publications:{},
      scholars:{},
      logoUrl:"",
      name:"",
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
    this.getPublications()
    this.getScholars()
    this.initChart()
    this.initNetChart();
  },
  methods:{
    getInfo(){
      this.$axios({
        method: "get",
        url: "/api/search/info/institution/"+this.id,
      }).then(response => {
        if(!response.data.success){
          console.log(response.data.message)
        }else{
          console.log(response.data)
          this.logoUrl=response.data.data.logoUrl
          this.name=response.data.data.name
        }
      }).catch(error => {
        console.log(error)
      })
    },
    getPublications(){
        let page=0
        this.$axios({
          method: "get",
          url: "/api/search/relation/publications/institution/"+this.id+"/"+page,
        }).then(response => {
          console.log(response.data);
          if(!response.data.success){
            console.log(response.data.message);
          }else{
            this.publications=response.data.data
          }
        }).catch(error => {
          console.log(error)
        })
      },
    getScholars(){
        let page=0
        this.$axios({
          method: "get",
          url: "/api/search/relation/scholars/"+this.id+"/"+page,
        }).then(response => {
          console.log(response.data);
          if(!response.data.success){
            console.log(response.data.message);
          }else{
            this.scholars=response.data.data
          }
        }).catch(error => {
          console.log(error)
        })
      }
  }
}
</script>

<style>

</style>
