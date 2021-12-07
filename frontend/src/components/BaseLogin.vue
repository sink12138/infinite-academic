<template>
  <div>
      <v-menu
        open-on-hover
        top
      >
        <template v-slot:activator="{ on, attrs }">
          <v-btn
            depressed
            height="100%"
            dark
            v-bind="attrs"
            v-on="on"
            v-if="isLogin === true"
          >
            Dropdown
          </v-btn>
        </template>
        <v-list>
          <v-list-item
            v-for="(item, index) in items"
            :key="index"
          >
            <v-list-item-title>{{ item.title }}</v-list-item-title>
          </v-list-item>
        </v-list>
      </v-menu>
      <router-link to='/register'>
        <v-btn
          depressed
          height="100%"
          dark
          v-bind="attrs"
          v-on="on"
          v-if="isLogin === false"
        >
          注册
        </v-btn>
      </router-link>
      <v-dialog
        v-model="dialog"
        persistent
        max-width="600px"
      >
        <template v-slot:activator="{ on, attrs }">
          <v-btn
            depressed
            height="100%"
            dark
            v-bind="attrs"
            v-on="on"
            v-if="isLogin === false"
            @click="isClickLogin = true"
          >
            登录
          </v-btn>
        </template>
        <v-card v-if="isClickLogin === true">
          <v-card-title>
            <span class="headline">登录</span>
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
            <v-spacer></v-spacer>
            <v-btn
              color="blue darken-1"
              text
              @click="findPassword()"
            >找回密码</v-btn>
            <router-link to='register'>
              <v-btn
                color="blue darken-1"
                text
              >暂无账号？注册账号</v-btn>
            </router-link>
            <v-btn
              color="blue darken-1"
              text
              @click="closeDialog()"
            >关闭</v-btn>
            <v-btn
              color="blue darken-1"
              text
              @click="accountLogin()"
            >登录</v-btn>
          </v-card-actions>
        </v-card>
      </v-dialog>
  </div>
</template>

<script>
import { sha256 } from "js-sha256";
export default {
  mounted() {},
  data: () => ({
    email: "",
    password: "",
    dialog: false,
    isClickLogin: false,
    isLogin: false,
    emailRules: [
      (v) => !!v || "邮箱未填写",
      (v) => /.+@.+/.test(v) || "邮箱无效",
    ],
    passwordRules: [(v) => !!v || "密码未填写"],
    items: [
        { title: 'Click Me' },
        { title: 'Click Me' },
        { title: 'Click Me' },
        { title: 'Click Me 2' },
      ],
  }),
  methods: {
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
            this.$notify({
              title: "成功",
              message: "登陆成功",
              type: "success",
            });
            this.dialog = false;
            this.login = true;
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
    findPassword() {},
  },
};
</script>
