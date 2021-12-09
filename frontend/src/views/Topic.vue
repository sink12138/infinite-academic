<template>
  <div class="chart">
    <p>topic</p>
    <div 
      id="publish"
      class="ma-auto"
      style="width: 600px;height:300px;"
    ></div>
    <div 
      id="association"
      class="ma-auto"
      style="width: 400px;height:400px;"
    ></div>
  </div>
</template>

<script>
export default {
  data:()=> ({
    associations: [
      {
				"confidence": 0.98,
				"name": "人工智能"
			},
      {
				"confidence": 0.99,
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
    /*let name = this.$route.query.name;
    this.$axios({
      method: "get",
      url: "/api/analysis/topic",
      params: {
        name: name
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

      var associationEchart = this.$echarts.init(document.getElementById('association'), null, { renderer: 'svg'});
      var associationOption = {
        title: {
          text: '关联关系'
        },
        series: {
          type: 'graph',
          layout: 'force',
          force: {
            repulsion: 100,
            gravity: 0.1,
            edgeLength: 100
          },
          symbolSize: 30,
          label: {
            show: true
          },
          data: this.associations.map(function (node) {
            node.symbolSize = node.confidence*50;
            return node;
          }),
          links: this.associations.map(function (node, idx) {
            var link = {}
            link.source = 0;
            link.target = idx;
            link.value = node.confidence;
            return link;
          }),
          lineStyle: {
            width: 4,
            curveness: 0
          }
        }
      }
      associationEchart.setOption(associationOption);
    }
  }
}
</script>

<style>

</style>