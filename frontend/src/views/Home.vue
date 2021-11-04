<template>
  <div>
    <v-toolbar 
      dense 
      elevation="0" 
      class="d-flex justify-center pa-0"
    >
      <v-btn-toggle>
        <v-btn 
          v-for="item in router"
          :key="item.title"
          :to=item.href
        >
          <v-icon>
            {{item.icon}}
          </v-icon>
          {{item.title}}
        </v-btn>
      </v-btn-toggle>
      <v-btn outlined>登录/注册</v-btn>
    </v-toolbar>
    <div class="big_font">Infinite Acadmic</div>
    <v-btn @click="notify()">Notify</v-btn>
    <search-bar></search-bar>
  </div>
</template>

<script>
import SearchBar from '../components/SearchBar.vue'
export default {
  components: {
    SearchBar,
  },
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
  },
  methods: {
    notify() {
      this.$notify({
        title: 'hello',
        message: 'test message',
        type: 'success'
      });
    }
  }
}
</script>