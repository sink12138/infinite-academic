<template>
  <div class="search_bar d-inline-flex">
    <v-select
      class="select"
      v-model="filter"
      :items="items"
      hide-details
      prepend-inner-icon="mdi-dots-grid"
      background-color="grey lighten-2"
      filled
      @change="emitFilter"
    ></v-select>
    <v-divider vertical></v-divider>
    <v-text-field
      class="input"
      v-model="text"
      clearable
      filled
      full-width
      background-color="grey lighten-5"
      counter="30"
      hide-details
      append-icon="mdi-magnify"
      @click:append="search"
    >
    </v-text-field>
  </div>
</template>

<script>
import qs from 'qs'
export default {
  data() {
    return {
      items: ["全部", "论文", "期刊", "专利", "机构", "科研人员", "精确"],
      filter: "全部",
      text: null,
      filters: {},
      data: {},
      request: {},
      page: 0,
    };
  },
  methods: {
    emitFilter(){
      this.$emit('filterChange',this.filter);
    },
    search() {
      this.filters = this.$parent.filters;
      console.log("搜索过滤器");
      console.log(this.filters);
      var i;
      var url = "/api/search";
      var keyword = this.text.trim();
      var keywords = keyword.split(/\s+/);
      var condition;
      var fuzzy=true;
      var translated=true;
      console.log(keywords);
      this.request = {
            conditions: [
              {
                compound: false,
                fuzzy: true,
                keyword: "",
                languages: ["zh"],
                logic: "and",
                translated: true,
              },
            ],
            filters: [],
            page: this.page,
            size: 10,
          };
      switch (this.filter) {
        case "全部": {
          url = url + "/smart";
          break;
        }
        case "论文": {
          url = url + "/paper";
          var scope=[];
          switch(this.filters.paperType){
            case "标题/摘要/关键词/学科":scope=["title","abstract","keywords","subjects"];break;
            case "论文类别":scope=["type"];break;
            case "作者姓名":scope=["authors.name"];break;
            case "机构名称":scope=["institutions.name"];break;
            case "期刊名称":scope=["journal.title"];break;
          }
          this.request.conditions[0].scope= scope;
          this.request.conditions[0].keyword = keywords[0];
          if(keywords.length!=1)
            for(i=1;i<keywords.length;i++){
              condition={
                compound: false,
                fuzzy: true,
                keyword: keywords[i],
                languages: ["zh"],
                logic: "and",
                scope: scope,
                translated: true,
              };
              this.request.conditions[i]=condition;
            }
          break;
        }
        case "期刊": {
          url = url + "/journal";
          this.request.conditions[0].scope= ["title"];
          this.request.conditions[0].keyword = keywords[0];
          if(keywords.length!=1)
            for(i=1;i<keywords.length;i++){
              condition={
                compound: false,
                fuzzy: true,
                keyword: keywords[i],
                languages: ["zh"],
                logic: "and",
                scope: ["title"],
                translated: true,
              };
              this.request.conditions[i]=condition;
            }
          break;
        }
        case "机构": {
          url = url + "/institution";
          this.request.conditions[0].scope=["name"];
          this.request.conditions[0].keyword = keywords[0];
          if(keywords.length!=1)
            for(i=1;i<keywords.length;i++){
              condition={
                compound: false,
                fuzzy: true,
                keyword: keywords[i],
                languages: ["zh"],
                logic: "and",
                scope: ["name"],
                translated: true,
              };
              this.request.conditions[i]=condition;
            }
          break;
        }
        case "专利": {
          url = url + "/patent";
          scope=[];
          switch(this.filters.patentType){
            case "标题/摘要":scope=["title","abstract"];break;
            case "类型":scope=["type"];break;
            case "申请人":scope=["applicant"];break;
            case "发明人":scope=["inventors.name"];break;
          }
          this.request.conditions[0].scope= scope;
          this.request.conditions[0].keyword = keywords[0];
          if(keywords.length!=1)
            for(i=1;i<keywords.length;i++){
              condition={
                compound: false,
                fuzzy: true,
                keyword: keywords[i],
                languages: ["zh"],
                logic: "and",
                scope: scope,
                translated: true,
              };
              this.request.conditions[i]=condition;
            }
          break;
        }
        case "科研人员": {
          url = url + "/researcher";
          scope=[];
          switch(this.filters.researcherType){
            case "姓名":scope=["name"];fuzzy=false;translated=false;break;
            case "研究方向":scope=["interests"];fuzzy=true;translated=true;break;
            case "相关机构":scope=["currentInst.name","institutions.name"];fuzzy=true;translated=true;break;
          }
          this.request.conditions[0].scope=scope;
          this.request.conditions[0].fuzzy=fuzzy;
          this.request.conditions[0].translated=translated;
          this.request.conditions[0].keyword = keywords[0];
          if(keywords.length!=1)
            for(i=1;i<keywords.length;i++){
              condition={
                compound: false,
                fuzzy: fuzzy,
                keyword: keywords[i],
                languages: ["zh"],
                logic: "and",
                scope: scope,
                translated: translated,
              };
              this.request.conditions[i]=condition;
            }
          break;
        }
        case "精确": {
          url = url + "/query";
          this.request = {
            key:this.text,
            type:this.type,
          };
          break;
        }
      }
      console.log(this.request);
      console.log(JSON.stringify(this.request));
      console.log(url);
      if(this.filter!="精确"){
        this.$axios({
          method: "post",
          url: url,
          data: this.request,
        })
        .then((response) => {
          console.log(response.data);
          this.data = response.data;
          this.$emit('searchResult',this.data.data);
        })
        .catch((error) => {
          console.log(error);
        });
      }
      else{
        this.$axios({
          method: "post",
          url: url,
          data: qs.stringify(this.request),
        })
          .then((response) => {
            console.log(response.data);
            this.data = response.data;   
            this.$emit('searchResult',this.data.data);
          })
          .catch((error) => {
            console.log(error);
          });
      }
    },
  },
};
</script>

<style scoped>
.search_bar {
  width: 66%;
}
.select {
  height: 40px;
  width: 12%;
}
.input {
  width: 88%;
}
</style>