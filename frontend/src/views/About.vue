<template>
  <div>
    <PaperCard 
      v-for="item in papers" 
      :key="item.id" 
      :item="item"
    ></PaperCard>
    <AuthorCard 
      v-for="item in authors" 
      :key="item.id" 
      :item="item"
    ></AuthorCard>
    <div class="d-flex">
      <InstitutionCard 
        v-for="item in institutions" 
        :key="item.id" 
        :item="item"
      ></InstitutionCard>
    </div>
    <div class="d-flex">
      <JournalCard 
        v-for="item in journals" 
        :key="item.id" 
        :item="item"
      ></JournalCard>
    </div>
  </div>
</template>

<script>
import PaperCard from "../components/card/CardPaper.vue";
import AuthorCard from "../components/card/CardAuthor.vue";
import InstitutionCard from "../components/card/CardInstitution.vue"
import JournalCard from "../components/card/CardJournal.vue"

export default {
  components: {
    PaperCard,
    AuthorCard,
    InstitutionCard,
    JournalCard,
  },
  data: () => ({
    papers: [
      {
        abstract:  "假装这是一大段摘要假装这是一大段假装这是一大段摘要假装这是一大段假装这是一大段摘要假装这是一大段假装这是一大段摘要假装这是一大段假装这是一大段摘要假装这是一大段假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要",
        authors: [
          {
            id: "GF_4ynwBF-Mu8unTG1hc",
            name: "谭火彬",
          },
        ],
        citationNum: 114,
        date: "2021-10-15",
        id: "GF_4ynwBF-Mu8unTG1hd",
        journal: {
          id: "GF_4ynwBF-Mu8unTG1hc",
          title: "Science",
        },
        keywords: ["dasd", "asdf", "agagd",],
        title: "基于机器学习的无需人工编制词典的切词系统",
        type: "期刊论文",
      }
    ],
    authors: [
      {
				"citationNum": 666,
				"id": "GF_4ynwBF-Mu8unTG1hc",
				"institution": {
					"id": "GF_4ynwBF-Mu8unTG1hc",
					"name": "北京航空航天大学软件学院"
				},
				"interests": ["智能化软件工程"],
				"name": "谭火彬",
				"paperNum": 114514,
				"patentNum": 1919810
			}
    ],
    institutions: [
      {
        "id": "GF_4ynwBF-Mu8unTG1hf",
				"name": "北京航空航天大学软件学院"
      },
      {
        "id": "GF_4ynwBF-Mu8unTG1hk",
				"name": "北京航空航天大学空天学院"
      }
    ],
    journals: [
      {
				"id": "GF_4ynwBF-Mu8unTG1hg",
				"sponsor": "华北计算技术研究所",
				"title": "计算机工程与应用"
			},
    ]
  }),
  methods: {
  },
};
</script>


<style>
</style>
