<template>
  <v-card
    class="text-left my-2"
    max-width="1950"
  >
    <v-card-title class="d-flex">
      <v-icon class="mx-1">
        mdi-text-box-multiple-outline
      </v-icon>
      <span class="link" @click="href('patent', patentdata.id)">{{patentdata.title}}</span>
      <v-spacer></v-spacer>
      <v-btn icon @click="addCitationItem(patentdata)">
        <v-icon>mdi-comma-box</v-icon>
      </v-btn>
    </v-card-title>
    <v-card-subtitle class="pb-0">
      <span v-if="patentdata.date" v-text="patentdata.date.substr(0,4)"></span>&nbsp;
      <span>被引量:{{patentdata.citationNum}}</span>&nbsp;
      <span>来源:</span>&nbsp;
      <span v-for="item in patentdata.sources" :key="item.id">
        <span class="link" @click=toSource(item.url)>{{item.website}}</span>&nbsp;
      </span>
      <br>
      <span v-if="patentdata.journal">
        <span class="link"
          @click="href('journal', patentdata.journal.id)"
          v-text="patentdata.journal.title"
        >
        </span>&nbsp;
      </span>
    </v-card-subtitle>
    <v-card-text class="pb-0">
      <span
        v-for="(author, idx) in patentdata.authors"
        :key="author.id"
      >
        <span class="link"
          v-if="patentdata.authors && idx == patentdata.authors.length-1"
          @click="href('author', author.id)"
          v-text="author.name"
        ></span>
        <span class="link"
          v-else
          @click="href('author', author.id)"
          v-text="author.name + ','"
        ></span>
      </span>
    </v-card-text>
    <v-card-text class="pt-2 pb-0">
      <span
        class="mx-1"
        v-for="keyword in patentdata.keywords"
        :key="keyword"
        v-show="keyword"
      >
        <v-btn
          small
          outlined
        >
          <v-icon small>
            mdi-tag-outline
          </v-icon>
          <span v-text="keyword"></span>
        </v-btn>
      </span>
    </v-card-text>
    <v-card-text>
      <span v-if="expand" v-text="patentdata.abstract"></span>
      <span v-else v-text="$options.filters.abstract(patentdata.abstract)"></span>
      <v-btn
        x-small
        outlined
        v-if="patentdata.abstract && patentdata.abstract.length > 1100"
        @click="expand = !expand"
      >
        <span v-if="expand">收起</span>
        <span v-else>全部</span>

        <v-icon small v-if="expand">mdi-chevron-up</v-icon>
        <v-icon small v-else>mdi-chevron-down</v-icon>
      </v-btn>
    </v-card-text>
    <v-card-text class="pb-1">
      <span v-if="patentdata.institutions">
        <span>所有机构:</span>&nbsp;
        <span v-for="institution in patentdata.institutions" :key="institution.id">
          <span class="link" @click="href('institution',institution.id)" v-html="institution.name"></span>
        </span>
      </span>
      <br>
      <span v-if="patentdata.type">
        <span>论文类别:</span>&nbsp;
        <span>{{patentdata.type}}</span>
      </span>
      <br>
      <span v-if="patentdata.subjects">
        <span>学科分类:</span>&nbsp;
        <span v-for="subject in patentdata.subjects" :key="subject.id">
          <a @click="hrefname('subject',subject)">{{subject}}&nbsp;</a>
        </span>
      </span>
      <br>
      <span v-if="patentdata.publisher">
        <span>出版商:</span>&nbsp;
        <span>{{patentdata.publisher}}</span>
      </span>
      <br>
      <span v-if="patentdata.doi">
        <span>doi:</span>
        {{patentdata.doi}}
      </span>
    </v-card-text>
  </v-card>
</template>

<script>
export default {
  props:{
    patentdata: {
      type: Object,
      default:() => {}
    }
  },
  data() {
    return {
      expand: false
    }
  },
  filters: {
    abstract(text) {
      if (!text) return " ";
      if (text.length > 1100) {
        return text.slice(0,1100) + "..."
      }
      return text;
    }
  },
  methods: {
    href(type, id) {
      if (id == null) {
        this.$notify({
          title: '数据缺失',
          message: '信息暂未收录，给您带来不便敬请谅解。',
          type: 'warning'
        });
        return;
      }
      this.$router.push({
        path: type,
        query: { id: id }
      })
    },
    hrefname(type,name){
      if (name == null) {
        this.$notify({
          title: '数据缺失',
          message: '信息暂未收录，给您带来不便敬请谅解。',
          type: 'warning'
        });
        return;
      }
      this.$router.push({
        path: type,
        query: { name: name }
      })
    },
    toSource(url){
      window.location.href=url
    }
  },
  mounted(){
    console.log(this.patentdata)
  }
}
</script>

<style>

</style>
