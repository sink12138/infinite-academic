<template>
  <v-container>
    <v-navigation-drawer>
      <v-sheet
        color="grey lighten-4"
        class="pa-4"
      >
        <v-avatar size="64">
          <v-icon size="64">mdi-account-circle</v-icon>
        </v-avatar>
        <div>NAME</div>
        <div>123456@buaa.edu.cn</div>
      </v-sheet>
      <v-divider></v-divider>
      <v-list>
        <v-list-item @click="changeToPersonalProfile()">
          <v-list-item-icon>
            <v-icon>{{icons.mdiAccount}}</v-icon>
          </v-list-item-icon>
          <v-list-item-title>个人信息</v-list-item-title>
        </v-list-item>
        <v-list-item @click="changeToMessage()">
          <v-list-item-icon>
            <v-icon>{{icons.mdiMail}}</v-icon>
          </v-list-item-icon>
          <v-list-item-title>信息列表</v-list-item-title>
        </v-list-item>
        <v-list-item
          v-for="[icon, text] in links"
          :key="icon"
          link
        >
          <v-list-item-icon>
            <v-icon>{{ icon }}</v-icon>
          </v-list-item-icon>
          <v-list-item-title>{{ text }}</v-list-item-title>
        </v-list-item>
      </v-list>
    </v-navigation-drawer>
    <v-main>
      <router-view></router-view>
    </v-main>
  </v-container>
</template>

<script>
import { mdiAccount, mdiMail } from "@mdi/js";
export default {
  mounted() {
    this.$axios({
      method: "get",
      url: "/api/account/profile",
    }).then((response) => {
      console.log(response.data);

    });
  },
  data() {
    return {
      links: [["mdi-send", "申请信息"]],
      icons: {
        mdiAccount,
        mdiMail,
      },
      isPersonalProfile: true,
      isMessage: false,
      emailRules: [
        (v) => !!v || "邮箱未填写",
        (v) => /.+@.+/.test(v) || "邮箱无效",
      ],
      passwordRules: [(v) => !!v || "密码未填写"],
    };
  },
  methods: {
    changeToMessage() {
      this.isPersonalProfile = false;
      this.isMessage = true;
      console.log(this.isPersonalProfile);
      console.log(this.isMessage);
    },
    changeToPersonalProfile() {
      this.isPersonalProfile = true;
      this.isMessage = false;
      console.log(this.isPersonalProfile);
      console.log(this.isMessage);
    },
  },
};
</script>

<style>
</style>
