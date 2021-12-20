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
              <h2>机构学者:</h2>
              <v-row v-for="item in authors" :key="item.id">
                <v-col  offset-md="1"  cols="1">
                  <p>{{item.name}}</p>
                </v-col>
                <v-col cols="1">
                  <p>{{item.interests}}</p>
                </v-col>
                <v-col cols="1">
                  <p>{{item.paperNum}}</p>
                </v-col>
                <v-col cols="1">
                  <p>{{item.patentNum}}</p>
                </v-col>
              </v-row>
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
import PaperCard from '../components/card/CardPaper.vue'

export default {
  components: {
    Banner,
    BaseAnalytics,
    PaperCard,
  },
  data() {
    return {
      data:[3,2,4,3,2],
      id:"GF_4ynwBF-Mu8unTG1hc",
      name:"机构名",
      interests:"科研方向",
      paperNum:0,
      patentNum:0,
      logoUrl:"https://ma-v3-images.azureedge.net/images/149899117.png",
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
      authors:[
        {
          id:1,
          name:"a",
          interests:"math",
          paperNum:100,
          patentNum:99,
        },
        {
          id:2,
          name:"d",
          interests:"wath",
          paperNum:1000,
          patentNum:992,
        }
      ],
    }
  },
  methods:{
    getInfo(){
      this.$axios({
        method: "get",
        url: "api/search/info/institution/"+this.id,
        params: {
          id: this.id
        }
      }).then(response => {
        console.log(response.data)
        this.avatarUrl=response.data.avatarUrl
        this.name=response.data.name
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
        url: "/search/relation/publications/institution/"+this.id+"/"+page,
        params: {
          entity:"institution",
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
          url: "/search/relation/publications/institution/"+this.id+"/"+page,
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
    },
    getAuthors(){
      this.authors=[]
      let aAuthor=Object
      let hasMore=false
      let page=0
      this.$axios({
        method: "get",
        url: "/search/relation/scholars/"+this.id+"/"+page,
        params: {
          entity:"institution",
          id: this.id,
          page:page,
        }
      }).then(response => {
        console.log(response.data)
        aAuthor=response.data.item
        hasMore=response.data.hasMore
        this.papers.push(aAuthor)
      }).catch(error => {
        console.log(error)
      })
      while (hasMore) {
          this.$axios({
          method: "get",
          url: "/search/relation/scholars/"+this.id+"/"+page,
          params: {
            entity:"researcher",
            id: this.id,
            page:page,
          }
        }).then(response => {
          console.log(response.data)
          aAuthor=response.data.item
          hasMore=response.data.hasMore
          this.papers.push(aAuthor)
        }).catch(error => {
          console.log(error)
        })
      }
    }
  }
}
</script>

<style>

</style>
