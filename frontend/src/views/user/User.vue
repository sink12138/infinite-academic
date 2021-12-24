<template>
  <v-app id="inspire">
    <v-navigation-drawer app dark>
      <v-list-item>
        <v-list-item-content>
          <v-list-item-title>
            <v-avatar size="64">
              <v-icon size="64">mdi-account-circle</v-icon>
            </v-avatar>
          </v-list-item-title>
          <v-list-item-subtitle>
            <div>{{ username }}</div>
            <div>{{ email }}</div>
            <v-spacer></v-spacer>
            <v-btn outlined class="ma-5" @click="goBack">返回首页</v-btn>
          </v-list-item-subtitle>
        </v-list-item-content>
      </v-list-item>

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
        this.researcherId = response.data.data.researcherId;
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
      researcherId: "",
    };
  },
  methods: {
    goBack() {
      this.$router.push({ path: '/'})
    }
  }
};
</script>

<style>
</style>
