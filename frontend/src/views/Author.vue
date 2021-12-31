<template>
  <v-main>
    <Banner :title="{ text: '科研人员', icon: 'mdi-clipboard-account' }"></Banner>
    <v-card outlined>
      <v-container class="d-flex justify-center">
        <v-container class="text-right">
          <v-img
            v-if="authorData.avatarUrl"
            class="roundIMG"
            :src="authorData.avatarUrl"
          />
          <v-icon v-else size="150">mdi-account-box</v-icon>
        </v-container>
        <v-container class="text-left">
          <v-card-text class="text-h3"> {{ authorData.name }}</v-card-text>
          <v-card-text v-if="authorData.paperNum" class="py-0"
            >文章数量:<b>{{ authorData.paperNum }}</b></v-card-text
          >
          <v-card-text v-if="authorData.patentNum" class="py-0"
            >专利数量:<b>{{ authorData.patentNum }}</b></v-card-text
          >
          <v-card-text v-if="authorData.citationNum" class="py-0"
            >引用次数:<b>{{ authorData.citationNum }}</b></v-card-text
          >
          <v-card-text v-if="authorData.gIndex || authorData.hIndex">
            <span v-if="authorData.gIndex"
              >G指数:<b>{{ authorData.gIndex }}</b
              >&nbsp;</span
            >
            <span v-if="authorData.hIndex"
              >H指数:<b>{{ authorData.hIndex }}</b></span
            >
          </v-card-text>
          <v-card-text v-if="authorData.currentInst" class="pt-0">
            <span>现工作单位:&nbsp;</span>
            <b
              class="link"
              @click="href('institution', authorData.currentInst.id)"
              >{{ authorData.currentInst.name }}</b
            >
          </v-card-text>
          <v-card-text v-if="authorData.institutions" class="pt-0">
            <span>有合作关系的机构:&nbsp;</span>
            <b
              v-for="item in authorData.institutions"
              :key="item.name"
              @click="href('institution', item.id)"
            >
              {{ item.name }}&nbsp;
            </b>
          </v-card-text>
        </v-container>
      </v-container>
    </v-card>
    <v-container class="d-flex">
      <v-img id="author" class="ma-auto" height="400" width="600"></v-img>
      <v-img id="net" class="ma-auto" height="400" width="600"></v-img>
    </v-container>
    <v-container>
      <PaperTabs :styles="styles"></PaperTabs>
    </v-container>
  </v-main>
</template>

<script>
import Banner from "../components/BaseBanner.vue";
import {
  publishChart,
  relationshipNetworkChart,
} from "../components/mixins/mixin";
import PaperTabs from "../components/PaperTabs.vue";
export default {
  mixins: [publishChart, relationshipNetworkChart],
  components: { Banner, PaperTabs },
  data() {
    return {
      id: null,
      name: null,
      styles: "author",
      authorData: {},
    };
  },
  watch: {
    $route() {
      this.$router.go(0);
    },
  },
  mounted() {
    this.id = this.$route.query.id;
    this.getInfo();
    this.initChart();
    this.initNetChart();
  },
  methods: {
    href(type, id) {
      if (id == null) {
        this.$notify({
          title: "数据缺失",
          message: "信息暂未收录，给您带来不便敬请谅解。",
          type: "warning",
        });
        return;
      }
      this.$router.push({
        path: type,
        query: { id: id },
      });
    },
    getInfo() {
      this.$axios({
        method: "get",
        url: "/api/search/info/researcher/" + this.id,
      })
        .then((response) => {
          if (!response.data.success) {
            console.log(response.data.message);
          } else {
            this.authorData = response.data.data;
            this.name = response.data.data.name;
          }
        })
        .catch((error) => {
          console.log(error);
        });
    },
  },
};
</script>

<style>
.roundIMG {
  width: 200px;
  height: 200px;
  display: flex;
  border-radius: 10%;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  float: left;
}
.center {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100%;
}
</style>