<template>
  <div>
    <Banner :title="{ text: 'Author', icon: 'mdi-clipboard-account' }"></Banner>
    <div class="whole">
      <div class="message">
        <v-row>
          <v-col cols="3">
            <div class="roundIMG" style="float: left">
              <img :src="authorData.avatarUrl" />
            </div>
          </v-col>
          <v-col>
            <div>
              <h1>
                {{ authorData.name }}
              </h1>
              <h3>
                g指数:{{ authorData.gIndex }} h指数:{{ authorData.hIndex }}
              </h3>
              <h3>
                文章数量:{{ authorData.paperNum }} 专利数量:{{
                  authorData.patentNum
                }}
                引用次数:{{ authorData.citationNum }}
              </h3>
              <h3 v-if="authorData.currentInst">
                现工作单位:{{ authorData.currentInst.name }}
              </h3>
              <h3
                v-if="authorData.institutions"
                style="text-align: left; float: left"
              >
                曾工作单位:
              </h3>
              <h3 v-if="authorData.institutions">
                <h3
                  style="text-align: left; float: left"
                  v-for="i in authorData.institutions.length"
                  :key="i"
                >
                  {{ authorData.institutions[i - 1].name }}&emsp;&emsp;
                </h3>
              </h3>
            </div>
          </v-col>
        </v-row>
      </div>
      <v-container class="d-flex">
        <v-img id="author" class="ma-auto" height="400" width="600"></v-img>
        <v-img id="net" class="ma-auto" height="400" width="600"></v-img>
      </v-container>
      <PaperTabs
        :styles="styles"
      ></PaperTabs>
    </div>
  </div>
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
      publications: {},
      patents: {},
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
    this.getPublications();
    this.getPatents();
    this.initChart();
    this.initNetChart();
  },
  methods: {
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
    getPublications() {
      let page = 0;
      this.$axios({
        method: "get",
        url:
          "/api/search/relation/publications/researcher/" +
          this.id +
          "/" +
          page,
      })
        .then((response) => {
          console.log(response.data);
          if (!response.data.success) {
            console.log(response.data.message);
          } else {
            this.publications = response.data.data;
          }
        })
        .catch((error) => {
          console.log(error);
        });
    },
    getPatents() {
      let page = 0;
      this.$axios({
        method: "get",
        url: "/api/search/relation/inventions/" + this.id + "/" + page,
      })
        .then((response) => {
          console.log(response.data);
          if (!response.data.success) {
            console.log(response.data.message);
          } else {
            this.patents = response.data.data;
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
}
</style>