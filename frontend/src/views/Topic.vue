<template>
  <div class="chart">
    <Banner></Banner>
    <v-divider></v-divider>
    <span>{{this.name}}</span>
    <v-btn @click="push">push</v-btn>
    <div 
      id="publish"
      class="ma-auto"
      style="width: 600px;height:300px;"
    ></div>
    <div 
      id="association"
      class="ma-auto"
      style="width: 600px;height:600px;"
    ></div>
  </div>
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
      publishEchart: null,
      assEchart: null
    }
  },
  watch: {
    $route() {
      this.loadData();
    }
  },
  mounted() {
    this.init();
  },
  methods: {
    init() {
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
      this.myEcharts();
    },
    push() {
      this.$router.push({ path: 'topic', query: { name: 'hello'}})
    },
    myEcharts() {
      this.publishEchart = this.$echarts.init(document.getElementById('publish'), 'infinite', { renderer: 'svg' });
      this.assEchart = this.$echarts.init(document.getElementById('association'), 'infinite', { renderer: 'svg'});
      
      this.loadData();

      var _this = this;
      this.assEchart.on('click', function(param) {
        var target = param.data.name + 'hello';
        if (target != _this.name)
          _this.$router.push({ path: 'topic', query: { name: target}})
      })
    },
    loadData() {
      this.name = this.$route.query.name;
      var publishOption = {
        title: {
          text: '出版物数量/年'
        },
        xAxis: {
          data: ['2012','2013','2014']
        },
        yAxis: {},
        series: [
          {
            type: 'line',
            data: [100,500,2000]
          },
          {
            type: 'line',
            data: [500,30,200]
          },
          {
            type: 'line',
            data: [400,1000,600]
          }
        ]
      }
      var assOption = {
        title: {
          text: '关联关系'
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
      assOption.series.data.push(topic);
      this.associations.forEach(function(item) {
        var node = {}
        node.name = item.name
        assOption.series.data.push(node);
      })
      assOption.series.links = this.associations.map(function (data, idx) {
        var link = {}
        link.source = 0;
        link.target = (idx+1);
        link.value = data.confidence;
        return link;
      });
      
      this.publishEchart.setOption(publishOption);
      this.assEchart.setOption(assOption);
    }
  }
}
</script>

<style>

</style>