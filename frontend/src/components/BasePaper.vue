<template>
  <v-card
    class="mx-auto"
    max-width="80%"
  >
    <v-card-text>
      <!-- <div>Word of the Day</div> -->
      <p class="text--md" style="font:Microsoft YaHei,Arial,Helvetica,sans-serif;font-size:24px;line-height:1.2;color:#06c">
        <a href="">{{paperdata.title}}</a>
      </p>
      <div class="text-md-left black--text"><span class="grey--text">摘要:</span>&nbsp;&nbsp;{{paperdata.abstract}}</div>
      <div class="text-md-left black--text"><span class="grey--text">作者:</span>&nbsp;&nbsp;<span v-for="item in paperdata.authors" v-bind:key="item.index">&nbsp;&nbsp;<a href="">{{item.name}}</a>&nbsp;&nbsp;</span></div>
      <div class="text-md-left black--text"><span class="grey--text">论文被引量:</span>&nbsp;&nbsp;{{paperdata.citationNum}}</div>
      <div class="text-md-left black--text"><span class="grey--text">发表日期:</span>&nbsp;&nbsp;{{paperdata.date}}</div>
      <div class="text-md-left black--text"><span class="grey--text">DOI编号:</span>&nbsp;&nbsp;{{paperdata.doi}}</div>
      <!-- <div class="text-md-left"><span class="red--text">数据库中ID:</span>&nbsp;&nbsp;{{paperdata.id}}</div> -->
      <!-- <div class="text-md-left"><span class="red--text">论文所有机构:</span><span v-for="item in paperdata.institutions" v-bind:key="item.index">&nbsp;&nbsp;{{item.name}}&nbsp;&nbsp;</span></div> -->
      <!-- <div class="text-md-left"><span class="red--text">ISBN编号:</span>&nbsp;&nbsp;{{paperdata.isbn}}</div> -->
      <!-- <div class="text-md-left"><span class="red--text">ISSN编号:</span>&nbsp;&nbsp;{{paperdata.issn}}</div> -->
      <div class="text-md-left black--text"><span class="grey--text">关键词:</span>&nbsp;&nbsp;<span v-for="item in paperdata.keywords" v-bind:key="item.index">&nbsp;&nbsp;{{item}}&nbsp;&nbsp;</span></div>
      <!-- <div class="text-md-left"><span class="red--text">出版商:</span>&nbsp;&nbsp;{{paperdata.publisher}}</div> -->
      <!-- <div class="text-md-left"><span class="red--text">论文所有来源:</span>&nbsp;&nbsp;<a v-for="item in paperdata.sources" v-bind:key="item.index" :href="item.url">&nbsp;&nbsp;{{item.website}}&nbsp;&nbsp;</a></div> -->
      <!-- <div class="text-md-left"><span class="red--text">学科分类:</span>&nbsp;&nbsp;<span v-for="item in paperdata.subjects" v-bind:key="item.index">&nbsp;&nbsp;{{item}}&nbsp;&nbsp;</span></div> -->
      <!-- <div class="text-md-left"><span class="red--text">话题分类:</span>&nbsp;&nbsp;<span v-for="item in paperdata.topics" v-bind:key="item.index">&nbsp;&nbsp;{{item}}&nbsp;&nbsp;</span></div> -->
      <!-- <div class="text-md-left"><span class="red--text">论文的类别:</span>&nbsp;&nbsp;{{paperdata.type}}</div> -->
      <!-- <div class="text-md-left"><span class="red--text">发表年份:</span>&nbsp;&nbsp;{{paperdata.year}}</div> -->
    </v-card-text>
    <v-expansion-panels flat>
        <v-expansion-panel>
          <v-expansion-panel-header>
            <v-card-actions>
            <v-btn text color="blue accent-5">
              论文所属期刊信息
            </v-btn>
            </v-card-actions>
          </v-expansion-panel-header>
          <v-expansion-panel-content>
            <v-card-text height=0 v-if="paperdata.journal">
              <div class="text-md-left black--text"><span style="color:#999">期刊标题:</span>&nbsp;&nbsp;{{paperdata.journal.title}}</div>
              <!-- <div class="text-md-left black--text"><span style="color:#999">期刊的数据库id:</span>&nbsp;&nbsp;{{paperdata.journal.id}}</div> -->
              <div class="text-md-left black--text"><span style="color:#999">论文在期刊中的卷号:</span>&nbsp;&nbsp;{{paperdata.journal.volume}}</div>
              <div class="text-md-left black--text"><span style="color:#999">论文在期刊中的期号:</span>&nbsp;&nbsp;{{paperdata.journal.issue}}</div>
              <div class="text-md-left black--text"><span style="color:#999">论文在期刊中的起始页码:</span>&nbsp;&nbsp;{{paperdata.journal.startPage}}</div>
              <div class="text-md-left black--text"><span style="color:#999">论文在期刊中的结束页码:</span>&nbsp;&nbsp;{{paperdata.journal.endPage}}</div>
            </v-card-text>
          </v-expansion-panel-content>
        </v-expansion-panel>
      </v-expansion-panels>
    <v-tabs v-model="tab">
      <v-tab key="citations">引证文献</v-tab>
      <v-tab key="references">参考文献</v-tab>
    </v-tabs>
    <v-tabs-items v-model="tab">
        <v-tab-item key="citations">
          <v-card-text v-for="item in citations.items" v-bind:key="item.index">
            <p class="text--md" style="font:Microsoft YaHei,Arial,Helvetica,sans-serif;font-size:20px;line-height:1.2;color:#06c">
              <a href="">{{item.title}}</a>
            </p>
            <div class="text-md-left black--text"><span class="grey--text">摘要:</span>&nbsp;&nbsp;{{item.abstract}}</div>
            <div class="text-md-left black--text"><span class="grey--text">作者:</span>&nbsp;&nbsp;<span v-for="item in item.authors" v-bind:key="item.index">&nbsp;&nbsp;<a href="">{{item.name}}</a>&nbsp;&nbsp;</span></div>
            <div class="text-md-left black--text"><span class="grey--text">论文被引量:</span>&nbsp;&nbsp;{{item.citationNum}}</div>
            <div class="text-md-left black--text"><span class="grey--text">发表日期:</span>&nbsp;&nbsp;{{item.date}}</div>
          </v-card-text>
        </v-tab-item>
        <v-tab-item key="references">
          <v-card-text v-for="item in references.items" v-bind:key="item.index">
            <p class="text--md" style="font:Microsoft YaHei,Arial,Helvetica,sans-serif;font-size:20px;line-height:1.2;color:#06c">
              <a href="">{{item.title}}</a>
            </p>
            <div class="text-md-left black--text"><span class="grey--text">摘要:</span>&nbsp;&nbsp;{{item.abstract}}</div>
            <div class="text-md-left black--text"><span class="grey--text">作者:</span>&nbsp;&nbsp;<span v-for="item in item.authors" v-bind:key="item.index">&nbsp;&nbsp;<a href="">{{item.name}}</a>&nbsp;&nbsp;</span></div>
            <div class="text-md-left black--text"><span class="grey--text">论文被引量:</span>&nbsp;&nbsp;{{item.citationNum}}</div>
            <div class="text-md-left black--text"><span class="grey--text">发表日期:</span>&nbsp;&nbsp;{{item.date}}</div>
          </v-card-text>
        </v-tab-item>
        <!-- <v-tab-item key="chuban">
          <v-card-text v-for="item in chuban.items" v-bind:key="item.index">
            <p class="text--md" style="font:Microsoft YaHei,Arial,Helvetica,sans-serif;font-size:20px;line-height:1.2;color:#06c">
              <a href="">{{item.title}}</a>
            </p>
            <div class="text-md-left black--text"><span class="grey--text">摘要:</span>&nbsp;&nbsp;{{item.abstract}}</div>
            <div class="text-md-left black--text"><span class="grey--text">作者:</span>&nbsp;&nbsp;<span v-for="item in item.authors" v-bind:key="item.index">&nbsp;&nbsp;<a href="">{{item.name}}</a>&nbsp;&nbsp;</span></div>
            <div class="text-md-left black--text"><span class="grey--text">论文被引量:</span>&nbsp;&nbsp;{{item.citationNum}}</div>
            <div class="text-md-left black--text"><span class="grey--text">发表日期:</span>&nbsp;&nbsp;{{item.date}}</div>
          </v-card-text>
        </v-tab-item> -->
    </v-tabs-items>
  </v-card>
</template>
<script>
export default{
  props:{
    paperdata: {
      type: Object,
      default:() => {}
    },
    references:{
      type:Object,
      default:()=>{}
    },
    citations:{
      type:Object,
      default:()=>{}
    }
  },
  data(){
    return{
      tab:null
    }
  },
  mounted() {

  }
}
</script>
<style scoped>
a{
  text-decoration: none;
}
</style>