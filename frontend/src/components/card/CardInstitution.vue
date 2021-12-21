<template>
  <v-card
    class="text-left my-2"
    max-width="240"
  >
    <v-row no-gutters>
      <v-col cols="2" class="px-1 d-flex">
        <v-icon size="48">
          mdi-domain
        </v-icon>
      </v-col>
      <v-col>
        <v-card-title>
          <a 
            @click="href('institution', item.id)"
            v-html="item.name"
          ></a>
        </v-card-title>
      </v-col>
    </v-row>
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
      let { href } = this.$router.resolve({ path: type, query: { id: id }})
      window.open(href, '_blank')
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