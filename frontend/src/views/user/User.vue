<template>
  <v-app id="inspire">
    <v-navigation-drawer app>

      <v-sheet
        color="grey lighten-4"
        class="pa-4"
      >
        <v-avatar size="64">
          <v-icon size="64">mdi-account-circle</v-icon>
        </v-avatar>

        <div>{{username}}</div>
        <div>{{email}}</div>
      </v-sheet>

      <v-divider></v-divider>

      <v-list>
        <v-list-item
          v-for="[icon, text, url] in links"
          link
          :key="icon"
          :to="url"
        >
          <v-list-item-icon>
            <v-icon>{{ icon }}</v-icon>
          </v-list-item-icon>

          <v-list-item-title>{{ text }}</v-list-item-title>
        </v-list-item>
        <v-list-item
          v-if="researcherId!=''"
          link
          :key="icon1"
          :to="url1"
        >
          <v-list-item-icon>
            <v-icon>{{ icon1 }}</v-icon>
          </v-list-item-icon>

          <v-list-item-title>{{ text1 }}</v-list-item-title>
        </v-list-item>
      </v-list>
    </v-navigation-drawer>

    <v-main app>
      <router-view></router-view>
    </v-main>

  </v-app>
</template>

<script>
export default {
  mounted() {
    this.$axios({
      method: "get",
      url: "/api/account/profile",
    }).then((response) => {
      console.log(response.data);
      if (response.data.success === true) {
        this.email = response.data.data.email;
        this.username = response.data.data.username;
      }
    });
  },
  data() {
    return {
      links: [
        ["mdi-inbox-arrow-down", "个人资料", "/user/profile"],
        ["mdi-send", "申请信息", "/user/apply"],
        ["mdi-delete", "个人消息", "/user/message"],
        ["mdi-message-text", "专利转移", "/user/patenttransfer"],
        ["mdi-account", "学者认证", "/user/scholarIdentity"],
      ],
      email: "",
      username: "",
      researcherId:"",
      url1:"/user/door",
      icon1:"mdi-account",
      text1:"门户页面"
    };
  },
};
</script>

<style>
</style>
