<template>
  <div>
    <v-card class="betop">
      <BaseGoBack class="ml-6" style="float:left"></BaseGoBack>
      <div class="ma-auto d-inline-flex">
        <v-icon 
          class="ml-5 text-h2"
          color="indigo darken-4"
        >mdi-home
        </v-icon>
        <div class="my-font my-5">
          Author
        </div>
      </div>
    </v-card>
    <div class="whole">
      <div class="message">
        <h2>
          {{name}}
        </h2>
        <h3>职位:{{position}}</h3>
        <h3>科研方向:{{interests}}</h3>
        <h3>邮箱:{{email}}</h3>
        <h3>g指数:{{gIndex}}  h指数:{{hIndex}}</h3>
        <h3>文章数量:{{paperNum}}  专利数量:{{patentNum}}</h3>
        <h3>邮箱:{{email}}</h3>
      </div>
      <BaseAnalytics :data0=data></BaseAnalytics>
    </div>
  </div>
  
</template>

<script>
import BaseGoBack from '../components/BaseGoBack.vue';
import BaseAnalytics from "../components/BaseAnalytics.vue";
export default {
    name: "Income",
    components: {BaseAnalytics,BaseGoBack},
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
      this.getInfo()
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
</style>