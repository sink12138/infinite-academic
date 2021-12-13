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
    <!--<BaseLogin>
    </BaseLogin>-->
    <UserMenu></UserMenu>
  </v-toolbar>
</template>

<script>
import BaseCitation from "./BaseCitation.vue";
import UserMenu from "./UserMenu.vue"
//import BaseLogin from "./BaseLogin.vue";
export default {
  components: {
    UserMenu,
    BaseCitation,
    //BaseLogin,
  },
  props: {
    router: {
      type: Array,
      default:() => []
    }
  },
};
</script>

<style>
</style>
