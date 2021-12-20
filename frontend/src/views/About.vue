<template>
  <div>
    <PaperCard v-for="item in items" :key="item.id" :item="item"></PaperCard>
    <user-menu></user-menu>
    <div>
      <input
        v-model="text"
        type="text"
        list="suggestList"
        @change="getSuggest()"
      />
      <datalist id="suggestList">
        <option v-for="(item, index) in suggest" :key="index">
          <span v-html="item"></span>
        </option>
      </datalist>
    </div>
    <div>
      
    </div>
  </div>
</template>

<script>
import MessageDialog from "../components/MessageDialog.vue";

export default {
  components: {
    MessageDialog
  },
  data: () => ({
    test: 
    {
      basic: {
        "email": "114514@qq.com",
        "fileToken": "B0KAR4A8Q3BHHHPMF0TT0I0B6YQMBAPPPHNPMIYM6GJMDZF4EQB7CLGOCP8S211R",
        "id": "BcYuon0BGFAD_tUkFP7g",
        "status": "审核中",
        "time": "2021-08-17 19:26",
        "type": "专利转让",
        "userId": "3cYPmX0BGFAD_tUkT_03",
        "websiteLink": ""
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
    filter: "论文",
    text: "",
    suggest: [],
    count: 0,
  }),
  methods: {
    getSuggest() {
      var url = "";
      switch (this.filter) {
        case "全部":
        case "论文":
          url = "/api/search/suggest/paper";
          break;
        case "期刊": {
          url = "/api/search/suggest/paper";
          break;
        }
        case "专利": {
          url = "/api/search/suggest/paper";
          break;
        }
        case "机构": {
          url = "/api/search/suggest/paper";
          break;
        }
        default:
          break;
      }
      if (url != "") {
        this.$axios({
          method: "post",
          url: url,
          params: {
            text: this.text,
          },
        })
          .then((response) => {
            this.suggest = [];
            console.log(response);
            if (response.data.success == true) {
              console.log(response.data.data);
              if (response.data.data.correction.length != 0)
                this.suggest = this.suggest.concat(
                  response.data.data.correction
                );
              if (response.data.data.completion.length != 0)
                this.suggest = this.suggest.concat(
                  response.data.data.completion
                );
              // for (var i = 0; i < this.suggest.length; i++) {
              //   this.suggest[i] = this.suggest[i]
              //     .replaceAll("<b>", "")
              //     .replaceAll("</b>", "");
              // }
            } else {
              this.suggest = [];
            }
          })
          .catch((error) => {
            console.log(error);
          });
      }
    },
  },
};
</script>


<style>
</style>
