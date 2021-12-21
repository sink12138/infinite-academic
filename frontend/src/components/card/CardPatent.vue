<template>
  <v-card
    class="text-left my-2"
    max-width="1950"
  >
    <v-card-title class="d-flex">
      <v-icon class="mx-1">
        mdi-text-box-multiple-outline
      </v-icon>
      <a @click="href('patent', item.id)" v-html="item.title"></a>
      <v-spacer></v-spacer>
    </v-card-title>
    <v-card-subtitle class="pb-0">
        <span>{{item.type}}</span>
        <br>
        <span v-if="item.applicant" v-text="item.applicant"></span>于&nbsp;
        <span v-if="item.filingDate" v-text="item.filingDate"></span>&nbsp;申请
        <br>
        <span>发明人列表:</span>&nbsp;
        <span v-for="inventor in item.inventors" :key="inventor.id">
            <a @click="href('author',inventor.id)">{{inventor.name}}</a>&nbsp;
        </span>
        <br>
        <span>公开日:</span><span>{{item.publicationDate}}</span>
    </v-card-subtitle>
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
    
    }
  },
  methods: {
    href(type, id) {
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