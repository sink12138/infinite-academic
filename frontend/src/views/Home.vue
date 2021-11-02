<template>
  <div>
    <v-toolbar elevation="0" class="d-flex justify-center">
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
    <div class="bigfont">Infinit Acadmic</div>
  </div>
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

<style lang="scss">
.bigfont {
  font-family: Fira Code;
  font-size: 80px;
  font:800;
}
</style>