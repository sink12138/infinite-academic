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
              <h3>现工作单位:{{currentInst.name}}</h3>
              <h3 style="text-align:left;float:left">曾工作单位:</h3>
              <h3 style="text-align:left;float:left" v-for="i in institutions.length" :key="i">{{institutions[i-1].name}}&emsp;&emsp;</h3>
            </div>
          </v-col>
        </v-row>
      </div>
      <BaseAnalytics :data0=data></BaseAnalytics>
      <v-row
        v-for="i in onePageNum"
        :key="i"
        justify="center"
      >
        <v-col>
          <PaperCard :item="papers[i+(page-1)*onePageNum-1]" style="margin:0 auto;left:0;right:0;"></PaperCard>
        </v-col>
      </v-row>
      <v-row>
        <v-col>
          <v-pagination
            v-model="page"
            class="my-4"
            :length="8>pageNum?pageNum:8"
          ></v-pagination>
        </v-col>
      </v-row>
    </div>
  </div>
  
</template>

<script>
import Banner from '../components/BaseBanner.vue'
import BaseAnalytics from "../components/BaseAnalytics.vue";
import PaperCard from '../components/BasePaperCard.vue'
export default {
    name: "Income",
    components: {BaseAnalytics,Banner,PaperCard},
    props:{
      a:Number
    },
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
          page:1,
          onePageNum:1,
          pageNum:2,
          papers: [
            {
              "abstract": "假装这是一大段摘要",
              "authors": [
                {
                  "id": "GF_4ynwBF-Mu8unTG1hc",
                  "name": "谭火彬"
                }
              ],
              "citationNum": 114,
              "date": "2021-10-15",
              "id": "GF_4ynwBF-Mu8unTG1hc",
              "journal": {
                "id": "GF_4ynwBF-Mu8unTG1hc",
                "title": "Science"
              },
              "keywords": [12,23,34],
              "title": "基于机器学习的无需人工编制词典的切词系统",
              "type": "期刊论文"
            },
            {
              "abstract": "假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要",
              "authors": [
                {
                  "id": "GF_4ynwBF-Mu8unTG1hc",
                  "name": "谭火彬"
                }
              ],
              "citationNum": 114,
              "date": "2021-10-15",
              "doi":"1234123",
              institutions:[
                {
                  id:"in1",
                  name:"ins1"
                }
              ],
              publisher:"asda",
              referencePapers:[
                {
                  id:"1",
                  title:"rep1"
                }
              ],
              "subjects":["ma","ca","ph"],
              "id": "GF_4ynwBF-Mu8unTG1hd",
              "journal": {
                "id": "GF_4ynwBF-Mu8unTG1hc",
                "title": "Science"
              },
              "keywords": ["dasd","asdf","agagd","dasd"],
              "title": "基于机器学习的无需人工编制词典的切词系统",
              "type": "期刊论文"
            },
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
      },
      getPapers(){
        this.papers=[]
        let aPaper=Object
        let hasMore=false
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
          console.log(response.data)
          aPaper=response.data.item
          hasMore=response.data.hasMore
          this.papers.push(aPaper)
        }).catch(error => {
          console.log(error)
        })
        while (hasMore) {
            this.$axios({
            method: "get",
            url: "/search/relation/publications/researcher/"+this.id+"/"+page,
            params: {
              entity:"researcher",
              id: this.id,
              page:page,
            }
          }).then(response => {
            console.log(response.data)
            aPaper=response.data.item
            hasMore=response.data.hasMore
            this.papers.push(aPaper)
          }).catch(error => {
            console.log(error)
          })
        }
        this.pageNum=page+1
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