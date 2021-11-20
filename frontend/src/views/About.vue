<template>
  <div>
    <base-navigation></base-navigation>
    <v-btn @click="addCitationItem(data1)">Data1</v-btn>
    <v-btn @click="addCitationItem(data2)">Data2</v-btn>
    <v-btn @click="addCitationItem(data3)">Data3</v-btn>
  </div>
</template>

<script>
import {addCitation} from '../components/mixins/mixin'
import BaseNavigation from '../components/BaseNavigation.vue'

export default {
  mixins: [addCitation],
  components: {
    BaseNavigation
  },
  data:() =>  ({
    data1: {
      "abstract": "假装这是一大段摘要",
      "authors": [
        {
          "id": "GF_4ynwBF-Mu8unTG1hc",
          "instOrders": [],
          "name": "谭火彬"
        }
      ],
      "citationNum": 114,
      "date": "2021-10-15",
      "doi": "10.1007/BF02943174",
      "id": "GF_4ynwBF-Mu8unTG1hc",
      "institutions": [
        {
          "id": "GF_4ynwBF-Mu8unTG1hc",
          "name": "北京航空航天大学软件学院"
        }
      ],
      "isbn": "ISBN 978-7-302-12260-9",
      "issn": "ISSN 1607-5161",
      "journal": {
        "endPage": 514,
        "id": "GF_4ynwBF-Mu8unTG1hc",
        "issue": "02",
        "startPage": 114,
        "title": "Science",
        "volume": 43
      },
      "keywords": [],
      "publisher": "Elsevier",
      "sources": [
        {
          "url": "http://www.researchgate.net/publication/273232093_The_Characteristics_of_Organizational_Environments_and_Perceived_Environmental_Uncertainty",
          "website": "ResearchGate"
        }
      ],
      "subjects": [],
      "title": "基于机器学习的无需人工编制词典的切词系统",
      "topics": [],
      "type": "期刊论文",
      "year": 2021
    },
    data2: {
      "abstract": "假装这是一大段摘要",
      "authors": [
        {
          "id": "GF_4ynwBF-Mu8unTG1hd",
          "instOrders": [],
          "name": "谭水彬"
        }
      ],
      "citationNum": 114,
      "date": "2021-10-15",
      "doi": "10.1007/BF02943174",
      "id": "GF_4ynwBF-Mu8unTG1hbbb",
      "institutions": [
        {
          "id": "GF_4ynwBF-Mu8unTG1hc",
          "name": "北京航空航天大学软件学院"
        }
      ],
      "isbn": "ISBN 978-7-302-12260-9",
      "issn": "ISSN 1607-5161",
      "journal": {
        "endPage": 4514,
        "id": "GF_4ynwBF-Mu8unTG1hc",
        "issue": "02",
        "startPage": 11,
        "title": "Science",
        "volume": 43
      },
      "keywords": [],
      "publisher": "Elsevier",
      "sources": [
        {
          "url": "http://www.researchgate.net/publication/273232093_The_Characteristics_of_Organizational_Environments_and_Perceived_Environmental_Uncertainty",
          "website": "ResearchGate"
        }
      ],
      "subjects": [],
      "title": "基于人工学习的无需机器编制词典的切词系统",
      "topics": [],
      "type": "期刊论文",
      "year": 1921
    },
    data3: {
      "abstract": "假装这是一大段摘要",
      "authors": [
        {
          "id": "GF_4ynwBF-Mu8unTG1hc",
          "instOrders": [],
          "name": "谭草彬"
        }
      ],
      "citationNum": 114,
      "date": "2021-10-15",
      "doi": "10.1007/BF02943174",
      "id": "GF_4ynwBF-Mu8unTG1hdd",
      "institutions": [
        {
          "id": "GF_4ynwBF-Mu8unTG1hc",
          "name": "北京航空航天大学软件学院"
        }
      ],
      "isbn": "ISBN 978-7-302-12260-9",
      "issn": "ISSN 1607-5161",
      "journal": {
        "endPage": 114,
        "id": "GF_4ynwBF-Mu8unTG1hc",
        "issue": "02",
        "startPage": 514,
        "title": "Science",
        "volume": 43
      },
      "keywords": [],
      "publisher": "Elsevier",
      "sources": [
        {
          "url": "http://www.researchgate.net/publication/273232093_The_Characteristics_of_Organizational_Environments_and_Perceived_Environmental_Uncertainty",
          "website": "ResearchGate"
        }
      ],
      "subjects": [],
      "title": "基于无需人工编制词典的机器学习的系统切词",
      "topics": [],
      "type": "期刊论文",
      "year": 2021
    },
  }),
}

</script>


<style>

</style>