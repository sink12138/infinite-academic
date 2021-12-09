<template>
  <div class="chart">
    <Banner></Banner>
    <v-divider></v-divider>
    <div 
      id="publish"
      class="ma-auto"
      style="width: 600px;height:300px;"
    ></div>
    <div 
      id="association"
      class="ma-auto"
      style="width: 400px;height:300px;"
    ></div>
  </div>
</template>

<script>
import Banner from "../components/BaseBanner.vue"
export default {
  components: {
    Banner
  },
  data:()=> ({
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
				"name": "黑洞"
			}
    ],
    heat: null,
    pubsPerYear: [],
  }),
  mounted() {
    /*this.name = this.$route.query.name;
    this.$axios({
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
  methods: {
    myEcharts() {
      var publishEchart = this.$echarts.init(document.getElementById('publish'), null, { renderer: 'svg' });
      var publishOption = {
        title: {
          text: '出版物数量/年'
        },
        xAxis: {
          data: ['2012','2013','2014']
        },
        yAxis: {},
        series: {
          type: 'bar',
          data: [100,500,2000]
        }
      }
      publishEchart.setOption(publishOption);

      var assEchart = this.$echarts.init(document.getElementById('association'), null, { renderer: 'svg'});
      var assOption = {
        title: {
          text: '关联关系'
        },
        series: {
          type: 'graph',
          layout: 'force',
          force: {
            repulsion: 150,
            gravity: 0.1,
            edgeLength: [60, 80]
          },
          symbolSize: 30,
          label: {
            show: true
          },
          data: [],
          links: [],
          lineStyle: {
            width: 2,
            curveness: 0
          }
        }
      }
      var topic = {
        name: this.name,
        symbolSize: 60
      }
      assOption.series.data.push(topic);
      this.associations.forEach(function(item) {
        var node = {}
        node.name = item.name
        node.symbolSize = 50;
        assOption.series.data.push(node);
      })
      assOption.series.links = this.associations.map(function (data, idx) {
        var link = {}
        link.source = 0;
        link.target = (idx+1);
        link.value = data.confidence;
        return link;
      });
      assEchart.setOption(assOption);
    }
  }
}
</script>

<style>

</style>