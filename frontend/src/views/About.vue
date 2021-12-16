<template>
  <div>
    <PaperCard v-for="item in items" :key="item.id" :item="item"></PaperCard>
    <user-menu></user-menu>
    <div>
      <input
        type="text"
        placeholder="请输入客户信息"
        class="inputInfo"
        v-model="searchcursom"
        v-on:focus="focusCustomer()"
        v-on:blur="blurCustomer()"
      />
      <ul class="sel-ul customer-ht" v-show="showCustomer">
        <div v-show="suggest.length">
          <li
            v-for="(item, index) in suggest"
            :value="item"
            :key="index"
            @click="chooseCustomer(item)"
            v-html="item"
          ></li>
        </div>
        <div class="text-center" v-show="!suggest.length">暂无数据推荐</div>
      </ul>
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
    showCustomer: false,
    searchcursom: "",
    suggest: [],
  }),
  watch: {
    searchcursom: {
      handler: function () {
        this.focusCustomer();
      },
    },
  },

  mounted() {
    let that = this;
    document.addEventListener("click", function (e) {
      if (e.target.className != "inputInfo") {
        that.$nextTick(() => {
          that.showCustomer = false;
        });
      }
    });
  },
  methods: {
    chooseCustomer(item) {
      console.log(item);
      this.searchcursom = item.replace("<b>", "").replace("</b>", "");
    },
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
            text: this.searchcursom,
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
            }
          })
          .catch((error) => {
            console.log(error);
          });
      }, wait);
    },
    blurCustomer() {
      this.showCustomer = false;
    },
  },
};
</script>


<style>
</style>
