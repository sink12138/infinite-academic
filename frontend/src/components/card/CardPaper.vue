<template>
  <v-card class="text-left my-1" max-width="1950">
    <v-card-title class="d-flex">
      <v-icon class="mx-1"> mdi-text-box-multiple-outline </v-icon>
      <span
        class="link"
        @click="href('paper', item.id)"
        v-html="$options.filters.title(item.title)"
        :class="disabled"
      ></span>
      <v-spacer></v-spacer>
      <v-btn icon @click="addCitationItem(item)">
        <v-icon>mdi-comma-box</v-icon>
      </v-btn>
    </v-card-title>
    <v-card-subtitle class="pb-0">
      <span v-if="item.date" v-html="item.date.substr(0, 4)+'&nbsp;'"></span>
      <span
        class="link"
        v-if="item.journal"
        @click="href('journal', item.journal.id)"
        :class="disabled"
        v-html="item.journal.title+'&nbsp;'"
      ></span>
      <span>被引量:{{ item.citationNum }}</span>
    </v-card-subtitle>
    <v-card-text class="pb-0">
      <span v-for="(author, idx) in item.authors" :key="author.id">
        <span
          class="link"
          v-if="item.authors && idx == item.authors.length - 1"
          @click="href('author', author.id)"
          :class="disabled"
          v-html="author.name"
        ></span>
        <span
          class="link"
          v-else
          @click="href('author', author.id)"
          :class="disabled"
          v-html="author.name + ','"
        ></span>
      </span>
    </v-card-text>
    <v-card-text class="pt-2 pb-0">
      <span
        class="mx-1"
        v-for="keyword in item.keywords"
        :key="keyword"
        v-show="keyword"
      >
        <v-btn small outlined @click="href('topic', keyword)" :class="disabled" class="my-1">
          <v-icon small> mdi-tag-outline </v-icon>
          <span v-html="keyword"></span>
        </v-btn>
      </span>
    </v-card-text>
    <v-card-text class="pt-2 pb-4">
      <span v-if="expand" v-html="item.abstract"></span>
      <span v-else v-html="$options.filters.abstract(item.abstract)"></span>
      <v-btn
        x-small
        outlined
        v-if="item.abstract && item.abstract.length > 120"
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
import { addCitation } from "../mixins/mixin";
export default {
  mixins: [addCitation],
  props: {
    item: {
      type: Object,
      default: () => {},
    },
    disabled: {
      type: String,
      default: "",
    },
  },
  data() {
    return {
      expand: false,
    };
  },
  filters: {
    title(text) {
      if (!text) return " ";
      if (text.length > 60) {
        return text.slice(0, 57) + "...";
      }
      return text;
    },
    abstract(text) {
      if (!text) return " ";
      if (text.length > 120) {
        return text.slice(0, 120) + "...";
      }
      return text;
    },
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
      var this_path = this.$route.path.substring(1);
      var this_id = "";
      if (this.$route.query.id != null) this_id = this.$route.query.id;
      else if (this.$route.query.name != null) this_id = this.$route.query.name;
      if (type == this_path && id == this_id) return;
      if (type == "topic" || type == "subject") {
        let { href } = this.$router.resolve({
          path: type,
          query: { name: id.replaceAll("<b>", "").replaceAll("</b>", "") },
        });
        window.open(href, "_blank");
      } else {
        let { href } = this.$router.resolve({ path: type, query: { id: id } });
        window.open(href, "_blank");
      }
    },
  },
};
</script>

<style>
</style>
