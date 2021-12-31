<template>
  <div>
    <Banner
      :title="{ text: '期刊', icon: 'mdi-newspaper-variant' }"
    ></Banner>
    <v-card outlined>
      <v-container class="d-flex justify-center">
        <v-container class="text-right">
        <v-icon size="150">mdi-newspaper-variant-outline</v-icon>
        </v-container>
        <v-container class="text-left">
          <v-card-text class="text-h4">{{journalData.title}}</v-card-text>
          <v-card-text v-if="journalData.sponsor" class="py-0"
            >主办单位:<b>{{ journalData.sponsor }}</b></v-card-text
          >
          <v-card-text v-if="journalData.issn" class="py-0"
            >ISSN:<b>{{ journalData.issn }}</b></v-card-text
          >
        </v-container>
      </v-container>
    </v-card>

    <v-img
      id="journal"
      class="ma-auto"
      height="400"
      aspect-ratio="16/9"
    ></v-img>
    <PaperTabs :styles="styles"></PaperTabs>
  </div>
</template>

<script>
import Banner from "../components/BaseBanner.vue";
import { publishChart } from "../components/mixins/mixin";
import PaperTabs from "../components/PaperTabs.vue";
export default {
  mixins: [publishChart],
  components: {
    Banner,
    PaperTabs,
  },
  data() {
    return {
      id: {},
      journalData: {},
      styles: "journals",
      page: 0,
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
  },
  methods: {
    getInfo() {
      this.$axios({
        method: "get",
        url: "/api/search/info/journal/" + this.id,
      })
        .then((response) => {
          console.log(response.data);
          this.journalData = response.data.data;
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
