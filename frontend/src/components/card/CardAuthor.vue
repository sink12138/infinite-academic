<template>
  <v-card
    class="text-left my-2"
    max-width="850"
  >
    <v-card-title class="d-flex">
      <v-icon class="mx-1">
        mdi-account-tie-outline
      </v-icon>
      <a 
        @click="href('author', item.id)" 
        v-text="item.name"
      ></a>
    </v-card-title>
    <v-card-subtitle class="pb-0">
      <a
        v-if="item.institution"
        @click="href('institution', item.institution.id)"
        v-text="item.institution.name"
      >
      </a>&nbsp;
      <span>被引量:{{item.citationNum}}</span>
    </v-card-subtitle>
    <v-card-text class="pt-2 pb-0">
      <v-chip
        v-for="interest in item.interests"
        :key="interest"
        label
      >
        <v-icon left>
          mdi-animation
        </v-icon>
        {{interest}}
      </v-chip>
    </v-card-text>
    <v-card-text>
      <div>
        <span>发表文章数:</span>
        <span v-html="item.paperNum"></span>
      </div>
      <div v-if="item.patentNum != '0'">
        <span>发明专利数:</span>
        <span v-html="item.patentNum"></span>
      </div>
    </v-card-text>
  </v-card>
</template>

<script>
export default {
  props:{
    item: {
      type: Object,
      default:() => {}
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