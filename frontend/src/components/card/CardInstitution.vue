<template>
  <v-card class="text-left my-2" max-width="240">
    <v-row>
      <v-col>
        <h3 v-if="disabled == 'disabled'">ID: {{ item.id }}</h3>
      </v-col>
    </v-row>
    <v-row no-gutters>
      <v-col cols="2" class="px-1 d-flex">
        <v-icon size="48"> mdi-domain </v-icon>
      </v-col>
      <v-col>
        <v-card-title>
          <span
            class="link"
            @click="href('institution', item.id)"
            v-html="item.name"
            :class="disabled"
          ></span>
        </v-card-title>
      </v-col>
    </v-row>
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