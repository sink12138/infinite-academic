<template>
  <div class="login">
    <router-link to="/">
      <v-btn
        class="ma-2"
        color="green"
        dark
        max-width="150px"
      >
        <v-icon
          dark
          left
        >mdi-arrow-left</v-icon>返回
      </v-btn>
    </router-link>
    <v-container class="loginOverlay">
      <v-layout
        align-center
        justify-center
      >
        <v-flex
          xs12
          sm8
        >
          <v-card>
            <v-card-title>
              <span class="headline">注册</span>
            </v-card-title>
            <v-card-text class="pt-4">
              <v-form ref="form">
                <v-text-field
                  label="邮箱"
                  v-model="email"
                  required
                  :rules="emailRules"
                  append-icon="fa-info-circle"
                ></v-text-field>
                <v-text-field
                  label="用户名（不超过10位字母）"
                  v-model="username"
                  required
                  :rules="usernameRules"
                  append-icon="fa-info-circle"
                ></v-text-field>
                <v-text-field
                  label="密码"
                  v-model="password"
                  :rules="passwordRules"
                  :append-icon="show1 ? 'mdi-eye' : 'mdi-eye-off'"
                  :type="show1 ? 'text' : 'password'"
                  required
                ></v-text-field>
              </v-form>
            </v-card-text>
            <v-card-actions>
              <v-btn
                @click="register()"
                class="green white--text"
              >注册</v-btn>
            </v-card-actions>
          </v-card>
        </v-flex>
      </v-layout>
    </v-container>
  </div>
</template>

<script>
import { sha256 } from "js-sha256";
export default {
  mounted() {
    console.log("123456", sha256("123456"));
  },
  data: () => ({
    snackbar: false,
    message: "",
    vertical: true,
    show1: false,
    username: "",
    password: "",
    email: "",
    emailRules: [
      (v) => !!v || "邮箱未填写",
      (v) => /.+@.+/.test(v) || "邮箱无效",
    ],
    passwordRules: [(v) => !!v || "密码未填写"],
    usernameRules: [(v) => !!v || "密码未填写"],
  }),
  methods: {
    register() {
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
      } else if (this.username === "") {
        this.$notify({
          title: "错误",
          message: "用户名未填写",
          type: "warning",
        });
      } else {
        console.log(this.username);
        console.log(this.password);
        console.log(this.email);
        this.$axios({
          method: "post",
          url: "/api/account/register",
          params: {
            email: this.email,
            username: this.username,
            password: sha256(this.password),
          },
        }).then((response) => {
          console.log(response.data);
          if(response.data.success === true){
            this.$notify({
              titile:"注册成功",
              message:"请到邮箱验证后登录",
              type:"success"
            })
          }
          else{
            this.$notify({
              title:"注册失败",
              message:"用户名过长，请修改",
              type:"warning",
            })
          }
        });
      }
    },
  },
};
</script>

<style scoped>
.login {
  height: 100vh;
  background-color: #ffffff;
}
h4 {
  font-size: 1em !important;
  padding: 1em;
  color: black;
}
.v-card {
  text-align: center;
  margin: 1em;
}
.v-card__title {
  justify-content: center;
}
.v-btn {
  width: 100%;
}
.v-card {
  padding: 1em;
}
.green {
  margin-bottom: 1em;
}
.create {
  text-transform: capitalize;
  margin-bottom: 1em;
}
</style>
