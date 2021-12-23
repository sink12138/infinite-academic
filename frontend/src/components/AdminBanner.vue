<template>
  <v-app-bar
    dark
    dense
    elevation="0"
    style="position: sticky; top: 0; z-index: 999"
  >
    <BaseGoBack></BaseGoBack>
    <v-spacer></v-spacer>
    <v-icon 
      class="text-h4"
    >{{title.icon}}
    </v-icon>
    <div class="my-font">
      {{title.text}}
    </div>
    <v-spacer></v-spacer>
    <div
      v-if="title.status"
    >
      <span>自动退出</span>
      <v-icon>mdi-clock</v-icon>
      <span>{{title.time}}</span>
      <v-btn
        height="100%"
        dark
        @click="Logout"
        bottom
      >
        登出
      </v-btn>
    </div>
    
  </v-app-bar>
</template>

<script>
import BaseGoBack from "../components/BaseGoBack.vue"
export default {
  components: {
    BaseGoBack
  },
  props: {
    title: {
      type: Object,
      default:() => {}
    }
  },
  methods: {
    Logout() {
      let token = window.localStorage.token;
      this.$axios({
        method: "post",
        url: "api/admin/logout",
        headers:{
          'token':token
        }
      }).then((response) => {
        console.log(response.data);
        if (response.data.success === true) {
          this.$notify({
            title: "成功",
            message: "登出成功",
            type: "success",
          });
          this.$emit("logout")
        } else {
          this.$notify({
            title: "失败",
            message: "登出失败",
            type: "warning",
          });
        }
      });
    },
  }
}
</script>

<style>

</style>