<template>
  <v-card
    class="text-left my-2"
    max-width="1950"
  >
    <v-card-title class="d-flex">
      <v-icon class="mx-1">
        mdi-text-box-multiple-outline
      </v-icon>
      <a @click="href('paper', item.id)" v-html="item.title"></a>
      <v-spacer></v-spacer>
      <v-btn icon @click="addCitationItem(item)">
        <v-icon>mdi-comma-box</v-icon>
      </v-btn>
    </v-card-title>
    <v-card-subtitle class="pb-0">
      <span v-if="item.date" v-text="item.date.substr(0,4)"></span>&nbsp;
      <a
        v-if="item.journal"
        @click="href('journal', item.journal.id)"
        v-html="item.journal.title"
      >
      </a>&nbsp;
      <span>被引量:{{item.citationNum}}</span>
    </v-card-subtitle>
    <v-card-text class="pb-0">
      <span
        v-for="(author, idx) in item.authors"
        :key="author.id"
      >
        <a
          v-if="item.authors && idx == item.authors.length-1"
          @click="href('author', author.id)"
          v-html="author.name"
        ></a>
        <a
          v-else
          @click="href('author', author.id)"
          v-html="author.name + ','"
        ></a>
      </span>
    </v-card-text>
    <v-card-text class="pt-2 pb-0">
      <span
        class="mx-1"
        v-for="keyword in item.keywords"
        :key="keyword"
        v-show="keyword"
      >
        <v-btn
          small
          outlined
          @click="href('topic', keyword)"
        >
          <v-icon small>
            mdi-tag-outline
          </v-icon>
          <span v-html="keyword"></span>
        </v-btn>
      </span>
    </v-card-text>
    <v-card-text>
      <span v-if="expand" v-html="item.abstract"></span>
      <span v-else v-html="$options.filters.abstract(item.abstract)"></span>

      <v-btn
        x-small
        outlined
        v-if="item.abstract && item.abstract.length > 110"
        @click="expand = !expand"
      >
        <span v-if="expand">收起</span>
        <span v-else>全部</span>

        <v-icon small v-if="expand">mdi-chevron-up</v-icon>
        <v-icon small v-else>mdi-chevron-down</v-icon>
      </v-btn>
    </v-card-text>
  </v-card>
</template>

<script>
import { addCitation } from "../mixins/mixin"
export default {
  mixins: [addCitation],
  props:{
    item: {
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
      if (text.length > 110) {
        return text.slice(0,110) + "..."
      }
      return text;
    }
  },
  methods: {
    href(type, id) {
      var this_path = this.$route.path.substring(1)
      var this_id = ""
      if (this.$route.query.id != null)
        this_id = this.$route.query.id
      else if (this.$route.query.name !=null)
        this_id = this.$route.query.name
      if (type == this_path && id == this_id) return;
      if (type == "topic" || type == "subject")
        this.$router.push({
          path: type,
          query: { name: id }
        })
      else
        this.$router.push({
          path: type,
          query: { id: id }
        })
    }
  }
}
</script>

<style scoped>
a {
  color: #000000;
}
a:link {
  color: #000000;
  text-decoration: none;
}
a:visited {
  color: #000000;
  text-decoration: none;
}
a:hover {
  color: #0D47A1;
  text-decoration: underline;
}
</style>