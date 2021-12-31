<template>
  <v-main>
    <Banner :title="{ text: '机构', icon: 'mdi-home' }"></Banner>
    <v-card outlined>
      <v-container class="d-flex justify-center align-center">
        <v-container class="text-right">
          <v-img v-if="this.logoUrl" class="roundIMG" :src="this.logoUrl" />
          <v-icon v-else size="150">mdi-domain</v-icon>
        </v-container>
        <v-container class="text-left">
          <v-card-text class="text-h3">{{this.name}}</v-card-text>
        </v-container>
      </v-container>
    </v-card>
    <v-container class="d-flex">
      <v-img id="institution" class="ma-auto" height="400" width="600"></v-img>
      <v-img id="net" class="ma-auto" height="400" width="600"></v-img>
    </v-container>
    <PaperTabs :styles="styles"></PaperTabs>
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
  components: {
    Banner,
    PaperTabs,
  },
  data() {
    return {
      id: {},
      styles: "institutions",
      logoUrl: "",
      name: "",
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
    getInfo() {
      this.$axios({
        method: "get",
        url: "/api/search/info/institution/" + this.id,
      })
        .then((response) => {
          if (!response.data.success) {
            console.log(response.data.message);
          } else {
            console.log(response.data);
            this.logoUrl = response.data.data.logoUrl;
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
</style>
