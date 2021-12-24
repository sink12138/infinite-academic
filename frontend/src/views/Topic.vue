<template>
  <v-main>
    <Banner :title="{text: 'Topic', icon: 'mdi-message'}"></Banner>
    <v-row no-gutters>
      <v-col cols="4">
        <v-card outlined>
          <div class="my-3 my-font text-no-wrap">
            <v-icon
              class="text-h4"
              color="indigo darken-4"
            >mdi-message
            </v-icon>
            {{this.name}}
          </div>

          <v-img
            id="publish"
            class="ma-auto"
            height="500"
            aspect-ratio="16/9"
          ></v-img>

          <v-card-text v-if="this.heat != null">
            <v-icon>mdi-fire-circle</v-icon>
            热度: {{this.heat.toFixed(2)}}
          </v-card-text>

          <v-card-actions v-if="tab == 0">
            <v-spacer></v-spacer>
            <v-btn outlined @click="changePage(0)">
              <v-icon>mdi-arrow-left-bold-outline</v-icon>
            </v-btn>
            <v-btn outlined>{{this.page}}</v-btn>
            <v-btn outlined @click="changePage(1)">
              <v-icon>mdi-arrow-right-bold-outline</v-icon>
            </v-btn>
            <v-spacer></v-spacer>
          </v-card-actions>
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
              相关话题
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
            <PaperCard
              v-for="item in sliceList(items, 3)[this.page-1]"
              :key="item.id"
              :item="item"
            ></PaperCard>
          </div>

          <v-card-actions v-if="tab != 0">
            <v-img
              id="analysis"
              v-if="tab != 0"
              class="ma-auto"
              height="500"
              aspect-ratio="16/9"
            ></v-img>
            

            <v-btn-toggle
              style="flex-direction: column;"
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
import PaperCard from "../components/card/CardPaper.vue"
export default {
  components: {
    Banner,
    PaperCard
  },
  mixins: [
    getData, 
    getChart
  ],
  data() {
    return {
      chart1: null,
      chart2: null,
      tab: null,
      chartType: 0,
      items: [],
      page: 1,
      currentPage: 0,
      total: 0
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
    },
    currentPage() {
      this.getPapers(this.currentPage);
    }
  },
  computed: {
    sliceList() {
      return function (data, count) {
        if (data != undefined) {
          let index = 0;
          let arrTemp = [];
          for (let i = 0; i < data.length; i++) {
            index = parseInt(i / count);
            if (arrTemp.length <= index) {
              arrTemp.push([]);
            }
            arrTemp[index].push(data[i]);
          }
          return arrTemp;
        }
      };
    },
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
      this.getPapers(0);
      this.getResearcher();
      this.getJournal();
      this.getInstitution();
    },
    changePage(f) {
      if (f == 1) {
        if (this.page*3 > this.total) return;
        this.page++;
      }
      else {
        if (this.page == 1) return;
        this.page--;
      }
      if (Math.floor((this.page-1) / 4) > this.currentPage)
        this.currentPage = Math.floor((this.page-1) / 4)
    }
  }
}
</script>

<style>
</style>