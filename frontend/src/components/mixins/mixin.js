export const addCitation = {
  methods: {
    addCitationItem(item) {
      this.$axios.get('/api/search/info/paper/'+item.id).then(res=>{
        if(res.data.success) {
          item = res.data.data
          var citationList = JSON.parse(localStorage.getItem("citations"));
          if (citationList == null) citationList = {};
          var citation = {
            paperId: item.id,
            MLA: {},
            APA: {},
            GBT: {},
          };
          citation["MLA"] = this.MLACitation(item);
          citation["APA"] = this.APACitation(item);
          citation["GBT"] = this.GBTCitation(item);
          citationList[item.id] = citation;
          localStorage.setItem("citations", JSON.stringify(citationList));
          this.$store.commit('incCitations');
          this.$notify({
            title: '引用成功',
            type: 'success'
          });
        } else {
          console.log(res.data.message);
        }
      })
    },
    MLACitation(item) {
      /*var italicLeft = "<span style='font-style:italic'>"
      var italicRight = "</span>"*/
      var text = item.authors[0].name + ", et al.";
      text += "\"" + item.title + ".\"";
      if (item.journal != null) {
        text += item.journal.title;
        if (item.journal.volume != null)
          text += ", vol. " + item.journal.volume;
        if (item.journal.issue != null)
          text += ", no. " + item.journal.issue;
        if (item.year != null)
          text += ", " + item.year;
        if (item.journal.startPage != null)
          text += ", pp. " + item.journal.startPage + "-" + item.journal.endPage + ".";
        else
          text += "."
      }
      return text;
    },
    APACitation(item) {
      /* 作者 */
      var text = item.authors[0].name;
      if (item.authors.length > 2) {
        text += " et al. ";
      }
      else if (item.authors.length == 2) {
        text += " and " + item.authors[1].name + " ";
      }
      else {
        text += " ";
      }

      /* 出版年份 */
      text += item.year + " ";

      /* 文章标题 */
      text += item.title + " ";

      if (item.journal != null) {
        /* 期刊标题(应当为斜体) */
        text += item.journal.title + " ";

        /* 卷 + 期数 + 页码*/
        text += item.journal.volume + "(" + item.journal.issue + ")" + " n.pag ";
      }
      
      /* DOI */
      text += "doi:" + item.doi

      return text;
    },
    GBTCitation(item) {
      /* 序号 + 作者 */
      var text = "[序号] " + item.authors[0].name;
      if (item.authors.length > 3) {
        text += " , " + item.authors[1].name + ", " + item.authors[2].name + ", 等. ";
      }
      else if (item.authors.length == 3) {
        text += " , " + item.authors[1].name + ", " + item.authors[2].name + ". ";
      }
      else if (item.authors.length == 2) {
        text += " , " + item.authors[1].name + ". ";
      }
      else {
        text += " ";
      }

      /* 题名 */
      text += item.title + "[J]. ";

      if (item.journal != null) {
        /* 期刊名 */
        text += item.journal.title + ", ";

        /* 年 + 卷 + 期数 + 页码*/
        text += item.year + ", " + item.journal.volume + "(" + item.journal.issue + ")" + ": " + item.journal.startPage + "-" + item.journal.endPage + ".";
      }

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
        if (param.data.name == null && param.data.url == null) return;
        var target = ""
        console.log(param.data.type)
        if (param.data.type == "related") {
          target = param.data.name
          if (target != _this.name)
            _this.$router.push({ path: this.type, query: { name: target}})
        } else {
          var path = param.data.type
          target = param.data.url;
          _this.$router.push({ path: path, query: { id: target}})
        }
      })
      window.addEventListener('resize', () => { this.chart2.resize(); })
    },
    chartReload() {
      if (this.chart1 != null)
        this.reload1();
      if (this.chart2 != null)
        this.reload2();
    },
    reload1() {
      var option = {
        title: {
          text: '发文趋势'
        },
        xAxis: {
          data: []
        },
        yAxis: {},
        tooltip: {
          trigger: 'item'
        },
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
      this.chart1.setOption(option);
    },
    reload2() {
      this.$nextTick(() => { 
        if (this.chart2 != null)
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
          left: 'left'
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
            fontSize: 14
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
        type: "related",
        symbolSize: 120
      }
      console.log(this.associations)
      option.series.data.push(topic);
      this.associations.forEach(function(item) {
        var node = {}
        node.name = item.name
        node.type = "related"
        option.series.data.push(node);
      })
      option.series.links = this.associations.map(function (data, idx) {
        var link = {}
        link.source = 0;
        link.target = (idx+1);
        link.value = data.confidence.toFixed(3);
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
                url: item.id,
                type : "author"
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
                url: item.id,
                type : "author"
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
                url: item.id,
                type : "journal"
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
                url: item.id,
                type : "journal"
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
                url: item.id,
                type : "institution"
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
                url: item.id,
                type : "institution"
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
      associations: [],
      pubsPerYear: [],
      authors: [],
      journals: [],
      institutions: []
    }
  },
  methods: {
    getBasic() {
      this.name = this.$route.query.name;
      this.type = this.$route.path.substring(1);
      if (this.type == "topic")
        this.getTopic();
      else
        this.getSubject();
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
          this.heat = response.data.data.heat;
          this.pubsPerYear = response.data.data.pubsPerYear;
          this.associations = response.data.data.associationTopics;
          console.log(response.data)
          this.chart1.setOption({
            xAxis: {
              data: this.pubsPerYear.years
            },
            series: [
              {
                name: '发文量',
                data: this.pubsPerYear.nums
              }
            ]
          })
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
          this.heat = response.data.data.heat;
          this.pubsPerYear = response.data.data.pubsPerYear;
          this.associations = response.data.data.associationSubjects;
          console.log(response.data)
          this.chart1.setOption({
            xAxis: {
              data: this.pubsPerYear.years
            },
            series: [
              {
                name: '发文量',
                data: this.pubsPerYear.nums
              }
            ]
          })
        } else {
          console.log(response.data.message);
        }
      }).catch(error => {
        console.log(error)
      })
    },
    getPapers(page) {
      var scope = ""
      if (this.type == "topic")
        scope = "keywords"
      else
        scope = "subjects"
      this.$axios({
        method: "post",
        url: "/api/search/paper",
        data: {
          conditions: [
            {
              compound: false,
              fuzzy: false,
              keyword: this.name,
              languages: ["zh"],
              logic: "and",
              scope: [scope],
              subConditions: [],
              translated: false
            }
          ],
          filters: [],
          page: page,
          size: 12,
          sort: "citationNum.desc"
        }
      }).then(res => {
        if (res.data.success == true) {
          this.items = this.items.concat(res.data.data.items);
          if (page == 0)
            this.total = res.data.data.totalHits;
        } else {
          console.log(res.data.message);
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
        tooltip: {
          trigger: 'item'
        },
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

export const relationshipNetworkChart = {
  data:() => ({
    netChart: null,
  }),
  methods: {
    initNetChart() {
      this.netChart = this.$echarts.init(document.getElementById("net"), 'infinite', { renderer: 'svg' });
      this.loadData();
      var type = this.$route.path.substring(1);
      var _this = this;
      this.netChart.on('click', function(param) {
        if (param.data.id == null) return;
        var target = param.data.id
        if (target != _this.id)
          _this.$router.push({ path: type, query: { id: target}})
      })
      window.addEventListener('resize', () => { this.netChart.resize(); })
    },
    loadData() {
      var id = this.$route.query.id;
      var entity = this.$route.path.substring(1);
      if (entity == "author") entity = "researcher";
      this.$axios({
        method: "get",
        url: "/api/analysis/cooperation/"+entity+"/"+id,
        params: {
          num: 10
        }
      }).then(response => {
        if (response.data.success == true) {
          var network = response.data.data;
          var option = {
            title: {
              text: '合作关系网络',
              left: 'left'
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
                fontSize: 12
              },
              edgeLabel: {
                show: true,
                position: 'middle',
                formatter: '合作次数:{c}'
              },
              data: [],
              links: [],
            }
          }
          var origin = {
            name: this.name,
            symbolSize: 100
          }
          option.series.data.push(origin);
          network.forEach(function(item) {
            var node = {}
            node.name = item.term
            node.id = item.id
            option.series.data.push(node);
          })
          option.series.links = network.map(function (item, idx) {
            var link = {}
            link.source = 0;
            link.target = (idx+1);
            link.value = item.frequency;
            return link;
          });
          this.netChart.setOption(option);
        } else {
          console.log(response.data.message);
        }
      }).catch(error => {
        console.log(error)
      })
    }
  }
}