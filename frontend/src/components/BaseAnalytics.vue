<template>
  <div>
    <div @click="change()">
      <v-tabs v-model="type" :centered="true">
        <v-tab 
          v-for="i in types"
          :key="i"
          >
          {{choices[i-1]}}
      </v-tab>
    </v-tabs>
    </div>
      
    <div id="main" style="width: 800px;height:600px; margin:0 auto;"></div>
  </div>
 
</template>
 
<script>
export default {
  name: "BaseAnalytics",
  props:{
    data0:Array
  },
  data() {
    return{
      type:0,
      types:3,
      choices:['a','b','c','d'],
      dataX:[1,2,3,4,5],
      data1:[2,4,6,8,0],
      data2:[5,3,6,8,7],
      data3:[4,7,2,5,7],
      data:[],
      myChart:{},
      option:{},
    }
  },
  created() {
    setTimeout( ()=>{ //延时加载echarts初始化函数
    this.myEcharts()
    },100)
  },
  methods:{
    change(){
      switch (this.type) {
        case 0:
          this.data=this.data0
          break;
        case 1:
          this.data=this.data2
          break;
        case 2:
          this.data=this.data3
          break;
        default:
          break;
      }
      this.option.series=[{
        data:this.data,
        type:'line'
      }]
      this.myChart.setOption(this.option);
    },
    myEcharts(){
      // 基于准备好的dom，初始化echarts实例
      this.myChart = this.$echarts.init(document.getElementById('main'));
      // 指定图表的配置项和数据
      this.option = {
      xAxis: {
              type: 'category',
              data: this.dataX
      },
      yAxis: {
              type: 'value'
              },
      series: [{
              data: this.data0,
              type: 'line'
              }]
      };
      // 使用刚指定的配置项和数据显示图表。
      this.myChart.setOption(this.option);
    }
  }
}
</script>