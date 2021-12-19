<template>
  <div>
    <Banner :title="{text: 'Journal', icon: 'mdi-newspaper-variant'}"></Banner>
    <div class="whole">
      <div class="message">
        <v-row>
          <v-col cols="3">
            <div class="roundIMG" style="float:left;">
              <img :src=coverUrl>
            </div>
          </v-col>
          <v-col>
            <div>
              <h1>
                {{title}}
              </h1>
              <h3>主办单位:{{sponsor}}</h3>
              <h3>ISSN:{{issn}}</h3>
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
  components: {
    Banner,
    BaseAnalytics,
    PaperCard,
  },
  data() {
    return {
      data:[3,2,4,3,2],
      id:"GF_4ynwBF-Mu8unTG1hc",
      title:"标题",
      interests:"科研方向",
      sponsor:"**大学",
      issn:"1234",
      paperNum:0,
      patentNum:0,
      coverUrl:"https://ma-v3-images.azureedge.net/images/149899117.png",
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
}
</script>

<style>

</style>
