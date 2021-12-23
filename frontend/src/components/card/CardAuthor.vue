<template>
  <v-card class="text-left my-2" max-width="1950">
    <v-card-title class="d-flex">
      <v-icon class="mx-1"> mdi-account-tie-outline </v-icon>
      <span
        class="link"
        v-if="item.id!=null"
        @click="href('author', item.id)"
        v-html="item.name"
        :class="disabled"
      ></span>
      <span
        class="link"
        v-if="item.id==null"
        v-html="item.name"
      ></span>
    </v-card-title>
    <v-card-subtitle class="pb-0">
      <span
        class="link"
        v-if="item.institution"
        @click="href('institution', item.institution.id)"
        :class="disabled"
        v-html="item.institution.name"
      ></span>
      &nbsp;
      <span>被引量:{{ item.citationNum }}</span>
    </v-card-subtitle>
    <v-card-text class="pt-2 pb-0">
      <v-chip v-for="interest in item.interests" :key="interest" label>
        <v-icon left> mdi-animation </v-icon>
        <span v-html="interest"></span>
      </v-chip>
    </v-card-text>
    <v-card-text>
      <div>
        <span>发表文章数:</span>
        <span v-text="item.paperNum"></span>
      </div>
      <div v-if="item.patentNum != '0'">
        <span>发明专利数:</span>
        <span v-text="item.patentNum"></span>
      </div>
    </v-card-text>
  </v-card>
</template>

<script>
export default {
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
      let { href } = this.$router.resolve({ path: type, query: { id: id }})
      window.open(href, '_blank')
    }
  }
}
</script>

<style>
</style>