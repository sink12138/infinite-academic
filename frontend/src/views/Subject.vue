<template>
  <v-main>
    <Banner :title="{text: 'Subject', icon: 'mdi-flask-empty'}"></Banner>
    <v-row no-gutters>
      <v-col cols="4">
        <v-card outlined>
          <div class="my-3 my-font text-no-wrap">
            <v-icon
              class="text-h4"
              color="indigo darken-4"
            >mdi-flask-empty
            </v-icon>
            {{this.name}}
          </div>

          <v-img
            id="publish"
            class="ma-auto"
            height="400"
            aspect-ratio="16/9"
          ></v-img>

          <v-card-text v-if="this.heat != null">
            <v-icon>mdi-fire-circle</v-icon>
            热度: {{this.heat.toFixed(2)}}
          </v-card-text>
        </v-card>
      </v-col>
      <v-col cols="8">
        <v-card 
          color="light-blue lighten-5"
          height="100%"
          outlined
        >
          
          <v-tabs
            v-model="tab"
            background-color="light-blue lighten-5"
            color="teal darken-4"
            grow  
          >
            <v-tab class="font-weight-bold">
              出版物
            </v-tab>
            <v-tab class="font-weight-bold">
              相关学科
            </v-tab>
            <v-tab class="font-weight-bold">
              学者
            </v-tab>
            <v-tab class="font-weight-bold">
              期刊
            </v-tab>
            <v-tab class="font-weight-bold">
              机构
            </v-tab>
          </v-tabs>

          <div v-if="tab == 0">
            <CardPaper
              v-for="item in items"
              :key="item.id"
              :item="item"
            ></CardPaper>
          </div>

          <v-card-actions>

            <v-img
              id="analysis"
              v-if="tab != 0"
              class="ma-auto"
              height="500"
              aspect-ratio="16/9"
            ></v-img>

            <v-btn-toggle
              v-if="tab > 1"
              v-model="chartType"
              tile
              borderless
              mandatory
            >
              <v-btn>
                <v-icon color="blue accent-4">mdi-chart-pie</v-icon>
              </v-btn>
              <v-btn>
                <v-icon color="blue accent-4">mdi-chart-bar</v-icon>
              </v-btn>
            </v-btn-toggle>

          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>
  </v-main>
</template>

<script>
import {getData, getChart} from "../components/mixins/mixin"
import Banner from "../components/BaseBanner.vue"
import CardPaper from "../components/card/CardPaper.vue"
export default {
  components: {
    Banner,
    CardPaper
  },
  mixins: [
    getData, 
    getChart,
  ],
  data() {
    return {
      chart1: null,
      chart2: null,
      tab: null,
      chartType: 0,
      items: {}
    }
  },
  watch: {
    $route() {
      this.tab = 0;
      this.loadData();
      this.chartReload();
    },
    tab() {
      if (this.tab != 0) {
        if (this.chart2 == null) 
          this.$nextTick(() => { 
            this.initChart2();
            this.reload2();
          })
        else {
          this.reload2();
        }
      } else {
        if (this.chart2 != null) {
          this.chart2.dispose();
          this.chart2 = null;
        }
      }
    },
    chartType() {
      if (this.tab != 0)
        this.reload2();
    }
  },
  mounted() {
    this.init();
  },
  methods: {
    init() {
      this.loadData();
      this.initChart1();
    },
    loadData() {
      this.getBasic();
      this.getPapers();
      this.getResearcher();
      this.getJournal();
      this.getInstitution();
    },
  }
}
</script>

<style scoped>
.v-btn-toggle {
  flex-direction: column;
}
</style>