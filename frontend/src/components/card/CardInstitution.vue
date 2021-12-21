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
      this.$router.push({
        path: type,
        query: { id: id },
      });
    },
  },
};
</script>

<style>
</style>