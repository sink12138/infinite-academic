<template>
  <div>
    <template
      v-if="isLogin === true"
      v-bind="attrs"
      v-on="on"
    >
      <v-badge
        color="red"
        :content="num"
        v-if="num != 0"
        offset-x="29"
        offset-y="16"
        overlap
      >
        <router-link to="/user/message">
          <v-btn>
            <v-icon>mdi-message-text</v-icon>
          </v-btn>
        </router-link>
      </v-badge>
      <router-link to="/user/message">
        <v-btn
          v-if="num === 0"
        >
          <v-icon>mdi-message-text</v-icon>
        </v-btn>
      </router-link>
    </template>
    <v-menu
      open-on-hover
      offset-y
      v-if="isLogin === true"
    >
      <template v-slot:activator="{ on, attrs }">
        <v-btn
          v-bind="attrs"
          v-on="on"
          @click="href('/user/profile')"
        >
          <v-icon>
            mdi-account-circle
          </v-icon>
        </v-btn>
      </template>
      <v-list dense>
        <v-list-item
          v-for="(item, index) in items"
          :key="index"
          @click="href(item.url)"
        >
          <v-list-item-content>{{ item.title }}</v-list-item-content>
        </v-list-item>
        <v-list-item @click="logout()">
          <v-list-item-content>登出</v-list-item-content>
        </v-list-item>
      </v-list>
    </v-menu>

    <v-dialog
      v-model="dialog"
      persistent
      max-width="600px"
      v-if="isLogin === false && isFindPassword === false"
    >
      <template v-slot:activator="{ on, attrs }">
        <v-btn
          depressed
          v-bind="attrs"
          v-on="on"
        >
          登录
        </v-btn>
      </template>
      <v-card>
        <v-card-title>
          <span class="headline">登录</span>
          <v-divider></v-divider>
          <v-btn
            color="red darken-1"
            text
            @click="closeDialog()"
          >X</v-btn>
        </v-card-title>
        <v-card-text>
          <v-container>
            <v-row>
              <v-col cols="12">
                <v-text-field
                  v-model="email"
                  label="邮箱*"
                  :rules="emailRules"
                  required
                ></v-text-field>
              </v-col>
              <v-col cols="12">
                <v-text-field
                  v-model="password"
                  label="密码*"
                  type="password"
                  :rules="passwordRules"
                  required
                ></v-text-field>
              </v-col>
            </v-row>
          </v-container>
        </v-card-text>
        <v-card-actions>
          <v-btn
            color="blue darken-1"
            text
            @click="isFindPassword=true"
          >找回密码</v-btn>
          <router-link to='/register'>
            <v-btn
              color="blue darken-1"
              text
            >暂无账号？注册账号</v-btn>
          </router-link>
          <v-spacer></v-spacer>
          <v-btn
            color="blue darken-1"
            text
            @click="accountLogin()"
          >登录</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
    <v-dialog
      v-model="dialog"
      persistent
      max-width="600px"
      v-if="isFindPassword === true"
    >
      <v-card>
        <v-card-title>
          <span class="headline">找回密码</span>
        </v-card-title>
        <v-card-text>
          <v-container>
            <v-row>
              <v-col cols="12">
                <v-text-field
                  v-model="findEmail"
                  label="邮箱*"
                  :rules="emailRules"
                  required
                  v-if="inputNewPassword === false"
                ></v-text-field>
              </v-col>
              <v-col cols="12">
                <v-text-field
                  v-model="code"
                  label="验证码*"
                  :rules="codeRules"
                  required
                  v-if="inputNewPassword === true"
                ></v-text-field>
              </v-col>
              <v-col cols="12">
                <v-text-field
                  v-model="newPassword"
                  label="密码*"
                  type="password"
                  :rules="passwordRules"
                  required
                  v-if="inputNewPassword === true"
                ></v-text-field>
              </v-col>
            </v-row>
          </v-container>
        </v-card-text>
        <v-card-actions>
          <v-btn
            color="blue darken-1"
            text
            @click="isFindPassword = false"
            v-if="isFindPassword === true"
          >返回</v-btn>
          <v-spacer></v-spacer>
          <v-btn
            color="blue darken-1"
            text
            @click="sendCode()"
            v-if="inputNewPassword === false"
          >发送验证码</v-btn>
          <v-btn
            color="blue darken-1"
            text
            @click="changePassword()"
            v-if="inputNewPassword === true"
          >修改密码</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script>
