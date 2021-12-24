<template>
  <v-app>
    <v-navigation-drawer
      v-if="this.isLogin"
      fixed
      dark
      app
      class="d-flex justify-center"
    >
      <v-list-item>
        <v-list-item-content>
          <v-list-item-title class="text-h6 pa-5">
            {{ this.menu[this.menu_id - 1].title }}
          </v-list-item-title>
          <v-list-item-subtitle>
            将于 <v-icon>mdi-clock</v-icon>
            <span>{{ this.logoutTime }} </span>
            <span>自动退出</span>
          </v-list-item-subtitle>
        </v-list-item-content>
      </v-list-item>
      <v-list nav>
        <v-list-item
          v-for="(item, id) in menu"
          :key="id"
          @click="setMenu(item.menu_id)"
        >
          <v-list-item-icon>
            <v-icon>{{ item.icon }}</v-icon>
          </v-list-item-icon>
          <v-list-item-title>{{ item.title }}</v-list-item-title>
        </v-list-item>
      </v-list>
      <v-list-item>
        <v-list-item-content>
          <v-btn @click="Logout" small outlined> 登出 </v-btn>
        </v-list-item-content>
      </v-list-item>
    </v-navigation-drawer>

    <v-main app>
      <v-container class="personal" v-if="menu_id == 0" style="height: 100%">
        <v-card class="pa-5" width="600" dark rounded="lg">
          <v-card-title class="pb-5">
            <v-row justify="center" align="center">
              <v-icon x-large> mdi-account-cog </v-icon>
              <h2>管理员登录</h2>
            </v-row>
          </v-card-title>

          <v-divider></v-divider>

          <v-card-text>
            <v-form ref="userForm" v-model="valid" lazy-validation>
              <v-text-field
                outlined
                v-model="userName"
                :counter="10"
                :rules="userNameRules"
                label="用户名"
                required
              ></v-text-field>

              <v-text-field
                outlined
                v-model="passWords"
                :rules="passWordsRules"
                label="密码"
                type="password"
                required
              ></v-text-field>

              <v-btn outlined bottom v-if="!isLogin" @click="Login">
                登录
              </v-btn>
            </v-form>
          </v-card-text>
        </v-card>
      </v-container>

      <v-card class="personal" v-if="menu_id == 1" flat height="100%">
        <v-card class="mx-auto">
          <h1 class="pa-5">
            <v-icon x-large>mdi-account</v-icon>
            {{ this.userName }},已登入管理员系统
          </h1>
          <v-list-item two-line>
            <v-list-item-content>
              <v-list-item-title class="text-h5"> 北京 </v-list-item-title>
              <v-list-item-subtitle>{{ this.date }}</v-list-item-subtitle>
            </v-list-item-content>
          </v-list-item>
        </v-card>
      </v-card>

      <div class="accountManagement" v-if="menu_id == 2">
        <AccountManagement></AccountManagement>
      </div>

      <div class="reviewApplication" v-if="menu_id == 3">
        <ReviewApplication></ReviewApplication>
      </div>

      <div class="systemSettings" v-if="menu_id == 4">
        <SystemSetting></SystemSetting>
      </div>
    </v-main>
  </v-app>
</template>

<script>
import AccountManagement from "../components/AdminAccountManagement.vue";
import ReviewApplication from "../components/AdminReviewApplication.vue";
import SystemSetting from "../components/AdminSetting.vue";
import { sha256 } from "js-sha256";

export default {
  components: {
    AccountManagement,
    ReviewApplication,
    SystemSetting,
  },
  data() {
    return {
      isLogin: false,
      logoutTime: "",
      valid: true,
      userName: "admin",
      userNameRules: [
        (v) => !!v || "用户名不能为空",
        (v) => (v && v.length <= 10) || "用户名不能超过10个字符",
      ],
      passWords: "admin-academic-2021",
      passWordsRules: [(v) => !!v || "密码不能为空"],
      menu_id: 0,
      menu: [
        {
          menu_id: 1,
          title: "个人中心",
          icon: "mdi-account-cog-outline",
        },
        {
          menu_id: 2,
          title: "账号管理",
          icon: "mdi-account-box-multiple-outline",
        },
        {
          menu_id: 3,
          title: "审核申请",
          icon: "mdi-check-circle-outline",
        },
        {
          menu_id: 4,
          title: "系统设置",
          icon: "mdi-cog-outline",
        },
      ],
      date: new Date(),
    };
  },
  mounted() {
    let that = this;
    this.timer = setInterval(function () {
      that.date = new Date().toLocaleString();
    });
  },
  methods: {
    setMenu(i) {
      this.menu_id = i;
    },
    Login() {
      let token = window.localStorage.token;
      this.$axios({
        method: "post",
        url: "api/admin/login",
        params: {
          username: this.userName,
          password: sha256(this.passWords),
        },
        headers: {
          token: token,
        },
      }).then((response) => {
        console.log(response.data);
        if (response.data.success === true) {
          this.$notify({
            title: "成功",
            message: "登陆成功",
            type: "success",
          });
          this.isLogin = true;
          this.menu_id = 1;
          this.logoutTime = new Date(
            new Date().getTime() + 1000 * 30 * 60
          ).toLocaleTimeString();
          this.leftTime = 30 * 60;
          this.timer = setInterval(() => {
            this.leftTime--;

            if (this.leftTime === 0) {
              this.Logout();
              clearInterval(this.timer);
              this.$notify({
                title: "成功",
                message: "自动登出成功",
                type: "success",
              });
            }
          }, 1000);
        } else {
          this.$notify({
            title: "失败",
            message: "登录名或密码错误",
            type: "warning",
          });
        }
      });
    },

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
          this.isLogin = false;
          this.menu_id = 0;
        } else {
          this.$notify({
            title: "失败",
            message: "登出失败",
            type: "warning",
          });
        }
      });
    },
  },
  beforeDestroy: function () {
    if (this.timer) {
      clearInterval(this.timer);
    }
  },
};
</script>

<style>
.login {
  background: -webkit-gradient(
    linear,
    100% 0%,
    0% 0%,
    from(#59c2fd),
    to(#2b99ff)
  );
  border-radius: 20px;
  color: #fff;
}
.personal {
  display: flex;
  flex-direction: column;
  justify-content: space-around;
  align-items: center;
}
</style>
