<template>
  <v-card
    class="text-left my-2"
    max-width="240"
  >
    <v-row>
      <v-col>
        <h3 v-if="disabled=='disabled'">ID: {{item.id}}</h3>
      </v-col>
    </v-row>
    <v-row no-gutters>
      <v-col cols="2" class="px-1 d-flex">
        <v-icon size="48">
          mdi-newspaper-variant-outline
        </v-icon>
      </v-col>
      <v-col>
        <v-card-title class="pb-1">
          <a 
            @click="href('journal', item.id)"
            v-html="item.title"
            :class="disabled"
          ></a>
        </v-card-title>
        <v-card-text
          v-html="item.sponsor"
        ></v-card-text>
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
    },
    disabled:{
      type:String,
      default:""
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