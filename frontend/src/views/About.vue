<template>
  <div>
    <PaperCard 
      v-for="item in items"
      :key="item.id"
      :item="item"
    ></PaperCard>
    <user-menu></user-menu>
  </div>
</template>

<script>
import PaperCard from '../components/BasePaperCard.vue'
import UserMenu from '../components/UserMenu.vue'

export default {
  components: {
    UserMenu,
    PaperCard
  },
  data:() =>  ({
    "items": [
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
  })
}

</script>


<style>

</style>