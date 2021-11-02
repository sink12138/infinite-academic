<template>
  <v-toolbar class="d-flex justify-center">
    <v-btn-toggle>
      <v-btn 
        v-for="item in router"
        :key="item.title"
        :href=item.href
      >
        <v-icon>
          {{item.icon}}
        </v-icon>
        {{item.title}}
      </v-btn>
    </v-btn-toggle>
  </v-toolbar>
</template>

<script>
export default {
  data() {
    return {
      router: [
        {href: '/paper', icon: 'mdi-book-open-blank-variant', title: 'Paper'},
        {href: '/author', icon: 'mdi-clipboard-account', title: 'Author'},
        {href: '/institution', icon: 'mdi-home', title: 'Institution'},
        {href: '/journal', icon: 'mdi-newspaper-variant', title: 'Journals'},
        {href: '/search', icon: 'mdi-magnify-expand', title: 'Search'},
        {href: '/admin', icon: 'mdi-shield-lock', title: 'Admin'},
      ]
    }
  }
}
</script>

<style>

</style>