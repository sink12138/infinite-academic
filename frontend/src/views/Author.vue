<template>
  <div>
    <Banner :title="{text: 'Author', icon: 'mdi-clipboard-account'}"></Banner>
    <div class="whole">
      <div class="message">
        <v-row>
          <v-col cols="3">
            <div class="roundIMG" style="float:left;">
              <img :src=this.authorData.avatarUrl>
            </div>
          </v-col>
          <v-col>
            <div>
              <h1>
                {{this.authorData.name}}
              </h1>
              <h3>g指数:{{this.authorData.gIndex}}  h指数:{{this.authorData.hIndex}}</h3>
              <h3>文章数量:{{this.authorData.paperNum}}  专利数量:{{this.authorData.patentNum}}  引用次数:{{this.authorData.citationNum}}</h3>
              <h3>现工作单位:{{this.authorData.currentInst.name}}</h3>
              <h3 style="text-align:left;float:left">曾工作单位:</h3>
              <h3 style="text-align:left;float:left" v-for="i in this.authorData.institutions.length" :key="i">{{this.authorData.institutions[i-1].name}}&emsp;&emsp;</h3>
            </div>
          </v-col>
        </v-row>
      </div>
      <v-img
        id="author"
        class="ma-auto"
        height="400"
        aspect-ratio="16/9"
      ></v-img>
      <PaperTabs :styles="styles" :publications="this.publications" :patents="this.patents"></PaperTabs>
    </div>
  </div>

</template>

<script>
import Banner from '../components/BaseBanner.vue'
import { publishChart } from '../components/mixins/mixin'
import PaperTabs from '../components/PaperTabs.vue'
export default {
    name: "Income",
    id:{},
    mixins: [publishChart],
    components: {Banner,PaperTabs},
    props:{
      a:Number
    },
    data() {
      return {
        styles:"author",
        authorData:{},
        publications:{},
        patents:{}
      }
    },
    mounted(){
      this.id = this.$route.query.id
      this.getInfo();
      this.getPublications()
      this.initChart();
    },
    methods:{
      getInfo(){
        this.$axios({
          method: "get",
          url: "api/search/info/researcher/"+this.id,
          params: {
            id: this.id
          }
        }).then(response => {
          console.log(response.data)
          this.authorData=response.data.data
          this.avatarUrl=response.data.avatarUrl
          this.citationNum=response.data.citationNum
          this.name=response.data.name
          this.position=response.data.position
          this.interests=response.data.interests
          this.email=response.data.email
          this.hIndex=response.data.gIndex
          this.paperNum=response.data.paperNum
          this.patentNum=response.data.patentNum
          this.currentInst=response.data.currentInst
        }).catch(error => {
          console.log(error)
        })
      },
      getPublications(){
        let page=0
        this.$axios({
          method: "get",
          url: "/search/relation/publications/researcher/"+this.id+"/"+page,
          params: {
            entity:"researcher",
            id: this.id,
            page:page,
          }
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
      getPatents(){
        let page=0
        this.$axios({
          method: "get",
          url: "api/search/relation/inventions/"+this.id+"/"+page,
          params: {
            id: this.id,
            page:page,
          }
        }).then(response => {
          console.log(response.data);
          if(!response.data.success){
            console.log(response.data.message);
          }else{
            this.patents=response.data.data
          }
        }).catch(error => {
          console.log(error)
        })
      }
    }
}

</script>

<style>
.roundIMG{
  width:200px;
  height:200px;
  display: flex;
  border-radius: 10%;
  align-items: center;
  justify-content: center;
  overflow:hidden;
}
</style>