import { sha256 } from "js-sha256";
export default {
  mounted() {
    this.getUnreadMessage();
    this.$axios({
      method: "get",
      url: "/api/account/profile",
    }).then((response) => {
      if (response.data.success === true) {
        this.isLogin = true;
      }
    });
    this.$nextTick(() => {
      setInterval(this.getUnreadMessage, 10000);
    });
  },
  data: () => ({
    num: 0,
    token: "",
    code: "",
    email: "",
    password: "",
    findEmail: "",
    newPassword: "",
    dialog: false,
    isLogin: false,
    isRouterAlive: true,
    isFindPassword: false,
    inputNewPassword: false,
    emailRules: [
      (v) => !!v || "邮箱未填写",
      (v) => /.+@.+/.test(v) || "邮箱无效",
    ],
    passwordRules: [(v) => !!v || "密码未填写"],
    codeRules: [(v) => !!v || "验证码未填写"],
    items: [
      {
        title: "个人中心",
        url: "/user/profile",
      },
      {
        title: "个人门户",
        url: "/door",
      },
    ],
  }),
  methods: {
    getUnreadMessage() {
      this.$axios({
        method: "get",
        url: "/api/account/message/count",
      }).then((response) => {
        if (response.data.success === true) {
          this.num = response.data.data;
        }
      });
    },
    href(url) {
      this.$router.push({ path: url });
    },
    closeDialog() {
      this.dialog = false;
    },
    accountLogin() {
      if (this.email === "") {
        this.$notify({
          title: "错误",
          message: "邮箱未填写",
          type: "warning",
        });
      } else if (this.password === "") {
        this.$notify({
          title: "错误",
          message: "密码未填写",
          type: "warning",
        });
      } else {
        this.$axios({
          method: "post",
          url: "/api/account/login",
          params: {
            email: this.email,
            password: sha256(this.password),
          },
        }).then((response) => {
          console.log(this.email);
          console.log(this.password);
          console.log(sha256(this.password));
          console.log(response.data);
          if (response.data.success === true) {
            this.getUnreadMessage();
            this.dialog = false;
            this.isLogin = true;
            sessionStorage.setItem("isLogin", true);
            this.$notify({
              title: "成功",
              message: "登录成功",
              type: "success",
            });
            console.log(this.isLogin);
          } else {
            this.$notify({
              title: "失败",
              message: "登录名或密码错误",
              type: "warning",
            });
          }
        });
      }
    },
    logout() {
      this.$axios({
        method: "post",
        url: "/api/account/logout",
      }).then((response) => {
        console.log(response.data);
        if (response.data.success === true) {
          this.isLogin = false;
          sessionStorage.setItem("isLogin", false);
          this.$notify({
            title: "成功",
            message: "登出成功",
            type: "success",
          });
        } else {
          this.$notify({
            title: "失败",
            message: "登出失败",
            type: "warning",
          });
        }
      });
    },
    sendCode() {
      console.log(this.findEmail);
      if (this.findEmail === "") {
        this.$notify({
          title: "错误",
          message: "邮箱未填写",
          type: "warning",
        });
      } else {
        this.$axios({
          method: "post",
          url: "/api/account/code",
          params: {
            email: this.findEmail,
            action: "找回密码",
          },
        }).then((response) => {
          console.log(response.data);
          if (response.data.success) {
            this.$notify({
              title: "发送成功",
              message: "请去邮箱查看验证码",
              type: "success",
            });
            this.inputNewPassword = true;
          } else {
            this.$notify({
              title: "发送错误",
              message: "请检查邮箱是否正确",
              type: "warning",
            });
          }
        });
      }
    },
    changePassword() {
      console.log(this.newPassword);
      if (this.newPassword === "") {
        this.$notify({
          title: "错误",
          message: "密码未填写",
          type: "warning",
        });
      } else {
        this.$axios({
          method: "post",
          url: "/api/account/forget/submit",
          params: {
            newPassword: sha256(this.newPassword),
            code: this.code,
          },
        }).then((response) => {
          console.log(response.data);
          if (response.data.success === true) {
            this.$notify({
              title: "修改成功",
              message: "请记住您修改的密码",
              type: "success",
            });
            this.inputNewPassword = false;
            this.isFindPassword = false;
          }
        });
      }
    },
  },
};
</script>

<style scoped>
#app {
  position: relative;
}
@media only screen and (min-width: 1200px) {
  .wrap {
    width: 65%;
    margin: 0 auto;
  }
}
a {
  text-decoration: none;
}

.router-link-active {
  text-decoration: none;
}
</style>

