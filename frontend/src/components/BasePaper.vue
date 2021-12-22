<template>
  <v-card
    class="text-left my-2"
    max-width="1950"
  >
    <v-card-title class="d-flex">
      <v-icon class="mx-1">
        mdi-text-box-multiple-outline
      </v-icon>
      <span class="link" @click="href('paper', paperdata.id)">{{paperdata.title}}</span>
      <v-spacer></v-spacer>
      <v-btn icon @click="addCitationItem(paperdata)">
        <v-icon>mdi-comma-box</v-icon>
      </v-btn>
    </v-card-title>
    <v-card-subtitle class="pb-0">
      <span v-if="paperdata.date" v-text="paperdata.date.substr(0,4)"></span>&nbsp;
      <span class="link"
        v-if="paperdata.journal"
        @click="href('journal', paperdata.journal.id)"
        v-text="paperdata.journal.title"
      >
      </span>&nbsp;
      <span>被引量:{{paperdata.citationNum}}</span>&nbsp;
      <span>来源:</span>&nbsp;
      <span v-for="item in paperdata.sources" :key="item.id">
        <span class="link" @click=toSource(item.url)>{{item.website}}</span>&nbsp;
      </span>
    </v-card-subtitle>
    <v-card-text class="pb-0">
      <span
        v-for="(author, idx) in paperdata.authors"
        :key="author.id"
      >
        <span class="link"
          v-if="paperdata.authors && idx == paperdata.authors.length-1"
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
        v-for="keyword in paperdata.keywords"
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
      <span v-if="expand" v-text="paperdata.abstract"></span>
      <span v-else v-text="$options.filters.abstract(paperdata.abstract)"></span>
      <v-btn
        x-small
        outlined
        v-if="paperdata.abstract && paperdata.abstract.length > 1100"
        @click="expand = !expand"
      >
        <span v-if="expand">收起</span>
        <span v-else>全部</span>

        <v-icon small v-if="expand">mdi-chevron-up</v-icon>
        <v-icon small v-else>mdi-chevron-down</v-icon>
      </v-btn>
    </v-card-text>
    <v-card-text class="pb-1">
      <span>所有机构:</span>&nbsp;
      <span v-for="institution in paperdata.institutions" :key="institution.id">
        <span class="link" @click="href('institution',institution.id)" v-html="institution.name"></span>
      </span>
      <br>
      <span>论文类别:</span>&nbsp;
      <span>{{paperdata.type}}</span>
      <br>
      <span>学科分类:</span>&nbsp;
      <span v-for="subject in paperdata.subjects" :key="subject.id">
        {{subject}}&nbsp;
      </span>
      <br>
      <span>出版商:</span>&nbsp;
      <span>{{paperdata.publisher}}</span>
      <br>
      <span>doi:</span>
      {{paperdata.doi}}
    </v-card-text>
  </v-card>
</template>

<script>
import { addCitation } from "./mixins/mixin"
export default {
  mixins: [addCitation],
  props:{
    paperdata: {
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
      this.$router.push({
        path: type,
        query: { id: id }
      })
    },
    toSource(url){
      window.location.href=url
    }
  },
  mounted(){
    console.log(this.paperdata)
  }
}
</script>

<style>

</style>