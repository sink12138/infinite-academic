<template>
  <div>
    <PaperCard v-for="item in items" :key="item.id" :item="item"></PaperCard>
    <user-menu></user-menu>
    <div>
      <v-combobox
          label="Combobox"
          :value="text"
          :items="suggest"
          :search-input.sync="text"
          @update:search-input="focusCustomer"
          outlined
        ></v-combobox>
    </div>
  </div>
</template>

<script>
import PaperCard from "../components/BasePaperCard.vue";

export default {
  components: {
    PaperCard,
  },
  data: () => ({
    items: [
      {
        abstract: "假装这是一大段摘要",
        authors: [
          {
            id: "GF_4ynwBF-Mu8unTG1hc",
            name: "谭火彬",
          },
        ],
        citationNum: 114,
        date: "2021-10-15",
        id: "GF_4ynwBF-Mu8unTG1hc",
        journal: {
          id: "GF_4ynwBF-Mu8unTG1hc",
          title: "Science",
        },
        keywords: [12, 23, 34],
        title: "基于机器学习的无需人工编制词典的切词系统",
        type: "期刊论文",
      },
      {
        abstract:
          "假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要假装这是一大段摘要",
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
        keywords: ["dasd", "asdf", "agagd", "dasd"],
        title: "基于机器学习的无需人工编制词典的切词系统",
        type: "期刊论文",
      },
    ],
    text: '',
    suggest: [],
    count: 0
  }),
  methods: {
    focusCustomer() {
      if (document.querySelector("input") == document.activeElement) {
        this._debounce(500);
      }
      this.showCustomer = true;
    },
    // 函数防抖
    _debounce(wait) {
      clearTimeout(this.timer);
      this.timer = setTimeout(() => {
        this.$axios({
          method: "post",
          url: "/api/search/suggest/paper",
          params: {
            text: this.text,
          },
        })
          .then((response) => {
            this.suggest=[];
            console.log("suggest");
            console.log(response);
            if (response.data.success == true) {
              console.log(response.data.data);
              if (response.data.data.correction.length != 0)
                this.suggest=this.suggest.concat(response.data.data.correction);
              if (response.data.data.completion.length != 0)
                this.suggest=this.suggest.concat(response.data.data.completion);
              console.log(this.suggest);
              for(var i=0;i<this.suggest.length;i++){
                this.suggest[i]=this.suggest[i].replace("<b>","").replace("</b>","");
              }
            }
          })
          .catch((error) => {
            console.log(error);
          });
      }, wait);
    },
  },
};
</script>


<style>
</style>
