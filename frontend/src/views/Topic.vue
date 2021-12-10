<template>
  <v-main>
    <Banner></Banner>
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
            height="400"
            aspect-ratio="16/9"
          ></v-img>
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
          
          <v-img
            id="analysis"
            v-if="tab != 0"
            class="ma-auto"
            height="500"
            aspect-ratio="16/9"
          ></v-img>
        </v-card>
      </v-col>
    </v-row>
  </v-main>
</template>

<script>
import Banner from "../components/BaseBanner.vue"
export default {
  components: {
    Banner
  },
  data() {
    return {
      name: null,
      associations: [
        {
          "confidence": 0.98,
          "name": "人工智能"
        },
        {
          "confidence": 1,
          "name": "物理"
        },
        {
          "confidence": 0.88,
          "name": "量子物理"
        },
        {
          "confidence": 0.78,
          "name": "宇宙学"
        },
        {
          "confidence": 0.95,
          "name": "白洞"
        }
      ],
      heat: null,
      pubsPerYear: [],
      chart1: null,
      chart2: null,
      tab: null,
    }
  },
  watch: {
    $route() {
      this.loadData();
      this.chartReload();
    },
    tab() {
      if (this.tab != 0) {
        console.log(this.tab);
        if (this.chart2 == null) 
          this.$nextTick(() => { 
            this.initChart2();
            this.reload2();
          })
        else {
          this.reload2();
          this.chart2.resize();
        }
      } else {
        if (this.chart2 != null) {
          this.chart2.dispose();
          this.chart2 = null;
        }
      }
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
      this.name = this.$route.query.name;
      /*this.$axios({
        method: "get",
        url: "/api/analysis/topic",
        params: {
          name: this.name
        }
      }).then(response => {
        console.log(response.data)
        this.heat = response.data.heat;
        this.pubsPerYear = response.data.pubsPerYear;
        this.associations = response.data.associationTopics;
      }).catch(error => {
        console.log(error)
      })*/
    },
    initChart1() {
      this.chart1 = this.$echarts.init(document.getElementById('publish'), 'infinite', { renderer: 'svg' });
      this.reload1();
      window.addEventListener('resize', () => { this.chart1.resize(); })
    },
    reload1() {
      var option = {
        title: {
          text: '发文趋势'
        },
        xAxis: {
          data: ['2012','2013','2014']
        },
        yAxis: {},
        series: [
          {
            type: 'line',
            areaStyle: {
              color: '#9fe6f3de'
            },
            smooth: 0.3,
            data: [100,500,400]
          },
        ]
      }
      this.chart1.setOption(option);
    },
    initChart2() {
      this.chart2 = this.$echarts.init(document.getElementById('analysis'), 'infinite', { renderer: 'svg'});
      var _this = this;
      this.chart2.on('click', function(param) {
        var target = param.data.name + 'hello';
        if (target != _this.name)
          _this.$router.push({ path: 'topic', query: { name: target}})
      })
      window.addEventListener('resize', () => { this.chart2.resize(); })
    },
    reload2() {
      switch (this.tab) {
        case 1:
          this.loadRelated();
          break;
        case 2:
          this.loadAuthor();
          break;
        case 3:
          this.loadJournal();
          break;
        case 4:
          this.loadInstitution();
          break;
        default:
          this.chart2.resize();
      }
    },
    loadRelated() {
      console.log('hello')
      var option = {
        title: {
          text: '关联关系',
          left: 'center'
        },
        series: {
          type: 'graph',
          layout: 'force',
          force: {
            repulsion: 500,
            gravity: 0.1,
          },
          label: {
            show: true,
            fontSize: 18
          },
          edgeLabel: {
            show: true,
            position: 'middle',
            formatter: '相关度:{c}'
          },
          data: [],
          links: [],
        }
      }
      var topic = {
        name: this.name,
        symbolSize: 120
      }
      option.series.data.push(topic);
      this.associations.forEach(function(item) {
        var node = {}
        node.name = item.name
        option.series.data.push(node);
      })
      option.series.links = this.associations.map(function (data, idx) {
        var link = {}
        link.source = 0;
        link.target = (idx+1);
        link.value = data.confidence;
        return link;
      });
      
      this.chart2.setOption(option);
    },
    loadAuthor() {
      var option = {
        title: {
          text: '学者发文数',
          left: 'center'
        },
        tooltip: {
          trigger: 'item'
        },
        legend: {
          orient: 'vertical',
          left: 'left'
        },
        series: {
          type: 'pie',
          radius: '80%',
          data: [
            { value: 1048, name: 'Search Engine' },
            { value: 735, name: 'Direct' },
            { value: 580, name: 'Email' },
            { value: 484, name: 'Union Ads' },
            { value: 300, name: 'Video Ads' }
          ],
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }
      }
      this.chart2.setOption(option);
    },
    loadJournal() {
      var option = {
        title: {
          text: '期刊发文数',
          left: 'center'
        },
        tooltip: {
          trigger: 'item'
        },
        legend: {
          orient: 'vertical',
          left: 'left'
        },
        series: {
          type: 'pie',
          radius: '80%',
          data: [
            { value: 1048, name: 'Search Engine' },
            { value: 735, name: 'Direct' },
            { value: 580, name: 'Email' },
            { value: 484, name: 'Union Ads' },
            { value: 300, name: 'Video Ads' }
          ],
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }
      }
      this.chart2.setOption(option);
    },
    loadInstitution() {
      var option = {
        title: {
          text: '机构发文数',
          left: 'center'
        },
        tooltip: {
          trigger: 'item'
        },
        legend: {
          orient: 'vertical',
          left: 'left'
        },
        series: {
          type: 'pie',
          radius: '80%',
          data: [
            { value: 1048, name: 'Search Engine' },
            { value: 735, name: 'Direct' },
            { value: 580, name: 'Email' },
            { value: 484, name: 'Union Ads' },
            { value: 300, name: 'Video Ads' }
          ],
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }
      }
      this.chart2.setOption(option);
    },
    chartReload() {
      this.reload1();
      this.reload2();
    }
  }
}
</script>

<style>
.class {
  color: #9fe6f3de;
}
</style>