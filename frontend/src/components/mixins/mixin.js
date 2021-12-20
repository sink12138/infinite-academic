export const addCitation = {
  methods: {
    addCitationItem(item) {
      var citationList = JSON.parse(localStorage.getItem("citations"));
      console.log(citationList);
      if (citationList == null) citationList = {};
      var citation = {
        paperId: item.id,
        MLA: {},
      };
      citation["MLA"] = this.MLACitation(item);
      citationList[item.id] = citation;
      localStorage.setItem("citations", JSON.stringify(citationList));
      this.$store.commit('incCitations');
    },
    MLACitation(item) {
      var italicLeft = "<span style='font-style:italic'>"
      var italicRight = "</span>"
      var text = item.authors[0].name + ", et al.";
      text += "\"" + item.title + ".\"";
      text += italicLeft + item.journal.title + italicRight;
      text += ", vol. " + item.journal.volume;
      text += ", no. " + item.journal.issue;
      text += ", " + item.year;
      text += ", pp. " + item.journal.startPage + "-" + item.journal.endPage + ".";
      return text;
    }
  },
}

export const getChart = {
  methods: {
    initChart1() {
      this.chart1 = this.$echarts.init(document.getElementById('publish'), 'infinite', { renderer: 'svg' });
      this.reload1();
      window.addEventListener('resize', () => { this.chart1.resize(); })
    },
    initChart2() {
      this.chart2 = this.$echarts.init(document.getElementById('analysis'), 'infinite', { renderer: 'svg'});
      var _this = this;
      this.chart2.on('click', function(param) {
        var target = param.data.name + 'hello';
        if (target != _this.name)
          _this.$router.push({ path: this.type, query: { name: target}})
      })
      window.addEventListener('resize', () => { this.chart2.resize(); })
    },
    chartReload() {
      this.reload1();
      this.reload2();
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
    reload2() {
      this.$nextTick(() => { 
        this.chart2.resize({
          animation: {
            duration: 400,
          }
        });
      })
      switch (this.tab) {
        case 1:
          this.loadRelated();
          break;
        case 2:
          this.loadRank();
          this.loadAuthor();
          break;
        case 3:
          this.loadRank();
          this.loadJournal();
          break;
        case 4:
          this.loadRank();
          this.loadInstitution();
          break;
        default:
          this.chart2.resize();
      }
    },
    loadRelated() {
      this.chart2.clear();
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
    loadRank() {
      if (this.chartType == 0) this.loadPie();
      else this.loadBar();
    },
    loadPie() {
      this.chart2.clear();
      var option = {
        title: {
          text: null,
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
          data: [],
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
    loadBar() {
      this.chart2.clear();
      var option = {
        title: {
          text: null,
          left: 'center'
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          }
        },
        colorBy: 'data',
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'value',
          boundaryGap: [0, 0.01]
        },
        yAxis: {
          type: 'category',
          data: []
        },
        series: {
          type: 'bar',
          data: [],
        }
      }
      this.chart2.setOption(option);
    },
    loadAuthor() {
      if (this.chartType == 0) {
        this.chart2.setOption({
          title: {
            text: '学者发文数'
          },
          series: {
            data: this.authors.map(function(item) {
              var obj = {
                value: item.frequency,
                name: item.term,
                url: item.id
              }
              return obj;
            })
          }
        })
      } else {
        this.chart2.setOption({
          title: {
            text: '学者发文数'
          },
          yAxis: {
            data: this.authors.map(function(item) {
              return item.term;
            })
          },
          series: {
            data: this.authors.map(function(item) {
              var obj = {
                value: item.frequency,
                name: item.term,
                url: item.id
              }
              return obj;
            })
          }
        })
      }
    },
    loadJournal() {
      if (this.chartType == 0) {
        this.chart2.setOption({
          title: {
            text: '期刊发文数'
          },
          yAxis: null,
          series: {
            data: this.journals.map(function(item) {
              var obj = {
                value: item.frequency,
                name: item.term,
                url: item.id
              }
              return obj;
            })
          }
        })
      } else {
        this.chart2.setOption({
          title: {
            text: '期刊发文数'
          },
          yAxis: {
            data: this.journals.map(function(item) {
              return item.term;
            })
          },
          series: {
            data: this.journals.map(function(item) {
              var obj = {
                value: item.frequency,
                name: item.term,
                url: item.id
              }
              return obj;
            })
          }
        })
      }
        
    },
    loadInstitution() {
      if (this.chartType == 0) {
        this.chart2.setOption({
          title: {
            text: '机构发文数'
          },
          series: {
            data: this.institutions.map(function(item) {
              var obj = {
                value: item.frequency,
                name: item.term,
                url: item.id
              }
              return obj;
            })
          }
        })
      } else {
        this.chart2.setOption({
          title: {
            text: '机构发文数'
          },
          yAxis: {
            data: this.institutions.map(function(item) {
              return item.term;
            })
          },
          series: {
            data: this.institutions.map(function(item) {
              var obj = {
                value: item.frequency,
                name: item.term,
                url: item.id
              }
              return obj;
            })
          }
        })
      }
    },
  }
}

export const getData = {
  data() {
    return {
      name: null,
      type: null,
      heat: null,
      num: 10,
      associations: [
        {
          "confidence": 0.88,
          "name": "C"
        },
        {
          "confidence": 0.9,
          "name": "Java"
        },
        {
          "confidence": 0.75,
          "name": "Go"
        },
        {
          "confidence": 0.66,
          "name": "Python"
        },
        {
          "confidence": 0.98,
          "name": "C++"
        },
        {
          "confidence": 0.92,
          "name": "C#"
        },
      ],
      pubsPerYear: [],
      authors: [
        { frequency: 1050, term: 'Search Engine', id: 'G1' },
        { frequency: 735, term: 'Direct', id: 'G2' },
        { frequency: 580, term: 'Email', id: 'G3' },
        { frequency: 484, term: 'Union Ads', id: 'G4' },
        { frequency: 300, term: 'Video Ads', id: 'G5' }
      ],
      journals: [
        { frequency: 21252, term: 'Social Science Research Network', id: 'G1' },
        { frequency: 12300, term: 'Nature', id: 'G2' },
        { frequency: 9457, term: 'PLOS ONE', id: 'G3' },
        { frequency: 4585, term: 'Science', id: 'G4' },
        { frequency: 2678, term: 'Physical Review Letters', id: 'G5' }
      ],
      institutions: [
        { frequency: 57894, term: 'Harvard University', id: 'G1' },
        { frequency: 35667, term: 'Chinese Academy of Sciences', id: 'G2' },
        { frequency: 23543, term: 'Stanford University', id: 'G3' },
        { frequency: 21235, term: 'Max Planck Society', id: 'G4' },
        { frequency: 15789, term: 'University of Michigan', id: 'G5' }
      ]
    }
  },
  methods: {
    getBasic() {
      this.name = this.$route.query.name;
      this.type = this.$route.path.substring(1);
      /*if (this.type == "topic")
        this.getTopic();
      else
        this.getSubject();*/
    },
    getTopic() {
      this.$axios({
        method: "get",
        url: "/api/analysis/topic",
        params: {
          name: this.name
        }
      }).then(response => {
        if (response.data.success == true) {
          this.heat = response.data.heat;
          this.pubsPerYear = response.data.pubsPerYear;
          this.associations = response.data.associationTopics;
        } else {
          console.log(response.data.message);
        }
      }).catch(error => {
        console.log(error)
      })
    },
    getSubject() {
      this.$axios({
        method: "get",
        url: "/api/analysis/subject",
        params: {
          name: this.name
        }
      }).then(response => {
        if (response.data.success == true) {
          this.heat = response.data.heat;
          this.pubsPerYear = response.data.pubsPerYear;
          this.associations = response.data.associationSubjects;
        } else {
          console.log(response.data.message);
        }
      }).catch(error => {
        console.log(error)
      })
    },
    getResearcher() {
      this.$axios({
        method: "get",
        url: "/api/analysis/participants/researcher",
        params: {
          name: this.name,
          num: this.num,
          type: this.type,
        }
      }).then(response => {
        if (response.data.success == true) {
          this.authors = response.data.data;
        } else {
          console.log(response.data.message);
        }
      }).catch(error => {
        console.log(error)
      })
    },
    getJournal() {
      this.$axios({
        method: "get",
        url: "/api/analysis/participants/journal",
        params: {
          name: this.name,
          num: this.num,
          type: this.type,
        }
      }).then(response => {
        if (response.data.success == true) {
          this.journals = response.data.data;
        } else {
          console.log(response.data.message);
        }
      }).catch(error => {
        console.log(error)
      })
    },
    getInstitution() {
      this.$axios({
        method: "get",
        url: "/api/analysis/participants/institution",
        params: {
          name: this.name,
          num: this.num,
          type: this.type,
        }
      }).then(response => {
        if (response.data.success == true) {
          this.institutions = response.data.data;
        } else {
          console.log(response.data.message);
        }
      }).catch(error => {
        console.log(error)
      })
    }
  }
}

export const publishChart = {
  data:() => ({
    chart: null,
    nums: [],
    years: []
  }),
  methods: {
    initChart() {
      var id = this.$route.path.substring(1);
      this.chart = this.$echarts.init(document.getElementById(id), 'infinite', { renderer: 'svg' });
      this.reload();
      window.addEventListener('resize', () => { this.chart.resize(); })
    },
    reload() {
      this.getData();
      var option = {
        title: {
          text: '发文趋势'
        },
        xAxis: {
          data: []
        },
        yAxis: {},
        series: [
          {
            name: '发文量',
            type: 'line',
            areaStyle: {
              color: '#9fe6f3de'
            },
            smooth: 0.3,
            data: []
          },
        ]
      }
      this.chart.setOption(option);
    },
    getData() {
      var id = this.$route.query.id;
      var entity = this.$route.path.substring(1);
      if (entity == "author") entity = "researcher";
      this.$axios({
        method: "get",
        url: "/api/analysis/statistics/"+entity+"/"+id,
      }).then(response => {
        if (response.data.success == true) {
          this.chart.setOption({
            xAxis: {
              data: response.data.data.years
            },
            series: [
              {
                name: '发文量',
                data: response.data.data.nums
              }
            ]
          })
        } else {
          console.log(response.data.message);
        }
      }).catch(error => {
        console.log(error)
      })
    }
  }
}