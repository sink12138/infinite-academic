<template>
  <div>
    <Banner :title="{text: 'Author', icon: 'mdi-clipboard-account'}"></Banner>
    <div class="whole">
      <div class="message">
        <v-row>
          <v-col cols="3">
            <div class="roundIMG" style="float:left;">
              <img :src=avatarUrl>
            </div>
          </v-col>
          <v-col>
            <div>
              <h1>
                {{name}}
              </h1>
              <h3>职位:{{position}}</h3>
              <h3>科研方向:{{interests}}</h3>
              <h3>邮箱:{{email}}</h3>
              <h3>g指数:{{gIndex}}  h指数:{{hIndex}}</h3>
              <h3>文章数量:{{paperNum}}  专利数量:{{patentNum}}  引用次数:{{citationNum}}</h3>
              <h3>邮箱:{{email}}</h3>
            </div>
          </v-col>
        </v-row>
      </div>
      <BaseAnalytics :data0=data></BaseAnalytics>
    </div>
  </div>
  
</template>

<script>
import Banner from '../components/BaseBanner.vue'
import BaseAnalytics from "../components/BaseAnalytics.vue";
export default {
    name: "Income",
    components: {BaseAnalytics,Banner},
    data() {
        return {
          data:[3,2,4,3,2],
          id:"GF_4ynwBF-Mu8unTG1hc",
          name:"姓名",
          position:"职位",
          interests:"科研方向",
          email:"1234@buaa.edu.cn",
          gIndex:0,
          hIndex:0,
          paperNum:0,
          patentNum:0,
          avatarUrl:"https://ma-v3-images.azureedge.net/images/161269817.jpeg",
          citationNum:0,
          currentInst:{
            id:"000000",
            name:"机构名"
          },
          institutions:[
            {
              id:"000000",
              name:"机构名"
            }
          ],
        }
    },
    mounted(){
      // this.getInfo()
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