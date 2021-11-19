<template>
  <v-toolbar
    dense
    dark
    elevation="0"
  >
    <!-- 导航组 -->
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
    <v-spacer></v-spacer>
    <!-- 引用 -->
    <BaseCitation>
    </BaseCitation>
    <!-- 登录注册组件 -->
    <router-link to="/login">
      <v-btn outlined>登录/注册</v-btn>
    </router-link>
  </v-toolbar>
</template>

<script>
import BaseCitation from "./BaseCitation.vue";

export default {
  components: {
    BaseCitation,
  },
  data() {
    return {
      router: [
        { href: "/paper", icon: "mdi-book-open-blank-variant", title: "Paper" },
        { href: "/author", icon: "mdi-clipboard-account", title: "Author" },
        { href: "/institution", icon: "mdi-home", title: "Institution" },
        { href: "/journal", icon: "mdi-newspaper-variant", title: "Journals" },
        { href: "/search", icon: "mdi-magnify-expand", title: "Search" },
        { href: "/admin", icon: "mdi-shield-lock", title: "Admin" },
      ],
    };
  },
};
</script>

<style>
</style>
